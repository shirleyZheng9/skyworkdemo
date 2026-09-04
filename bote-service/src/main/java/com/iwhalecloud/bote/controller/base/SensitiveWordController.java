package com.iwhalecloud.bote.controller.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.SensitiveWordCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.base.CheckSensitiveWordRequest;
import com.iwhalecloud.bote.dto.base.SensitiveWordDTO;
import com.iwhalecloud.bote.dto.base.query.SensitiveWordQueryParams;
import com.iwhalecloud.bote.service.base.ISensitiveWordService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 敏感词管理
 *
 * @author bianjp
 * @since 2025-01-15
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/sensitiveWord", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：敏感词管理")
public class SensitiveWordController {
  private final ISensitiveWordService sensitiveWordService;
  private final IRefreshCacheService refreshCacheService;
  private final SensitiveWordCache sensitiveWordCache;

  @Operation(summary = "保存敏感词")
  @PostMapping("saveSensitiveWord")
  public ResultVO<Long> saveSensitiveWord(@RequestBody SensitiveWordDTO sensitiveWord) {
    ResultVO<Long> result = sensitiveWordService.saveSensitiveWord(sensitiveWord);
    if (result.isSuccess()) {
      refreshCacheService.refreshAll(CacheConsts.CACHE_NAME_SENSITIVE_WORD);
    }
    return result;
  }

  @Operation(summary = "删除敏感词")
  @GetMapping("removeSensitiveWord")
  public ResultVO<Void> removeSensitiveWord(@RequestParam("wordId") Long wordId) {
    if (sensitiveWordService.deleteSensitiveWord(wordId)) {
      refreshCacheService.refreshAll(CacheConsts.CACHE_NAME_SENSITIVE_WORD);
    }
    return ResultVO.success();
  }

  @Operation(summary = "查询单个敏感词")
  @GetMapping("findSensitiveWord")
  public ResultVO<SensitiveWordDTO> findSensitiveWord(@RequestParam("wordId") Long wordId) {
    return ResultVO.success(sensitiveWordService.findSensitiveWordById(wordId));
  }

  @Operation(summary = "分页查询敏感词")
  @PostMapping("qrySensitiveWordPage")
  public ResultVO<PageInfo<SensitiveWordDTO>> qrySensitiveWordPage(@RequestBody SensitiveWordQueryParams queryParams) {
    return ResultVO.success(sensitiveWordService.qrySensitiveWordPage(queryParams));
  }

  @Operation(summary = "检查文本中包含哪些敏感词")
  @PostMapping("checkSensitiveWord")
  public ResultVO<List<String>> checkSensitiveWord(@RequestBody CheckSensitiveWordRequest request) {
    return ResultVO.success(sensitiveWordCache.findSensitiveWords(request.getText()));
  }

}
