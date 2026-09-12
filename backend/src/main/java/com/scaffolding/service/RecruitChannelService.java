package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.dto.ChannelQualityDTO;
import com.scaffolding.entity.RecruitChannel;

import java.util.List;

/**
 * 招聘渠道服务接口
 *
 * @author scaffolding
 */
public interface RecruitChannelService extends IService<RecruitChannel> {

    /**
     * 分页查询渠道
     */
    Page<RecruitChannel> pageQuery(Long current, Long size, String channelName, String channelType, String status);

    /**
     * 暂停渠道（必须填写暂停原因）
     */
    void pauseChannel(Long id, String pauseReason);

    /**
     * 恢复渠道
     */
    void resumeChannel(Long id);

    /**
     * 渠道质量回算：实时聚合候选人漏斗数据，按真实到岗和稳定率计算结算金额
     *
     * @param channelId 渠道ID，为空则回算全部渠道
     * @return 各渠道质量回算结果
     */
    List<ChannelQualityDTO> recalcQuality(Long channelId);

    /**
     * 执行回算并保存当日快照，返回回算结果
     */
    List<ChannelQualityDTO> recalcAndSaveSnapshot(Long channelId);
}
