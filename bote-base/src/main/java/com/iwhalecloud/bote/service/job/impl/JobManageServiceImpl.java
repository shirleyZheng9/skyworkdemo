package com.iwhalecloud.bote.service.job.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.dto.job.JobLogDTO;
import com.iwhalecloud.bote.dto.job.query.JobQueryParams;
import com.iwhalecloud.bote.mapper.job.JobManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.CronParameterConverter;
import com.iwhalecloud.bote.common.util.CronTranslateUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.job.JobDTO;
import com.iwhalecloud.bote.dto.job.cron.JobCronParameterDTO;
import com.iwhalecloud.bote.service.job.IJobManageService;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.job.config.JobReader;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 定时任务管理服务实现
 *
 * @author qian.sisheng
 * @since 2025-11-06
 */
@Service
@RequiredArgsConstructor
public class JobManageServiceImpl implements IJobManageService {

  private final JobManageMapper jobManageMapper;

  private JobReader jobReader;

  /**
   * 使用 setter 方式注入依赖，以兼容应用未开启定时任务功能的场景
   */
  @Autowired(required = false)
  public void setJobReader(JobReader jobReader) {
    this.jobReader = jobReader;
  }

  /**
   * 懒填充：查询返回时若 jobCronParameter 为空，则从 bssJobCron 实时补上（不回写数据库）。
   */
  private static void lazyFillCronParameter(JobDTO job) {
    if (job != null && StringUtils.isBlank(job.getJobCronParameter()) && StringUtils.isNotBlank(job.getBssJobCron())) {
      JobCronParameterDTO bean = CronParameterConverter.fromCron(job.getBssJobCron());
      job.setJobCronParameter(JsonUtil.toJsonString(bean));
    }
  }

  /**
   * 同步 jobCronParameter 与 bssJobCron：
   * <ul>
   *   <li>有 jobCronParameter → 推导 bssJobCron 和 cronDesc</li>
   *   <li>只有 bssJobCron → 反向填充 jobCronParameter</li>
   * </ul>
   */
  private static void syncCronFields(JobDTO job) {
    if (StringUtils.isNotBlank(job.getJobCronParameter())) {
      // 前端 / 接口传入了结构化参数 → 生成 cron
      JobCronParameterDTO bean = JsonUtil.parseJson(job.getJobCronParameter(), JobCronParameterDTO.class);
      if (bean != null && StringUtils.isNotBlank(bean.getType())) {
        job.setBssJobCron(CronParameterConverter.toCron(bean));
        job.setCronDesc(CronTranslateUtil.toChineseDescription(job.getBssJobCron()));
      }
    } else if (StringUtils.isNotBlank(job.getBssJobCron())) {
      // 智能体 / 旧接口只传了 cron → 反向填充结构化参数
      JobCronParameterDTO bean = CronParameterConverter.fromCron(job.getBssJobCron());
      job.setJobCronParameter(JsonUtil.toJsonString(bean));
    }
  }

  /**
   * 校验 cron 表达式，失败时抛出带解析详情的异常（便于定位是哪一个节点配置错误）。
   * Quartz 原始英文提示会转换为中文以便用户理解。
   */
  private static void validateCronExpression(String cron) {
    try {
      new CronExpression(cron);
    } catch (ParseException e) {
      String raw = e.getMessage() != null ? e.getMessage() : "请检查表达式格式";
      String detail = toChineseCronError(raw);
      throw new IllegalArgumentException("执行频率表达式格式错误：" + detail, e);
    }
  }

  /** Quartz cron 解析错误英文 → 中文提示的规则表（顺序即匹配优先级）。 */
  private static final List<AbstractMap.SimpleEntry<Predicate<String>, String>> CRON_ERROR_RULES = Arrays.asList(
      new AbstractMap.SimpleEntry<>(
          m -> m.contains("can only be specified for") && m.contains("Day-of-Month") && m.contains("Day-of-Week"),
          "字符'?'只能用于“日（月）”或“星期”段，不能用于其他段"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("Day of week and day of month can not both be"),
          "“日（月）”和“星期”不能同时为'?'，需二选一指定"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("Support for specifying both a day-of-week AND a day-of-month"),
          "不能同时指定“日（月）”和“星期”，需二选一"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("invalid character") || m.contains("Invalid character"),
          "存在非法字符，请检查秒、分、时、日、月、星期各段是否仅使用数字、*、?、-、,、/等合法符号"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("Unexpected end of expression"),
          "表达式不完整，请检查是否缺少某一段（秒、分、时、日、月、星期）"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("seconds") || m.contains("Seconds"),
          "“秒”段格式有误，请检查该段是否符合规范（0-59 或 * , - /）"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("minutes") || m.contains("Minutes"),
          "“分”段格式有误，请检查该段是否符合规范（0-59 或 * , - /）"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("hours") || m.contains("Hours"),
          "“时”段格式有误，请检查该段是否符合规范（0-23 或 * , - /）"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("day-of-month") || m.contains("Day-of-Month") || m.contains("day of month"),
          "“日（月）”段格式有误，请检查该段是否符合规范（1-31 或 * ? , - / L W）"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("month") || m.contains("Month"),
          "“月”段格式有误，请检查该段是否符合规范（1-12 或 JAN-DEC 或 * , - /）"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("day-of-week") || m.contains("Day-of-Week") || m.contains("day of week"),
          "“星期”段格式有误，请检查该段是否符合规范（1-7 或 SUN-SAT 或 * ? , - / L #）"),
      new AbstractMap.SimpleEntry<>(m -> m.contains("year") || m.contains("Year"),
          "“年”段格式有误，请检查该段是否符合规范（1970-2199 或 * , - /）")
  );

  private static final String CRON_ERROR_DEFAULT_MSG =
      "表达式格式有误，请按“秒、分、时、日、月、星期”顺序检查各段是否完整且使用合法符号（数字、*、?、-、,、/等）。";

  /** 将 Quartz 常见的 cron 解析英文错误信息转换为中文提示。 */
  private static String toChineseCronError(String message) {
    if (message == null || message.isEmpty()) {
      return "请检查表达式格式";
    }
    for (AbstractMap.SimpleEntry<Predicate<String>, String> rule : CRON_ERROR_RULES) {
      if (rule.getKey().test(message)) {
        return rule.getValue();
      }
    }
    return CRON_ERROR_DEFAULT_MSG;
  }

  @Override
  public JobDTO findJob(Long jobId) {
    JobDTO job = jobManageMapper.getJob(jobId);
    lazyFillCronParameter(job);
    return job;
  }

  @Override
  @Transactional
  public ResultVO<JobDTO> saveJob(JobDTO job) {
    // 校验名称唯一性
    if (jobManageMapper.existsJobName(job)) {
      return BaseErrorConstant.CHECK_NAME.toResult();
    }
    String center = SpringUtil.getProperty("job.config.center", "bote");
    job.setCenter(center);
    // 同步 jobCronParameter 与 bssJobCron
    syncCronFields(job);
    // 校验执行频率表达式的合法性，避免应用启动失败
    Assert.hasText(job.getBssJobCron(), "cron表达式不能为空");
    validateCronExpression(job.getBssJobCron());
    job.setState(StringUtils.defaultIfEmpty(job.getState(), CommonConsts.STATUS_CD_VALID));
    // 默认执行类为 CallFlowServiceJob
    job.setBssJobClass(StringUtils.defaultIfEmpty(job.getBssJobClass(), "com.iwhalecloud.bote.job.CallFlowServiceJob"));
    // 默认分片总数为 1
    if (job.getShardingTotalCount() == null) {
      job.setShardingTotalCount(1L);
    }
    JobDTO old = job.getBssJobId() == null ? null : findJob(job.getBssJobId());
    DataDifference<JobDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, job, false, job.getTenantId(), OperClassEnum.JOB);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteJob(Long jobId) {
    JobDTO job = findJob(jobId);
    Assert.notNull(job, "定时任务不存在");
    jobManageMapper.deleteJob(jobId, SessionUtil.getLoginInfo().getUserId());
    jobReader.shutdown(jobId.toString());
    return ResultVO.success();
  }

  @Override
  public List<JobDTO> queryJobList(JobQueryParams queryParams) {
    return jobManageMapper.selectJobList(queryParams);
  }

  @Override
  public PageInfo<JobDTO> queryJobPage(JobQueryParams queryParams) {
    // noinspection resource
    PageInfo<JobDTO> pageInfo = jobManageMapper.selectJobPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
    setJobIsRunning(pageInfo.getList());
    return pageInfo;
  }

  /**
   * 设置定时任务是否正在运行
   *
   * <p>以定时任务实际是否在运行作为定时任务【运行/启用】状态标志位</p>
   */
  private void setJobIsRunning(List<JobDTO> jobDTOList) {
    if (CollectionUtils.isEmpty(jobDTOList)) {
      return;
    }
    for (JobDTO job : jobDTOList) {
      lazyFillCronParameter(job);
      if (jobReader != null) {
        boolean isRunning = jobReader.isJobRunning(job.getBssJobId().toString());
        job.setIsRunning(isRunning ? CommonConsts.TRUE : CommonConsts.FALSE);
      }
      else {
        job.setIsRunning(CommonConsts.FALSE);
      }
    }
  }

  @Override
  public ResultVO<String> checkJobRunning(Long jobId) {
    if (jobReader == null) {
      return ResultVO.success(CommonConsts.FALSE);
    }
    return ResultVO.success(jobReader.isJobRunning(jobId.toString()) ? CommonConsts.TRUE : CommonConsts.FALSE);
  }

  @Override
  @Transactional
  public ResultVO<Void> pauseJob(Long jobId) {
    // 兼容未开启定时任务功能的场景
    // 暂停功能需要做到持久化，因此需要使用定时任务框架的下线功能而非暂停功能（暂停功能只是在 ZooKeeper 上标记禁用状态，应用重启、启动新应用实例仍会执行定时任务）
    if (jobReader != null) {
      jobReader.shutdown(jobId.toString());
    }
    // 调整定时任务状态，避免应用重启后，暂停过的定时任务又运行
    updateJobStatusCd(jobId, CommonConsts.STATUS_CD_WAITE);
    return ResultVO.successWithMsg("应用定时任务暂停成功");
  }

  @Override
  @Transactional
  public ResultVO<Void> resumeJob(Long jobId) {
    // 恢复的目的是执行定时任务，未开启定时任务时要报错，避免用户误以为启动成功
    if (jobReader == null) {
      throw new BssException("未开启定时任务功能");
    }
    jobReader.startJob(jobId.toString());
    // 恢复定时任务状态
    updateJobStatusCd(jobId, CommonConsts.STATUS_CD_VALID);
    return ResultVO.successWithMsg("应用定时任务恢复成功");
  }

  @Override
  public ResultVO<Void> triggerJob(Long jobId) {
    // 无法触发任务时要报错
    if (jobReader == null) {
      throw new BssException("未开启定时任务功能");
    }
    JobDTO job = jobManageMapper.getJob(jobId);
    Assert.notNull(job, "定时任务不存在");
    // 无论任务状态是正常还是暂停，都允许手动触发
    // 传递 BssJobVO 以方便触发暂停状态的定时任务
    jobReader.triggerJob(job.toBssJob());
    return ResultVO.success();
  }

  @Override
  public ResultVO<Void> refreshJob(Long jobId) {
    // 兼容未开启定时任务功能的场景
    if (jobReader != null) {
      jobReader.startJob(jobId.toString());
    }
    return ResultVO.success();
  }

  /**
   * 更新定时任务状态
   *
   * @param jobId 定时任务 ID
   * @param state 定时任务状态
   */
  private void updateJobStatusCd(Long jobId, String state) {
    JobDTO job = new JobDTO();
    job.setBssJobId(jobId);
    job.setState(state);
    job.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    jobManageMapper.updateJob(job);
  }

  @Override
  public PageInfo<JobLogDTO> queryJobLogPage(JobQueryParams queryParams) {
    // noinspection resource
    return jobManageMapper.selectJobLogPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<JobDTO> queryJobPageForRuntime(JobQueryParams queryParams) {
    queryParams.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    // noinspection resource
    PageInfo<JobDTO> pageInfo = jobManageMapper.selectJobPageForRuntime(queryParams, queryParams.buildRowBounds()).toPageInfo();
    setJobIsRunning(pageInfo.getList());
    return pageInfo;
  }
}
