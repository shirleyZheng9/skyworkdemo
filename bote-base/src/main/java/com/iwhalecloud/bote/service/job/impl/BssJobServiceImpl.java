package com.iwhalecloud.bote.service.job.impl;

import com.iwhalecloud.bote.dto.job.JobLogDTO;
import com.iwhalecloud.bote.dto.job.JobDTO;
import com.iwhalecloud.bote.dto.job.query.JobQueryParams;
import com.iwhalecloud.bote.mapper.job.JobManageMapper;
import com.iwhalecloud.bss.litchi.job.service.IBssJobService;
import com.iwhalecloud.bss.litchi.job.vo.BssJobLogVO;
import com.iwhalecloud.bss.litchi.job.vo.BssJobVO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * 定时任务配置服务，定制实现
 *
 * @author qian.sisheng
 * @since 2025-11-07
 */
@Service
@RequiredArgsConstructor
public class BssJobServiceImpl implements IBssJobService {

  private final JobManageMapper jobManageMapper;

  @Override
  public BssJobVO selectOneById(String jobId, String center) {
    JobDTO job = jobManageMapper.getJob(Long.valueOf(jobId));
    return job == null ? null : job.toBssJob();
  }

  @Override
  public List<BssJobVO> selectAll(String center, String state) {
    JobQueryParams jobQueryParams = new JobQueryParams();
    jobQueryParams.setState(state);
    List<JobDTO> jobs = jobManageMapper.selectJobList(jobQueryParams);
    if (CollectionUtils.isEmpty(jobs)) {
      return null;
    }
    return jobs.stream().map(JobDTO::toBssJob).collect(Collectors.toList());
  }

  @Override
  public void updateById(BssJobVO job) {
    // 无需实现
  }

  @Override
  public void insert(BssJobVO job) {
    // 无需实现
  }

  @Override
  public boolean existsById(String jobId) {
    return jobManageMapper.getJob(Long.valueOf(jobId)) != null;
  }

  @Override
  public void insertLog(BssJobLogVO log) {
    JobLogDTO jobLog = new JobLogDTO();
    jobManageMapper.insertJobLog(jobLog.toBssJobLog(log));
  }
}
