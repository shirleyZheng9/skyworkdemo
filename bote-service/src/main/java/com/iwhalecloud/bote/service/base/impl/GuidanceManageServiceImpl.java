package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.common.consts.PrivConsts;
import com.iwhalecloud.bote.dto.base.GuidanceCfgDTO;
import com.iwhalecloud.bote.mapper.base.GuidanceCfgMapper;
import com.iwhalecloud.bote.service.base.IGuidanceManageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 平台使用指引管理服务
 *
 * @author wangtingyun
 * @since 2025-11-11
 */
@Service
@RequiredArgsConstructor
public class GuidanceManageServiceImpl implements IGuidanceManageService {

  private final GuidanceCfgMapper guidanceCfgMapper;

  /** 指引步骤列表 */
  private static final List<String> GUIDE_STEP_LIST = Arrays.asList("bote_navigation_guide_step", "bote_side_guide_step", "bote_beginner_guide_step");

  @Override
  public List<GuidanceCfgDTO> queryStepGuidance(String roleCode) {
    Assert.hasText(roleCode, "用户角色编码不能为空");
    List<GuidanceCfgDTO> result = new ArrayList<>();
    // 查询引导步骤列表
    List<GuidanceCfgDTO> stepGuidanceList = guidanceCfgMapper.selectStepGuidance(GUIDE_STEP_LIST);
    if (stepGuidanceList.isEmpty()) {
      return result;
    }
    // 根据用户角色返回步骤指引信息
    switch (roleCode) {
      // 平台管理员、企业管理员、项目管理、返回全部
      case PrivConsts.ROLE_SUPER_ADMIN, PrivConsts.ROLE_SPACE_ADMIN, PrivConsts.ROLE_MANAGE: break;
      case PrivConsts.ROLE_EDIT:
        // 项目编辑角色过滤管理菜单
        stepGuidanceList = stepGuidanceList.stream().filter(o -> !"manageIntro".equals(o.getCode())).toList();
        break;
      case PrivConsts.ROLE_READONLY:
        // 项目只读角色过滤创建、设置、管理菜单
        List<String> excludeList = Arrays.asList("createIntro", "settingIntro", "manageIntro");
        stepGuidanceList = stepGuidanceList.stream().filter(o -> !excludeList.contains(o.getCode())).toList();
        break;
      case PrivConsts.ROLE_USE:
        // 使用者角色只返回应用和广场菜单
        List<String> includeList = Arrays.asList("appIntro", "groundIntro");
        stepGuidanceList = stepGuidanceList.stream().filter(o -> includeList.contains(o.getCode())).toList();
        break;
      default: return List.of();
    }
    // 根据步骤类型分组装数据
    for (String step : GUIDE_STEP_LIST) {
      List<GuidanceCfgDTO> guideList = stepGuidanceList.stream().filter(guide -> step.equals(guide.getGuideType())).toList();
      if (CollectionUtils.isNotEmpty(guideList)) {
        // 取列表中的第一个作为父引导对象
        GuidanceCfgDTO guideDTO = new GuidanceCfgDTO();
        guideDTO.setGuideType(guideList.getFirst().getGuideType());
        guideDTO.setDetails(guideList);
        result.add(guideDTO);
      }
    }
    return result;
  }

  @Override
  public List<GuidanceCfgDTO> getBeginnerGuidance() {
    // 查询新手指引配置信息
    Map<Long, List<GuidanceCfgDTO>> guideGroupMap = guidanceCfgMapper.selectBeginnerGuidanceCfg().stream()
      .collect(Collectors.groupingBy(GuidanceCfgDTO::getGroupId));
    // 按分组进行封装
    List<GuidanceCfgDTO> result = new ArrayList<>();
    for (Map.Entry<Long, List<GuidanceCfgDTO>> entry : guideGroupMap.entrySet()) {
      List<GuidanceCfgDTO> guideList = entry.getValue();
      if (CollectionUtils.isNotEmpty(guideList)) {
        // 取列表中的第一个作为父引导对象
        GuidanceCfgDTO guideDTO = new GuidanceCfgDTO();
        guideDTO.setName(guideList.getFirst().getName());
        guideDTO.setCode(guideList.getFirst().getCode());
        guideDTO.setSortby(guideList.getFirst().getSortby());
        guideDTO.setDetails(guideList);
        result.add(guideDTO);
      }
    }
    // 排序
    return result.stream().sorted(Comparator.comparing(GuidanceCfgDTO::getSortby)).toList();
  }

  @Override
  public Boolean existsBeginnerGuidance() {
    return guidanceCfgMapper.existsBeginnerGuidanceCfg();
  }

}
