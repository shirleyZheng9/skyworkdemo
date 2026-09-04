package com.iwhalecloud.bote.controller.agent;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.util.CronTranslateUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.channel.SimpleChannelJobDTO;
import com.iwhalecloud.bote.dto.job.JobDTO;
import com.iwhalecloud.bote.dto.job.query.JobQueryParams;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.service.job.IJobManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * AI通用智能体定时任务管理 controller
 *
 * @author wangtingyun
 * @since 2026-03-10
 */
@RestController
@RequestMapping(value = CommonConsts.API_PREFIX + "manager/aiAgentJob", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "AI智能体定时任务管理")
public class AiAgentJobManageController {

  private final BotQueryMapper botQueryMapper;
  private final IJobManageService JobManageService;

  @Operation(summary = "查询单个定时任务")
  @GetMapping("findJob")
  public ResultVO<JobDTO> findJob(@RequestParam(name = "jobId") Long jobId) {
    Assert.notNull(jobId, "主键 ID 不能为空");
    return ResultVO.success(JobManageService.findJob(jobId));
  }

  @Operation(summary = "保存定时任务")
  @PostMapping("saveJob")
  public ResultVO<JobDTO> saveJob(@RequestBody JobDTO job) {
    Assert.notNull(job.getSpaceId(), "企业空间 ID 不能为空");
    Assert.notNull(job.getBotId(), "智能应用 ID 不能为空");
    if (StringUtils.isEmpty(job.getBssJobClass())) {
      job.setBssJobClass("com.iwhalecloud.bote.job.CallAiAgentJob");
    }
    if (job.getTenantId() == null) {
      // 计算出 boteclaw 归属的空间 ID，作为 tenant_id 数据
      Long tenantId = job.getSpaceId();
      if (!BaseConsts.BOTE_AI_ID.equals(job.getBotId())) {
        tenantId = botQueryMapper.getBoteClawSpacId(job.getBotId());
      }
      job.setTenantId(tenantId);
    }
    job.setDataFrom(BaseConsts.DATA_FROM_PORTAL_BOT);
    job.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    // cronDesc 由 Service 层 syncCronFields 统一处理，仅在未传 jobCronParameter 时兜底填充
    if (StringUtils.isBlank(job.getJobCronParameter()) && StringUtils.isNotBlank(job.getBssJobCron())) {
      job.setCronDesc(CronTranslateUtil.toChineseDescription(job.getBssJobCron()));
    }
    // 填充定时任务参数
    fillJobParams(job);
    // 执行保存定时任务
    ResultVO<JobDTO> result = JobManageService.saveJob(job);
    if (result.isSuccess()) {
      // 刷新定时任务
      JobManageService.refreshJob(job.getBssJobId());
    }
    return result;
  }

  /**
   * 填充定时任务参数
   */
  private void fillJobParams(JobDTO job) {
    SimpleChannelJobDTO channel = JsonUtil.parseJson(job.getJobParameter(), SimpleChannelJobDTO.class);
    if (channel == null) {
      return;
    }
    Map<String, String> map = new HashMap<>();
    // 渠道类型为可选项
    if (StringUtils.isNotEmpty(channel.getChannelType())) {
      map.put("channelType", channel.getChannelType());
      // 钉钉和飞书渠道需要填写 webhook
      if (BaseConsts.CHANNEL_TYPE_DINGTALK.equals(channel.getChannelType()) || BaseConsts.CHANNEL_TYPE_FEISHU.equals(channel.getChannelType())) {
        Assert.hasText(channel.getWebhook(), "渠道 webhook 不能为空");
      }
      map.put("webhook", channel.getWebhook());
    }
    map.put("taskType", channel.getTaskType());
    map.put("requestInput", channel.getRequestInput());
    map.put("title", job.getBssJobName());
    map.put("spaceId", String.valueOf(job.getSpaceId()));
    map.put("tenantId", String.valueOf(job.getTenantId()));
    map.put("botId", String.valueOf(job.getBotId()));
    map.put("userId", String.valueOf(SessionUtil.getLoginInfo().getUserId()));
    job.setJobParameter(JsonUtil.toJsonString(map));
  }

  @Operation(summary = "删除定时任务")
  @GetMapping("deleteJob")
  public ResultVO<Void> deleteJob(@RequestParam(name = "jobId") Long jobId) {
    Assert.notNull(jobId, "主键 ID 不能为空");
    return JobManageService.deleteJob(jobId);
  }

  @Operation(summary = "分页查询定时任务")
  @PostMapping("queryJobPage")
  public ResultVO<PageInfo<JobDTO>> queryJobPage(@RequestBody JobQueryParams queryParams) {
    Assert.notNull(queryParams.getSpaceId(), "企业空间 ID 不能为空");
    return ResultVO.success(JobManageService.queryJobPageForRuntime(queryParams));
  }

  @Operation(summary = "禁用定时任务")
  @GetMapping("disableJob")
  public ResultVO<Void> disableJob(@RequestParam(name = "jobId") Long jobId) {
    JobManageService.pauseJob(jobId);
    return ResultVO.successWithMsg("定时任务禁用成功");
  }

  @Operation(summary = "启用定时任务")
  @GetMapping("enableJob")
  public ResultVO<Void> enableJob(@RequestParam(name = "jobId") Long jobId) {
    JobManageService.resumeJob(jobId);
    return ResultVO.successWithMsg("定时任务启用成功");
  }

  @Operation(summary = "运行定时任务")
  @GetMapping("triggerJob")
  public ResultVO<Void> triggerJob(@RequestParam(name = "jobId") Long jobId) {
    return JobManageService.triggerJob(jobId);
  }

}
