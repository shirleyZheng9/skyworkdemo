package com.iwhalecloud.bote.service.planning.hepler;

import com.iwhalecloud.bote.cache.PlanContextCache;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.PlanConsts;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.dto.planning.PlanStepDTO;
import com.iwhalecloud.bote.mapper.planning.PlanManageMapper;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * 计划执行记录
 *
 * @author chen.linfa
 * @since 2025-07-02
 */
@Component
@RequiredArgsConstructor
public class PlanRecorder {

  private final PlanManageMapper planManageMapper;
  private final PlanContextCache planContextCache;

  /**
   * 更新计划记录
   */
  public void updateRecord(PlanRecordDTO record, ChatContext chatContext, ReplyHandler replyHandler) {
    record.setEndTime(new Date());
    record.setSpentTime((int) (record.getEndTime().getTime() - record.getStartTime().getTime()));
    if (PlanConsts.STATUS_SUCCESS == record.getStatus()) {
      for (PlanStepDTO step : record.getSteps()) {
        step.setStepStatus(PlanConsts.STATUS_SUCCESS);
        updateFlowStatus(step);
      }
    }
    TransactionUtil.executeNew(() -> planManageMapper.updatePlanRecord(record));
    planContextCache.put(record.getPlanId(), record);
    if (chatContext != null) {
      // 发送一个消息，通知前端更新计划状态
      chatContext.sendMessage(ChatMessageType.UPDATE_PLAN_STATE, record.toMap());
      if (PlanConsts.STATUS_SUCCESS == record.getStatus()) {
        // 计划执行结束，退出虚拟的主流程
        chatContext.sendExitSceneMessage();
      }
      chatContext.sendDoneMessage();
    }
    else if (replyHandler != null) {
      replyHandler.pushMessage(ChatMessageType.UPDATE_PLAN_STATE, record.toMap());
    }
  }

  public void updateStep(PlanRecordDTO record, PlanStepDTO step, ReplyHandler replyHandler) {
    if (step.getStartTime() == null) {
      step.setStartTime(new Date());
    }
    else {
      step.setEndTime(new Date());
      step.setSpentTime((int) (step.getEndTime().getTime() - step.getStartTime().getTime()));
    }
    if (PlanConsts.STATUS_SUCCESS == step.getStepStatus() && CollectionUtils.isNotEmpty(step.getFlowSteps())) {
      updateFlowStatus(step);
      step.setFlowStepJson(JsonUtil.toJsonString(step.getFlowSteps()));
    }
    TransactionUtil.executeNew(() -> planManageMapper.updateStepStatus(step));
    // 发送一个消息，通知前端更新计划状态
    replyHandler.updatePlanState(record);
  }

  private void updateFlowStatus(PlanStepDTO step) {
    for (SimpleFlowStepDTO flow : CollectionUtils.emptyIfNull(step.getFlowSteps())) {
      flow.setStepStatus(PlanConsts.STATUS_SUCCESS);
      for (SimpleFlowStepDTO child : CollectionUtils.emptyIfNull(flow.getChildren())) {
        child.setStepStatus(PlanConsts.STATUS_SUCCESS);
      }
    }
  }
}
