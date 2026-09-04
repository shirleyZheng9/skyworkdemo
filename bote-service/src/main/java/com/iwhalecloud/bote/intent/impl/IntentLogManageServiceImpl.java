package com.iwhalecloud.bote.intent.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.intent.IntentLogDTO;
import com.iwhalecloud.bote.dto.intent.IntentQuestionDTO;
import com.iwhalecloud.bote.dto.intent.query.IntentQueryParams;
import com.iwhalecloud.bote.intent.IIntentLogManageService;
import com.iwhalecloud.bote.intent.IIntentQuestionManageService;
import com.iwhalecloud.bote.mapper.intent.IntentLogManageMapper;
import com.iwhalecloud.bote.mapper.intent.IntentQuestionManageMapper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 意图识别日志管理服务实现
 *
 * @author auto
 * @since 2024-12-18
 */
@Service
@RequiredArgsConstructor
public class IntentLogManageServiceImpl implements IIntentLogManageService {

  private final IntentLogManageMapper intentLogMapper;

  private final IntentQuestionManageMapper intentQuestionMapper;

  private final IIntentQuestionManageService intentAnnotationService;

  @Override
  public PageInfo<IntentLogDTO> queryIntentLogPage(IntentQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    return intentLogMapper.selectIntentLogPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> markIntentLog(IntentLogDTO log) {
    IntentLogDTO intentLog = intentLogMapper.getIntentLog(log.getLogId());
    if (intentLog == null) {
      return ResultVO.fail("意图日志不存在");
    }
    if (BaseConsts.TRUE.equals(intentLog.getMarkStatus())) {
      return ResultVO.fail("意图日志已标记");
    }
    intentLogMapper.updateIntentLogStatus(log.getLogId(), BaseConsts.TRUE, SessionUtil.getLoginInfo().getUserId());
    IntentQuestionDTO question = new IntentQuestionDTO();
    question.setQuestion(log.getContent());
    question.setSceneId(log.getSceneId());
    question.setTenantId(log.getTenantId());
    question.setAttribute(log.getAttribute());
    intentAnnotationService.saveIntentQuestion(question);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> cancelIntentLog(Long tenantId, Long logId) {
    IntentLogDTO log = intentLogMapper.getIntentLog(logId);
    if (log == null) {
      return ResultVO.fail("意图日志不存在");
    }
    if (BaseConsts.FALSE.equals(log.getMarkStatus())) {
      return ResultVO.fail("意图日志未标记");
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    intentLogMapper.updateIntentLogStatus(logId, BaseConsts.FALSE, userId);
    intentQuestionMapper.deleteIntentQuestionByQuestion(tenantId, log.getContent(), userId);
    return ResultVO.success();
  }
}
