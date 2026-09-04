package com.iwhalecloud.bote.service.element.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.job.JobDTO;
import com.iwhalecloud.bote.mapper.job.JobManageMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 定时任务
 *
 * @author qian.sisheng
 * @since 2025-12-05
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.JOB)
public class JobResourceElementCustomizer extends AbstractResourceElementCustomizer {
  private final JobManageMapper jobManageMapper;
  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.JOB.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long resourceId) {
    JobDTO job = jobManageMapper.getJob(resourceId);
    List<ResourceElementDTO> elements = new ArrayList<>();
    if (job == null) {
      return elements;
    }
    Map<String, Object> parameter = JsonUtil.parseJson(job.getJobParameter(), new TypeReference<>() {
    });
    if (MapUtils.isEmpty(parameter)) {
      return elements;
    }
    elements.add(createElement(tenantId, resourceId, MapUtils.getLong(parameter, "flowId"), DataSyncCodeEnum.SKILL_FLOW.getCode()));
    return elements;
  }
}
