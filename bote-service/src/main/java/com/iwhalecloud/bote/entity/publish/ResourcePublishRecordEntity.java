package com.iwhalecloud.bote.entity.publish;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;

/**
 * 资源发布记录 Entity
 *
 * @author lizuyin
 * @since 2025-07-25
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_resource_publish_record")
public class ResourcePublishRecordEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long recordId;

  @DiffField(name = "PUBLISH_CHANNEL")
  @Schema(description = "发布渠道")
  private String publishChannel;

  @DiffField(name = "RESOURCE_TYPE")
  @Schema(description = "资源类型")
  private String resourceType;

  @DiffField(name = "RESOURCE_ID")
  @Schema(description = "资源ID")
  private Long resourceId;

  @DiffField(name = "EXT_RESOURCE_ID")
  @Schema(description = "外系统资源ID")
  private String extResourceId;

  @DiffField(name = "PUBLISH_STATUS")
  @Schema(description = "发布状态")
  private String publishStatus;

  @DiffField(name = "PUBLISH_PARAMS")
  @Schema(description = "发布参数记录")
  private String publishParams;

  @DiffField(name = "PUBLISH_MSG")
  @Schema(description = "发布信息")
  private String publishMsg;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "回调唯一编码")
  @DiffField(name = "CALLBACK_CODE")
  private String callbackCode;
  @DiffField(name = "last_access_time")
  @Schema(description = "最后访问时间")
  private Date lastAccessTime;
}
