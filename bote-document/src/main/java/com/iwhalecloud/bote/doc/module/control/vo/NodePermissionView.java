package com.iwhalecloud.bote.doc.module.control.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.iwhalecloud.bote.doc.common.support.serializer.NullBooleanSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 节点权限视图
 * </p>
 */
@Data
@Schema(description = "节点权限视图")
public class NodePermissionView {

  @Schema(description = "可管理", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean manageable;

  @Schema(description = "可编辑", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean editable;

  @Schema(description = "可查看", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean readable;

  @Schema(description = "可创建子节点", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean childCreatable;

  @Schema(description = "可重命名", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean renameAble;

  @Schema(description = "可编辑图标", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean iconEditable;

  @Schema(description = "可移动", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean movable;

  @Schema(description = "可复制", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean copyable;

  @Schema(description = "可导出", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean exportable;

  @Schema(description = "可删除", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean removable;

  @Schema(description = "可评论", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean commentEditable;

  @Schema(description = "可查看历史", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean historyReadable;

  @Schema(description = "可分享", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean sharable;

  @Schema(description = "可设置允许编辑", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean allowEditConfigurable;

  @Schema(description = "可分配权限", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean nodeAssignable;

  @Schema(description = "可修订文档", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean contentCorrection;

  @Schema(description = "审核文档修订建议", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean approvalCorrection;

  @Schema(description = "是否具有文档的重新上传的权限", example = "true")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean reuploadable;
}
