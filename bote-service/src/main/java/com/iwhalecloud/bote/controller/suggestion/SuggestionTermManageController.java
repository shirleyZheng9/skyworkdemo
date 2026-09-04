package com.iwhalecloud.bote.controller.suggestion;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.BoteEsRequest;
import com.iwhalecloud.bote.dto.base.BoteSuggestionResponse;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermDTO;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermDeleteDTO;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermImportDTO;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermSaveDTO;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermSearchDTO;
import com.iwhalecloud.bote.dto.suggestion.query.SuggestionTermQueryParams;
import com.iwhalecloud.bote.suggestion.ISuggestionTermManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 联想术语管理 Controller，提供分页查询、增删改及ElasticSearch操作。
 *
 * @author lizuyin
 * @since 2025-06-09
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/suggestionTerm", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "联想术语：联想术语管理")
public class SuggestionTermManageController {

  private final ISuggestionTermManageService suggestionTermService;

  @Operation(summary = "分页查询联想术语")
  @PostMapping("querySuggestionTermPage")
  public ResultVO<PageInfo<SuggestionTermDTO>> querySuggestionTermPage(@RequestBody SuggestionTermQueryParams queryParams) {
    return ResultVO.success(suggestionTermService.querySuggestionTermPage(queryParams));
  }

  @Operation(summary = "新增&修改联想术语")
  @PostMapping("saveSuggestionTerm")
  public ResultVO<SuggestionTermDTO> saveSuggestionTerm(@RequestBody SuggestionTermSaveDTO dto) {
    return suggestionTermService.saveSuggestionTerm(dto);
  }

  @Operation(summary = "删除联想术语")
  @PostMapping("deleteSuggestionTerm")
  public ResultVO<Void> deleteSuggestionTerm(@RequestBody SuggestionTermDeleteDTO deleteDTO) {
    suggestionTermService.deleteSuggestionTerm(deleteDTO.getTermId(), deleteDTO.getTenantId());
    return ResultVO.success();
  }

  @Operation(summary = "输入框联想")
  @PostMapping("/searchSuggestionTerm")
  public ResultVO<SuggestionTermSearchDTO> searchSuggestionTerm(@RequestBody BoteEsRequest req) {
    return ResultVO.success(suggestionTermService.search(req));
  }

  @Operation(summary = "术语联想搜索")
  @PostMapping("/searchTerm")
  public ResultVO<List<BoteSuggestionResponse>> searchSuggestionTermByTerm(@RequestBody BoteEsRequest req) {
    return ResultVO.success(suggestionTermService.searchTerm(req));
  }

  @Operation(summary = "单词匹配搜索")
  @PostMapping("/searchWord")
  public ResultVO<List<BoteSuggestionResponse>> searchSuggestionTermByWord(@RequestBody BoteEsRequest req) {
    return ResultVO.success(suggestionTermService.searchWord(req));
  }

  @Operation(summary = "一键向量化-重置ES节点")
  @GetMapping("/syncSuggestionTermToEs")
  public ResultVO<Void> syncSuggestionTermToEs(Long tenantId) {
    return suggestionTermService.listSuggestionTerm(tenantId);
  }

  @Operation(summary = "根据模板Excel批量导入联想术语")
  @PostMapping("batchImportSuggestionTerms")
  public ResultVO<SuggestionTermImportDTO> batchImportSuggestionTerms(@RequestParam("file") MultipartFile file, @RequestParam String ownerType,
    @RequestParam Long ownerId, @RequestParam Long tenantId) {
    return suggestionTermService.batchImportSuggestionTerms(file, ownerType, ownerId, tenantId);
  }

  @Operation(summary = "批量导出联想术语为Excel")
  @GetMapping("batchExportSuggestionTerms")
  public void batchExportSuggestionTerms(@RequestParam Long tenantId, @RequestParam(required = false) String ownerType,
    @RequestParam(required = false) Long ownerId, @RequestParam(required = false) String termType, HttpServletResponse response) {
    suggestionTermService.batchExportSuggestionTerms(tenantId, ownerType, ownerId, termType, response);
  }

}
