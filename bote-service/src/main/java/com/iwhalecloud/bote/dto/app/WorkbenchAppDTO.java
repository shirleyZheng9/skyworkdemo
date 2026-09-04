package com.iwhalecloud.bote.dto.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationUserDTO;
import com.iwhalecloud.bote.entity.app.WorkbenchAppEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * 工作台应用 DTO
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_workbench_app")
@JsonInclude(Include.NON_NULL)
public class WorkbenchAppDTO extends WorkbenchAppEntity {

  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "创建人头像")
  private String creatorIcon;
  @Schema(description = "分类名称")
  private String catalogName;
  @Schema(description = "应用能力状态")
  private Boolean appRelStatus;
  @Schema(description = "应用能力关联对象")
  private WorkbenchAppRelObj appRelObj;
  @Schema(description = "应用授权对象")
  private WorkbenchAppAuthObj appAuthObj;

  /**
   * 设置关联AI助理列表
   */
  public void setRelAiAppList(List<SimpleBotDTO> botList) {
    if (CollectionUtils.isEmpty(botList)) {
      return;
    }
    if (appRelObj == null) {
      appRelObj = new WorkbenchAppRelObj();
    }
    appRelObj.setAiAppList(botList);
  }

  /**
   * 设置关联网页应用
   */
  public void setRelWebApp(WebAppDTO webAppDTO) {
    if (webAppDTO == null) {
      return;
    }
    if (appRelObj == null) {
      appRelObj = new WorkbenchAppRelObj();
    }
    appRelObj.setWebApp(webAppDTO);
  }

  /**
   * 设置关联网页应用ID
   */
  public void setRelWebAppId(Long webAppId) {
    if (webAppId == null) {
      return;
    }
    if (appRelObj == null) {
      appRelObj = new WorkbenchAppRelObj();
    }
    appRelObj.setWebAppId(webAppId);
  }

  /**
   * 设置授权用户列表
   */
  public void setAuthUserList(List<OrganizationUserDTO> userList) {
    if (CollectionUtils.isEmpty(userList)) {
      return;
    }
    if (appAuthObj == null) {
      appAuthObj = new WorkbenchAppAuthObj();
    }
    appAuthObj.setUserList(userList);
  }

  /**
   * 设置授权组织列表
   */
  public void setAuthOrgList(List<OrganizationUserDTO> orgList) {
    if (CollectionUtils.isEmpty(orgList)) {
      return;
    }
    if (appAuthObj == null) {
      appAuthObj = new WorkbenchAppAuthObj();
    }
    appAuthObj.setOrgList(orgList);
  }

  @Getter
  @Setter
  @ToString
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonInclude(Include.NON_NULL)
  public static class WorkbenchAppRelObj {
    @Schema(description = "AI助理应用列表")
    private List<SimpleBotDTO> aiAppList;
    @Schema(description = "网页应用")
    private WebAppDTO webApp;
    @Schema(description = "网页应用ID")
    private Long webAppId;
  }

  @Getter
  @Setter
  @ToString
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonInclude(Include.NON_NULL)
  public static class WorkbenchAppAuthObj {
    @Schema(description = "授权用户列表")
    private List<OrganizationUserDTO> userList;
    @Schema(description = "授权用户ID列表")
    private List<Long> userIdList;
    @Schema(description = "授权组织列表")
    private List<OrganizationUserDTO> orgList;
    @Schema(description = "授权组织ID列表")
    private List<Long> orgIdList;
  }

}
