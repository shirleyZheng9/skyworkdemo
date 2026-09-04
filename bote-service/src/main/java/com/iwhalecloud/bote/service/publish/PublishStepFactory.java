package com.iwhalecloud.bote.service.publish;

import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.service.publish.step.AbstractPublishStep;
import com.iwhalecloud.bote.service.publish.step.datasync.imp.ChangeEnvInstStep;
import com.iwhalecloud.bote.service.publish.step.datasync.copy.CollectDataForCopyStep;
import com.iwhalecloud.bote.service.publish.step.datasync.export.CollectDataStep;
import com.iwhalecloud.bote.service.publish.step.datasync.imp.ConfirmImportStep;
import com.iwhalecloud.bote.service.publish.step.datasync.export.InitializeExportStep;
import com.iwhalecloud.bote.service.publish.step.datasync.copy.InitializeForCopyStep;
import com.iwhalecloud.bote.service.publish.step.datasync.imp.ParseImportFileStep;
import com.iwhalecloud.bote.service.publish.step.datasync.copy.SaveDataForCopyStep;
import com.iwhalecloud.bote.service.publish.step.datasync.export.WrapExportStep;
import com.iwhalecloud.bote.service.publish.step.datasync.online.CollectDataOnlineStep;
import com.iwhalecloud.bote.service.publish.step.datasync.online.ConfirmPublishOnlineStep;
import com.iwhalecloud.bote.service.publish.step.datasync.online.InitializeOnlineStep;
import com.iwhalecloud.bote.service.publish.step.eval.CallEvalStep;
import com.iwhalecloud.bote.service.publish.step.eval.UpdateEvalStatusStep;
import com.iwhalecloud.bote.service.publish.step.intent.CallFinetuneStep;
import com.iwhalecloud.bote.service.publish.step.intent.InitializeFinetuneStep;
import com.iwhalecloud.bote.service.publish.step.intent.UpdateFinetuneStatusStep;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 步骤执行器工厂类
 *
 * @author chen.linfa
 * @since 2024-10-21
 */
public final class PublishStepFactory {
  private PublishStepFactory() {
  }

  /** 步骤执行器注册中心 */
  private static final Map<PublishStepType, BiFunction<PublishRecordDTO, PublishStepDTO, AbstractPublishStep<?>>> registry = new HashMap<>(16);

  static {
    // 注册步骤执行器
    registry.put(PublishStepType.INITIALIZE_EXPORT, InitializeExportStep::new);
    registry.put(PublishStepType.COllECT_DATA, CollectDataStep::new);
    registry.put(PublishStepType.WRAP_EXPORT, WrapExportStep::new);

    registry.put(PublishStepType.PARSE_FILE, ParseImportFileStep::new);
    registry.put(PublishStepType.CHANGE_ENV_INST, ChangeEnvInstStep::new);
    registry.put(PublishStepType.CONFIRM_IMPORT, ConfirmImportStep::new);

    registry.put(PublishStepType.INITIALIZE_FOR_COPY, InitializeForCopyStep::new);
    registry.put(PublishStepType.COllECT_DATA_FOR_COPY, CollectDataForCopyStep::new);
    registry.put(PublishStepType.SAVE_DATA_FOR_COPY, SaveDataForCopyStep::new);

    registry.put(PublishStepType.INITIALIZE_FINETUNE, InitializeFinetuneStep::new);
    registry.put(PublishStepType.CALL_FINETUNE, CallFinetuneStep::new);
    registry.put(PublishStepType.UPDATE_FINETUNE_STATUS, UpdateFinetuneStatusStep::new);

    registry.put(PublishStepType.CALL_EVAL, CallEvalStep::new);
    registry.put(PublishStepType.UPDATE_EVAL_STATUS, UpdateEvalStatusStep::new);

    registry.put(PublishStepType.INITIALIZE_ONLINE, InitializeOnlineStep::new);
    registry.put(PublishStepType.COLLECT_DATA_ONLINE, CollectDataOnlineStep::new);
    registry.put(PublishStepType.CONFIRM_PUBLISH_ONLINE, ConfirmPublishOnlineStep::new);
  }

  /**
   * 构造步骤执行器实例
   *
   * @param record 发布记录
   * @param step 发布步骤
   * @return 步骤执行器实例
   */
  public static AbstractPublishStep<?> create(PublishRecordDTO record, PublishStepDTO step) {
    PublishStepType stepType = PublishStepType.findByValue(step.getStepType());
    if (stepType == null) {
      throw new BssException("未知的发布步骤类型: stepId=" + step.getId() + ", stepType=" + step.getStepType());
    }

    BiFunction<PublishRecordDTO, PublishStepDTO, AbstractPublishStep<?>> factory = registry.get(stepType);
    if (factory == null) {
      throw new BssException("未知的发布步骤类型: stepId=" + step.getId() + ", stepType=" + step.getStepType());
    }
    return factory.apply(record, step);
  }
}
