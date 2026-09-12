package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 渠道质量回算快照实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recruit_channel_stats")
public class RecruitChannelStats extends BaseEntity {

    /**
     * 渠道ID
     */
    private Long channelId;

    /**
     * 回算日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate statDate;

    /**
     * 报名人数
     */
    private Integer registeredCount;

    /**
     * 面试人数
     */
    private Integer interviewCount;

    /**
     * 试岗人数
     */
    private Integer trialCount;

    /**
     * 真实到岗人数
     */
    private Integer onboardCount;

    /**
     * 留任七天人数
     */
    private Integer retainedCount;

    /**
     * 离职人数
     */
    private Integer resignedCount;

    /**
     * 到岗率（%）= 真实到岗 / 报名
     */
    private BigDecimal arrivalRate;

    /**
     * 稳定率（%）= 留任七天 / 真实到岗
     */
    private BigDecimal stabilityRate;

    /**
     * 回算结算金额（元）= 真实到岗 × 结算单价 × 稳定率
     */
    private BigDecimal settleAmount;
}
