package com.iwhalecloud.bote.agent.tools;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.cache.GeneraAgentCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.CronParameterConverter;
import com.iwhalecloud.bote.dto.job.JobDTO;
import com.iwhalecloud.bote.dto.job.cron.JobCronParameterDTO;
import com.iwhalecloud.bote.dto.job.query.JobQueryParams;
import com.iwhalecloud.bote.service.job.IJobManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 定时任务工具：供通用智能体查询、创建、删除、禁用、启用、触发定时任务（参照 CoPaw cron skill + executor）
 *
 * @author chen.linfa
 * @since 2026-03-13
 */
@SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public final class CronTools {

  private static final String CALL_AI_AGENT_JOB_CLASS = "com.iwhalecloud.bote.job.CallAiAgentJob";
  /** 钉钉 Webhook 环境变量名 */
  private static final String ENV_DINGTALK_WEBHOOK = "DINGTALK_WEBHOOK";
  /** 飞书 Webhook 环境变量名 */
  private static final String ENV_FEISHU_WEBHOOK = "FEISHU_WEBHOOK";
  private static final IJobManageService jobManageService = SpringUtil.getBean(IJobManageService.class);

  private CronTools() {
  }

  @Tool(
    name = "cron_list",
    description = "List scheduled jobs (cron jobs) for the current user and space with pagination. Returns job id, name, cron, state, etc. Use before get/delete/disable/enable/trigger to get job_id."
  )
  public static String cronList(
    @ToolParam(description = "Page number, 1-based, default 1") @Nullable Integer pageNum,
    @ToolParam(description = "Page size, default 20") @Nullable Integer pageSize,
    @ToolParam(description = "Search by job name (fuzzy)") @Nullable String searchContent,
    @Nullable ToolContext toolContext
  ) {
    Assert.notNull(toolContext, "toolContext is required");

    JobQueryParams params = new JobQueryParams();
    params.setSpaceId(toolContext.spaceId());
    params.setTenantId(toolContext.tenantId());
    params.setBotId(toolContext.botId());
    params.setCreatorId(toolContext.userId());
    params.setPageNum(pageNum != null ? pageNum : 1);
    params.setPageSize(pageSize != null ? pageSize : 20);
    if (StringUtils.isNotBlank(searchContent)) {
      params.setSearchContent(searchContent.trim());
    }

    PageInfo<JobDTO> pageInfo = jobManageService.queryJobPage(params);

    Map<String, Object> out = new HashMap<>();
    out.put("list", pageInfo.getList());
    out.put("total", pageInfo.getTotal());
    out.put("pageNum", pageInfo.getPageNum());
    out.put("pageSize", pageInfo.getPageSize());
    return JsonUtil.toJsonString(out);
  }

  @Tool(
    name = "cron_get",
    description = "Get a single scheduled job by job_id. Returns full job details including name, cron, state, jobParameter."
  )
  public static String cronGet(
    @ToolParam(description = "Job ID (bss_job_id)") Long jobId,
    @Nullable ToolContext toolContext
  ) {
    Assert.notNull(jobId, "jobId is required");

    JobDTO job = jobManageService.findJob(jobId);
    if (job == null) {
      throw new ToolExecutionException("Error: 定时任务不存在: jobId=" + jobId);
    }
    return JsonUtil.toJsonString(job);
  }

  @Tool(
    name = "cron_create",
    description = "Create a scheduled job that runs the general agent or sends text at a fixed time. Required: name (in Chinese), cron (Quartz 6-field syntax only — not Linux 5-field crontab), channelType, taskType, requestInput. For DingTalk/Feishu: webhook is resolved from param, else env DINGTALK_WEBHOOK/FEISHU_WEBHOOK, else guide user to set env or input webhook."
  )
  public static String cronCreate(
    @ToolParam(description = "Job name, must be in Chinese (e.g. 每日早安、每周汇总)") String name,
    @ToolParam(description = "Quartz 6-field: sec min hour dom month dow (not Linux crontab). Dow 1=SUN..7=SAT; Tue=3 (crontab Tue=2 maps to Mon=2 here). Tue 9:00: 0 0 9 ? * 3. Daily 9:00: 0 0 9 * * ?") String cron,
    @ToolParam(description = "Channel type, e.g. DingTalk, Feishu, console") String channelType,
    @ToolParam(description = "Task type: text = send fixed text; agent = run general agent with requestInput as prompt") String taskType,
    @ToolParam(description = "Content: for text = message to send; for agent = prompt to the agent") String requestInput,
    @ToolParam(description = "Webhook URL; for DingTalk/Feishu can be omitted if env DINGTALK_WEBHOOK/FEISHU_WEBHOOK is set; otherwise user must set env or provide here.") @Nullable String webhook,
    @Nullable ToolContext toolContext
  ) {
    Assert.notNull(toolContext, "toolContext is required");
    Assert.hasText(name, "name is required");
    Assert.hasText(cron, "cron is required");
    Assert.hasText(taskType, "taskType is required");
    Assert.hasText(requestInput, "requestInput is required");

    if (isDingTalkOrFeishuChannel(channelType)) {
      webhook = getWebhook(channelType, webhook, toolContext);
      if (StringUtils.isEmpty(webhook)) {
        String channelLabel = isDingTalkChannel(channelType) ? "钉钉" : "飞书";
        String envKey = isDingTalkChannel(channelType) ? ENV_DINGTALK_WEBHOOK : ENV_FEISHU_WEBHOOK;
        String msg = """
          Error: 对接%s渠道需要 Webhook 地址。请引导用户任选其一:
          1. 在智能体「高级配置」中配置 %s
          2. 在此处直接提供 Webhook URL 后再次创建""".formatted(channelLabel, envKey);
        throw new ToolExecutionException(msg);
      }
    }
    else {
      channelType = "";
    }

    Map<String, String> paramMap = new HashMap<>();
    paramMap.put("channelType", channelType);
    paramMap.put("taskType", taskType);
    paramMap.put("requestInput", requestInput.trim());
    paramMap.put("webhook", webhook);
    paramMap.put("title", name.trim());
    paramMap.put("spaceId", String.valueOf(toolContext.spaceId()));
    paramMap.put("tenantId", String.valueOf(toolContext.tenantId()));
    paramMap.put("botId", String.valueOf(toolContext.botId()));
    paramMap.put("userId", String.valueOf(toolContext.userId()));

    JobDTO job = new JobDTO();
    job.setBssJobName(name.trim());
    job.setBssJobCron(cron.trim());
    job.setBssJobClass(CALL_AI_AGENT_JOB_CLASS);
    job.setSpaceId(toolContext.spaceId());
    job.setTenantId(toolContext.tenantId());
    job.setBotId(toolContext.botId());
    job.setCreatorId(toolContext.userId());

    // 将 cron 表达式转译为结构化调度参数
    JobCronParameterDTO cronParam = CronParameterConverter.fromCron(cron.trim());
    job.setJobCronParameter(JsonUtil.toJsonString(cronParam));

    job.setJobParameter(JsonUtil.toJsonString(paramMap));

    ResultVO<JobDTO> result = jobManageService.saveJob(job);
    if (!result.isSuccess()) {
      throw new ToolExecutionException("Error: " + result.getResultMsg());
    }
    if (result.getResultObject() != null) {
      jobManageService.refreshJob(result.getResultObject().getBssJobId());
    }
    return JsonUtil.toJsonString(Map.of("success", true, "job", result.getResultObject(), "message", "定时任务创建成功"));
  }

  @Tool(name = "cron_delete", description = "Delete a scheduled job by job_id. Use cron_list to get job_id first.")
  public static String cronDelete(
    @ToolParam(description = "Job ID to delete") Long jobId,
    @Nullable ToolContext toolContext
  ) {
    Assert.notNull(jobId, "jobId is required");
    ResultVO<Void> result = jobManageService.deleteJob(jobId);
    if (!result.isSuccess()) {
      throw new ToolExecutionException("Error: " + result.getResultMsg());
    }
    return JsonUtil.toJsonString(Map.of("success", true, "message", "定时任务已删除"));
  }

  @Tool(name = "cron_disable", description = "Disable (pause) a scheduled job by job_id. The job will not run until cron_enable is called.")
  public static String cronDisable(
    @ToolParam(description = "Job ID to disable") Long jobId,
    @Nullable ToolContext toolContext
  ) {
    Assert.notNull(jobId, "jobId is required");
    ResultVO<Void> result = jobManageService.pauseJob(jobId);
    if (!result.isSuccess()) {
      throw new ToolExecutionException("Error: " + result.getResultMsg());
    }
    return JsonUtil.toJsonString(Map.of("success", true, "message", "定时任务已禁用"));
  }

  @Tool(name = "cron_enable", description = "Enable a previously disabled scheduled job by job_id.")
  public static String cronEnable(
    @ToolParam(description = "Job ID to enable") Long jobId,
    @Nullable ToolContext toolContext
  ) {
    Assert.notNull(jobId, "jobId is required");
    ResultVO<Void> result = jobManageService.resumeJob(jobId);
    if (!result.isSuccess()) {
      throw new ToolExecutionException("Error: " + result.getResultMsg());
    }
    return JsonUtil.toJsonString(Map.of("success", true, "message", "定时任务已启用"));
  }

  @Tool(name = "cron_trigger", description = "Run a scheduled job once immediately by job_id. Does not change the schedule.")
  public static String cronTrigger(
    @ToolParam(description = "Job ID to run once") Long jobId,
    @Nullable ToolContext toolContext
  ) {
    Assert.notNull(jobId, "jobId is required");
    ResultVO<Void> result = jobManageService.triggerJob(jobId);
    if (!result.isSuccess()) {
      throw new ToolExecutionException("Error: " + result.getResultMsg());
    }
    return JsonUtil.toJsonString(Map.of("success", true, "message", "已触发执行一次"));
  }

  private static String getWebhook(String channelType, @Nullable String webhook, ToolContext toolContext) {
    if (StringUtils.isNotEmpty(webhook)) {
      return webhook;
    }
    String envKey = isDingTalkChannel(channelType) ? ENV_DINGTALK_WEBHOOK : ENV_FEISHU_WEBHOOK;
    GeneraAgentCache cache = SpringUtil.getBean(GeneraAgentCache.class);
    Map<String, String> envVariables = cache.getEnvVariables(toolContext.tenantId(), toolContext.botId(), toolContext.userId());
    if (MapUtils.isNotEmpty(envVariables)) {
      return envVariables.get(envKey);
    }
    return "";
  }

  private static boolean isDingTalkOrFeishuChannel(String channelType) {
    if (StringUtils.isBlank(channelType)) {
      return false;
    }
    String t = channelType.trim();
    return BaseConsts.CHANNEL_TYPE_DINGTALK.equalsIgnoreCase(t) || "钉钉".equals(t)
      || BaseConsts.CHANNEL_TYPE_FEISHU.equalsIgnoreCase(t) || "飞书".equals(t);
  }

  private static boolean isDingTalkChannel(String channelType) {
    if (StringUtils.isBlank(channelType)) {
      return false;
    }
    String t = channelType.trim();
    return BaseConsts.CHANNEL_TYPE_DINGTALK.equalsIgnoreCase(t) || "钉钉".equals(t);
  }
}
