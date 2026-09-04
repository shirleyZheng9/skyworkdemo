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
 * 模型评测 Entity
 *
 * @author auto
 * @since 2025-03-04
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_MODEL_EVAL")
public class ModelEvalEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "MODEL_CODE")
  @Schema(description = "模型编码")
  private Long modelId;
  @DiffField(name = "REQUEST_FILE_ID")
  @Schema(description = "请求文件 ID")
  private Long requestFileId;
  @DiffField(name = "STATUS")
  @Schema(description = "启用状态")
  private String status;
  @DiffField(name = "accuracy")
  @Schema(description = "准确率")
  private BigDecimal accuracy;
}
