package com.iwhalecloud.bote.controller.intent;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.intent.IntentQuestionDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemWithSceneDTO;
import com.iwhalecloud.bote.dto.intent.IntentQuestionImportDTO;
import com.iwhalecloud.bote.dto.intent.query.IntentQueryParams;
import com.iwhalecloud.bote.intent.IIntentEmbeddingService;
import com.iwhalecloud.bote.intent.IIntentQuestionManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * 意图问句管理 controller
 *
 * @author auto
 * @since 2024-12-18
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/intentQuestion", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "意图：意图问句管理")
public class IntentQuestionManageController {

  private final IIntentQuestionManageService intentQuestionService;

  private final IIntentEmbeddingService intentEmbeddingService;

  @Operation(summary = "查询单个意图问句")
  @GetMapping("findIntentQuestion")
  public ResultVO<IntentQuestionDTO> findIntentQuestion(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "id") Long id) {
    Assert.notNull(id, "主键 ID 不能为空");
    return ResultVO.success(intentQuestionService.findIntentQuestion(tenantId, id));
  }

  @Operation(summary = "保存意图问句")
  @PostMapping("saveIntentQuestion")
  public ResultVO<IntentQuestionDTO> saveIntentQuestion(@RequestBody IntentQuestionDTO question) {
    Assert.hasText(question.getQuestion(), "问题不能为空");
    return intentQuestionService.saveIntentQuestion(question);
  }

  @Operation(summary = "批量保存意图问句")
  @PostMapping("batchSaveIntentQuestion")
  public ResultVO<List<IntentQuestionDTO>> batchSaveIntentQuestion(@RequestBody List<IntentQuestionDTO> questions) {
    Assert.notEmpty(questions, "意图问句列表不能为空");
    return ResultVO.success(intentQuestionService.batchSaveIntentQuestion(questions));
  }

  @Operation(summary = "删除意图问句")
  @GetMapping("deleteIntentQuestion")
  public ResultVO<Void> deleteIntentQuestion(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(name = "id") Long id) {
    Assert.notNull(id, "主键 ID 不能为空");
    return intentQuestionService.deleteIntentQuestion(tenantId, id);
  }

  @Operation(summary = "查询意图问句列表")
  @PostMapping("queryIntentQuestionList")
  public ResultVO<List<IntentQuestionDTO>> queryIntentQuestionList(@RequestBody IntentQueryParams queryParams) {
    return ResultVO.success(intentQuestionService.queryIntentQuestionList(queryParams));
  }

  @Operation(summary = "分页查询意图问句")
  @PostMapping("queryIntentQuestionPage")
  public ResultVO<PageInfo<IntentQuestionDTO>> queryIntentQuestionPage(@RequestBody IntentQueryParams queryParams) {
    return ResultVO.success(intentQuestionService.queryIntentQuestionPage(queryParams));
  }

  @Operation(summary = "批量导入意图问句")
  @PostMapping("batchImportIntentQuestions")
  public ResultVO<IntentQuestionImportDTO> batchImportIntentQuestions(
    @Parameter(description = "Excel文件") @RequestPart("file") MultipartFile file,
    @Parameter(description = "租户ID") @RequestParam("tenantId") Long tenantId,
    @Parameter(description = "场景ID") @RequestParam("sceneId") Long sceneId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(sceneId, "场景 ID 不能为空");
    return intentQuestionService.batchImportIntentQuestions(file, tenantId, sceneId);
  }

  @Operation(summary = "查询意图机器人和场景列表", description = "后续作废")
  @GetMapping("queryIntentBotSceneList")
  public ResultVO<Object> queryIntentBotSceneList(@RequestParam(name = "tenantId") Long tenantId) {
    return ResultVO.success();
  }

  @Operation(summary = "测试问题匹配")
  @PostMapping("testMatch")
  public ResultVO<List<IntentionMatchItemWithSceneDTO>> testMatch(@RequestParam("tenantId") Long tenantId, @RequestParam("question") String question,
    @RequestParam(name = "scoreThreshold", required = false) Float scoreThreshold, @RequestParam("topNum") Integer topNum) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.hasLength(question, "问句不能为空");
    Assert.notNull(topNum, "返回数量不能为空");
    List<IntentionMatchItemWithSceneDTO> intentionMatchItems = intentEmbeddingService.topMatches(tenantId, null, question, scoreThreshold, topNum);
    return ResultVO.success(intentionMatchItems);
  }

  @Operation(summary = "重建向量库")
  @PostMapping("rebuild")
  public ResultVO<Void> rebuild(@RequestParam("tenantId") Long tenantId) {
    intentEmbeddingService.rebuild(tenantId);
    return ResultVO.success();
  }
}
