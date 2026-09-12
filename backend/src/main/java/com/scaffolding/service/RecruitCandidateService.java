package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.RecruitCandidate;

/**
 * 招聘候选人服务接口
 *
 * @author scaffolding
 */
public interface RecruitCandidateService extends IService<RecruitCandidate> {

    /**
     * 分页查询候选人
     */
    Page<RecruitCandidate> pageQuery(Long current, Long size, String candidateName, Long channelId, String stage);

    /**
     * 推进候选人到下一阶段（interview-面试，trial-试岗，onboard-到岗，retain-留任七天，eliminate-淘汰）
     */
    void advanceStage(Long id, String action);

    /**
     * 登记离职（必须填写离职原因）
     */
    void resign(Long id, String resignReason);
}
