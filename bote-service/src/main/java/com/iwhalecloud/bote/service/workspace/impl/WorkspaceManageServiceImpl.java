package com.iwhalecloud.bote.service.workspace.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.publish.PublishGatewaySimpleDTO;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bote.dto.workspace.SpaceIdMappingDTO;
import com.iwhalecloud.bote.dto.workspace.WorkspaceDTO;
import com.iwhalecloud.bote.dto.workspace.query.WorkspaceQueryParams;
import com.iwhalecloud.bote.mapper.portal.TenantQueryMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bote.service.organization.IOrganizationManageService;
import com.iwhalecloud.bote.service.publish.IPublishGatewayManageService;
import com.iwhalecloud.bote.service.workspace.IWorkspaceManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

/**
 * 工作空间服务实现
 *
 * @author chen.linfa
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
public class WorkspaceManageServiceImpl implements IWorkspaceManageService {

  private static final Logger logger = LoggerFactory.getLogger(WorkspaceManageServiceImpl.class);

  /** 目标环境 queryWorkspacePage 接口相对路径（不含网关前缀） */
  private static final String QUERY_WORKSPACE_PAGE_PATH = "/bote/manager/workspace/queryWorkspacePage";

  private final WorkspaceManageMapper workspaceManageMapper;
  private final IOrganizationManageService organizationManageService;
  private final IPublishGatewayManageService publishGatewayManageService;
  private final TenantQueryMapper tenantQueryMapper;

  @Override
  public WorkspaceDTO getWorkspace(Long spaceId) {
    return workspaceManageMapper.selectWorkspaceById(spaceId);
  }

  @Override
  @Transactional
  public ResultVO<Long> saveWorkspace(WorkspaceDTO workspace) {
    // 校验名称唯一性
    if (workspaceManageMapper.existsSpaceName(workspace.getSpaceId(), workspace.getSpaceName())) {
      return BaseErrorConstant.CHECK_NAME.toResult(workspace.getSpaceName());
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    workspace.setUpdatorId(userId);
    if (workspace.getSpaceId() == null) {
      workspace.setSpaceId(IDUtils.nextId());
      workspace.setStatusCd(BaseConsts.STATUS_CD_VALID);
      workspace.setCreatorId(userId);
      if (workspaceManageMapper.insertWorkspace(workspace) == 0) {
        return BaseErrorConstant.CHECK_NAME.toResult(workspace.getSpaceName());
      }
      // 同步创建对应的根组织
      organizationManageService.createRootOrgForWorkspace(workspace.getSpaceId(), workspace.getSpaceName(), userId);
    }
    else {
      workspaceManageMapper.updateWorkspace(workspace);
      // 同步修改对应的根组织名称
      organizationManageService.updateRootOrgName(workspace.getSpaceId(), workspace.getSpaceName(), userId);
    }
    return ResultVO.success(workspace.getSpaceId());
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteWorkspace(Long spaceId) {
    workspaceManageMapper.deleteWorkspace(spaceId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public PageInfo<WorkspaceDTO> queryWorkspacePage(WorkspaceQueryParams queryParams) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (!SessionUtil.isSuperAdmin(userId)) {
      queryParams.setUserId(userId);
    }
    if (queryParams.getTenantId() != null) {
      Long spaceId = tenantQueryMapper.getSpaceId(queryParams.getTenantId());
      WorkspaceDTO workspaceDTO = workspaceManageMapper.selectWorkspaceById(spaceId);
      if (workspaceDTO != null) {
        // 将单个结果包装为分页对象返回
        List<WorkspaceDTO> list = Collections.singletonList(workspaceDTO);
        PageInfo<WorkspaceDTO> pageInfo = new PageInfo<>(list);
        pageInfo.setTotal(1);
        pageInfo.setPageNum(1);
        pageInfo.setPageSize(1);
        return pageInfo;
      }
    }
    //noinspection resource
    return workspaceManageMapper.selectWorkspacePage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public String getWorkspaceIcon(Long spaceId) {
    return workspaceManageMapper.getWorkspcaeIcon(spaceId);
  }

  @Override
  public ResultVO<PageInfo<WorkspaceDTO>> queryWorkspacePageFromExternal(Long gatewayId, WorkspaceQueryParams queryParams, Long tenantId) {
    // 获取网关信息
    PublishGatewaySimpleDTO gateway = publishGatewayManageService.findPublishGatewaySimple(tenantId, gatewayId);
    Assert.notNull(gateway, "网关不存在或无权访问");

    // 构建请求头
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (StringUtils.isNotEmpty(gateway.getGatewayToken())) {
      headers.set(BaseConsts.HEADER_AUTHORIZATION, gateway.getGatewayToken());
    }
    HttpEntity<String> requestEntity = new HttpEntity<>(JsonUtil.toJsonString(queryParams), headers);

    // 构建目标URL
    String targetUrl = StringUtils.stripEnd(gateway.getGatewayUrl(), "/") + QUERY_WORKSPACE_PAGE_PATH;

    // 调用外部接口
    RestTemplate restTemplate = HttpUtil.getRestTemplate();
    ResponseEntity<ResultVO<PageInfo<WorkspaceDTO>>> responseEntity = restTemplate.exchange(targetUrl, HttpMethod.POST,
      requestEntity, new ParameterizedTypeReference<>() {
      });

    ResultVO<PageInfo<WorkspaceDTO>> response = responseEntity.getBody();
    if (response != null && response.isSuccess()) {
      return response;
    }
    else {
      String errorMsg = response != null ? response.getResultMsg() : "未知错误";
      logger.error("queryWorkspacePageFromExternal failed: gatewayId={}, error={}", gatewayId, errorMsg);
      return ResultVO.fail("查询企业空间列表失败: " + errorMsg);
    }
  }

  @Override
  public List<SimpleWorkspaceDTO> querySimpleWorkspaceList() {
    return workspaceManageMapper.selectSimpleWorkspaces();
  }

  @Override
  public SpaceIdMappingDTO querySpaceId(Long spaceId, String extSpaceId) {
    SpaceIdMappingDTO result = new SpaceIdMappingDTO();
    if (spaceId != null) {
      String extSpaceIdResult = workspaceManageMapper.getExtSpaceIdBySpaceId(spaceId);
      result.setSpaceId(spaceId);
      result.setExtSpaceId(extSpaceIdResult);
      return result;
    }
    if (StringUtils.isNotEmpty(extSpaceId)) {
      Long spaceIdResult = workspaceManageMapper.getSpaceIdByExtSpaceId(extSpaceId);
      result.setSpaceId(spaceIdResult);
      result.setExtSpaceId(extSpaceId);
      return result;
    }
    return null;
  }
}
