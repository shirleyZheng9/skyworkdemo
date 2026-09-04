package com.iwhalecloud.bote.suggestion.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.elasticsearch.ElasticsearchHelper;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.BoteEsDocumentTypeEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SuggestionTermOwnerTypeEnum;
import com.iwhalecloud.bote.common.enums.SuggestionTermTypeEnum;
import com.iwhalecloud.bote.common.util.ExcelUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.BoteEsDocument;
import com.iwhalecloud.bote.dto.base.BoteEsRequest;
import com.iwhalecloud.bote.dto.base.BoteEsSuggest;
import com.iwhalecloud.bote.dto.base.BoteSuggestionResponse;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermDTO;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermImportDTO;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermSaveDTO;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermSearchDTO;
import com.iwhalecloud.bote.dto.suggestion.query.SuggestionTermQueryParams;
import com.iwhalecloud.bote.entity.suggestion.SuggestionTermEntity;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.suggestion.SuggestionTermManageMapper;
import com.iwhalecloud.bote.suggestion.ISuggestionTermManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 联想术语管理服务实现
 *
 * @author lizuyin
 * @since 2025-06-09
 */
@Service
@RequiredArgsConstructor
public class SuggestionTermManageServiceImpl implements ISuggestionTermManageService {
  private static final Integer BATCH_SIZE = 200;
  private static final Integer INITIAL_ROW = 2;

  private final SuggestionTermManageMapper suggestionTermManageMapper;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final ElasticsearchHelper elasticsearchHelper;
  private final BotQueryMapper botQueryMapper;

  /**
   * 分页查询联想术语
   *
   * @param queryParams 查询参数
   * @return 分页结果
   */
  @Override
  public PageInfo<SuggestionTermDTO> querySuggestionTermPage(SuggestionTermQueryParams queryParams) {
    //noinspection resource
    return suggestionTermManageMapper.selectSuggestionTermPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  /**
   * 新增&修改联想术语
   *
   * @param dto 联想术语保存参数
   * @return 联想术语ID
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVO<SuggestionTermDTO> saveSuggestionTerm(SuggestionTermSaveDTO dto) {
    // 参数校验
    validateSuggestionTermParams(dto);
    validateSuggestionTermScope(dto);
    Long ownerId = getOwnerIdByParameters(dto.getTenantId(), dto.getBotId(), dto.getSceneId(), dto.getOwnerType());
    boolean isExist = suggestionTermManageMapper.checkSuggestionTermExists(dto.getTermId(), ownerId,
      dto.getOwnerType().name(), dto.getTermType().name(), dto.getTermContent(), dto.getTenantId());
    if (isExist) {
      throw new BssException("同一归属范围下已存在重复词条");
    }

    // 检查重复性（排除当前正在编辑的术语）
    SuggestionTermEntity entity = new SuggestionTermEntity();
    entity.setTermId(dto.getTermId());
    entity.setTermContent(dto.getTermContent());
    entity.setTermType(dto.getTermType().name());
    entity.setOwnerType(dto.getOwnerType().name());
    entity.setBotId(dto.getBotId());
    entity.setSceneId(dto.getSceneId());
    entity.setTenantId(dto.getTenantId());
    entity.setRemark(dto.getRemark());
    entity.setStatusCd("00A");
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    if (entity.getTermId() == null) {
      entity.setTermId(Sequences.SUGGESTION_TERM_ID.next());
      entity.setCreatorId(currentUserId);
      entity.setUpdatorId(currentUserId);
    }
    else {
      entity.setUpdatorId(currentUserId);
    }
    SuggestionTermEntity oldEntity = null;
    if (entity.getTermId() != null) {
      oldEntity = suggestionTermManageMapper.selectSuggestionTermById(entity.getTermId(), entity.getTenantId());
    }
    DataDifference<SuggestionTermEntity> difference = DataDifferenceStarter.computeSave(oldEntity, entity, false, entity.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }

    SuggestionTermTypeEnum termType = SuggestionTermTypeEnum.valueOf(entity.getTermType());
    String operation = oldEntity == null ? "add" : "update";
    handleElasticsearchOperation(entity, termType, operation);

    return ResultVO.success();
  }

  /**
   * 删除联想术语
   *
   * @param termId 联想术语ID
   * @param tenantId 租户ID
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteSuggestionTerm(Long termId, Long tenantId) {
    Assert.notNull(termId, "联想术语ID不能为空");
    Assert.notNull(tenantId, "租户ID不能为空");

    SuggestionTermEntity existingTerm = suggestionTermManageMapper.selectSuggestionTermById(termId, tenantId);
    Assert.notNull(existingTerm, "联想术语不存在");

    int rows = suggestionTermManageMapper.deleteSuggestionTerm(termId, tenantId);
    Assert.isTrue(rows > 0, "联想术语不存在或已被删除");

    SuggestionTermTypeEnum termType = SuggestionTermTypeEnum.valueOf(existingTerm.getTermType());
    handleElasticsearchOperation(existingTerm, termType, "delete");
  }

  /**
   * 获取联想术语列表
   *
   * @param tenantId 租户ID
   */
  @Override
  public ResultVO<Void> listSuggestionTerm(Long tenantId) {
    Assert.notNull(tenantId, "租户ID不能为空");

    List<SuggestionTermDTO> suggestionTermDTOS = suggestionTermManageMapper.selectSuggestionTerm(tenantId);
    if (!suggestionTermDTOS.isEmpty()) {
      List<BoteEsDocument> documents = suggestionTermDTOS.stream()
        .map(this::generateSuggestionDocumentFromDTO)
        .collect(Collectors.toList());
      elasticsearchHelper.rebuildNgramIndex(documents, tenantId);
    }
    return ResultVO.success();
  }

  /**
   * 执行联想术语搜索
   *
   * @param req 搜索请求
   * @return 搜索结果，包含match查询和建议查询的结果
   */
  @Override
  public SuggestionTermSearchDTO search(BoteEsRequest req) {
    // 调用拆分后的方法来实现相同功能，保持向后兼容性
    List<BoteSuggestionResponse> matchResults = searchWord(req);
    List<BoteSuggestionResponse> suggestionResults = searchTerm(req);

    return new SuggestionTermSearchDTO(matchResults, suggestionResults);
  }

  /**
   * 执行术语联想搜索
   *
   * @param req 搜索请求参数
   * @return 返回建议查询的结果
   */
  @Override
  public List<BoteSuggestionResponse> searchTerm(BoteEsRequest req) {
    BoteEsRequest processedReq = processSearchParameters(req, 10);
    req.setType(BoteEsDocumentTypeEnum.SUGGESTION_TERM.name());
    return elasticsearchHelper.searchNgramSuggestions(processedReq);
  }

  /**
   * 执行单词匹配搜索
   *
   * @param req 搜索请求参数
   * @return 返回match查询的结果
   */
  @Override
  public List<BoteSuggestionResponse> searchWord(BoteEsRequest req) {
    BoteEsRequest processedReq = processSearchParameters(req, 10);
    req.setType(BoteEsDocumentTypeEnum.SUGGESTION_WORD.name());
    return elasticsearchHelper.searchWithNgramMatch(processedReq);
  }

  /**
   * 验证联想术语基本参数
   *
   * @param dto 联想术语参数
   */
  private void validateSuggestionTermParams(SuggestionTermSaveDTO dto) {
    Assert.notNull(dto, "参数不能为空");
    Assert.hasText(dto.getTermContent(), "联想术语内容不能为空");
    Assert.notNull(dto.getTenantId(), "租户ID不能为空");
    Assert.notNull(dto.getTermType(), "联想话术类型不能为空");
    Assert.notNull(dto.getOwnerType(), "归属者类型不能为空");
  }

  /**
   * 验证联想术语归属范围
   *
   * @param dto 新增或修改参数
   */
  private void validateSuggestionTermScope(SuggestionTermSaveDTO dto) {
    SuggestionTermOwnerTypeEnum ownerType = dto.getOwnerType();

    switch (ownerType) {
      case BOT:
        validateBotScope(dto);
        break;
      case SCENE:
        validateSceneScope(dto);
        break;
      case TENANT:
        validateTenantScope(dto);
        break;
      default:
        throw new BssException("不支持的归属者类型: " + ownerType);
    }
  }

  /**
   * 根据参数获取归属者ID
   *
   * @param tenantId 租户ID
   * @param botId 智能应用ID
   * @param sceneId 智能体ID
   * @param ownerType 归属者类型
   * @return 归属者ID
   */
  private Long getOwnerIdByParameters(Long tenantId, Long botId, Long sceneId, SuggestionTermOwnerTypeEnum ownerType) {
    switch (ownerType) {
      case BOT:
        return botId;
      case SCENE:
        return sceneId;
      case TENANT:
        return tenantId;
      default:
        throw new BssException("不支持的归属者类型: " + ownerType);
    }
  }

  /**
   * 验证智能应用维度参数
   *
   * @param dto 参数对象
   */
  private void validateBotScope(SuggestionTermSaveDTO dto) {
    Assert.notNull(dto.getBotId(), "智能应用维度的联想术语必须指定智能应用ID");
    Assert.isNull(dto.getSceneId(), "智能应用维度的联想术语不能指定智能体ID");
    Assert.isTrue(botQueryMapper.existsBotById(dto.getBotId(), dto.getTenantId()),
      "指定的智能应用不存在或已失效");
  }

  /**
   * 验证智能体维度参数
   *
   * @param dto 参数对象
   */
  private void validateSceneScope(SuggestionTermSaveDTO dto) {
    Assert.notNull(dto.getSceneId(), "智能体维度的联想术语必须指定智能体ID");
    Assert.isNull(dto.getBotId(), "智能体维度的联想术语不能指定智能应用ID");
    Assert.isTrue(suggestionTermManageMapper.checkSceneExists(dto.getSceneId()),
      "指定的智能体不存在或已失效");
  }

  /**
   * 验证租户维度参数
   *
   * @param dto 参数对象
   */
  private void validateTenantScope(SuggestionTermSaveDTO dto) {
    Assert.isNull(dto.getBotId(), "租户维度的联想术语不能指定智能应用ID");
    Assert.isNull(dto.getSceneId(), "租户维度的联想术语不能指定智能体ID");
    Assert.isTrue(suggestionTermManageMapper.checkTenantExists(dto.getTenantId()),
      "指定的租户不存在或已失效");
  }

  /**
   * 处理Elasticsearch相关操作
   *
   * @param entity 实体对象
   * @param termType 术语类型
   * @param operation 操作类型: add, update, delete
   */
  private void handleElasticsearchOperation(SuggestionTermEntity entity, SuggestionTermTypeEnum termType, String operation) {
    BoteEsDocument suggestionDocument = generateSuggestionDocument(entity);

    if (SuggestionTermTypeEnum.WORD.equals(termType)) {
      handleWordTypeOperation(suggestionDocument, operation);
    }
    else if (SuggestionTermTypeEnum.TERM.equals(termType)) {
      handleTermTypeOperation(suggestionDocument, operation);
    }
  }

  /**
   * 处理WORD类型的Elasticsearch操作
   *
   * @param document 文档对象
   * @param operation 操作类型
   */
  private void handleWordTypeOperation(BoteEsDocument document, String operation) {
    switch (operation) {
      case "add":
        elasticsearchHelper.addNgramDocument(document);
        break;
      case "update":
        elasticsearchHelper.updateNgramDocument(document);
        break;
      case "delete":
        elasticsearchHelper.deleteNgramDocument(document.getId(), document.getTenantId());
        break;
      default:
        throw new IllegalArgumentException("不支持的操作类型: " + operation);
    }
  }

  /**
   * 处理TERM类型的Elasticsearch操作
   *
   * @param document 文档对象
   * @param operation 操作类型
   */
  private void handleTermTypeOperation(BoteEsDocument document, String operation) {
    if (!"delete".equals(operation)) {
      buildSuggestionField(document);
    }

    switch (operation) {
      case "add":
        elasticsearchHelper.addSuggestionDocument(document);
        break;
      case "update":
        elasticsearchHelper.updateSuggestionDocument(document);
        break;
      case "delete":
        elasticsearchHelper.deleteSuggestionDocument(document.getId(), document.getTenantId());
        break;
      default:
        throw new IllegalArgumentException("不支持的操作类型: " + operation);
    }
  }

  /**
   * 根据联想术语实体生成ES文档
   *
   * @param term 联想术语实体
   * @return ES文档
   */
  private BoteEsDocument generateSuggestionDocument(SuggestionTermEntity term) {
    BoteEsDocument document = new BoteEsDocument();
    document.setId(term.getTermId().toString());
    document.setContent(term.getTermContent());
    document.setTenantId(term.getTenantId());

    SuggestionTermTypeEnum termType = SuggestionTermTypeEnum.valueOf(term.getTermType().toUpperCase());
    document.setType(SuggestionTermTypeEnum.WORD.equals(termType) ?
      BoteEsDocumentTypeEnum.SUGGESTION_WORD.name() :
      BoteEsDocumentTypeEnum.SUGGESTION_TERM.name());

    setDocumentOwnerInfo(document, term.getOwnerType(), term.getBotId(), term.getSceneId(), term.getTenantId());
    return document;
  }

  /**
   * 根据联想术语DTO生成ES文档
   *
   * @param dto 联想术语DTO
   * @return ES文档
   */
  private BoteEsDocument generateSuggestionDocumentFromDTO(SuggestionTermDTO dto) {
    BoteEsDocument document = new BoteEsDocument();
    document.setId(dto.getTermId().toString());
    document.setContent(dto.getTermContent());
    document.setTenantId(dto.getTenantId());

    SuggestionTermTypeEnum termType = SuggestionTermTypeEnum.valueOf(dto.getTermType().toUpperCase());
    document.setType(SuggestionTermTypeEnum.WORD.equals(termType) ?
      BoteEsDocumentTypeEnum.SUGGESTION_WORD.name() :
      BoteEsDocumentTypeEnum.SUGGESTION_TERM.name());


    setDocumentOwnerInfo(document, dto.getOwnerType(), dto.getBotId(), dto.getSceneId(), dto.getTenantId());

    if (SuggestionTermTypeEnum.TERM.equals(termType)) {
      buildSuggestionField(document);
    }

    return document;
  }

  /**
   * 设置文档归属者信息
   *
   * @param document 文档对象
   * @param ownerTypeStr 归属者类型字符串
   * @param botId 智能应用ID
   * @param sceneId 智能体ID
   * @param tenantId 租户ID
   */
  private void setDocumentOwnerInfo(BoteEsDocument document, String ownerTypeStr, Long botId, Long sceneId, Long tenantId) {
    SuggestionTermOwnerTypeEnum ownerType = SuggestionTermOwnerTypeEnum.valueOf(ownerTypeStr.toUpperCase());
    Long ownerId = getOwnerIdByParameters(tenantId, botId, sceneId, ownerType);

    document.setOwnerType(ownerType.name());
    document.setOwnerId(String.valueOf(ownerId));
  }

  /**
   * 处理搜索参数，设置 score 和 limit
   *
   * @param req 搜索请求
   * @param maxLimit 最大限制条数
   * @return 处理后的搜索请求
   */
  private BoteEsRequest processSearchParameters(BoteEsRequest req, int maxLimit) {
    Assert.notNull(req.getTenantId(), "租户ID不能为空");
    req.setField("content");

    int limit = req.getLimit() != null ? req.getLimit() : tenantSettingInfoCache.getSuggestionLimit(req.getTenantId());
    double score = req.getScore() != null ? req.getScore() : tenantSettingInfoCache.getSuggestionScore(req.getTenantId());

    req.setLimit(Math.max(1, Math.min(maxLimit, limit)));
    req.setScore(Math.max(0.0, Math.min(20.0, score)));

    return req;
  }

  /**
   * 为文档构建suggest字段
   *
   * @param document 文档对象
   */
  private void buildSuggestionField(BoteEsDocument document) {
    BoteEsSuggest suggest = BoteEsSuggest.createWithSingleInput(document.getContent());

    try {
      Map<String, Object> contextMap = new HashMap<>();
      contextMap.put("ownerType", document.getOwnerType());
      contextMap.put("ownerId", document.getOwnerId());
      contextMap.put("tenantId", String.valueOf(document.getTenantId()));

      String combinedContextJson = JsonUtil.toJsonString(contextMap);
      suggest.addSingleContext("combined_context", combinedContextJson);
    }
    catch (Exception e) {
      throw new BssException("构建combined_context失败!" + e.getMessage(), e);
    }

    document.setSuggest(suggest);
  }

  /**
   * 批量导入联想术语
   *
   * @param file Excel文件
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   * @param tenantId 租户ID
   * @return 导入结果
   */
  @Override
  public ResultVO<SuggestionTermImportDTO> batchImportSuggestionTerms(MultipartFile file, String ownerType, Long ownerId, Long tenantId) {
    try {
      // 1. 获取文件名并校验文件类型
      String fileName = file.getOriginalFilename();
      if (StringUtils.isEmpty(fileName)) {
        return ResultVO.fail("批量导入联想术语失败，文件名不能为空");
      }

      // 检查文件扩展名
      String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
      if (!("xls".equals(fileExtension) || "xlsx".equals(fileExtension))) {
        return ResultVO.fail("批量导入联想术语失败，请使用正确的模板进行导入");
      }

      // 2. 解析Excel文件
      List<Map<String, Object>> dataList = parseExcel(file, fileName);
      if (!StringUtils.isEmpty((String) dataList.get(0).get("errMsg"))) {
        return ResultVO.fail("批量导入联想术语失败，请使用正确的模板进行导入");
      }

      // 3. 数据校验
      if (!isValidOwnerType(ownerType)) {
        return ResultVO.fail("批量导入联想术语失败，归属者类型非法");
      }
      validateOwnerExists(ownerType, ownerId, tenantId);

      SuggestionTermImportDTO importResult = validateImportData(dataList, ownerType, ownerId, tenantId);

      // 4. 批量插入操作
      if (importResult.getSuccessCount() > 0) {
        batchInsertSuggestionTerms(importResult, ownerType, ownerId, tenantId);
      }

      return ResultVO.success(importResult);
    }
    catch (Exception e) {
      return ResultVO.fail("批量导入联想术语失败: " + e.getMessage());
    }
  }

  /**
   * 批量导出联想术语为Excel
   *
   * @param tenantId 租户ID
   * @param ownerType 归属者类型（可选）
   * @param ownerId 归属者ID（可选）
   * @param termType 话术类型（可选）
   * @param response HttpServletResponse
   */
  @Override
  @SuppressFBWarnings("REC_CATCH_EXCEPTION")
  @SuppressWarnings("PMD.LooseCoupling")
  public void batchExportSuggestionTerms(Long tenantId, String ownerType, Long ownerId, String termType, HttpServletResponse response) {
    try {
      SuggestionTermQueryParams queryParams = buildExportQueryParams(tenantId, ownerType, ownerId, termType);
      List<SuggestionTermDTO> suggestionTermList = suggestionTermManageMapper.selectSuggestionTermListForExport(queryParams);
      List<Map<String, Object>> resultList = buildExcelDataList(suggestionTermList);
      exportToExcel(resultList, response);
    }
    catch (Exception e) {
      throw new BssException("导出联想术语失败: " + e.getMessage(), e);
    }
  }

  /**
   * 构建导出查询参数
   *
   * @param tenantId 租户ID
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   * @param termType 话术类型
   * @return 查询参数
   */
  private SuggestionTermQueryParams buildExportQueryParams(Long tenantId, String ownerType, Long ownerId, String termType) {
    SuggestionTermQueryParams queryParams = new SuggestionTermQueryParams();
    queryParams.setTenantId(tenantId);
    queryParams.setOwnerType(ownerType);
    queryParams.setTermType(termType);

    if (StringUtils.isNotBlank(ownerType) && ownerId != null) {
      setOwnerIdByType(queryParams, ownerType, ownerId);
    }

    return queryParams;
  }

  /**
   * 根据归属者类型设置对应的ID
   *
   * @param queryParams 查询参数
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   */
  private void setOwnerIdByType(SuggestionTermQueryParams queryParams, String ownerType, Long ownerId) {
    SuggestionTermOwnerTypeEnum ownerTypeEnum = SuggestionTermOwnerTypeEnum.valueOf(ownerType);

    switch (ownerTypeEnum) {
      case BOT:
        queryParams.setBotId(ownerId);
        break;
      case SCENE:
        queryParams.setSceneId(ownerId);
        break;
      case TENANT:
        // 租户ID已经设置
        break;
      default:
        throw new BssException("不支持的归属者类型: " + ownerType);
    }
  }

  /**
   * 构建Excel数据列表
   *
   * @param suggestionTermList 联想术语列表
   * @return Excel数据列表
   */
  private List<Map<String, Object>> buildExcelDataList(List<SuggestionTermDTO> suggestionTermList) {
    List<Map<String, Object>> resultList = new ArrayList<>();

    for (int i = 0; i < suggestionTermList.size(); i++) {
      SuggestionTermDTO dto = suggestionTermList.get(i);
      Map<String, Object> rowData = buildSingleRowData(dto, i + 1);
      resultList.add(rowData);
    }

    return resultList;
  }

  /**
   * 构建单行数据
   *
   * @param dto 联想术语DTO
   * @param rowIndex 行索引
   * @return 单行数据
   */
  private Map<String, Object> buildSingleRowData(SuggestionTermDTO dto, int rowIndex) {
    Map<String, Object> rowData = new HashMap<>();
    rowData.put("rowIndex", String.valueOf(rowIndex));
    rowData.put("termContent", dto.getTermContent());
    rowData.put("termType", dto.getTermType());
    rowData.put("ownerType", SuggestionTermOwnerTypeEnum.valueOf(dto.getOwnerType()).getDesc());

    setOwnerInfo(rowData, dto);
    return rowData;
  }

  /**
   * 设置归属者信息
   *
   * @param rowData 行数据
   * @param dto 联想术语DTO
   */
  private void setOwnerInfo(Map<String, Object> rowData, SuggestionTermDTO dto) {
    SuggestionTermOwnerTypeEnum ownerTypeEnum = SuggestionTermOwnerTypeEnum.valueOf(dto.getOwnerType());

    switch (ownerTypeEnum) {
      case BOT:
        rowData.put("ownerName", dto.getBotName() != null ? dto.getBotName() : "");
        rowData.put("ownerCode", dto.getBotId() != null ? String.valueOf(dto.getBotId()) : "");
        break;
      case SCENE:
        rowData.put("ownerName", dto.getSceneName() != null ? dto.getSceneName() : "");
        rowData.put("ownerCode", dto.getSceneId() != null ? String.valueOf(dto.getSceneId()) : "");
        break;
      case TENANT:
        rowData.put("ownerName", dto.getTenantName() != null ? dto.getTenantName() : "");
        rowData.put("ownerCode", dto.getTenantId() != null ? String.valueOf(dto.getTenantId()) : "");
        break;
      default:
        rowData.put("ownerName", "");
        rowData.put("ownerCode", "");
    }
  }

  /**
   * 导出到Excel
   *
   * @param resultList 结果列表
   * @param response HttpServletResponse
   * @throws Exception 异常
   */
  private void exportToExcel(List<Map<String, Object>> resultList, HttpServletResponse response) throws Exception {
    Map<String, String> titleMap = buildExcelTitleMap();

    Map<String, Object> param = new HashMap<>();
    param.put("title", "联想术语导出");

    String fileName = "联想术语导出_" + DateUtil.format("yyyy-MM-dd_HH_mm_ss") + ".xlsx";
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString());

    ExcelUtil.exportExcelDocument(param, titleMap, resultList, response.getOutputStream());
  }

  /**
   * 构建Excel标题映射
   *
   * @return 标题映射
   */
  private Map<String, String> buildExcelTitleMap() {
    Map<String, String> titleMap = new LinkedHashMap<>();
    titleMap.put("rowIndex", "序号");
    titleMap.put("termContent", "话术内容");
    titleMap.put("termType", "话术类型（热词-WORD 话术-TERM）");
    titleMap.put("ownerType", "归属范围");
    titleMap.put("ownerName", "归属者名");
    titleMap.put("ownerCode", "归属者编码");
    return titleMap;
  }

  /**
   * 解析Excel文件
   */
  private List<Map<String, Object>> parseExcel(MultipartFile file, String fileName) throws IOException {
    // 使用现有的ExcelUtil类解析Excel文件
    String[] colName = {"rowIndex", "termContent", "termType"};
    boolean isXls = !fileName.toLowerCase().endsWith(".xlsx");
    try (InputStream inputStream = file.getInputStream()) {
      return ExcelUtil.getExcelData(inputStream, INITIAL_ROW, isXls, colName);
    }
  }

  /**
   * 验证归属者类型是否合法
   */
  private boolean isValidOwnerType(String ownerType) {
    if (StringUtils.isEmpty(ownerType)) {
      return false;
    }
    try {
      SuggestionTermOwnerTypeEnum.valueOf(ownerType);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * 验证归属者是否存在
   */
  private void validateOwnerExists(String ownerType, Long ownerId, Long tenantId) {
    SuggestionTermOwnerTypeEnum ownerTypeEnum = SuggestionTermOwnerTypeEnum.valueOf(ownerType);
    switch (ownerTypeEnum) {
      case BOT:
        Assert.isTrue(botQueryMapper.existsBotById(ownerId, tenantId), "指定的智能应用不存在或已失效");
        break;
      case SCENE:
        Assert.isTrue(suggestionTermManageMapper.checkSceneExists(ownerId), "指定的智能体不存在或已失效");
        break;
      case TENANT:
        Assert.isTrue(suggestionTermManageMapper.checkTenantExists(ownerId), "指定的租户不存在或已失效");
        break;
      default:
        throw new BssException("不支持的归属者类型: " + ownerType);
    }
  }

  /**
   * 验证导入数据
   * 优化后的校验逻辑：确保数据状态一致性，早期过滤失败数据
   *
   * @param dataList 数据列表
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   * @param tenantId 租户ID
   * @return 导入结果
   */
  private SuggestionTermImportDTO validateImportData(List<Map<String, Object>> dataList, String ownerType, Long ownerId, Long tenantId) {
    SuggestionTermImportDTO result = initImportResult();
    // 按类型分组的Map结构，key为termType（WORD/TERM），value为该类型下已见的内容集合
    Map<String, Set<String>> seenTermContents = new HashMap<>();

    int row = INITIAL_ROW;
    for (Map<String, Object> termData : dataList) {
      row++;
      // 只有通过所有校验的数据才会进入successList
      if (processRowDataWithValidation(termData, row, seenTermContents, result)) {
        result.getSuccessList().add(termData);
      }
      // 实时更新统计信息
      updateStatisticsRealTime(result, dataList.size());
    }

    // 最后进行数据库重复检查
    checkDatabaseDuplicates(result, ownerType, ownerId, tenantId);
    // 最终统计更新
    updateImportResultCounts(result, dataList.size());

    return result;
  }

  /**
   * 初始化导入结果
   *
   * @return 导入结果对象
   */
  private SuggestionTermImportDTO initImportResult() {
    SuggestionTermImportDTO result = new SuggestionTermImportDTO();
    result.setSuccessList(new ArrayList<>());
    result.setFailList(new ArrayList<>());
    return result;
  }

  /**
   * 处理单行数据并进行完整校验
   * 优化版本：早期过滤，确保数据状态一致性
   *
   * @param termData         术语数据
   * @param row              行号
   * @param seenTermContents 已见的术语内容集合（按类型分组）
   * @param result           导入结果
   * @return true表示通过所有校验，false表示校验失败
   */
  private boolean processRowDataWithValidation(Map<String, Object> termData, int row, Map<String, Set<String>> seenTermContents, SuggestionTermImportDTO result) {
    String rowIndex = (String) termData.get("rowIndex");

    // 1. 行号校验
    if (StringUtils.isEmpty(rowIndex)) {
      addErrorToImportResult(result, "行号: " + row, "缺少序号，已跳过此数据", termData);
      return false;
    }

    // 2. 内容校验
    if (!validateTermContent(termData, rowIndex, result)) {
      return false;
    }

    // 3. 类型校验
    if (!validateTermType(termData, rowIndex, result)) {
      return false;
    }

    // 4. 批次内重复检查
    return checkBatchDuplicate(termData, rowIndex, seenTermContents, result);
  }

  /**
   * 验证术语内容（优化版本）
   *
   * @param termData 术语数据
   * @param rowIndex 行索引
   * @param result   导入结果
   * @return true表示校验通过，false表示校验失败
   */
  private boolean validateTermContent(Map<String, Object> termData, String rowIndex, SuggestionTermImportDTO result) {
    String termContent = (String) termData.get("termContent");
    if (StringUtils.isEmpty(termContent)) {
      addErrorToImportResult(result, "序号" + rowIndex + "所在行，话术内容列数据异常", "话术内容不能为空", termData);
      return false;
    }
    return true;
  }

  /**
   * 验证术语类型（优化版本）
   *
   * @param termData 术语数据
   * @param rowIndex 行索引
   * @param result   导入结果
   * @return true表示校验通过，false表示校验失败
   */
  private boolean validateTermType(Map<String, Object> termData, String rowIndex, SuggestionTermImportDTO result) {
    String termType = (String) termData.get("termType");
    if (StringUtils.isEmpty(termType)) {
      addErrorToImportResult(result, "序号" + rowIndex + "所在行，话术类型列数据异常", "话术类型不能为空", termData);
      return false;
    }

    if (!isValidTermType(termType)) {
      addErrorToImportResult(result, "序号" + rowIndex + "所在行，话术类型列数据异常", "话术类型不存在", termData);
      return false;
    }
    return true;
  }

  /**
   * 检查批次内重复（优化版本）
   *
   * @param termData         术语数据
   * @param rowIndex         行索引
   * @param seenTermContents 已见的术语内容集合，按类型分组
   * @param result           导入结果
   * @return true表示校验通过，false表示校验失败
   */
  private boolean checkBatchDuplicate(Map<String, Object> termData, String rowIndex, Map<String, Set<String>> seenTermContents, SuggestionTermImportDTO result) {
    String termContent = (String) termData.get("termContent");
    String termType = (String) termData.get("termType");

    // 如果内容或类型为空，则不进行重复检查（应该在前面的校验中被拦截）
    if (StringUtils.isEmpty(termContent) || StringUtils.isEmpty(termType)) {
      return true; // 这种情况不应该出现，如果出现则跳过重复检查
    }

    // 获取或创建对应类型的内容集合
    Set<String> contentSetForType = seenTermContents.computeIfAbsent(termType, k -> new HashSet<>());

    // 检查该类型下是否已存在相同内容
    if (!contentSetForType.add(termContent)) {
      String typeDesc = "WORD".equals(termType) ? "热词" : "TERM".equals(termType) ? "术语" : termType;
      addErrorToImportResult(result, "序号" + rowIndex + "所在行",
        "当前批次中存在重复的" + typeDesc + "内容：" + termContent + "，已跳过", termData);
      return false;
    }
    return true;
  }

  /**
   * 检查数据库重复数据（优化版本）
   * 只对通过前面所有校验的数据进行数据库检查，避免无效查询
   *
   * @param result 导入结果
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   * @param tenantId 租户ID
   */
  private void checkDatabaseDuplicates(SuggestionTermImportDTO result, String ownerType, Long ownerId, Long tenantId) {
    if (result.getSuccessList().isEmpty()) {
      return;
    }

    // 只查询有效的数据内容
    List<String> termContents = result.getSuccessList().stream()
      .map(data -> (String) data.get("termContent"))
      .filter(StringUtils::isNotEmpty) // 过滤空内容（理论上不应该存在）
      .distinct() // 去重减少数据库查询压力
      .collect(Collectors.toList());

    if (termContents.isEmpty()) {
      return;
    }

    List<String> existingTermContents = suggestionTermManageMapper.checkSuggestionTermContentsExist(
      termContents, ownerType, ownerId, tenantId);

    if (!existingTermContents.isEmpty()) {
      removeDuplicateFromSuccess(result, existingTermContents);
    }
  }

  /**
   * 从成功列表中移除重复数据（优化版本）
   * 确保统计信息的一致性
   *
   * @param result 导入结果
   * @param existingTermContents 已存在的术语内容列表
   */
  private void removeDuplicateFromSuccess(SuggestionTermImportDTO result, List<String> existingTermContents) {
    Iterator<Map<String, Object>> iterator = result.getSuccessList().iterator();
    while (iterator.hasNext()) {
      Map<String, Object> data = iterator.next();
      String content = (String) data.get("termContent");
      if (existingTermContents.contains(content)) {
        String rowIndex = (String) data.get("rowIndex");
        addErrorToImportResult(result, "序号" + rowIndex + "所在行", "数据库中已存在相同的话术内容：" + content, data);
        iterator.remove(); // 安全地从successList中移除
      }
    }
  }

  /**
   * 更新导入结果统计
   *
   * @param result 导入结果
   * @param totalSize 总数据量
   */
  private void updateImportResultCounts(SuggestionTermImportDTO result, int totalSize) {
    result.setSuccessCount(result.getSuccessList().size());
    result.setFailCount(totalSize - result.getSuccessCount());
    result.setTotalCount(totalSize);
  }

  /**
   * 实时更新统计信息
   *
   * @param result 导入结果
   * @param totalSize 总数据量
   */
  private void updateStatisticsRealTime(SuggestionTermImportDTO result, int totalSize) {
    result.setSuccessCount(result.getSuccessList().size());
    result.setFailCount(result.getFailList() == null ? 0 : result.getFailList().size());
    result.setTotalCount(totalSize);
  }

  /**
   * 向失败列表添加错误记录
   */
  private void addErrorToImportResult(SuggestionTermImportDTO result, String rowIndex, String errorMessage, Map<String, Object> data) {
    result.failPut(rowIndex, errorMessage, data);
  }

  /**
   * 验证话术类型是否合法
   */
  private boolean isValidTermType(String termType) {
    if (StringUtils.isEmpty(termType)) {
      return false;
    }
    try {
      SuggestionTermTypeEnum.valueOf(termType);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * 批量插入联想术语
   *
   * @param result 导入结果
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   * @param tenantId 租户ID
   */
  @Transactional(rollbackFor = Exception.class)
  public void batchInsertSuggestionTerms(SuggestionTermImportDTO result, String ownerType, Long ownerId, Long tenantId) {
    List<Map<String, Object>> successList = result.getSuccessList();
    if (CollectionUtils.isEmpty(successList)) {
      return;
    }

    List<SuggestionTermEntity> entityList = buildEntityList(successList, ownerType, ownerId, tenantId);
    batchInsertAndUpdateElasticsearch(entityList);
  }

  /**
   * 构建实体列表
   *
   * @param successList 成功数据列表
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   * @param tenantId 租户ID
   * @return 实体列表
   */
  private List<SuggestionTermEntity> buildEntityList(List<Map<String, Object>> successList, String ownerType, Long ownerId, Long tenantId) {
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    List<SuggestionTermEntity> entityList = new ArrayList<>();

    for (Map<String, Object> data : successList) {
      String termContent = (String) data.get("termContent");
      if (StringUtils.isBlank(termContent)) {
        continue;
      }

      SuggestionTermEntity entity = createSuggestionTermEntity(data, ownerType, ownerId, tenantId, currentUserId);
      entityList.add(entity);
    }

    return entityList;
  }

  /**
   * 创建联想术语实体
   *
   * @param data 数据映射
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   * @param tenantId 租户ID
   * @param currentUserId 当前用户ID
   * @return 联想术语实体
   */
  private SuggestionTermEntity createSuggestionTermEntity(Map<String, Object> data, String ownerType, Long ownerId, Long tenantId, Long currentUserId) {
    String termContent = (String) data.get("termContent");
    String termType = (String) data.get("termType");

    SuggestionTermEntity entity = new SuggestionTermEntity();
    entity.setTermId(Sequences.SUGGESTION_TERM_ID.next());
    entity.setTermContent(termContent);
    entity.setTermType(termType);
    entity.setOwnerType(ownerType);
    entity.setTenantId(tenantId);
    entity.setCreatorId(currentUserId);
    entity.setUpdatorId(currentUserId);

    setEntityOwnerInfo(entity, ownerType, ownerId);
    return entity;
  }

  /**
   * 设置实体归属者信息
   *
   * @param entity 实体对象
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   */
  private void setEntityOwnerInfo(SuggestionTermEntity entity, String ownerType, Long ownerId) {
    SuggestionTermOwnerTypeEnum ownerTypeEnum = SuggestionTermOwnerTypeEnum.valueOf(ownerType);

    switch (ownerTypeEnum) {
      case BOT:
        entity.setBotId(ownerId);
        break;
      case SCENE:
        entity.setSceneId(ownerId);
        break;
      case TENANT:
        // 租户ID已经设置
        break;
      default:
        throw new BssException("不支持的归属者类型: " + ownerType);
    }
  }

  /**
   * 分批插入并更新Elasticsearch
   *
   * @param entityList 实体列表
   */
  private void batchInsertAndUpdateElasticsearch(List<SuggestionTermEntity> entityList) {
    for (int i = 0; i < entityList.size(); i += BATCH_SIZE) {
      int end = Math.min(i + BATCH_SIZE, entityList.size());
      List<SuggestionTermEntity> batch = entityList.subList(i, end);

      suggestionTermManageMapper.batchInsertSuggestionTerms(batch);
      updateElasticsearchForBatch(batch);
    }
  }

  /**
   * 为批次更新Elasticsearch
   *
   * @param batch 批次实体列表
   */
  private void updateElasticsearchForBatch(List<SuggestionTermEntity> batch) {
    for (SuggestionTermEntity entity : batch) {
      SuggestionTermTypeEnum termType = SuggestionTermTypeEnum.valueOf(entity.getTermType());
      handleElasticsearchOperation(entity, termType, "add");
    }
  }
}
