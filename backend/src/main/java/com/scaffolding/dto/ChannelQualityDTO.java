package com.scaffolding.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 渠道质量回算结果DTO
 * 结算口径：不看报名人数，按真实到岗人数 × 结算单价 × 稳定率 回算
 *
 * @author scaffolding
 */
@Data
public class ChannelQualityDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 渠道ID
     */
    private Long channelId;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 渠道类型
     */
    private String channelType;

    /**
     * 渠道状态（active-合作中，paused-已暂停）
     */
    private String status;

    /**
     * 暂停原因
     */
    private String pauseReason;

    /**
     * 结算单价（元/人）
     */
    private BigDecimal settlePrice;

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
     * 到岗率（%）= 真实到岗 / 报名 × 100
     */
    private BigDecimal arrivalRate;

    /**
     * 稳定率（%）= 留任七天 / 真实到岗 × 100
     */
    private BigDecimal stabilityRate;

    /**
     * 回算结算金额（元）= 真实到岗 × 结算单价 × 稳定率
     */
    private BigDecimal settleAmount;

    /**
     * 质量评级（excellent-优，good-良，medium-中，poor-差）
     */
    private String qualityLevel;

    /**
     * 是否建议暂停（稳定率低于阈值且样本量足够）
     */
    private Boolean suggestPause;
}
