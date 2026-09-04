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
 * 用户常用应用 Entity
 *
 * @author wang.tingyun
 * @since 2025-09-12
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_user_fav_app")
public class UserFavAppEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键ID")
  private Long favId;

  @DiffField(name = "USER_ID")
  @Schema(description = "用户ID")
  private Long userId;

  @DiffField(name = "APP_ID")
  @Schema(description = "应用ID")
  private Long appId;

  @DiffField(name = "SORT_ORDER")
  @Schema(description = "排序")
  private Integer sortOrder;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "工作空间ID")
  private Long spaceId;

}