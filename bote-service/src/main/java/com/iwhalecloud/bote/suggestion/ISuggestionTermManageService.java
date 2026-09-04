package com.iwhalecloud.bote.suggestion;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.BoteEsRequest;
import com.iwhalecloud.bote.dto.base.BoteSuggestionResponse;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermImportDTO;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermSaveDTO;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermDTO;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermSearchDTO;
import com.iwhalecloud.bote.dto.suggestion.query.SuggestionTermQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 联想术语管理服务
 *
 * @author lizuyin
 * @since 2025-06-09
 */
public interface ISuggestionTermManageService {

  /**
   * 查询联想术语列表（分页）
   *
   * @param queryParams 查询条件
   * @return 联想术语分页列表
   */
  PageInfo<SuggestionTermDTO> querySuggestionTermPage(SuggestionTermQueryParams queryParams);

  /**
   * 新增&修改联想术语
   *
   * @param dto 联想术语保存参数
   * @return 联想术语ID
   */
  ResultVO<SuggestionTermDTO> saveSuggestionTerm(SuggestionTermSaveDTO dto);

  /**
   * 删除联想术语
   *
   * @param termId 联想术语ID
   * @param tenantId 租户ID
   */
  void deleteSuggestionTerm(Long termId, Long tenantId);

  /**
   * 获取联想术语列表
   *
   */
  ResultVO<Void> listSuggestionTerm(Long tenantId);

  /**
   * 执行全文搜索
   *
   * @param req 搜索请求参数
   * @return 返回匹配的搜索结果，包含match查询和建议查询的结果
   */
  SuggestionTermSearchDTO search(BoteEsRequest req);

  /**
   * 执行术语联想搜索
   *
   * @param req 搜索请求参数
   * @return 返回建议查询的结果
   */
  List<BoteSuggestionResponse> searchTerm(BoteEsRequest req);

  /**
   * 执行单词匹配搜索
   *
   * @param req 搜索请求参数
   * @return 返回match查询的结果
   */
  List<BoteSuggestionResponse> searchWord(BoteEsRequest req);

  /**
   * 批量导入联想术语
   *
   * @param file Excel文件
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   * @param tenantId 租户ID
   * @return 导入结果
   */
  ResultVO<SuggestionTermImportDTO> batchImportSuggestionTerms(
    MultipartFile file, String ownerType, Long ownerId, Long tenantId);

  /**
   * 批量导出联想术语为Excel
   *
   * @param tenantId 租户ID
   * @param ownerType 归属者类型（可选）
   * @param ownerId 归属者ID（可选）
   * @param termType 话术类型（可选）
   * @param response HttpServletResponse
   */
  void batchExportSuggestionTerms(Long tenantId, String ownerType, Long ownerId, String termType, HttpServletResponse response);
}
