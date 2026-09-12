package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 招聘候选人实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recruit_candidate")
public class RecruitCandidate extends BaseEntity {

    /**
     * 候选人姓名
     */
    private String candidateName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 来源渠道ID
     */
    private Long channelId;

    /**
     * 应聘岗位
     */
    private String position;

    /**
     * 当前阶段（registered-已报名，interviewed-已面试，trial-试岗中，onboard-已到岗，
     * retained-留任七天，resigned-已离职，eliminated-已淘汰）
     */
    private String stage;

    /**
     * 报名时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyTime;

    /**
     * 面试时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime interviewTime;

    /**
     * 试岗时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime trialTime;

    /**
     * 真实到岗时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime onboardTime;

    /**
     * 留任七天确认时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime retainTime;

    /**
     * 离职时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime resignTime;

    /**
     * 离职原因
     */
    private String resignReason;

    /**
     * 备注
     */
    private String remark;
}
