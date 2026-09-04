package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillPageManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 技能：页面 controller
 *
 * @author auto
 * @since 2024-09-15
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/page", name = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@RestController
@Tag(name = "技能：页面管理")
public class SkillPageManageController {
  private static final Logger logger = LoggerFactory.getLogger(SkillPageManageController.class);
  /** javascript 媒体类型 */
  private static final MediaType MEDIA_TYPE_JAVASCRIPT = MediaType.parseMediaType("application/javascript");

  private final ISkillPageManageService skillPageManageService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "保存页面基本信息")
  @PostMapping("saveSkillPageInfo")
  public ResultVO<SkillPageDTO> saveSkillPageInfo(@RequestBody SkillPageDTO skillPage) {
    Long pageId = skillPage.getPageId();
    ResultVO<SkillPageDTO> result = skillPageManageService.saveSkillPageInfo(skillPage);
    if (pageId != null && result.isSuccess()) {
      String key = skillPage.getTenantId() + CacheConsts.COLON + pageId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_PAGE, key);
    }
    return result;
  }

  @Operation(summary = "保存页面")
  @PostMapping("saveSkillPage")
  public ResultVO<SkillPageDTO> saveSkillPage(@RequestBody @Valid SkillPageDTO skillPage) {
    Long pageId = skillPage.getPageId();
    ResultVO<SkillPageDTO> result = skillPageManageService.saveSkillPage(skillPage);
    if (pageId != null && result.isSuccess()) {
      String key = skillPage.getTenantId() + CacheConsts.COLON + pageId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_PAGE, key);
    }
    return result;
  }

  @Operation(summary = "查询单个页面")
  @GetMapping("findSkillPage")
  public ResultVO<SkillPageDTO> findSkillPage(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("pageId") Long pageId) {
    Assert.notNull(pageId, "页面ID不能为空");
    return ResultVO.success(skillPageManageService.findSkillPage(tenantId, pageId));
  }

  @Operation(summary = "查询页面列表", description = "用于其他模块引用")
  @PostMapping("querySkillPageList")
  public ResultVO<List<SimpleSkillPageDTO>> querySkillPageList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(skillPageManageService.querySkillPageList(params));
  }

  @Operation(summary = "分页查询页面")
  @PostMapping("querySkillPagePage")
  public ResultVO<PageInfo<SkillPageDTO>> querySkillPagePage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(skillPageManageService.querySkillPagePage(params));
  }

  @Operation(summary = "分页查询页面", description = "用于其他模块引用")
  @PostMapping("querySimpleSkillPagePage")
  public ResultVO<PageInfo<SimpleSkillPageDTO>> querySimpleSkillPagePage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(skillPageManageService.querySimpleSkillPagePage(params));
  }

  @Operation(summary = "删除页面")
  @GetMapping("deleteSkillPage")
  public ResultVO<Void> deleteSkillPage(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("pageId") Long pageId) {
    Assert.notNull(pageId, "页面ID不能为空");
    ResultVO<Void> result = skillPageManageService.deleteSkillPage(tenantId, pageId);
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + pageId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_PAGE, key);
    }
    return result;
  }

  @GetMapping(path = "getRemoteComponent", produces = "application/javascript")
  @Operation(summary = "获取远程组件内容")
  @SuppressFBWarnings("CRLF_INJECTION_LOGS")
  public ResponseEntity<?> getRemoteComponent(@Parameter(description = "pageCode用于旧的业务系统调用，后续都使用botePageCode")
                                              @RequestParam(value = "pageCode", required = false) String pageCode,
                                              @RequestParam(value = "fileInfoId", required = false) Long fileInfoId,
                                              @RequestParam(value = "botePageCode", required = false) String botePageCode,
                                              @RequestParam("tenantId") Long tenantId) {
    try {
      Assert.notNull(tenantId, "tenantId 不能为空");
      Assert.isTrue(fileInfoId != null || StringUtils.isNotEmpty(pageCode) || StringUtils.isNotEmpty(botePageCode), "botePageCode、pageCode 和 fileInfoId 不能同时为空");
      if (StringUtils.isNotEmpty(pageCode)) {
        botePageCode = pageCode;
      }
      String content = skillPageManageService.getRemoteComponent(botePageCode, fileInfoId, tenantId);
      return ResponseEntity.ok().contentType(MEDIA_TYPE_JAVASCRIPT).body(content);
    }
    // 自行处理异常，避免 Spring 报错 HttpMediaTypeNotAcceptableException
    catch (IllegalArgumentException e) {
      ResultVO<?> result = new ResultVO<>("400", e.getMessage());
      return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(result);
    }
    catch (BssException e) {
      ResultVO<?> result = new ResultVO<>(StringUtils.defaultIfEmpty(e.getFailCode(), "500"), e.getMessage());
      return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).body(result);
    }
    catch (Exception e) {
      logger.error("Failed to get remote component: tenantId={}, fileInfoId={}, pageCode={}", tenantId, fileInfoId, botePageCode, e);
      ResultVO<?> result = new ResultVO<>("500", ExpUtil.getMsg(e));
      return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).body(result);
    }
  }
}
