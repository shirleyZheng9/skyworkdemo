package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.PlatformImageCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.base.PlatformVersionInfoDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台信息
 *
 * @author bianjp
 * @since 2025-06-30
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "platform/")
@RequiredArgsConstructor
@IgnoreSign
@IgnoreSession
@Tag(name = "平台信息")
public class PlatformInfoController implements InitializingBean {
  /** 默认的平台图标 */
  private static final Resource DEFAULT_LOGO = new ClassPathResource("assets/images/logo.png");
  /** 默认的管理平台首页的机器人图标 */
  private static final Resource DEFAULT_HOME_BOT_IMAGE = new ClassPathResource("assets/images/home-bot.png");
  /** 默认的管理平台首页的引导图 */
  private static final Resource DEFAULT_HOME_STEP_IMAGE = new ClassPathResource("assets/images/home-step.png");

  private final PlatformImageCache platformImageCache;
  /** 平台版本信息 */
  private final PlatformVersionInfoDTO platformVersionInfo = new PlatformVersionInfoDTO();

  /**
   * 初始化平台版本信息
   */
  @Override
  public void afterPropertiesSet() throws IOException {
    ClassPathResource resource = new ClassPathResource("git-version/bote-service.properties");
    // 本地开发时可能不存在
    if (!resource.exists()) {
      return;
    }
    Properties properties = new Properties();
    try (InputStream inputStream = resource.getInputStream()) {
      properties.load(inputStream);
    }
    // Maven 工程的版本号目前固定不变，使用分支名称作为版本
    platformVersionInfo.setBranch(properties.getProperty("git.branch"));
    platformVersionInfo.setCommitId(properties.getProperty("git.commit.id.abbrev"));
    platformVersionInfo.setCommitTime(properties.getProperty("git.commit.time"));
    platformVersionInfo.setBuildHost(properties.getProperty("git.build.host"));
    platformVersionInfo.setBuildTime(properties.getProperty("git.build.time"));
  }

  @GetMapping(path = "version", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "获取平台版本信息")
  public ResultVO<PlatformVersionInfoDTO> version() {
    return ResultVO.success(platformVersionInfo);
  }

  @GetMapping("logo")
  @Operation(summary = "获取平台图标")
  public void logo(HttpServletRequest request, HttpServletResponse response) throws IOException {
    platformImageCache.sendImage(request, response, SystemParameter.PLATFORM_ICON, "平台图标", DEFAULT_LOGO);
  }

  @GetMapping("loginBgImage")
  @Operation(summary = "获取登录页背景图")
  public void loginBgImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
    platformImageCache.sendImage(request, response, SystemParameter.PLATFORM_LOGIN_BG, "登录页背景图", null);
  }

  @GetMapping("homeBotImage")
  @Operation(summary = "获取首页机器人图片")
  public void homeBotImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
    platformImageCache.sendImage(request, response, SystemParameter.PLATFORM_BOT_IMAGE, "首页机器人图片", DEFAULT_HOME_BOT_IMAGE);
  }

  @GetMapping("homeStepImage")
  @Operation(summary = "获取首页使用步骤图片")
  public void homeStepImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
    platformImageCache.sendImage(request, response, SystemParameter.PLATFORM_STEP_IMAGE, "首页使用步骤图片", DEFAULT_HOME_STEP_IMAGE);
  }

}
