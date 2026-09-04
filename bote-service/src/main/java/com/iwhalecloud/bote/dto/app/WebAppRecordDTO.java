package com.iwhalecloud.bote.dto.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.app.WebAppRecordEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作台应用访问记录 DTO
 *
 * @author wang.tingyun
 * @since 2025-09-22
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_web_app_record")
@JsonInclude(Include.NON_NULL)
public class WebAppRecordDTO extends WebAppRecordEntity {

  @Schema(description = "应用名称")
  private String appName;
  @Schema(description = "应用场景")
  private String appScene;
  @Schema(description = "打开方式(portal:门户内打开, browser:浏览器打开)")
  private String openType;
  @Schema(description = "访问网址")
  private String accessUrl;

}