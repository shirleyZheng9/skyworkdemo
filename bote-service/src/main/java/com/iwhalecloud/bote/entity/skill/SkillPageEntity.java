package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 技能：页面
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_page")
public class SkillPageEntity extends BaseEntity {
  @DiffId
  @Schema(description = "页面ID")
  private Long pageId;
  @DiffField(name = "PAGE_NAME")
  @Schema(description = "页面名称")
  @Size(max = 20, message = "页面名称超过限定长度20")
  private String pageName;
  @DiffField(name = "PAGE_TITLE")
  @Schema(description = "页面标题")
  private String pageTitle;
  @DiffField(name = "PAGE_CODE")
  @Schema(description = "页面编码")
  @Size(max = 50, message = "页面编码超过限定长度50")
  private String pageCode;
  @DiffField(name = "REQ_PARAM_JSON")
  @Schema(description = "页面入参")
  private String reqParamJson;
  @DiffField(name = "MOCK_REQ_PARAM_JSON")
  @Schema(description = "页面入参模拟数据")
  private String mockReqParamJson;
  @DiffField(name = "PAGE_SOURCE_TYPE")
  @Schema(description = "页面来源: 高代码: HIGH_CODE 平台: PLATFORM 低代码: LOW_CODE")
  private String pageSourceType;
  @DiffField(name = "PAGE_TYPE")
  @Schema(description = "页面类型: 弹窗:Modal 页面:Page 手机弹窗:MobileModal 面板:dashboard 手机气泡:MobilePopover 气泡:Popover 推拉门:Drawer")
  private String pageType;
  @DiffField(name = "PAGE_CONTENT")
  @Schema(description = "页面内容")
  private String pageContent;
  @DiffField(name = "PAGE_CSS_JSON")
  @Schema(description = "页面样式")
  private String pageCssJson;
  @DiffField(name = "FILE_INFO_ID")
  @Schema(description = "关联文件ID")
  private Long fileInfoId;
  @DiffField(name = "PAGE_TEMPLATE_JSON")
  @Schema(description = "页面模板json")
  private String pageTemplateJson;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "租户ID")
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
}
