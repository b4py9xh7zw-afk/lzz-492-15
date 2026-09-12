package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.RecruitChannelStats;
import com.scaffolding.mapper.RecruitChannelStatsMapper;
import com.scaffolding.service.RecruitChannelStatsService;
import org.springframework.stereotype.Service;

/**
 * 渠道质量回算快照服务实现类
 *
 * @author scaffolding
 */
@Service
public class RecruitChannelStatsServiceImpl extends ServiceImpl<RecruitChannelStatsMapper, RecruitChannelStats>
        implements RecruitChannelStatsService {

    @Override
    public Page<RecruitChannelStats> pageQuery(Long current, Long size, Long channelId) {
        Page<RecruitChannelStats> page = new Page<>(current, size);
        LambdaQueryWrapper<RecruitChannelStats> wrapper = new LambdaQueryWrapper<>();
        if (channelId != null) {
            wrapper.eq(RecruitChannelStats::getChannelId, channelId);
        }
        wrapper.orderByDesc(RecruitChannelStats::getStatDate);
        wrapper.orderByDesc(RecruitChannelStats::getId);
        return this.page(page, wrapper);
    }
}
