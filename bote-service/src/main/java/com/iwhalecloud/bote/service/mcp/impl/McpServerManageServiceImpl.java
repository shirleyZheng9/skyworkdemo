package com.iwhalecloud.bote.service.mcp.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bote.dto.mcp.McpServerQueryParams;
import com.iwhalecloud.bote.dto.mcp.SimpleMcpServiceDTO;
import com.iwhalecloud.bote.mapper.mcp.McpServerManageMapper;
import com.iwhalecloud.bote.service.base.ILabelManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.mcp.IMcpServerManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * mcp管理服务实现
 *
 * @author auto
 * @since 2025-05-13
 */
@Service
@RequiredArgsConstructor
public class McpServerManageServiceImpl implements IMcpServerManageService {

  private final McpServerManageMapper mcpServerManageMapper;
  private final ILabelManageService labelManageService;
  private final IResourceElementService resourceElementService;

  @Override
  public McpServerDTO findMcpServer(Long tenantId, Long serverId) {
    McpServerDTO mcpServer = mcpServerManageMapper.getMcpServer(serverId, tenantId, BaseConsts.STATUS_CD_VALID);
    Assert.notNull(mcpServer, () -> "MCP 服务不存在: id=" + serverId);
    return mcpServer;
  }

  @Override
  @Transactional
  public ResultVO<McpServerDTO> saveMcpServer(McpServerDTO mcpServer) {
    // 校验名称唯一性
    if (mcpServerManageMapper.existsMcpServerName(mcpServer, SessionUtil.getLoginInfo().getUserId())) {
      return BaseErrorConstant.CHECK_NAME.toResult(mcpServer.getServerName());
    }

    McpServerDTO old = mcpServer.getServerId() == null ? null : findMcpServer(mcpServer.getTenantId(), mcpServer.getServerId());
    // 租户级数据默认生效
    if (old == null) {
      mcpServer.setServerEffect("1");
    }
    if (StringUtils.isEmpty(mcpServer.getServerEffect())) {
      mcpServer.setServerEffect(old != null ? old.getServerEffect() : "0");
    }
    mcpServer.setStatusCd(BaseConsts.STATUS_CD_VALID);
    DataDifference<McpServerDTO> difference = DataDifferenceStarter.computeSave(old, mcpServer, false, mcpServer.getTenantId());
    if (difference == null) {
      int count = saveSceneLabel(mcpServer);
      if (count > 0) {
        return ResultVO.success(mcpServer);
      }
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    else {
      McpServerDTO mcp = difference.getToSaveData();
      saveSceneLabel(mcp);
      return ResultVO.success(mcp);
    }
  }

  private int saveSceneLabel(McpServerDTO mcpServer) {
    int count = 0;
    List<LabelObjectRelDTO> labels = labelManageService.queryLabelObjectRelList(Collections.singletonList(mcpServer.getServerId()),
      BaseConsts.LABEL_TYPE_MCP, mcpServer.getTenantId());
    List<LabelObjectRelDTO> insertLabels = new ArrayList<>();
    List<Long> deleteIds = new ArrayList<>();
    for (LabelObjectRelDTO dto : CollectionUtils.emptyIfNull(labels)) {
      if (!CollectionUtils.emptyIfNull(mcpServer.getLabelIds()).contains(dto.getLabelId())) {
        deleteIds.add(dto.getRelId());
      }
    }
    for (Long labelId : CollectionUtils.emptyIfNull(mcpServer.getLabelIds())) {
      boolean exists = IterableUtils.matchesAny(CollectionUtils.emptyIfNull(labels), p -> Objects.equals(labelId, p.getLabelId()));
      if (!exists) {
        LabelObjectRelDTO label = new LabelObjectRelDTO();
        label.setRelId(Sequences.LABEL_OBJECT_REL_ID.next());
        label.setLabelId(labelId);
        label.setObjectId(mcpServer.getServerId());
        label.setObjectType(BaseConsts.LABEL_TYPE_SCENE);
        label.setTenantId(mcpServer.getTenantId());
        label.setStatusCd(BaseConsts.STATUS_CD_VALID);
        label.setCreatorId(SessionUtil.getLoginInfo().getUserId());
        insertLabels.add(label);
      }
    }
    if (CollectionUtils.isNotEmpty(insertLabels)) {
      labelManageService.saveLabelObjectRel(insertLabels);
      count = count + insertLabels.size();
    }
    if (CollectionUtils.isNotEmpty(deleteIds)) {
      labelManageService.deleteLabelObjectRel(deleteIds, mcpServer.getTenantId());
      count = count + deleteIds.size();
    }
    return count;
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteMcpServer(Long tenantId, Long serverId) {
    McpServerDTO mcpServer = mcpServerManageMapper.getMcpServer(serverId, tenantId, BaseConsts.STATUS_CD_VALID);
    if (mcpServer == null) {
      return ResultVO.fail("MCP 不存在");
    }
    if (CommonConsts.PLATFORM_TENANT_ID.equals(mcpServer.getTenantId())) {
      return ResultVO.fail("平台级MCP不允许删除");
    }
    if (resourceElementService.existsRelatedResource(tenantId, serverId, DataSyncCodeEnum.SKILL_MCP.getCode())) {
      return ResultVO.fail("MCP已存在关联配置数据，不允许删除");
    }
    mcpServerManageMapper.deleteMcpServer(serverId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public List<McpServerDTO> queryMcpServerList(McpServerQueryParams queryParams) {
    return mcpServerManageMapper.selectMcpServerList(queryParams);
  }

  @Override
  public PageInfo<McpServerDTO> queryMcpServerPage(McpServerQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    PageInfo<McpServerDTO> pageInfo = mcpServerManageMapper.selectMcpServerPage(queryParams, rowBounds).toPageInfo();
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      // 补充标签信息
      List<Long> sceneIds = pageInfo.getList().stream().map(McpServerDTO::getServerId).collect(Collectors.toList());
      Map<Long, List<LabelObjectRelDTO>> group = CollectionUtils.emptyIfNull(
          labelManageService.queryLabelObjectRelList(sceneIds, BaseConsts.LABEL_TYPE_MCP, queryParams.getTenantId())).stream()
        .collect(Collectors.groupingBy(LabelObjectRelDTO::getObjectId));
      for (Entry<Long, List<LabelObjectRelDTO>> entry : group.entrySet()) {
        McpServerDTO mcp = IterableUtils.find(pageInfo.getList(), p -> Objects.equals(entry.getKey(), p.getServerId()));
        mcp.setLabels(entry.getValue());
        mcp.setLabelIds(CollectionUtils.emptyIfNull(entry.getValue()).stream().map(LabelObjectRelDTO::getLabelId).collect(Collectors.toList()));
      }
      // 根据标签ID过滤
      if (queryParams.getLabelId() != null) {
        pageInfo.setList(pageInfo.getList().stream()
          .filter(p -> CollectionUtils.isNotEmpty(p.getLabelIds()) && p.getLabelIds().contains(queryParams.getLabelId()))
          .collect(Collectors.toList()));
      }
    }
    return pageInfo;
  }

  @Override
  public PageInfo<SimpleMcpServiceDTO> querySimpleMcpServerPage(McpServerQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return mcpServerManageMapper.selectSimpleMcpServerPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public ResultVO<Void> publishMcpServer(Long tenantId, Long serverId, String serverEffect) {
    int affectedRows = mcpServerManageMapper.updateMcpServerStatus(serverId, serverEffect, SessionUtil.getLoginInfo().getUserId(), tenantId);
    if (affectedRows == 0) {
      return ResultVO.fail("MCP服务不存在");
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public void syncMcpServerFromBeyond(McpServerDTO dto) {
    McpServerDTO server = mcpServerManageMapper.getMcpServer(dto.getServerId(), dto.getTenantId(), null);
    if (server != null) {
      server.setServerName(dto.getServerName());
      server.setServerDesc(dto.getServerDesc());
      server.setServerDetail(dto.getServerDetail());
      server.setServerType(dto.getServerType());
      server.setServerIcon(dto.getServerIcon());
      server.setServerUrl(dto.getServerUrl());
      server.setHeadersJson(dto.getHeadersJson());
      server.setUpdatorId(1L);
      server.setStatusCd(dto.getStatusCd());
      server.setRemark(dto.getRemark());
      mcpServerManageMapper.updateMcpServer(server);
      // 刷新缓存
      ThreadPools.getCommon()
        .submit(() -> SpringUtil.getBean(IRefreshCacheService.class).refresh(CacheConsts.CACHE_NAME_MCP_CLIENT, "-1:" + server.getServerId()));
    }
    else {
      dto.setCreatorId(1L);
      mcpServerManageMapper.insertMcpServer(dto);
    }
  }
}
