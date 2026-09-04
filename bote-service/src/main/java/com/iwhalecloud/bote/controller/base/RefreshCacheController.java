package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.dto.base.CacheConfigDTO;
import com.iwhalecloud.bote.dto.base.RefreshDTO;
import com.iwhalecloud.bote.service.base.ICacheConfigManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 缓存刷新管理
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cache", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：缓存刷新管理")
@SuppressWarnings("PMD.GuardLogStatement")
public class RefreshCacheController {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final IRefreshCacheService refreshCacheService;
  private final ICacheConfigManageService cacheConfigManageService;

  @Operation(summary = "获取缓存配置列表")
  @GetMapping("queryAllCacheConfig")
  public ResultVO<List<CacheConfigDTO>> queryAllCacheConfig() {
    return ResultVO.success(cacheConfigManageService.queryAllCacheConfig());
  }

  @Operation(summary = "刷新缓存", description = "通过刷新参数刷新分布式及本地缓存")
  @PostMapping("refresh")
  public ResultVO<Object> refresh(@RequestBody RefreshDTO params) {
    // 解析参数
    parseParams(params);

    try {
      if (params.isRefreshAll()) {
        if (Refreshable.ALL_CACHE_NAME.equals(params.getCacheName())) {
          // 全量刷新所有缓存
          refreshAllCaches();
        }
        else {
          refreshCacheService.refreshAll(params.getCacheName());
        }
      }
      else {
        refreshCacheService.refresh(params.getCacheName(), params.getKeys());
      }
    }
    catch (RuntimeException e) {
      logger.error("Refresh cache failed: cacheName={}, keys={}", params.getCacheName(), params.getRawKeys(), e);
      // 普通用户一般不会刷新缓存，显示原始错误有助于定位问题
      return new ResultVO<>("-1", e.getMessage());
    }
    return ResultVO.success();
  }

  @IgnoreSign
  @IgnoreSession
  @GetMapping("refreshIgnoreAuth")
  @Operation(summary = "一键刷新缓存：免鉴权使用")
  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
  public ResultVO<Object> refreshIgnoreAuth(@RequestParam("sign") String sign) {
    // 校验 sign 值: 当前时间 + $ + 加密密钥 => 取 32 位 md5 加密小写值
    LocalDateTime nowWithDate = LocalDateTime.now();
    String currentTime = nowWithDate.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
    String source = currentTime + "$" + SystemParameter.ENCRYPTION_AES.getValueFromDb();
    String md5Hex = DigestUtils.md5Hex(source).toLowerCase();
    if (!md5Hex.equals(sign)) {
      return ResultVO.fail("Failed to check sign value.");
    }

    // 全量刷新所有缓存
    try {
      refreshAllCaches();
    }
    catch (RuntimeException e) {
      logger.error("Refresh cache failed: error = {}", e.getMessage());
      return ResultVO.fail(e.getMessage());
    }
    return ResultVO.success();
  }

  /**
   * 解析刷新规格
   */
  public void parseParams(RefreshDTO params) {
    if (params == null) {
      return;
    }
    if (StringUtils.isEmpty(params.getCacheName())) {
      throw BaseErrorConstant.CACHE_REFRESH_ERROR.toException("缓存名称不能为空");
    }
    params.parseKeys();
  }

  /**
   * 全量刷新所有缓存
   * <p>耗时较长，为避免 HTTP 超时，异步刷新</p>
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  private void refreshAllCaches() {
    Future<?> future = ThreadPools.getCommon().submit(() -> refreshCacheService.refreshAll(Refreshable.ALL_CACHE_NAME));
    try {
      // 等待 10s, 以便发现部分错误。如果未刷新完成，不再等待，但缓存还要继续刷新
      future.get(10, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      // ignore timeout
    }
    catch (ExecutionException e) {
      throw BaseErrorConstant.CACHE_REFRESH_ERROR.toException(e, "刷新缓存失败");
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw BaseErrorConstant.CACHE_REFRESH_ERROR.toException(e, "刷新缓存异常");
    }
  }
}
