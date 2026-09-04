package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.ILLMProvider;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Content;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ContentType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunError;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorUsage;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.LLMCallParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Message;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ParseType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.PromptEvaluatorVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ReplyItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Scenario;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Tool;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ToolCallConfig;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ToolChoiceType;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorSourceService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.config.EvaluatorConfigService;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 评估器源提示服务实现类
 */
@Service
@RequiredArgsConstructor
public class EvaluatorSourcePromptServiceImpl implements EvaluatorSourceService {
  private static final Logger logger = LoggerFactory.getLogger(EvaluatorSourcePromptServiceImpl.class);
  private static final String TEMPLATE_START_TAG = "${";
  private static final String TEMPLATE_END_TAG = "}";
  private static final String CONTENT_SUFFIX = "\n<输出要求>\n" +
    "    最终输出必须是一个 json 对象，包含 reason 和 score 两个字段。必须严格按照下面的格式输出，不能包含其他任何多余字符，其中 score 必须是json number类型：\n" +
    "    {\n" +
    "      \"reason\":\"打分的理由\",\n" +
    "      \"score\":\"得分。得分范围从 0.0 到 1.0，1.0 表示完全满足评分标准，0.0 表示完全不满足评分标准\"\n" +
    "    }\n" +
    "    </输出要求>";

  private final ILLMProvider llmProvider;
  private final EvaluatorConfigService evaluatorConfigService;

  @Override
  public EvaluatorType evaluatorType() {
    return EvaluatorType.PROMPT;
  }

  @Override
  public EvaluatorRunResult run(Long spaceId, Evaluator evaluator, EvaluatorInputData input) {
    String traceId = null;
    try {
      // 验证基础信息
      evaluator.getEvaluatorVersion().validateBaseInfo();
      // 校验输入数据
      evaluator.getEvaluatorVersion().validateInput(input);
      // 渲染变量
      renderTemplate(evaluator.getPromptEvaluatorVersion(), input);
      // 执行评估逻辑
      String userIDInContext = SessionContext.getCurrentUserId();
      ReplyItem llmResp = chat(spaceId, evaluator.getPromptEvaluatorVersion(), userIDInContext);
      // 解析输出
      EvaluatorOutputData output = parseOutput(evaluator.getPromptEvaluatorVersion(), llmResp);
      return new EvaluatorRunResult(output, EvaluatorRunStatus.SUCCESS, traceId);

    }
    catch (Exception e) {
      logger.error("调用大模型失败：", e);
      EvaluatorOutputData errorOutput = new EvaluatorOutputData();
      errorOutput.setEvaluatorRunError(new EvaluatorRunError());
      errorOutput.getEvaluatorRunError().setCode(9999);
      errorOutput.getEvaluatorRunError().setMessage(e.getMessage());
      return new EvaluatorRunResult(errorOutput, EvaluatorRunStatus.FAIL, traceId);
    }
  }

  @Override
  public EvaluatorOutputData debug(Long workspaceId, Evaluator evaluator, EvaluatorInputData input) {
    EvaluatorRunResult result = run(workspaceId, evaluator, input);
    return result.getOutput();
  }

  @Override
  public void preHandle(Evaluator evaluator) {
    injectPromptTools(evaluator);
    injectParseType(evaluator);
  }

  private ReplyItem chat(Long spaceId, PromptEvaluatorVersion evaluatorVersion, String userIDInContext) {
    LLMCallParam llmCallParam = LLMCallParam.builder()
      .spaceId(spaceId)
      .evaluatorId(String.valueOf(evaluatorVersion.getEvaluatorId()))
      .userId(userIDInContext)
      .scenario(Scenario.EVALUATOR)
      .messages(evaluatorVersion.getMessageList())
      .modelConfig(evaluatorVersion.getModelConfig())
      .build();

    if (evaluatorVersion.getParseType() == ParseType.FUNCTION_CALL) {
      llmCallParam.setTools(evaluatorVersion.getTools());
      llmCallParam.setToolCallConfig(ToolCallConfig.builder()
        .toolChoice(ToolChoiceType.REQUIRED)
        .build());
    }
    List<Message> messageList = evaluatorVersion.getMessageList();
    llmCallParam.setMessages(messageList);
    return llmProvider.call(llmCallParam);
  }

  private EvaluatorOutputData parseOutput(PromptEvaluatorVersion evaluatorVersion, ReplyItem replyItem) {
    EvaluatorOutputData output = new EvaluatorOutputData();
    output.setEvaluatorResult(new EvaluatorResult());
    output.setEvaluatorUsage(new EvaluatorUsage());
    if (replyItem == null) {
      throw new BssException("LLM_OUTPUT_EMPTY", "resp is null");
    }
    String repairArgs;
    Object scoreFieldValue;
    try {
      repairArgs = getRepairArgs(evaluatorVersion, replyItem);
      // 提取score字段
      scoreFieldValue = extractFieldValue(repairArgs, "score");
    }
    catch (Exception e) {
      throw new BssException("提取评估数据失败", e);
    }

    if (scoreFieldValue instanceof Integer) {
      output.getEvaluatorResult().setScore(Double.valueOf((Integer) scoreFieldValue));
    }
    else if (scoreFieldValue instanceof Double) {
      output.getEvaluatorResult().setScore(Double.valueOf(scoreFieldValue + ""));
    }
    else {
      throw new BssException("INVALID_OUTPUT_FROM_MODEL", "score not float64");
    }
    // 提取reason字段
    Object reasonFieldValue = extractFieldValue(repairArgs, "reason");
    if (reasonFieldValue instanceof String) {
      output.getEvaluatorResult().setReasoning((String) reasonFieldValue);
    }
    else {
      throw new BssException("INVALID_OUTPUT_FROM_MODEL", "reason not string");
    }
    // 设置token使用情况
    if (replyItem.getTokenUsage() != null) {
      output.getEvaluatorUsage().setInputTokens(replyItem.getTokenUsage().getInputTokens());
      output.getEvaluatorUsage().setOutputTokens(replyItem.getTokenUsage().getOutputTokens());
    }
    return output;
  }

  private String getRepairArgs(PromptEvaluatorVersion evaluatorVersion, ReplyItem replyItem) {
    String repairArgs;
    if (evaluatorVersion.getParseType() == ParseType.CONTENT) {
      repairArgs = replyItem.getContent();
      // 兼容富文本格式
      if (repairArgs.startsWith("```json\n") && repairArgs.endsWith("\n```")) {
        repairArgs = repairArgs.replaceFirst("```json\n", "").replace("\n```", "");
      }
    }
    else {
      if (replyItem.getToolCalls() == null || replyItem.getToolCalls().isEmpty()) {
        throw new BssException("LLM_TOOL_CALL_FAIL", "tool call empty");
      }
      repairArgs = replyItem.getToolCalls().getFirst().getFunctionCall().getArguments();
    }
    return repairArgs;
  }

  private void renderTemplate(PromptEvaluatorVersion evaluatorVersion, EvaluatorInputData input) {

    try {
      // 设置输入
//      Map<String, Object> inputMap = new HashMap<>();
//      for (Map.Entry<String, Content> entry : input.getInputFields().entrySet()) {
//        String key = entry.getKey();
//        Content value = entry.getValue();
//        inputMap.put(key, value != null ? value.getText() : null);
//      }

      // 渲染模板
      for (Message message : evaluatorVersion.getMessageList()) {
        // 现阶段只支持text类型模板渲染
        if (message.getContent().getContentType() == ContentType.TEXT) {
          String renderedText = executeTemplate(message.getContent().getText(), input.getInputFields());
          message.getContent().setText(renderedText);
        }
      }

      // 添加后缀
      if (!evaluatorVersion.getMessageList().isEmpty()) {
        String firstMessageText = evaluatorVersion.getMessageList().get(0).getContent().getText();
        evaluatorVersion.getMessageList().get(0).getContent().setText(firstMessageText + evaluatorVersion.getPromptSuffix());
      }

      // 设置输出

    }
    catch (Exception e) {
      throw new BssException("渲染模板失败: " + e.getMessage(), e);
    }
  }

  private void injectPromptTools(Evaluator evaluator) {
    // 注入默认工具
    Map<String, Tool> evaluatorToolConf = evaluatorConfigService.getEvaluatorToolConf();
    Map<String, String> evaluatorToolMapping = evaluatorConfigService.getEvaluatorToolMapping();

    String toolKey = evaluatorToolMapping.getOrDefault(
      evaluator.getEvaluatorVersion().getPromptTemplateKey(),
      "default_evaluator_tool_key"
    );
    List<Tool> tools = Lists.newArrayList();
    Tool tool = evaluatorToolConf.get(toolKey);
    if (tool != null) {
      tools.add(tool);
    }

    evaluator.getEvaluatorVersion().setTools(tools);
  }

  private void injectParseType(Evaluator evaluator) {
    // 注入后缀
//    Map<String, String> suffixConf = evaluatorConfigService.getEvaluatorPromptSuffix();
    evaluator.getEvaluatorVersion().setPromptSuffix(CONTENT_SUFFIX);
    evaluator.getEvaluatorVersion().setParseType(ParseType.CONTENT);
  }

  private String executeTemplate(String template, Map<String, Content> inputFields) {
    StringWriter writer = new StringWriter();
    int startIndex = 0;

    while (true) {
      int startTagIndex = template.indexOf(TEMPLATE_START_TAG, startIndex);
      if (startTagIndex == -1) {
        writer.write(template.substring(startIndex));
        break;
      }

      int endTagIndex = template.indexOf(TEMPLATE_END_TAG, startTagIndex);
      if (endTagIndex == -1) {
        writer.write(template.substring(startIndex));
        break;
      }

      writer.write(template.substring(startIndex, startTagIndex));

      String tag = template.substring(startTagIndex + TEMPLATE_START_TAG.length(), endTagIndex);
      Content content = inputFields.get(tag);
      if (content != null && content.getText() != null) {
        writer.write(content.getText());
      }

      startIndex = endTagIndex + TEMPLATE_END_TAG.length();
    }

    return writer.toString();
  }

  private Object extractFieldValue(String dataJson, String fieldName) {
    Map<String, Object> dataMap = JsonUtil.parseJson(dataJson, new TypeReference<>() {
    });
    if (dataMap == null) {
      return null;
    }
    return dataMap.get(fieldName);
  }

}
