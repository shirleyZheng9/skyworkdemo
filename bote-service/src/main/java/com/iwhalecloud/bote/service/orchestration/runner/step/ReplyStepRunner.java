package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.event.QuestionsSseEvent;
import com.iwhalecloud.bote.common.sse.event.ReferencesSseEvent;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.sse.event.TextSseEvent;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.common.util.TemplateUtil;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.ContentTypeConfig;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.DownloadConfig;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.RelatedConfig;
import com.iwhalecloud.bote.dto.search.response.BochaSearchResponse.WebPageInfoGroup;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 回复步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class ReplyStepRunner extends AbstractStepRunner<ReplyStep> {
  @Override
  protected void doRun(SceneOrchestrationContext context, ReplyStep step) {
    // 微调输出内容格式配置
    processContentType(step);
    // 先于文本，输出内容格式
    String msgId = context.getReplyHandler().sentContentType(step.getContentTypeConfig());
    // 解析模板中的参数
    List<Object> templateFragments = TemplateUtil.parseTemplate(step.getMessageContent(), this::resolveTemplateParam);
    // 没有 SseInvoker 时直接拼接即可
    if (templateFragments.stream().noneMatch(f -> f instanceof SseInvoker)) {
      String replyContent = templateFragments.stream().map(String.class::cast).collect(Collectors.joining());
      context.getReplyHandler().replyText(ChatMessageType.TEXT, replyContent, step.getCode(), step.getName(), msgId);
      context.setStepOutput(step, ImmutableMap.of("text", replyContent));
      processDownload(context, step, msgId, replyContent);
      processRelated(context, step);
    }
    // 只有一个 SseInvoker 时直接调用
    else if (templateFragments.size() == 1) {
      SseInvoker invoker = (SseInvoker) templateFragments.getFirst();
      AnswerDTO answer = context.getReplyHandler().stream(invoker, step.getCode(), step.getName(), msgId);
      context.setStepOutput(step, answer.toMap());
      processDownload(context, step, msgId, answer.getText());
      processRelated(context, step);
    }
    // 处理固定文本与 SseInvoker 混合，或者有多个 SseInvoker 的情况
    else {
      SseInvoker invoker = new SseInvoker(new CompositeTemplateHandler(templateFragments));
      AnswerDTO answer = context.getReplyHandler().stream(invoker, step.getCode(), step.getName(), msgId);
      context.setStepOutput(step, answer.toMap());
      processDownload(context, step, msgId, answer.getText());
      processRelated(context, step);
    }
    // 标记文本对应的输出内容格式
    context.getReplyHandler().setContentType(msgId, step.getContentTypeConfig());
  }

  /**
   * 微调输出内容格式配置，兼容存量数据
   */
  private void processContentType(ReplyStep step) {
    ContentTypeConfig config = step.getContentTypeConfig();
    if (config == null) {
      config = new ContentTypeConfig();
      config.setContentType(StringUtils.isEmpty(step.getContentType()) ? BaseConsts.REPLY_CONTENT_TYPE_MARKDOWN : step.getContentType());
    }
    if (BaseConsts.REPLY_CONTENT_TYPE_PARAGRAPH.equals(config.getContentType())) {
      config.setGroup(StringUtils.isEmpty(config.getGroup()) ? "default" : config.getGroup());
      String sortby = config.getSortby();
      if (!StringUtils.isNumeric(sortby)) {
        Object value = SceneParamUtil.getParamValue(config.getSortby());
        if (value != null) {
          sortby = value.toString();
        }
      }
      config.setSortby(StringUtils.isNumeric(sortby) ? sortby : "1");
    }
    step.setContentTypeConfig(config);
  }

  /**
   * 处理文件下载
   */
  private void processDownload(SceneOrchestrationContext context, ReplyStep step, @Nullable String msgId, String replyContent) {
    // 没有消息 ID 时不处理
    if (StringUtils.isEmpty(msgId)) {
      return;
    }
    // 未启用下载时不处理
    DownloadConfig config = step.getDownload();
    if (config == null || !Boolean.TRUE.equals(config.getEnabled()) || StringUtils.isEmpty(config.getFileType())) {
      return;
    }

    // 下载内容
    String downloadContent;
    if (Boolean.TRUE.equals(config.getCustomEnabled())) {
      downloadContent = TemplateUtil.resolveTemplate(config.getCustomContent(), this::resolveTemplateParam);
    }
    else {
      downloadContent = replyContent;
    }

    // 发送消息
    context.getReplyHandler().download(context.getTenantId(), msgId, config.getFileType(), downloadContent);
  }

  /**
   * 处理关联配置
   */
  private void processRelated(SceneOrchestrationContext context, ReplyStep step) {
    RelatedConfig config = step.getRelated();
    if (config == null || StringUtils.isAllEmpty(config.getWebPageContent(), config.getFileContent())) {
      return;
    }
    if (StringUtils.isNotEmpty(config.getWebPageContent())) {
      String content = TemplateUtil.resolveTemplate(config.getWebPageContent(), this::resolveTemplateParam);
      List<WebPageInfoGroup> pageInfos = JsonUtil.parseJson(content, new TypeReference<List<WebPageInfoGroup>>() {
      });
      if (CollectionUtils.isNotEmpty(pageInfos)) {
        context.getReplyHandler().replyRelated(ChatMessageType.WEB_PAGE_INFO, pageInfos, step.getCode(), step.getName());
      }
    }
    if (StringUtils.isNotEmpty(config.getFileContent())) {
      String content = TemplateUtil.resolveTemplate(config.getFileContent(), this::resolveTemplateParam);
      List<FileInfoDTO> fileInfos = JsonUtil.parseJson(content, new TypeReference<List<FileInfoDTO>>() {
      });
      if (CollectionUtils.isNotEmpty(fileInfos)) {
        context.getReplyHandler().replyRelated(ChatMessageType.FILE_INFO, fileInfos, step.getCode(), step.getName());
      }
    }
  }

  /**
   * 模板处理器
   */
  @SuppressWarnings("ClassCanBeRecord")
  private static class CompositeTemplateHandler implements Consumer<Consumer<SseEvent>> {
    private static final Logger logger = LoggerFactory.getLogger(CompositeTemplateHandler.class);

    /** 模板片段列表 */
    private final List<Object> templateFragments;

    public CompositeTemplateHandler(List<Object> templateFragments) {
      this.templateFragments = templateFragments;
    }

    @Override
    public void accept(Consumer<SseEvent> partialHandler) {
      // 汇总参考文档、追问问题，在最后统一发送，以避免发送多次（前端不支持）
      // 总的参考文档列表
      List<ReferenceDocumentDTO> references = new ArrayList<>();
      // 总的追问问题列表
      List<String> questions = new ArrayList<>();
      // 当前片段的内容收集器
      StringBuilder fragmentContent = new StringBuilder();
      // 当前片段的参考文档
      AtomicReference<List<ReferenceDocumentDTO>> fragmentReferences = new AtomicReference<>();
      // 当前片段的追问问题
      AtomicReference<List<String>> fragmentQuestions = new AtomicReference<>();
      // 封装片段处理器，收集回复内容
      Consumer<SseEvent> finalPartialHandler = event -> {
        // 只有文本立即发送，其它事件先收集起来，最后再合并发送
        if (event instanceof TextSseEvent textSseEvent) {
          fragmentContent.append(textSseEvent.getText());
          partialHandler.accept(event);
        }
        else if (event instanceof ReferencesSseEvent referencesSseEvent) {
          fragmentReferences.set(referencesSseEvent.getReferences());
          references.addAll(referencesSseEvent.getReferences());
        }
        else if (event instanceof QuestionsSseEvent questionsSseEvent) {
          fragmentQuestions.set(questionsSseEvent.getQuestions());
          questions.addAll(questionsSseEvent.getQuestions());
        }
        else {
          partialHandler.accept(event);
        }
      };

      for (Object fragment : templateFragments) {
        // 文本片段直接返回
        if (fragment instanceof String) {
          partialHandler.accept(SseEvent.ofText((String) fragment));
          continue;
        }
        fragmentContent.setLength(0);
        fragmentReferences.set(null);
        fragmentQuestions.set(null);
        SseInvoker invoker = (SseInvoker) fragment;
        try {
          invoker.invoke(finalPartialHandler);
        }
        catch (BssException e) {
          handleException(finalPartialHandler, e, invoker, fragmentContent);
        }
        catch (Exception e) {
          handleException(finalPartialHandler, new BssException("处理回复片段失败: " + ExpUtil.getMsg(e), e), invoker, fragmentContent);
        }

        // 调用 SseInvoker 的完成回调，用于更新节点出参
        AnswerDTO answer = new AnswerDTO();
        answer.setText(fragmentContent.toString());
        answer.setReferences(fragmentReferences.get());
        answer.setQuestions(fragmentQuestions.get());
        invoker.finish(answer);
      }

      // 发送参考文档和追问问题
      if (!references.isEmpty()) {
        partialHandler.accept(SseEvent.ofReferences(references));
      }
      if (!questions.isEmpty()) {
        partialHandler.accept(SseEvent.ofQuestions(questions));
      }
    }

    /**
     * 处理异常
     */
    private void handleException(Consumer<SseEvent> partialHandler, BssException exception, SseInvoker invoker, StringBuilder fragmentContent) {
      // 未配置异常处理策略，直接退出
      Consumer<Throwable> errorCallback = invoker.getErrorCallback();
      if (errorCallback == null) {
        throw exception;
      }

      // 执行异常处理策略
      try {
        errorCallback.accept(exception);
      }
      catch (FallbackOutputException e) {
        String fallbackText = e.getFallbackText();
        fragmentContent.setLength(0);
        fragmentContent.append(fallbackText);
        partialHandler.accept(SseEvent.ofText(fallbackText));
        // 有设定内容时，不退出，继续执行
      }
      catch (BssException | ExecuteExceptionBranchException e) {
        throw e;
      }
      catch (Exception e) {
        logger.error("Failed to execute error callback", e);
        throw new BssException(e);
      }
    }
  }
}
