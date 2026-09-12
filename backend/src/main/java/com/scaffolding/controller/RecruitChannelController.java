package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.dto.ChannelQualityDTO;
import com.scaffolding.entity.RecruitChannel;
import com.scaffolding.entity.RecruitChannelStats;
import com.scaffolding.service.RecruitChannelService;
import com.scaffolding.service.RecruitChannelStatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 招聘渠道控制器
 *
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/recruit/channel")
@Api(tags = "招聘渠道管理")
public class RecruitChannelController {

    @Autowired
    private RecruitChannelService channelService;

    @Autowired
    private RecruitChannelStatsService statsService;

    @PostMapping
    @ApiOperation("新增渠道")
    public Result<RecruitChannel> save(@RequestBody RecruitChannel channel) {
        try {
            channel.setId(null);
            channel.setStatus("active");
            channel.setCreateTime(LocalDateTime.now());
            channel.setUpdateTime(LocalDateTime.now());
            channelService.save(channel);
            return Result.success("新增成功", channel);
        } catch (Exception e) {
            log.error("新增渠道失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新渠道")
    public Result<RecruitChannel> update(@PathVariable Long id, @RequestBody RecruitChannel channel) {
        try {
            channel.setId(id);
            // 状态和暂停信息只能由暂停/恢复接口修改，防止误改
            channel.setStatus(null);
            channel.setPauseReason(null);
            channel.setPauseTime(null);
            channel.setUpdateTime(LocalDateTime.now());
            channelService.updateById(channel);
            return Result.success("更新成功", channel);
        } catch (Exception e) {
            log.error("更新渠道失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除渠道")
    public Result<?> delete(@PathVariable Long id) {
        try {
            channelService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除渠道失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询渠道")
    public Result<RecruitChannel> getById(@PathVariable Long id) {
        RecruitChannel channel = channelService.getById(id);
        if (channel == null) {
            return Result.error("渠道不存在");
        }
        return Result.success(channel);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询渠道")
    public Result<PageResult<RecruitChannel>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String channelName,
            @RequestParam(required = false) String channelType,
            @RequestParam(required = false) String status) {
        Page<RecruitChannel> page = channelService.pageQuery(current, size, channelName, channelType, status);
        return Result.success(new PageResult<>(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize()));
    }

    @GetMapping("/list")
    @ApiOperation("查询全部渠道（下拉选择用）")
    public Result<List<RecruitChannel>> list() {
        return Result.success(channelService.list());
    }

    @PutMapping("/{id}/pause")
    @ApiOperation("暂停渠道（需填写暂停原因）")
    public Result<?> pause(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            channelService.pauseChannel(id, body.get("pauseReason"));
            return Result.success("渠道已暂停");
        } catch (Exception e) {
            log.error("暂停渠道失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/resume")
    @ApiOperation("恢复渠道")
    public Result<?> resume(@PathVariable Long id) {
        try {
            channelService.resumeChannel(id);
            return Result.success("渠道已恢复");
        } catch (Exception e) {
            log.error("恢复渠道失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/quality")
    @ApiOperation("渠道质量回算（实时）：按真实到岗和稳定率计算结算金额")
    public Result<List<ChannelQualityDTO>> quality(@RequestParam(required = false) Long channelId) {
        try {
            return Result.success(channelService.recalcQuality(channelId));
        } catch (Exception e) {
            log.error("渠道质量回算失败", e);
            return Result.error("回算失败：" + e.getMessage());
        }
    }

    @PostMapping("/recalc")
    @ApiOperation("执行渠道质量回算并保存当日快照")
    public Result<List<ChannelQualityDTO>> recalc(@RequestParam(required = false) Long channelId) {
        try {
            return Result.success("回算完成", channelService.recalcAndSaveSnapshot(channelId));
        } catch (Exception e) {
            log.error("渠道质量回算失败", e);
            return Result.error("回算失败：" + e.getMessage());
        }
    }

    @GetMapping("/recalc/history")
    @ApiOperation("分页查询回算历史快照")
    public Result<PageResult<RecruitChannelStats>> recalcHistory(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long channelId) {
        Page<RecruitChannelStats> page = statsService.pageQuery(current, size, channelId);
        return Result.success(new PageResult<>(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize()));
    }
}
