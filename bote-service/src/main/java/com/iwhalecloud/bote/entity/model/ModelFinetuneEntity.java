package com.iwhalecloud.bote.entity.model;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模型微调 Entity
 *
 * @author auto
 * @since 2025-03-03
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_MODEL_FINETUNE")
public class ModelFinetuneEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "MODEL_NAME")
  @Schema(description = "模型名称")
  private String modelName;
  @DiffField(name = "MODEL_ICON")
  @Schema(description = "模型图标")
  private String modelIcon;

  @DiffField(name = "USE_TYPE")
  @Schema(description = "用途")
  private String useType;
  @DiffField(name = "BASE_MODEL_TYPE")
  @Schema(description = "基础模型类型")
  private String baseModelType;
  @DiffField(name = "BASE_MODEL_SUB_TYPE")
  @Schema(description = "基础模型子类型")
  private String baseModelSubType;

  @DiffField(name = "REQUEST_FILE_ID")
  @Schema(description = "请求文件 ID")
  private Long requestFileId;
  @DiffField(name = "RESPONSE_FILE_ID")
  @Schema(description = "响应文件 ID")
  private Long responseFileId;

  @DiffField(name = "CORPUS_INFO")
  @Schema(description = "语料信息")
  private String corpusInfo;

  @DiffField(name = "NUM_TRAIN_EPOCHS")
  @Schema(description = "超参：循环次数")
  private Integer numTrainEpochs;
  @DiffField(name = "LEARNING_RATE")
  @Schema(description = "超参：学习率")
  private BigDecimal learningRate;
  @DiffField(name = "BATCH_SIZE")
  @Schema(description = "超参：批次大小")
  private Integer batchSize;
  @DiffField(name = "EVAL_STEPS")
  @Schema(description = "超参：验证步数")
  private Integer evalSteps;
  @DiffField(name = "MAX_LENGTH")
  @Schema(description = "超参：上下文长度")
  private Integer maxLength;
  @DiffField(name = "WEIGHT_DECAY")
  @Schema(description = "超参：学习率预热比例")
  private BigDecimal weightDecay;
  @DiffField(name = "THRESHOLD")
  @Schema(description = "超参：推理阈值")
  private BigDecimal threshold;
  @DiffField(name = "STATUS")
  @Schema(description = "启用状态")
  private String status;
}
