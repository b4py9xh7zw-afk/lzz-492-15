package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.RecruitCandidate;
import com.scaffolding.entity.RecruitChannel;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.RecruitCandidateMapper;
import com.scaffolding.mapper.RecruitChannelMapper;
import com.scaffolding.service.RecruitCandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 招聘候选人服务实现类
 *
 * @author scaffolding
 */
@Service
public class RecruitCandidateServiceImpl extends ServiceImpl<RecruitCandidateMapper, RecruitCandidate>
        implements RecruitCandidateService {

    @Autowired
    private RecruitChannelMapper channelMapper;

    @Override
    public Page<RecruitCandidate> pageQuery(Long current, Long size, String candidateName, Long channelId, String stage) {
        Page<RecruitCandidate> page = new Page<>(current, size);
        LambdaQueryWrapper<RecruitCandidate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(candidateName)) {
            wrapper.like(RecruitCandidate::getCandidateName, candidateName);
        }
        if (channelId != null) {
            wrapper.eq(RecruitCandidate::getChannelId, channelId);
        }
        if (StringUtils.hasText(stage)) {
            wrapper.eq(RecruitCandidate::getStage, stage);
        }
        wrapper.orderByDesc(RecruitCandidate::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public void advanceStage(Long id, String action) {
        RecruitCandidate candidate = this.getById(id);
        if (candidate == null) {
            throw new BusinessException("候选人不存在");
        }
        if ("resigned".equals(candidate.getStage()) || "eliminated".equals(candidate.getStage())) {
            throw new BusinessException("该候选人已离职或已淘汰，无法推进阶段");
        }
        LocalDateTime now = LocalDateTime.now();
        switch (action) {
            case "interview":
                requireStage(candidate, "registered");
                candidate.setStage("interviewed");
                candidate.setInterviewTime(now);
                break;
            case "trial":
                requireStage(candidate, "interviewed");
                candidate.setStage("trial");
                candidate.setTrialTime(now);
                break;
            case "onboard":
                requireStage(candidate, "trial");
                candidate.setStage("onboard");
                candidate.setOnboardTime(now);
                break;
            case "retain":
                requireStage(candidate, "onboard");
                candidate.setStage("retained");
                candidate.setRetainTime(now);
                break;
            case "eliminate":
                candidate.setStage("eliminated");
                break;
            default:
                throw new BusinessException("不支持的阶段操作：" + action);
        }
        candidate.setUpdateTime(now);
        this.updateById(candidate);
    }

    @Override
    public void resign(Long id, String resignReason) {
        if (!StringUtils.hasText(resignReason)) {
            throw new BusinessException("登记离职必须填写离职原因");
        }
        RecruitCandidate candidate = this.getById(id);
        if (candidate == null) {
            throw new BusinessException("候选人不存在");
        }
        // 只有已到岗（含已留任）的员工才能登记离职
        if (!"onboard".equals(candidate.getStage()) && !"retained".equals(candidate.getStage())) {
            throw new BusinessException("该候选人尚未到岗，无法登记离职");
        }
        LocalDateTime now = LocalDateTime.now();
        candidate.setStage("resigned");
        candidate.setResignTime(now);
        candidate.setResignReason(resignReason);
        candidate.setUpdateTime(now);
        this.updateById(candidate);
    }

    /**
     * 校验候选人当前阶段是否符合推进前置条件
     */
    private void requireStage(RecruitCandidate candidate, String expectedStage) {
        if (!expectedStage.equals(candidate.getStage())) {
            throw new BusinessException("当前阶段不允许该操作，请先完成前置阶段");
        }
    }
}
