package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.entity.loop.evaluation.TargetEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.TargetVersionEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ArgsSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Bot;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Workflow;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.LoopPromptDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 评估目标转换器
 * 对应Go: convertor/eval_target.go
 */
public final class EvalTargetConvertor {

  private EvalTargetConvertor() {
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * DO转PO
   * 对应Go: EvalTargetDO2PO(do *entity.EvalTarget) (po *model.Target)
   */
  public static TargetEntity convertDO2PO(EvalTarget evalTarget) {
    if (evalTarget == null) {
      return null;
    }

    Date now = new Date();
    String currentUserId = SessionContext.getCurrentUserId();

    TargetEntity.TargetEntityBuilder builder = TargetEntity.builder()
      .id(evalTarget.getId())
      .spaceId(evalTarget.getSpaceId())
      .sourceTargetId(evalTarget.getSourceTargetId())
      .targetType(evalTarget.getEvalTargetType().getValue());

    // 设置 createdBy 和 updatedBy
    if (evalTarget.getBaseInfo() != null && evalTarget.getBaseInfo().getCreatedBy() != null) {
      builder.createdBy(evalTarget.getBaseInfo().getCreatedBy().getUserId());
    } else {
      builder.createdBy(currentUserId);
    }

    if (evalTarget.getBaseInfo() != null && evalTarget.getBaseInfo().getUpdatedBy() != null) {
      builder.updatedBy(evalTarget.getBaseInfo().getUpdatedBy().getUserId());
    } else {
      builder.updatedBy(currentUserId);
    }

    // 设置 createdAt 和 updatedAt，确保不为 null
    if (evalTarget.getBaseInfo() != null && evalTarget.getBaseInfo().getCreatedAt() != null) {
      builder.createdAt(new Date(evalTarget.getBaseInfo().getCreatedAt()));
    } else {
      builder.createdAt(now);
    }

    if (evalTarget.getBaseInfo() != null && evalTarget.getBaseInfo().getUpdatedAt() != null) {
      builder.updatedAt(new Date(evalTarget.getBaseInfo().getUpdatedAt()));
    } else {
      builder.updatedAt(now);
    }

    return builder.build();
  }

  /**
   * 版本DO转PO
   * 对应Go: EvalTargetVersionDO2PO(do *entity.EvalTargetVersion) (po *model.TargetVersion, err error)
   */
  public static TargetVersionEntity convertVersionDO2PO(EvalTargetVersion evalTargetVersion) {
    if (evalTargetVersion == null) {
      return null;
    }

    Date now = new Date();
    String currentUserId = SessionContext.getCurrentUserId();

    TargetVersionEntity.TargetVersionEntityBuilder builder = buildBasicTargetVersionEntity(evalTargetVersion);

    // 设置 createdBy 和 updatedBy
    if (evalTargetVersion.getBaseInfo() != null && evalTargetVersion.getBaseInfo().getCreatedBy() != null) {
      builder.createdBy(evalTargetVersion.getBaseInfo().getCreatedBy().getUserId());
    } else {
      builder.createdBy(currentUserId);
    }

    if (evalTargetVersion.getBaseInfo() != null && evalTargetVersion.getBaseInfo().getUpdatedBy() != null) {
      builder.updatedBy(evalTargetVersion.getBaseInfo().getUpdatedBy().getUserId());
    } else {
      builder.updatedBy(currentUserId);
    }

    // 设置 createdAt 和 updatedAt，确保不为 null
    if (evalTargetVersion.getBaseInfo() != null && evalTargetVersion.getBaseInfo().getCreatedAt() != null) {
      builder.createdAt(new Date(evalTargetVersion.getBaseInfo().getCreatedAt()));
    } else {
      builder.createdAt(now);
    }

    if (evalTargetVersion.getBaseInfo() != null && evalTargetVersion.getBaseInfo().getUpdatedAt() != null) {
      builder.updatedAt(new Date(evalTargetVersion.getBaseInfo().getUpdatedAt()));
    } else {
      builder.updatedAt(now);
    }

    serializeTargetMeta(evalTargetVersion, builder);
    serializeSchemas(evalTargetVersion, builder);

    return builder.build();
  }

  private static TargetVersionEntity.TargetVersionEntityBuilder buildBasicTargetVersionEntity(EvalTargetVersion evalTargetVersion) {
    return TargetVersionEntity.builder()
      .id(evalTargetVersion.getId())
      .spaceId(evalTargetVersion.getSpaceId())
      .targetId(evalTargetVersion.getTargetId())
      .sourceTargetVersion(evalTargetVersion.getSourceTargetVersion());
  }

  private static void serializeTargetMeta(EvalTargetVersion evalTargetVersion, TargetVersionEntity.TargetVersionEntityBuilder builder) {
    try {
      String meta = getTargetMetaString(evalTargetVersion);
      builder.targetMeta(meta);
    } catch (JsonProcessingException e) {
      throw new BssException("Failed to serialize target meta: " + e.getMessage(), e);
    }
  }

  private static String getTargetMetaString(EvalTargetVersion evalTargetVersion) throws JsonProcessingException {
    return switch (evalTargetVersion.getEvalTargetType()) {
      case BOT -> evalTargetVersion.getBot() != null ?
        objectMapper.writeValueAsString(evalTargetVersion.getBot()) : null;
      case LOOP_PROMPT -> evalTargetVersion.getPrompt() != null ?
        objectMapper.writeValueAsString(evalTargetVersion.getPrompt()) : null;
      case WORKFLOW -> evalTargetVersion.getWorkflow() != null ?
        objectMapper.writeValueAsString(evalTargetVersion.getWorkflow()) : null;
      default -> null;
    };
  }

  private static void serializeSchemas(EvalTargetVersion evalTargetVersion, TargetVersionEntity.TargetVersionEntityBuilder builder) {
    try {
      serializeInputSchema(evalTargetVersion, builder);
      serializeOutputSchema(evalTargetVersion, builder);
    } catch (JsonProcessingException e) {
      throw new BssException("Failed to serialize input schema: " + e.getMessage(), e);
    }
  }

  private static void serializeInputSchema(EvalTargetVersion evalTargetVersion, TargetVersionEntity.TargetVersionEntityBuilder builder) throws JsonProcessingException {
    if (evalTargetVersion.getInputSchema() != null) {
      builder.inputSchema(objectMapper.writeValueAsString(evalTargetVersion.getInputSchema()));
    }
  }

  private static void serializeOutputSchema(EvalTargetVersion evalTargetVersion, TargetVersionEntity.TargetVersionEntityBuilder builder) throws JsonProcessingException {
    if (evalTargetVersion.getOutputSchema() != null) {
      builder.outputSchema(objectMapper.writeValueAsString(evalTargetVersion.getOutputSchema()));
    }
  }

  /**
   * PO列表转DO列表
   * 对应Go: EvalTargetPO2DOs(targetPOs []*model.Target) (targetDOs []*entity.EvalTarget)
   */
  public static List<EvalTarget> convertPOs2DOs(List<TargetEntity> targetEntities) {
    if (targetEntities == null) {
      return null;
    }

    List<EvalTarget> evalTargets = new ArrayList<>();
    for (TargetEntity targetEntity : targetEntities) {
      evalTargets.add(convertPO2DO(targetEntity));
    }
    return evalTargets;
  }

  /**
   * PO转DO
   * 对应Go: EvalTargetPO2DO(targetPO *model.Target) (targetDO *entity.EvalTarget)
   */
  public static EvalTarget convertPO2DO(TargetEntity targetEntity) {
    if (targetEntity == null) {
      return null;
    }

    EvalTarget.EvalTargetBuilder builder = EvalTarget.builder()
      .id(targetEntity.getId())
      .spaceId(targetEntity.getSpaceId())
      .sourceTargetId(targetEntity.getSourceTargetId())
      .evalTargetType(EvalTargetType.fromValue(targetEntity.getTargetType()));

    BaseInfo.BaseInfoBuilder baseInfoBuilder = BaseInfo.builder()
      .createdBy(UserInfo.builder().userId(targetEntity.getCreatedBy()).build())
      .updatedBy(UserInfo.builder().userId(targetEntity.getUpdatedBy()).build());

    if (targetEntity.getCreatedAt() != null) {
      baseInfoBuilder.createdAt(targetEntity.getCreatedAt().getTime());
    }
    if (targetEntity.getUpdatedAt() != null) {
      baseInfoBuilder.updatedAt(targetEntity.getUpdatedAt().getTime());
    }
    if (targetEntity.getDeletedAt() != null) {
      baseInfoBuilder.deletedAt(targetEntity.getDeletedAt().getTime());
    }

    return builder.baseInfo(baseInfoBuilder.build()).build();
  }

  /**
   * 版本PO转DO
   * 对应Go: EvalTargetVersionPO2DO(targetVersionPO *model.TargetVersion, targetType entity.EvalTargetType) (targetVersionDO *entity.EvalTargetVersion)
   */
  public static EvalTargetVersion convertVersionPO2DO(TargetVersionEntity targetVersionEntity, EvalTargetType targetType) {
    if (targetVersionEntity == null) {
      return null;
    }

    EvalTargetVersion.EvalTargetVersionBuilder builder = buildBasicEvalTargetVersion(targetVersionEntity);
    setBaseInfo(targetVersionEntity, builder);
    deserializeInputSchema(targetVersionEntity, builder);
    deserializeOutputSchema(targetVersionEntity, builder);
    deserializeTargetMeta(targetVersionEntity, targetType, builder);

    return builder.build();
  }

  private static EvalTargetVersion.EvalTargetVersionBuilder buildBasicEvalTargetVersion(TargetVersionEntity targetVersionEntity) {
    return EvalTargetVersion.builder()
      .id(targetVersionEntity.getId())
      .spaceId(targetVersionEntity.getSpaceId())
      .targetId(targetVersionEntity.getTargetId())
      .sourceTargetVersion(targetVersionEntity.getSourceTargetVersion());
  }

  private static void setBaseInfo(TargetVersionEntity targetVersionEntity, EvalTargetVersion.EvalTargetVersionBuilder builder) {
    BaseInfo.BaseInfoBuilder baseInfoBuilder = BaseInfo.builder()
      .createdBy(UserInfo.builder().userId(targetVersionEntity.getCreatedBy()).build())
      .updatedBy(UserInfo.builder().userId(targetVersionEntity.getUpdatedBy()).build());

    setTimestampInfo(targetVersionEntity, baseInfoBuilder);
    builder.baseInfo(baseInfoBuilder.build());
  }

  private static void setTimestampInfo(TargetVersionEntity targetVersionEntity, BaseInfo.BaseInfoBuilder baseInfoBuilder) {
    if (targetVersionEntity.getCreatedAt() != null) {
      baseInfoBuilder.createdAt(targetVersionEntity.getCreatedAt().getTime());
    }
    if (targetVersionEntity.getUpdatedAt() != null) {
      baseInfoBuilder.updatedAt(targetVersionEntity.getUpdatedAt().getTime());
    }
    if (targetVersionEntity.getDeletedAt() != null) {
      baseInfoBuilder.deletedAt(targetVersionEntity.getDeletedAt().getTime());
    }
  }

  private static void deserializeInputSchema(TargetVersionEntity targetVersionEntity, EvalTargetVersion.EvalTargetVersionBuilder builder) {
    if (targetVersionEntity.getInputSchema() != null && !targetVersionEntity.getInputSchema().isEmpty()) {
      try {
        List<ArgsSchema> inputSchema = objectMapper.readValue(
          targetVersionEntity.getInputSchema(),
          objectMapper.getTypeFactory().constructCollectionType(List.class, ArgsSchema.class));
        builder.inputSchema(inputSchema);
      } catch (IOException e) {
        // 忽略反序列化错误，保持与Go代码一致
      }
    }
  }

  private static void deserializeOutputSchema(TargetVersionEntity targetVersionEntity, EvalTargetVersion.EvalTargetVersionBuilder builder) {
    if (targetVersionEntity.getOutputSchema() != null && !targetVersionEntity.getOutputSchema().isEmpty()) {
      try {
        List<ArgsSchema> outputSchema = objectMapper.readValue(
          targetVersionEntity.getOutputSchema(),
          objectMapper.getTypeFactory().constructCollectionType(List.class, ArgsSchema.class));
        builder.outputSchema(outputSchema);
      } catch (IOException e) {
        // 忽略反序列化错误，保持与Go代码一致
      }
    }
  }

  private static void deserializeTargetMeta(TargetVersionEntity targetVersionEntity, EvalTargetType targetType, EvalTargetVersion.EvalTargetVersionBuilder builder) {
    if (targetVersionEntity.getTargetMeta() != null && !targetVersionEntity.getTargetMeta().isEmpty()) {
      try {
        deserializeTargetMetaByType(targetVersionEntity, targetType, builder);
      } catch (IOException e) {
        // 忽略反序列化错误，保持与Go代码一致
      }
    }
  }

  private static void deserializeTargetMetaByType(TargetVersionEntity targetVersionEntity, EvalTargetType targetType, EvalTargetVersion.EvalTargetVersionBuilder builder) throws IOException {
    switch (targetType) {
      case BOT -> {
        Bot bot = objectMapper.readValue(targetVersionEntity.getTargetMeta(), Bot.class);
        builder.bot(bot);
      }
      case LOOP_PROMPT -> {
        LoopPromptDO loopPrompt = objectMapper.readValue(targetVersionEntity.getTargetMeta(), LoopPromptDO.class);
        builder.prompt(loopPrompt);
      }
      case WORKFLOW -> {
        Workflow workflow = objectMapper.readValue(targetVersionEntity.getTargetMeta(), Workflow.class);
        builder.workflow(workflow);
      }
      default -> {
        // 默认情况不需要处理
      }
    }
  }
}
