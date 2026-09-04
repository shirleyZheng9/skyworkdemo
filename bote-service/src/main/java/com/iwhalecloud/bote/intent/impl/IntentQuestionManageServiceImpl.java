package com.iwhalecloud.bote.intent.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.intent.IntentQuestionDTO;
import com.iwhalecloud.bote.dto.intent.IntentQuestionImportDTO;
import com.iwhalecloud.bote.dto.intent.query.IntentQueryParams;
import com.iwhalecloud.bote.intent.IIntentEmbeddingService;
import com.iwhalecloud.bote.intent.IIntentQuestionManageService;
import com.iwhalecloud.bote.mapper.bot.BotSceneManageMapper;
import com.iwhalecloud.bote.mapper.intent.IntentQuestionManageMapper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.transform.export.Exporters;
import com.iwhalecloud.bss.litchi.transform.export.descriptor.ColumnModel;
import com.iwhalecloud.bss.litchi.transform.export.descriptor.ExportDescriptor;
import com.iwhalecloud.bote.common.util.ExcelUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 意图标注管理服务实现
 *
 * @author auto
 * @since 2024-12-18
 */
@Service
@RequiredArgsConstructor
public class IntentQuestionManageServiceImpl implements IIntentQuestionManageService {

  private final IntentQuestionManageMapper intentQuestionMapper;

  private final IIntentEmbeddingService intentEmbeddingService;

  private final BotSceneManageMapper botSceneManageMapper;

  /** Excel 数据起始行（0-based，2 表示从第三行开始读取数据，第1行是英文字段名，第2行是中文字段名） */
  private static final int INITIAL_ROW = 2;

  @Override
  public IntentQuestionDTO findIntentQuestion(Long tenantId, Long id) {
    return intentQuestionMapper.getIntentQuestion(tenantId, id);
  }

  @Override
  @Transactional
  public ResultVO<IntentQuestionDTO> saveIntentQuestion(IntentQuestionDTO question) {
    // 校验意图问句唯一性
    if (intentQuestionMapper.existsQuestion(question)) {
      return BaseErrorConstant.QUESTION_EXIST.toResult(question.getQuestion());
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    question.setStatusCd(BaseConsts.STATUS_CD_VALID);
    question.setCreatorId(userId);
    question.setUpdatorId(userId);
    if (question.getQuestionId() == null) {
      question.setQuestionId(Sequences.INTENTION_QUESTION_ID.next());
      intentQuestionMapper.insertIntentQuestion(question);
    }
    else {
      intentQuestionMapper.updateIntentQuestion(question);
    }
    intentEmbeddingService.save(question);
    return ResultVO.success(question);
  }

  @Override
  @Transactional
  public List<IntentQuestionDTO> batchSaveIntentQuestion(List<IntentQuestionDTO> questions) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    for (IntentQuestionDTO question : questions) {
      question.setQuestionId(Sequences.INTENTION_QUESTION_ID.next());
      question.setStatusCd(BaseConsts.STATUS_CD_VALID);
      question.setCreatorId(userId);
      question.setUpdatorId(userId);
    }
    intentQuestionMapper.batchInsertIntentQuestion(questions);
    intentEmbeddingService.batchSave(questions);
    return Collections.emptyList();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteIntentQuestion(Long tenantId, Long id) {
    IntentQuestionDTO question = intentQuestionMapper.getIntentQuestion(tenantId, id);
    if (question == null) {
      return BaseErrorConstant.NOT_EXIST.toResult(id);
    }
    intentQuestionMapper.deleteIntentQuestion(tenantId, id, SessionUtil.getLoginInfo().getUserId());
    intentEmbeddingService.delete(tenantId, id);
    return ResultVO.success();
  }

  @Override
  public List<IntentQuestionDTO> queryIntentQuestionList(IntentQueryParams queryParams) {
    return intentQuestionMapper.selectIntentQuestionList(queryParams);
  }

  @Override
  public PageInfo<IntentQuestionDTO> queryIntentQuestionPage(IntentQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    return intentQuestionMapper.selectIntentQuestionPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public File createIntentQuestionFile(Long tenantId) {
    List<IntentQuestionDTO> questions = intentQuestionMapper.selectQuestionsByTenantId(tenantId);
    // 生成 excel 文件
    String type = "xlsx";
    String fileName = "IntentQuestion-" + System.currentTimeMillis() + ".";
    List<ColumnModel<Map<String, String>>> columnModels = new ArrayList<>();
    columnModels.add(ColumnModel.<Map<String, String>>builder().name("instruction").field("instruction").build());
    columnModels.add(ColumnModel.<Map<String, String>>builder().name("input").field("input").build());
    columnModels.add(ColumnModel.<Map<String, String>>builder().name("output").field("output").build());
    columnModels.add(ColumnModel.<Map<String, String>>builder().name("system").field("system").build());
    columnModels.add(ColumnModel.<Map<String, String>>builder().name("history").field("history").build());

    // @formatter:off
    ExportDescriptor<Map<String, String>> descriptor = ExportDescriptor.<Map<String, String>>builder()
      .sheetList(Collections.singletonList(BaseConsts.DEFAULT_SHEET_NAME))
      .forceDownload(true)
      .filename(fileName + type)
      .showHeader(true)
      .columnModels(columnModels)
      .build();
    //  @formatter:on
    List<Map<String, String>> datas = new ArrayList<>();
    for (IntentQuestionDTO question : CollectionUtils.emptyIfNull(questions)) {
      Map<String, String> data = new HashMap<>(8);
      data.put("instruction", null);
      data.put("system", null);
      data.put("history", null);
      data.put("input", question.getQuestion());
      data.put("output", question.getSceneId() + "-" + question.getAttribute());
      datas.add(data);
    }
    File file = Exporters.createTempExcel(descriptor);
    Exporters.appendToExcel(file, descriptor, BaseConsts.DEFAULT_SHEET_NAME, datas);
    return file;
  }

  @Override
  @Transactional
  public ResultVO<IntentQuestionImportDTO> batchImportIntentQuestions(MultipartFile file, Long tenantId, Long sceneId) {
    if (botSceneManageMapper.getScene(tenantId, sceneId) == null) {
      return ResultVO.fail("选中的智能体非法或不存在!");
    }
    List<Map<String, Object>> dataList;
    try {
      dataList = parseExcel(file);
    }
    catch (IOException e) {
      return ResultVO.fail("批量导入意图问句失败，请使用正确的模板进行导入");
    }
    if (!CollectionUtils.isEmpty(dataList) && StringUtils.isNotBlank((String) dataList.getFirst().get("errMsg"))) {
      return ResultVO.fail("批量导入意图问句失败，" + dataList.getFirst().get("errMsg"));
    }

    IntentQuestionImportDTO importResult = validateData(dataList, tenantId, sceneId);

    if (importResult.getSuccessCount() > 0) {
      List<IntentQuestionDTO> questions = toIntentQuestionList(importResult.getSuccessList(), tenantId, sceneId);
      batchSaveIntentQuestion(questions);
    }

    return ResultVO.success(importResult);
  }

  private List<Map<String, Object>> parseExcel(MultipartFile file) throws IOException {
    String[] colName = {"rowIndex", "question", "attribute"};
    String fileName = file.getOriginalFilename();
    if (fileName == null) {
      throw new IllegalArgumentException("文件名不能为空");
    }
    String fileExtension = fileName.toLowerCase();
    boolean isXls = fileExtension.endsWith(".xls");
    if (!fileExtension.endsWith(".xls") && !fileExtension.endsWith(".xlsx")) {
      throw new IllegalArgumentException("不支持的文件格式，请使用.xls或.xlsx格式的Excel文件");
    }
    try (InputStream inputStream = file.getInputStream()) {
      return ExcelUtil.getExcelData(inputStream, INITIAL_ROW, isXls, colName);
    }
  }

  private IntentQuestionImportDTO validateData(List<Map<String, Object>> dataList, Long tenantId, Long sceneId) {
    IntentQuestionImportDTO result = new IntentQuestionImportDTO();
    result.setSuccessList(new ArrayList<>());
    result.setFailList(new ArrayList<>());

    Set<String> seenQuestions = new HashSet<>();
    int row = INITIAL_ROW;

    for (Map<String, Object> data : CollectionUtils.emptyIfNull(dataList)) {
      row++;
      String rowIndex = String.valueOf(data.get("rowIndex"));
      String question = (String) data.get("question");
      String attribute = (String) data.get("attribute");

      if (!validateQuestionNotEmpty(result, data, question, rowIndex, row)) {
        continue;
      }

      if (!validateDuplicateInExcel(result, data, question, tenantId, sceneId, rowIndex, seenQuestions)) {
        continue;
      }

      if (!validateDuplicateInDb(result, data, question, tenantId, sceneId, rowIndex)) {
        continue;
      }

      if (!validateAttribute(result, data, attribute, rowIndex)) {
        continue;
      }

      // 通过所有校验，加入成功列表
      result.getSuccessList().add(data);
    }

    result.setSuccessCount(result.getSuccessList().size());
    result.setFailCount(dataList.size() - result.getSuccessCount());
    result.setTotalCount(dataList.size());

    return result;
  }

  private List<IntentQuestionDTO> toIntentQuestionList(List<Map<String, Object>> successList, Long tenantId, Long sceneId) {
    List<IntentQuestionDTO> questions = new ArrayList<>();
    for (Map<String, Object> data : successList) {
      IntentQuestionDTO question = new IntentQuestionDTO();
      question.setQuestion((String) data.get("question"));
      question.setAttribute((String) data.get("attribute"));
      question.setSceneId(sceneId);
      question.setTenantId(tenantId);
      questions.add(question);
    }
    return questions;
  }

  private boolean validateQuestionNotEmpty(IntentQuestionImportDTO result, Map<String, Object> data, String question, String rowIndex, int row) {
    if (StringUtils.isBlank(question)) {
      String realRowIndex = StringUtils.isNotBlank(rowIndex) ? rowIndex : "行号: " + row;
      result.failPut(realRowIndex, "问句不能为空", data);
      return false;
    }
    return true;
  }

  private boolean validateDuplicateInExcel(IntentQuestionImportDTO result, Map<String, Object> data, String question,
    Long tenantId, Long sceneId, String rowIndex, Set<String> seenQuestions) {
    String key = tenantId + ":" + sceneId + ":" + question;
    if (seenQuestions.contains(key)) {
      result.failPut(rowIndex, "同一批次中问句重复", data);
      return false;
    }
    seenQuestions.add(key);
    return true;
  }

  private boolean validateDuplicateInDb(IntentQuestionImportDTO result, Map<String, Object> data, String question,
    Long tenantId, Long sceneId, String rowIndex) {
    IntentQuestionDTO dto = new IntentQuestionDTO();
    dto.setTenantId(tenantId);
    dto.setQuestion(question);
    dto.setSceneId(sceneId);
    if (intentQuestionMapper.existsQuestion(dto)) {
      result.failPut(rowIndex, "问句在系统中已存在", data);
      return false;
    }
    return true;
  }

  /**
   * 验证属性格式
   */
  private boolean validateAttribute(IntentQuestionImportDTO result, Map<String, Object> data, String attribute, String rowIndex) {
    if (StringUtils.isBlank(attribute)) {
      return true;
    }
    // 尝试解析为JSON数组
    List<Map<String, String>> attributeList = JsonUtil.parseJson(attribute, new TypeReference<>() {
    });

    if (CollectionUtils.isEmpty(attributeList)) {
      result.failPut(rowIndex, "扩展参数属性格式错误：必须是JSON数组", data);
      return false;
    }
    // 验证每个元素都必须包含key和value字段
    for (int i = 0; i < attributeList.size(); i++) {
      Map<String, String> item = attributeList.get(i);
      if (item == null) {
        result.failPut(rowIndex, "扩展参数属性格式错误：数组第" + (i + 1) + "个元素不能为空", data);
        return false;
      }
      if (!item.containsKey("key") || !item.containsKey("value")) {
        result.failPut(rowIndex, "扩展参数属性格式错误：数组第" + (i + 1) + "个元素必须包含key和value字段", data);
        return false;
      }
      String key = item.get("key");
      String value = item.get("value");
      if (StringUtils.isBlank(key)) {
        result.failPut(rowIndex, "扩展参数属性格式错误：数组第" + (i + 1) + "个元素的key不能为空", data);
        return false;
      }
      if (StringUtils.isBlank(value)) {
        result.failPut(rowIndex, "扩展参数属性格式错误：数组第" + (i + 1) + "个元素的value不能为空", data);
        return false;
      }
    }
    return true;
  }

}
