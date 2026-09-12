package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.RecruitCandidate;
import com.scaffolding.entity.RecruitChannel;
import com.scaffolding.service.RecruitCandidateService;
import com.scaffolding.service.RecruitChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 招聘候选人控制器
 *
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/recruit/candidate")
@Api(tags = "招聘候选人管理")
public class RecruitCandidateController {

    @Autowired
    private RecruitCandidateService candidateService;

    @Autowired
    private RecruitChannelService channelService;

    @PostMapping
    @ApiOperation("新增候选人（报名）")
    public Result<RecruitCandidate> save(@RequestBody RecruitCandidate candidate) {
        try {
            RecruitChannel channel = channelService.getById(candidate.getChannelId());
            if (channel == null) {
                return Result.error("来源渠道不存在");
            }
            if ("paused".equals(channel.getStatus())) {
                return Result.error("该渠道已暂停合作，无法报名");
            }
            candidate.setId(null);
            candidate.setStage("registered");
            if (candidate.getApplyTime() == null) {
                candidate.setApplyTime(LocalDateTime.now());
            }
            candidate.setCreateTime(LocalDateTime.now());
            candidate.setUpdateTime(LocalDateTime.now());
            candidateService.save(candidate);
            return Result.success("报名成功", candidate);
        } catch (Exception e) {
            log.error("新增候选人失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新候选人基础信息")
    public Result<RecruitCandidate> update(@PathVariable Long id, @RequestBody RecruitCandidate candidate) {
        try {
            candidate.setId(id);
            // 阶段与阶段时间只能由阶段推进/离职接口修改，防止误改
            candidate.setStage(null);
            candidate.setInterviewTime(null);
            candidate.setTrialTime(null);
            candidate.setOnboardTime(null);
            candidate.setRetainTime(null);
            candidate.setResignTime(null);
            candidate.setResignReason(null);
            candidate.setUpdateTime(LocalDateTime.now());
            candidateService.updateById(candidate);
            return Result.success("更新成功", candidate);
        } catch (Exception e) {
            log.error("更新候选人失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除候选人")
    public Result<?> delete(@PathVariable Long id) {
        try {
            candidateService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除候选人失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询候选人")
    public Result<RecruitCandidate> getById(@PathVariable Long id) {
        RecruitCandidate candidate = candidateService.getById(id);
        if (candidate == null) {
            return Result.error("候选人不存在");
        }
        return Result.success(candidate);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询候选人")
    public Result<PageResult<RecruitCandidate>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String candidateName,
            @RequestParam(required = false) Long channelId,
            @RequestParam(required = false) String stage) {
        Page<RecruitCandidate> page = candidateService.pageQuery(current, size, candidateName, channelId, stage);
        PageResult<RecruitCandidate> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
        return Result.success(pageResult);
    }

    @PutMapping("/{id}/stage")
    @ApiOperation("推进候选人阶段（interview-面试，trial-试岗，onboard-到岗，retain-留任七天，eliminate-淘汰）")
    public Result<?> advanceStage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            candidateService.advanceStage(id, body.get("action"));
            return Result.success("阶段推进成功");
        } catch (Exception e) {
            log.error("阶段推进失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/resign")
    @ApiOperation("登记离职（需填写离职原因）")
    public Result<?> resign(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            candidateService.resign(id, body.get("resignReason"));
            return Result.success("离职登记成功");
        } catch (Exception e) {
            log.error("离职登记失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/channel-options")
    @ApiOperation("查询候选人关联的渠道名称映射")
    public Result<Map<Long, String>> channelOptions() {
        List<RecruitChannel> channels = channelService.list();
        Map<Long, String> map = channels.stream()
                .collect(Collectors.toMap(RecruitChannel::getId, RecruitChannel::getChannelName));
        return Result.success(map);
    }
}
