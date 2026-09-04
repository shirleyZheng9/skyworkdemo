package com.iwhalecloud.bote.entity.app;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网页应用访问记录 Entity
 *
 * @author wang.tingyun
 * @since 2025-09-22
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_web_app_record")
public class WebAppRecordEntity extends BaseEntity {

  @DiffId
  @Schema(description = "访问记录ID")
  private Long recordId;

  @DiffField(name = "WEB_APP_ID")
  @Schema(description = "应用ID")
  private Long webAppId;

  @DiffField(name = "USER_ID")
  @Schema(description = "用户ID")
  private Long userId;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "工作空间ID")
  private Long spaceId;

}