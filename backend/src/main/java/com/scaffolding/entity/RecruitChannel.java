package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 招聘渠道实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recruit_channel")
public class RecruitChannel extends BaseEntity {

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 渠道类型（store_poster-门店海报，short_video-短视频投放，labor_agency-劳务中介，referral-熟人推荐）
     */
    private String channelType;

    /**
     * 结算单价（元/每个真实到岗且稳定的人）
     */
    private BigDecimal settlePrice;

    /**
     * 渠道状态（active-合作中，paused-已暂停）
     */
    private String status;

    /**
     * 暂停原因
     */
    private String pauseReason;

    /**
     * 暂停时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime pauseTime;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 备注
     */
    private String remark;
}
