package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.dto.ChannelQualityDTO;
import com.scaffolding.entity.RecruitCandidate;
import com.scaffolding.entity.RecruitChannel;
import com.scaffolding.entity.RecruitChannelStats;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.RecruitCandidateMapper;
import com.scaffolding.mapper.RecruitChannelMapper;
import com.scaffolding.mapper.RecruitChannelStatsMapper;
import com.scaffolding.service.RecruitChannelService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 招聘渠道服务实现类
 *
 * @author scaffolding
 */
@Service
public class RecruitChannelServiceImpl extends ServiceImpl<RecruitChannelMapper, RecruitChannel>
        implements RecruitChannelService {

    /**
     * 稳定率低于该值（%）判定为质量差
     */
    private static final BigDecimal POOR_STABILITY_THRESHOLD = new BigDecimal("40");

    /**
     * 判定质量差所需的最小到岗样本量
     */
    private static final int MIN_SAMPLE_SIZE = 3;

    @Autowired
    private RecruitCandidateMapper candidateMapper;

    @Autowired
    private RecruitChannelStatsMapper statsMapper;

    @Override
    public Page<RecruitChannel> pageQuery(Long current, Long size, String channelName, String channelType, String status) {
        Page<RecruitChannel> page = new Page<>(current, size);
        LambdaQueryWrapper<RecruitChannel> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(channelName)) {
            wrapper.like(RecruitChannel::getChannelName, channelName);
        }
        if (StringUtils.hasText(channelType)) {
            wrapper.eq(RecruitChannel::getChannelType, channelType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(RecruitChannel::getStatus, status);
        }
        wrapper.orderByDesc(RecruitChannel::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public void pauseChannel(Long id, String pauseReason) {
        if (!StringUtils.hasText(pauseReason)) {
            throw new BusinessException("暂停渠道必须填写暂停原因");
        }
        RecruitChannel channel = this.getById(id);
        if (channel == null) {
            throw new BusinessException("渠道不存在");
        }
        if ("paused".equals(channel.getStatus())) {
            throw new BusinessException("该渠道已处于暂停状态");
        }
        channel.setStatus("paused");
        channel.setPauseReason(pauseReason);
        channel.setPauseTime(LocalDateTime.now());
        channel.setUpdateTime(LocalDateTime.now());
        this.updateById(channel);
    }

    @Override
    public void resumeChannel(Long id) {
        RecruitChannel channel = this.getById(id);
        if (channel == null) {
            throw new BusinessException("渠道不存在");
        }
        if (!"paused".equals(channel.getStatus())) {
            throw new BusinessException("该渠道未处于暂停状态");
        }
        channel.setStatus("active");
        channel.setUpdateTime(LocalDateTime.now());
        this.updateById(channel);
    }

    @Override
    public List<ChannelQualityDTO> recalcQuality(Long channelId) {
        // 查询需要回算的渠道
        LambdaQueryWrapper<RecruitChannel> channelWrapper = new LambdaQueryWrapper<>();
        if (channelId != null) {
            channelWrapper.eq(RecruitChannel::getId, channelId);
        }
        List<RecruitChannel> channels = this.list(channelWrapper);
        if (channels.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询候选人并按渠道分组（一次性加载，避免循环查库）
        LambdaQueryWrapper<RecruitCandidate> candidateWrapper = new LambdaQueryWrapper<>();
        if (channelId != null) {
            candidateWrapper.eq(RecruitCandidate::getChannelId, channelId);
        }
        List<RecruitCandidate> candidates = candidateMapper.selectList(candidateWrapper);
        Map<Long, List<RecruitCandidate>> candidateMap = candidates.stream()
                .collect(Collectors.groupingBy(RecruitCandidate::getChannelId));

        List<ChannelQualityDTO> result = new ArrayList<>();
        for (RecruitChannel channel : channels) {
            List<RecruitCandidate> list = candidateMap.getOrDefault(channel.getId(), new ArrayList<>());
            result.add(buildQualityDTO(channel, list));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ChannelQualityDTO> recalcAndSaveSnapshot(Long channelId) {
        List<ChannelQualityDTO> qualityList = recalcQuality(channelId);
        LocalDate today = LocalDate.now();
        for (ChannelQualityDTO quality : qualityList) {
            // 同一渠道同一天只保留一条快照，先删后插
            LambdaQueryWrapper<RecruitChannelStats> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecruitChannelStats::getChannelId, quality.getChannelId());
            wrapper.eq(RecruitChannelStats::getStatDate, today);
            statsMapper.delete(wrapper);

            RecruitChannelStats stats = new RecruitChannelStats();
            BeanUtils.copyProperties(quality, stats);
            stats.setId(null);
            stats.setStatDate(today);
            stats.setCreateTime(LocalDateTime.now());
            stats.setUpdateTime(LocalDateTime.now());
            statsMapper.insert(stats);
        }
        return qualityList;
    }

    /**
     * 根据渠道与其候选人列表构建质量回算结果
     * 漏斗口径：报名 → 面试 → 试岗 → 真实到岗 → 留任七天
     * 结算口径：真实到岗人数 × 结算单价 × 稳定率（不按报名人数结算）
     */
    private ChannelQualityDTO buildQualityDTO(RecruitChannel channel, List<RecruitCandidate> candidates) {
        ChannelQualityDTO dto = new ChannelQualityDTO();
        dto.setChannelId(channel.getId());
        dto.setChannelName(channel.getChannelName());
        dto.setChannelType(channel.getChannelType());
        dto.setStatus(channel.getStatus());
        dto.setPauseReason(channel.getPauseReason());
        dto.setSettlePrice(channel.getSettlePrice() == null ? BigDecimal.ZERO : channel.getSettlePrice());

        // 漏斗各层人数：以各阶段时间戳是否记录为准，保证只增不减
        int registered = candidates.size();
        int interviewed = (int) candidates.stream().filter(c -> c.getInterviewTime() != null).count();
        int trial = (int) candidates.stream().filter(c -> c.getTrialTime() != null).count();
        int onboard = (int) candidates.stream().filter(c -> c.getOnboardTime() != null).count();
        int retained = (int) candidates.stream().filter(c -> c.getRetainTime() != null).count();
        int resigned = (int) candidates.stream().filter(c -> c.getResignTime() != null).count();

        dto.setRegisteredCount(registered);
        dto.setInterviewCount(interviewed);
        dto.setTrialCount(trial);
        dto.setOnboardCount(onboard);
        dto.setRetainedCount(retained);
        dto.setResignedCount(resigned);

        // 到岗率 = 真实到岗 / 报名
        BigDecimal arrivalRate = registered > 0
                ? BigDecimal.valueOf(onboard * 100.0 / registered).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        // 稳定率 = 留任七天 / 真实到岗
        BigDecimal stabilityRate = onboard > 0
                ? BigDecimal.valueOf(retained * 100.0 / onboard).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        dto.setArrivalRate(arrivalRate);
        dto.setStabilityRate(stabilityRate);

        // 回算结算金额 = 真实到岗 × 单价 × 稳定率
        BigDecimal settleAmount = dto.getSettlePrice()
                .multiply(BigDecimal.valueOf(onboard))
                .multiply(stabilityRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        dto.setSettleAmount(settleAmount);

        // 质量评级与暂停建议
        dto.setQualityLevel(evaluateQualityLevel(stabilityRate, onboard));
        dto.setSuggestPause(onboard >= MIN_SAMPLE_SIZE && stabilityRate.compareTo(POOR_STABILITY_THRESHOLD) < 0);
        return dto;
    }

    /**
     * 按稳定率评级：>=80优，>=60良，>=40中，<40差；样本不足暂不评级
     */
    private String evaluateQualityLevel(BigDecimal stabilityRate, int onboard) {
        if (onboard < MIN_SAMPLE_SIZE) {
            return "medium";
        }
        if (stabilityRate.compareTo(new BigDecimal("80")) >= 0) {
            return "excellent";
        }
        if (stabilityRate.compareTo(new BigDecimal("60")) >= 0) {
            return "good";
        }
        if (stabilityRate.compareTo(POOR_STABILITY_THRESHOLD) >= 0) {
            return "medium";
        }
        return "poor";
    }
}
