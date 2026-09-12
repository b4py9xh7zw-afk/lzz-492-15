package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.RecruitChannelStats;

/**
 * 渠道质量回算快照服务接口
 *
 * @author scaffolding
 */
public interface RecruitChannelStatsService extends IService<RecruitChannelStats> {

    /**
     * 分页查询回算历史快照
     */
    Page<RecruitChannelStats> pageQuery(Long current, Long size, Long channelId);
}
