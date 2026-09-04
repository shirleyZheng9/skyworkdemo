package com.iwhalecloud.bote.service.bot.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.bot.CopilotPointDTO;
import com.iwhalecloud.bote.dto.bot.SimpleCopilotPointDTO;
import com.iwhalecloud.bote.dto.bot.query.CopilotPointParams;
import com.iwhalecloud.bote.dto.bot.query.PointQueryParams;
import com.iwhalecloud.bote.dto.chat.SessionDTO;
import com.iwhalecloud.bote.dto.chat.query.CreateSessionParams;
import com.iwhalecloud.bote.mapper.bot.CopilotPointManageMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.bot.ICopilotPointManageService;
import com.iwhalecloud.bote.service.chat.IChatSessionService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 副驾指令管理服务实现
 *
 * @author chen.linfa
 * @since 2025-01-20
 */
@Service
@RequiredArgsConstructor
public class CopilotPointManageServiceImpl implements ICopilotPointManageService {

  private final CopilotPointManageMapper mapper;

  private final SessionMapper sessionMapper;

  private final IChatSessionService chatSessionService;

  private final ICatalogManageService catalogManageService;

  @Override
  public CopilotPointDTO getPoint(Long tenantId, Long pointId) {
    return mapper.getPoint(tenantId, pointId);
  }

  @Override
  @Transactional
  public ResultVO<CopilotPointDTO> savePoint(CopilotPointDTO point) {
    // 校验编码唯一性
    if (mapper.existsPointCode(point)) {
      return BaseErrorConstant.CHECK_CODE.toResult(point.getPointCode());
    }
    point.setStatusCd(BaseConsts.STATUS_CD_VALID);
    if (point.getCatalogItemId() == null) {
      point.setCatalogItemId(CatalogConsts.DEFAULT_CATALOG_ITEM_PARENT_ID);
    }
    CopilotPointDTO old = point.getPointId() == null ? null : getPoint(point.getTenantId(), point.getPointId());
    DataDifference<CopilotPointDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, point, false, point.getTenantId(),
      OperClassEnum.COPILOT_POINT);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deletePoint(Long tenantId, Long pointId) {
    mapper.deletePoint(tenantId, pointId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.COPILOT_POINT.name()).clear(tenantId, pointId);
    return ResultVO.success();
  }

  @Override
  public PageInfo<CopilotPointDTO> queryPointPage(PointQueryParams queryParams) {
    queryParams.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(), CatalogConsts.TYPE_API_AUTH));
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    return mapper.selectPointPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public List<CopilotPointDTO> queryPointList(PointQueryParams queryParams) {
    return mapper.selectPointList(queryParams);
  }

  @Override
  public ResultVO<SimpleCopilotPointDTO> auth(CopilotPointParams params) {
    CopilotPointDTO point = mapper.getPointByCode(params.getTenantId(), params.getPointCode());
    Assert.notNull(point, () -> "非法指令信息：" + params.getPointCode());
    if (!EnvUtil.isDevEnv() && StringUtils.isNotEmpty(point.getSceneStatus()) && !SceneConsts.SCENE_STATUS_PUBLISH.equals(point.getSceneStatus())) {
      return ResultVO.fail("智能体【" + point.getSceneName() + "】未上架，请先上架");
    }
    SimpleCopilotPointDTO dto = new SimpleCopilotPointDTO();
    dto.setBotId(point.getBotId());
    dto.setSceneId(point.getSceneId());
    dto.setSceneName(point.getSceneName());
    dto.setPointCode(point.getPointCode());
    dto.setParams(new HashMap<>(8));
    if (StringUtils.isNotEmpty(point.getRequestJson())) {
      ParameterSpec parameterSpec = JsonUtil.parseJson(point.getRequestJson(), ParameterSpec.class);
      if (parameterSpec != null) {
        for (ParameterSpec child : CollectionUtils.emptyIfNull(parameterSpec.getChildren())) {
          dto.getParams().put(child.getName(), params.getParams().get(child.getName()));
        }
      }
    }
    dto.setSessionId(getSessionId(params, point));
    return ResultVO.success(dto);
  }

  private Long getSessionId(CopilotPointParams params, CopilotPointDTO point) {
    Long sessionId = ChatConsts.DEFAULT_SESSION_ID;
    if (!BaseConsts.TRUE.equals(params.getIgnoreSession())) {
      boolean isNewSession = BaseConsts.TRUE.equals(point.getIsNewSession());
      if (!isNewSession) {
        if (params.getSessionId() != null) {
          // 前端传递了会话 ID，需要判断是否归属指令对应的应用
          SessionDTO session = sessionMapper.getSession(params.getSessionId());
          if (session != null && Objects.equals(session.getBotId(), point.getBotId())) {
            return params.getSessionId();
          }
        }
        sessionId = sessionMapper.selectLatestSessionId(point.getBotId(), SessionUtil.getLoginInfo().getUserId(), params.getExtSystemId());
        isNewSession = sessionId == null;
      }
      if (isNewSession) {
        CreateSessionParams createSessionParams = new CreateSessionParams();
        createSessionParams.setTenantId(params.getTenantId());
        createSessionParams.setBotId(point.getBotId());
        createSessionParams.setBotTenantId(params.getTenantId());
        createSessionParams.setExtSystemId(params.getExtSystemId());
        ResultVO<SessionDTO> result = chatSessionService.createSession(createSessionParams);
        if (!result.isSuccess()) {
          throw new BssException("新建会话出现异常：" + result.getResultMsg());
        }
        sessionId = result.getResultObject().getSessionId();
      }
    }
    return sessionId;
  }
}
