package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.google.common.base.CaseFormat;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.jdbc.LowerCaseColumnMapRowMapper;
import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.enums.DocSequences;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.CorpusInfoManageMapper;
import com.iwhalecloud.bote.doc.module.base.service.impl.FileUploadHelper;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentParameterDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentContentQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentContentManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentParameterManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentContentManageService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocumentHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.importer.DocumentImportListener;
import com.iwhalecloud.bote.dto.model.query.CorpusParams;
import com.iwhalecloud.bote.mapper.base.BoteFileInfoMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.transform.export.Exporters;
import com.iwhalecloud.bss.litchi.transform.export.descriptor.ColumnModel;
import com.iwhalecloud.bss.litchi.transform.export.descriptor.ExportDescriptor;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档内容管理
 *
 * @author qian.sisheng
 * @since 2025-3-12
 */
@Service
@RequiredArgsConstructor
public class DocumentContentManageServiceImpl implements IDocumentContentManageService {
  private final Logger logger = LoggerFactory.getLogger(DocumentContentManageServiceImpl.class);

  private static final Pattern SQL_FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
  // @formatter:off
  private final DocumentParameterManageMapper parameterManageMapper;
  private final DocumentContentManageMapper contentManageMapper;
  private final JdbcTemplate jdbcTemplate;
  private final IFileStoreService fileStoreService;
  private final DocumentHelper documentHelper;
  private final BoteFileInfoMapper fileInfoMapper;
  private final FileUploadHelper fileUploadHelper;
  private final DocumentManageMapper documentManageMapper;
  private final CorpusInfoManageMapper corpusInfoManageMapper;
  // @formatter:on

  @Override
  @Transactional
  public ResultVO<DocumentDTO> saveDocumentParameter(DocumentDTO document) {
    List<DocumentParameterDTO> addParameters = document.getParameters().stream()
      .filter(parameter -> parameter.getParameterId() == null).collect(Collectors.toList());
    List<DocumentParameterDTO> updateParameters = document.getParameters().stream()
      .filter(parameter -> parameter.getParameterId() != null).collect(Collectors.toList());
    List<DocumentParameterDTO> oldParameters = parameterManageMapper
      .selectDocumentParameterList(document.getDocumentId(), document.getTenantId());
    for (DocumentParameterDTO parameter : CollectionUtils.emptyIfNull(oldParameters)) {
      DocumentParameterDTO documentParameter = IterableUtils.find(document.getParameters(),
        p -> p.getParameterId() != null && Objects.equals(parameter.getParameterId(), p.getParameterId()));
      if (documentParameter == null) {
        parameter.setStatusCd(DocBaseConsts.STATUS_CD_INVALID);
        updateParameters.add(parameter);
      }
    }
    CollectionUtils.emptyIfNull(addParameters).forEach(parameter -> {
      parameter.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
      parameter.setParameterId(DocSequences.DOCUMENT_PARAMETER_ID.next());
    });
    // 设置映射字段
    setMappingCode(document.getParameters(), oldParameters, document.getDocumentId(), document.getTenantId());
    if (CollectionUtils.isNotEmpty(addParameters)) {
      parameterManageMapper.batchInsertDocumentParameter(addParameters);
    }
    if (CollectionUtils.isNotEmpty(updateParameters)) {
      updateParameters.forEach(parameterManageMapper::updateDocumentParameter);
    }
    return ResultVO.success();
  }

  @Override
  public PageInfo<DocumentParameterDTO> queryDocumentParameterPage(DocumentContentQueryParams queryParams) {
    // noinspection resource
    return parameterManageMapper.selectDocumentParameterPage(queryParams, queryParams.buildRowBounds())
      .toPageInfo();
  }

  @Override
  public List<DocumentParameterDTO> queryDocumentParameterList(DocumentContentQueryParams queryParams) {
    return parameterManageMapper.selectParameterList(queryParams);
  }

  @Override
  public List<DocumentContentDTO> queryDocumentContentList(DocumentContentQueryParams queryParams) {
    return contentManageMapper.selectDocumentContentList(queryParams);
  }

  @Override
  @Transactional
  public ResultVO<Void> saveDocumentContent(DocumentDTO document) {
    List<DocumentParameterDTO> parameters = parameterManageMapper
      .selectDocumentParameterList(document.getDocumentId(), document.getTenantId());
    List<DocumentContentDTO> contents = document.getContents();
    if (CollectionUtils.isEmpty(contents)) {
      return ResultVO.success();
    }
    List<DocumentContentDTO> addContentList = new ArrayList<>();
    List<DocumentContentDTO> updateContentList = new ArrayList<>();
    for (DocumentContentDTO content : document.getContents()) {
      if ("A".equals(content.getActionType())) {
        content.setContentId(DocSequences.DOCUMENT_CONTENT_ID.next());
        content.setDocumentId(document.getDocumentId());
        content.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
        content.setCreatorId(SessionUtil.getLoginInfo().getUserId());
        content.setTenantId(document.getTenantId());
        addContentList.add(content);
      }
      else if ("M".equals(content.getActionType())) {
        updateContentList.add(content);
      }
      else if ("D".equals(content.getActionType())) {
        content.setStatusCd(DocBaseConsts.STATUS_CD_INVALID);
        updateContentList.add(content);
      }
    }
    documentHelper.adjustMappingCode(addContentList, parameters, document.getDocumentId(), document.getTenantId());
    documentHelper.adjustMappingCode(updateContentList, parameters, document.getDocumentId(),
      document.getTenantId());
    if (CollectionUtils.isNotEmpty(addContentList)) {
      contentManageMapper.batchInsertDocumentContent(addContentList);
    }
    if (CollectionUtils.isNotEmpty(updateContentList)) {
      updateContentList.forEach(contentManageMapper::batchUpdateDocumentContent);
    }
    if (CollectionUtils.isNotEmpty(parameters)) {
      parameters = parameters.stream().filter(p -> Boolean.TRUE.equals(p.getIsChanged()))
        .collect(Collectors.toList());
      parameters.forEach(parameterManageMapper::updateDocumentParameter);
    }
    corpusInfoManageMapper.updateCorpusInfoTime(document.getDocumentId(), SessionUtil.getLoginInfo().getUserId(), document.getTenantId());
    return ResultVO.success();
  }

  @Override
  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  @Transactional
  public ResultVO<Void> updateDocumentContentCellValue(DocumentDTO document) {
    List<DocumentParameterDTO> parameters = parameterManageMapper
      .selectDocumentParameterList(document.getDocumentId(), document.getTenantId());
    if (CollectionUtils.isEmpty(parameters)) {
      return ResultVO.fail("参数不能为空");
    }
    Map<String, String> contentCellValue = document.getContentCellValue();
    if (MapUtils.isEmpty(contentCellValue)) {
      return ResultVO.success();
    }
    List<Object> args = new ArrayList<>();
    List<String> mappingCodes = new ArrayList<>();
    for (DocumentParameterDTO parameter : parameters) {
      String value = MapUtils.getString(contentCellValue, parameter.getParameterName());
      if (StringUtils.isEmpty(value)) {
        continue;
      }
      mappingCodes
        .add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, parameter.getMappingCode()) + " = ?");
      args.add(value);
    }
    String sql = "update bt_document_content set " + StringUtils.join(mappingCodes, ", ")
      + " where document_id = ? and tenant_id = ?";
    args.add(document.getDocumentId());
    args.add(document.getTenantId());
    jdbcTemplate.update(sql, args.toArray());
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteDocumentContent(DocumentDTO document) {
    contentManageMapper.batchDeleteDocumentContent(document.getTenantId(), document, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public PageInfo<DocumentContentDTO> queryDocumentContentPage(DocumentContentQueryParams queryParams) {
    // noinspection resource
    return contentManageMapper.selectDocumentContentPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> importDocumentContent(Long tenantId, Long documentId, MultipartFile file, Long fileId,
    String importType) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    try (InputStream originalStream = fileId != null ? fileStoreService.downloadFileStream(fileId) : file.getInputStream()) {
      String fileType;
      if (fileId == null) {
        fileType = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
      }
      else {
        FileInfoVO fileInfoById = fileInfoMapper.getFileInfoById(fileId);
        if (fileInfoById == null) {
          return ResultVO.fail("文件不存在");
        }
        fileType = StringUtils.substringAfterLast(fileInfoById.getFileName(), ".");
      }
      if (!FileTypeUtil.isExcel(fileType)) {
        throw new BssException("文档保存失败，目前只支持 xlsx,xls,csv 文件导入");
      }
      if (KnowledgeConsts.IMPORT_TYPE_OVERWRITE.equals(importType)) {
        parameterManageMapper.deleteDocumentParameterByDocumentId(tenantId, documentId, userId);
        contentManageMapper.deleteDocumentContentByDocumentId(tenantId, documentId, userId);
      }
      byte[] buffer = IOUtils.toByteArray(originalStream);
      try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        ExcelReader builder = EasyExcel.read(byteArrayInputStream).build()) {
        // @formatter:off
        ReadSheet sheet = EasyExcel.readSheet(0).registerReadListener(new DocumentImportListener(tenantId,
          documentId, new ByteArrayInputStream(buffer), importType, fileType)).build();
        // @formatter:on
        builder.read(sheet);
      }
      return ResultVO.success();
    }
    catch (IOException e) {
      logger.error("Failed to read excel", e);
      throw new BssException("文件读取异常: " + e.getMessage(), e);
    }
    catch (Exception e) {
      logger.error("Failed to import document content", e);
      throw new BssException("文档导入失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void exportDocumentContent(Long tenantId, Long documentId, HttpServletResponse response) {
    try {
      String fileName = "DocumentContentExport-" + System.currentTimeMillis();
      exportDocument(tenantId, documentId, fileName, response, null);
    }
    catch (Exception e) {
      logger.error("Failed to export document, documentId={}", documentId, e);
      throw new BssException("导出文档失败: " + e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void publishDocument(Long tenantId, Long documentId) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      DocumentDTO document = documentManageMapper.getDocument(tenantId, documentId);
      if (document == null) {
        throw new BssException("文档不存在");
      }
      exportDocument(tenantId, documentId, document.getDocName(), null, outputStream);
      Path tempFile = Files.createTempFile("botStructDoc_", ".xlsx");
      Files.write(tempFile, outputStream.toByteArray());
      try (InputStream inputStream = Files.newInputStream(tempFile)) {
        long fileSize = Files.size(tempFile);
        fileUploadHelper.uploadFileWithInfoSave(inputStream, document.getDocName(), fileSize, tenantId);
      }
      finally {
        Files.deleteIfExists(tempFile);
      }
    }
    catch (IOException e) {
      logger.error("文档发布失败", e);
      throw new BssException("文档发布失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void exportTemplate(Long tenantId, Long documentId, HttpServletResponse response) {
    List<DocumentParameterDTO> parameters = parameterManageMapper.selectDocumentParameterList(documentId, tenantId);
    if (CollectionUtils.isEmpty(parameters)) {
      throw new BssException("请先配置参数");
    }
    ExportDescriptor<Map<String, String>> descriptor = getDescriptor(parameters, "importTemplate");
    Map<String, List<Map<String, String>>> dataMap = new HashMap<>(16);
    List<Map<String, String>> columns = new ArrayList<>();
    Map<String, String> column = new HashMap<>(16);
    for (DocumentParameterDTO parameter : parameters) {
      if (AttrDataType.DATETIME.getCode().equals(parameter.getDataType())) {
        column.put(parameter.getMappingCode(), DateUtil.format(new Date()));
      }
      else if (AttrDataType.DATE.getCode().equals(parameter.getDataType())) {
        column.put(parameter.getMappingCode(), DateUtil.formatDate(new Date()));
      }
      else if (AttrDataType.STRING.getCode().equals(parameter.getDataType())) {
        column.put(parameter.getMappingCode(), "示例数据");
      }
      else if (AttrDataType.INTEGER.getCode().equals(parameter.getDataType())) {
        column.put(parameter.getMappingCode(), "1");
      }
      else if (AttrDataType.NUMBER.getCode().equals(parameter.getDataType())) {
        column.put(parameter.getMappingCode(), "1.0");
      }
    }
    columns.add(column);
    dataMap.put("sheet", columns);
    Exporters.export("xlsx", descriptor, new ArrayList<>(), dataMap, response);
  }

  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  @Override
  public List<Map<String, String>> collectCorpusQuestion(String corpusInfo, Long tenantId) {
    List<CorpusParams> info = JsonUtil.parseJson(corpusInfo, new TypeReference<List<CorpusParams>>() {
    });
    Assert.notEmpty(info, "语料信息不能为空");

    DocumentContentQueryParams params = new DocumentContentQueryParams();
    params.setTenantId(tenantId);
    params.setDocumentIds(info.stream().map(CorpusParams::getCorpusId).collect(Collectors.toList()));
    Map<Long, List<DocumentParameterDTO>> group = parameterManageMapper.selectParameterList(params).stream()
      .collect(Collectors.groupingBy(DocumentParameterDTO::getDocumentId));
    List<Map<String, String>> datas = new ArrayList<>();
    for (CorpusParams dto : info) {
      String questionColumn = Optional
        .ofNullable(IterableUtils.find(group.get(dto.getCorpusId()),
          p -> dto.getQuestion().equals(p.getParameterName())))
        .map(DocumentParameterDTO::getMappingCode).orElse(null);
      String answerColumn = Optional
        .ofNullable(
          IterableUtils.find(group.get(dto.getCorpusId()), p -> dto.getAnswer().equals(p.getParameterName())))
        .map(DocumentParameterDTO::getMappingCode).orElse(null);
      if (StringUtils.isEmpty(questionColumn) || StringUtils.isEmpty(answerColumn)) {
        continue;
      }
      questionColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, questionColumn);
      answerColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, answerColumn);
      StringBuilder sql = new StringBuilder();
      // @formatter:off
      sql.append("SELECT ").append(questionColumn).append(" AS question, ").append(answerColumn)
        .append(" AS answer").append(" FROM bt_document_content WHERE status_cd = ? AND document_id = ?");
      // @formatter:on
      List<Object> args = new ArrayList<>();
      args.add(DocBaseConsts.STATUS_CD_VALID);
      args.add(dto.getCorpusId());
      List<Map<String, Object>> records = jdbcTemplate.query(sql.toString(), new LowerCaseColumnMapRowMapper(),
        args.toArray(new Object[0]));
      for (Map<String, Object> record : CollectionUtils.emptyIfNull(records)) {
        // 剔除不齐全数据
        String input = MapUtils.getString(record, "question");
        String output = MapUtils.getString(record, "answer");
        if (StringUtils.isAnyEmpty(input, output)) {
          continue;
        }
        Map<String, String> data = new HashMap<>(8);
        data.put("instruction", null);
        data.put("system", null);
        data.put("history", null);
        data.put("input", input);
        data.put("output", output);
        datas.add(data);
      }
    }
    return datas;
  }

  @Override
  public Pair<File, BigDecimal> createCorpusQuestionFile(String corpusInfo, Long tenantId) {
    List<Map<String, String>> datas = collectCorpusQuestion(corpusInfo, tenantId);
    // 生成 excel 文件
    String type = "xlsx";
    String fileName = "CorpusQuestion-" + System.currentTimeMillis() + ".";
    List<ColumnModel<Map<String, String>>> columnModels = new ArrayList<>();
    columnModels.add(ColumnModel.<Map<String, String>>builder().name("instruction").field("instruction").build());
    columnModels.add(ColumnModel.<Map<String, String>>builder().name("input").field("input").build());
    columnModels.add(ColumnModel.<Map<String, String>>builder().name("output").field("output").build());
    columnModels.add(ColumnModel.<Map<String, String>>builder().name("system").field("system").build());
    columnModels.add(ColumnModel.<Map<String, String>>builder().name("history").field("history").build());
    // @formatter:off
    ExportDescriptor<Map<String, String>> descriptor = ExportDescriptor.<Map<String, String>>builder()
      .sheetList(Collections.singletonList(DocBaseConsts.DEFAULT_SHEET_NAME)).forceDownload(true)
      .filename(fileName + type).showHeader(true).columnModels(columnModels).build();
    // @formatter:on
    File file = Exporters.createTempExcel(descriptor);
    Exporters.appendToExcel(file, descriptor, DocBaseConsts.DEFAULT_SHEET_NAME, datas);
    return Pair.of(file, computeThreshold(datas));
  }

  /**
   * 根据条件查询知识文档列表 write by zyt 2025.5.20 11465479
   *
   * @param params 包含查询条件的参数映射，其中可能包括： - document_id：文档ID，用于指定查询的文档 - tenant_id：租户ID，用于指定查询的租户范围
   * @return 返回一个包含查询结果的列表，每个结果为一个键值对映射
   */
  public List<Map<String, Object>> queryDocumentListByCondition(Long documentId, Long tenantId,
    Map<String, Object> params) {
    validateMapKeys(params);
    return documentManageMapper.queryDocumentListByCondition(documentId, tenantId, params);
  }

  /**
   * 保存文档 write by zyt 2025.5.20 11465479
   *
   * @param paramsMap 包含文档相关信息的参数映射，包括文档ID、租户ID、内容ID和文档内容等
   * @return 一个映射，包含保存操作的状态信息
   * @throws BssException 如果保存文档过程中发生异常，则抛出此异常
   */
  public Map<String, Object> saveDocument(Long documentId, Long tenantId, Map<String, Object> paramsMap) {
    Map<String, Object> result = new HashMap<>();
    try {
      Long contentId = MapUtils.getLong(paramsMap, "content_id");
      validateMapKeys(paramsMap);
      if (contentId != null) {
        // 更新数据库
        documentManageMapper.updateDocumentContentByContentId(documentId, tenantId, contentId, paramsMap);
      }
      else {
        contentId = DocSequences.DOCUMENT_CONTENT_ID.next();
        // 插入新数据
        documentManageMapper.insertDocumentContentByContentId(documentId, tenantId, contentId, paramsMap);
      }
      publishDocument(tenantId, documentId);
      result.put("state", "success");

    }
    catch (Exception e) {
      throw new BssException("保存文档失败: " + e.getMessage(), e);
    }
    return result;
  }

  private void setMappingCode(List<DocumentParameterDTO> parameters, List<DocumentParameterDTO> oldsParameters,
    Long documentId, Long tenantId) {
    if (CollectionUtils.isEmpty(parameters)) {
      return;
    }
    // 获取已使用的字段映射
    List<String> usedMappingCodes = CollectionUtils.emptyIfNull(oldsParameters).stream()
      .map(DocumentParameterDTO::getMappingCode).collect(Collectors.toList());

    // 为每个参数分配合适的字段
    for (DocumentParameterDTO parameter : parameters) {
      if (StringUtils.isEmpty(parameter.getMappingCode())) {
        int estimatedLength = StringUtils.isNotEmpty(parameter.getDefaultValue())
          ? parameter.getDefaultValue().length()
          : 0;
        String mappingCode = documentHelper.allocateField(parameter, usedMappingCodes, estimatedLength);
        parameter.setMappingCode(mappingCode);
        usedMappingCodes.add(mappingCode);
      }
    }

    // 删除失效参数的字段内容
    List<DocumentParameterDTO> deleteDocumentParametersList = CollectionUtils.emptyIfNull(oldsParameters).stream()
      .filter(o -> parameters.stream().noneMatch(p -> o.getParameterId().equals(p.getParameterId())))
      .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(deleteDocumentParametersList)) {
      deleteUnUseContent(deleteDocumentParametersList, documentId, tenantId);
    }
  }

  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  private void deleteUnUseContent(List<DocumentParameterDTO> parameters, Long documentId, Long tenantId) {
    StringBuilder sqlBuilder = new StringBuilder("update bt_document_content set ");
    for (DocumentParameterDTO parameter : parameters) {
      sqlBuilder.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, parameter.getMappingCode()))
        .append(" = null, ");
    }
    sqlBuilder.append("updator_id = ?, updated_time = ").append(DbUtil.isOracle() ? "sysdate" : "now()")
      .append(" where document_id = ? and tenant_id = ?");
    jdbcTemplate.update(sqlBuilder.toString(), SessionUtil.getLoginInfo().getUserId(), documentId, tenantId);
  }

  private void exportDocument(Long tenantId, Long documentId, String fileName, HttpServletResponse response,
    OutputStream outputStream) {
    try {
      List<DocumentParameterDTO> parameterList = parameterManageMapper.selectDocumentParameterList(documentId,
        tenantId);
      if (CollectionUtils.isEmpty(parameterList)) {
        throw new BssException("文档参数不存在");
      }
      // 获取导出描述
      ExportDescriptor<Map<String, String>> descriptor = getDescriptor(parameterList, fileName);
      Map<String, List<Map<String, String>>> dataMap = getResult(tenantId, documentId, parameterList);

      // 根据输出类型选择导出方式
      if (response != null) {
        Exporters.export("xlsx", descriptor, new ArrayList<>(), dataMap, response);
      }
      else if (outputStream != null) {
        Exporters.export("xlsx", descriptor, new ArrayList<>(), dataMap, outputStream);
      }
      else {
        throw new IllegalArgumentException("Invalid output type: both response and outputStream are null");
      }
    }
    catch (Exception e) {
      logger.error("Failed to export excel, documentId={}", documentId, e);
      throw new BssException("导出文档失败: " + e.getMessage(), e);
    }
  }

  private Map<String, List<Map<String, String>>> getResult(Long tenantId, Long documentId,
    List<DocumentParameterDTO> parameterList) {
    Map<String, List<Map<String, String>>> dataMap = new HashMap<>(16);
    List<Map<String, String>> columns = new ArrayList<>();
    DocumentContentQueryParams params = new DocumentContentQueryParams();
    params.setDocumentId(documentId);
    params.setTenantId(tenantId);
    List<DocumentContentDTO> contentList = contentManageMapper.selectDocumentContentList(params);
    for (DocumentContentDTO content : contentList) {
      Map<String, String> data = new HashMap<>(16);
      for (DocumentParameterDTO parameter : parameterList) {
        try {
          String value = BeanUtils.getProperty(content, parameter.getMappingCode());
          data.put(parameter.getMappingCode(), value);
        }
        catch (Exception e) {
          logger.error("Failed to get field", e);
          throw new BssException("文档导出异常：" + e.getMessage(), e);
        }
      }
      columns.add(data);
    }
    dataMap.put("sheet", columns);
    return dataMap;
  }

  private ExportDescriptor<Map<String, String>> getDescriptor(List<DocumentParameterDTO> parameterList,
    String fileName) {
    String type = "xlsx";
    // 表头
    List<ColumnModel<Map<String, String>>> columnModels = new ArrayList<>();
    for (DocumentParameterDTO column : parameterList) {
      // @formatter:off
      columnModels.add(ColumnModel.<Map<String, String>>builder().name(column.getParameterName())
        .field(column.getMappingCode()).build());
      // @formatter:on
    }
    // @formatter:off
    return ExportDescriptor.<Map<String, String>>builder().sheetList(Collections.singletonList("sheet"))
      .forceDownload(true).filename(fileName + "." + type).showHeader(true).columnModels(columnModels).build();
    // @formatter:on
  }

  /**
   * 阈值指数，关系到微调模型推理的准确率，需要基于问题数计算出合理值
   * <p>
   * 规则：1.25 除以 output 去重数（维度：场景 + 扩展）
   * </p>
   */
  private BigDecimal computeThreshold(List<Map<String, String>> datas) {
    int labelNum = datas.stream().map(p -> MapUtils.getString(p, "output")).collect(Collectors.toSet()).size();
    return BigDecimal.valueOf(1.25).divide(BigDecimal.valueOf(labelNum), 2, RoundingMode.HALF_UP);
  }

  /**
   * 校验 map 中的 key 是否符合 SQL 字段命名规范（只允许字母、数字、下划线） write by zyt 2025.5.20 11465479
   */
  private void validateMapKeys(Map<String, Object> map) {
    if (map == null || map.isEmpty()) {
      return;
    }
    for (String key : map.keySet()) {
      if (!SQL_FIELD_NAME_PATTERN.matcher(key).matches()) {
        throw new IllegalArgumentException("非法字段名: " + key);
      }
    }
  }

}
