package com.iwhalecloud.bote.entity.chat;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 对话分组定义 Entity
 *
 * @author chen.linfa
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_chat_group_cfg")
public class ChatGroupCfgEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long id;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间ID")
  private Long spaceId;

  @DiffField(name = "USER_ID")
  @Schema(description = "用户ID")
  private Long userId;

  @DiffField(name = "SETTING_JSON")
  @Schema(description = "定义数据")
  private String settingJson;
}
