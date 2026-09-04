package com.iwhalecloud.bote.dto.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.entity.app.UserFavAppEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * 用户常用应用 DTO
 *
 * @author wang.tingyun
 * @since 2025-09-12
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_user_fav_app")
@JsonInclude(Include.NON_NULL)
public class UserFavAppDTO extends UserFavAppEntity {

  @Schema(description = "应用名称")
  private String appName;
  @Schema(description = "应用场景")
  private String appScene;
  @Schema(description = "工作台应用ID")
  private Long workbenchAppId;
  @Schema(description = "应用能力关联对象")
  private FavAppRelObj appRelObj;

  /**
   * 设置关联AI助理列表
   */
  public void setRelAiAppList(List<SimpleBotDTO> botList) {
    if (CollectionUtils.isNotEmpty(botList)) {
      if (appRelObj == null) {
        appRelObj = new FavAppRelObj();
      }
      appRelObj.setAiAppList(botList);
    }
  }

  /**
   * 设置关联AI助理ID列表
   */
  public void setRelAiAppIdList(List<Long> botIdList) {
    if (CollectionUtils.isNotEmpty(botIdList)) {
      if (appRelObj == null) {
        appRelObj = new FavAppRelObj();
      }
      appRelObj.setAiAppIdList(botIdList);
    }
  }

  /**
   * 设置关联网页应用
   */
  public void setRelWebApp(WebAppDTO webAppDTO) {
    if (webAppDTO != null) {
      if (appRelObj == null) {
        appRelObj = new FavAppRelObj();
      }
      appRelObj.setWebApp(webAppDTO);
    }
  }

  /**
   * 设置关联网页应用ID
   */
  public void setRelWebAppId(Long webAppId) {
    if (webAppId != null) {
      if (appRelObj == null) {
        appRelObj = new FavAppRelObj();
      }
      appRelObj.setWebAppId(webAppId);
    }
  }

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @JsonInclude(Include.NON_NULL)
  public static class FavAppRelObj {
    @Schema(description = "AI助理应用列表")
    private List<SimpleBotDTO> aiAppList;
    @Schema(description = "AI助理应用ID列表")
    private List<Long> aiAppIdList;
    @Schema(description = "网页应用")
    private WebAppDTO webApp;
    @Schema(description = "网页应用ID")
    private Long webAppId;
  }

}