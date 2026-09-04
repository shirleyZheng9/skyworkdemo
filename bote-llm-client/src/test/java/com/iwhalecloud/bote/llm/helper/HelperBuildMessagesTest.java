package com.iwhalecloud.bote.llm.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataType;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link ParamExtractorHelper} 与 {@link QuestionClassifierHelper} buildMessages 单元测试
 *
 * <p>覆盖纯消息构造方法（系统提示 + 用户输入 + 参数/分类结构 + 历史），无 LLM 调用</p>
 */
class HelperBuildMessagesTest {

  // ==================== ParamExtractorHelper.buildMessages ====================

  @Test
  void paramExtractor_buildMessages_returnsSystemAndUser() {
    JsonSchemaNode params = JsonSchemaNode.newObject()
        .addProperty("name", "人名", JsonSchemaDataType.STRING);

    List<Message> messages = ParamExtractorHelper.buildMessages("用户问题", null, params, null);

    assertThat(messages).isNotEmpty().hasSize(2);
    // 首条为 SystemMessage，content 含模板关键词
    assertThat(messages.get(0)).isInstanceOf(SystemMessage.class);
    String systemContent = ((SystemMessage) messages.get(0)).getContent();
    assertThat(systemContent).contains("extracting structured information");
    // 末条为 UserMessage，content 含输入文本
    assertThat(messages.get(1)).isInstanceOf(UserMessage.class);
    String userContent = (String) ((UserMessage) messages.get(1)).getContent();
    assertThat(userContent).contains("用户问题");
  }

  @Test
  void paramExtractor_buildMessages_withInstruction_andHistory() {
    JsonSchemaNode params = JsonSchemaNode.newObject()
        .addProperty("name", "人名", JsonSchemaDataType.STRING);
    List<Message> history = List.of(new UserMessage("历史问题"));

    List<Message> messages = ParamExtractorHelper.buildMessages("用户问题", "规则", params, history);

    assertThat(messages).isNotEmpty().hasSize(2);
    assertThat(messages.get(0)).isInstanceOf(SystemMessage.class);
    assertThat(messages.get(1)).isInstanceOf(UserMessage.class);
    // UserMessage 的 JSON 内容中含 instruction 与 history
    String userContent = (String) ((UserMessage) messages.get(1)).getContent();
    assertThat(userContent).contains("规则");
    assertThat(userContent).contains("历史问题");
  }

  @Test
  void paramExtractor_buildMessages_includesParamsSchema() {
    JsonSchemaNode params = JsonSchemaNode.newObject()
        .addProperty("name", "人名", JsonSchemaDataType.STRING);

    List<Message> messages = ParamExtractorHelper.buildMessages("用户问题", null, params, null);

    // 返回的消息中应含 params 结构描述（属性名 "name" 出现在 UserMessage 的 JSON 中）
    String userContent = (String) ((UserMessage) messages.get(1)).getContent();
    assertThat(userContent).contains("name");
    assertThat(userContent).contains("人名");
  }

  // ==================== QuestionClassifierHelper.buildMessages ====================

  @Test
  void questionClassifier_buildMessages_returnsSystemAndUser() {
    List<String> categories = List.of("A", "B");

    List<Message> messages = QuestionClassifierHelper.buildMessages("你好", null, categories, null);

    assertThat(messages).isNotEmpty().hasSize(2);
    // 首条为 SystemMessage，content 含模板关键词
    assertThat(messages.get(0)).isInstanceOf(SystemMessage.class);
    String systemContent = ((SystemMessage) messages.get(0)).getContent();
    assertThat(systemContent).contains("classification");
    // 末条为 UserMessage，content 含输入文本
    assertThat(messages.get(1)).isInstanceOf(UserMessage.class);
    String userContent = (String) ((UserMessage) messages.get(1)).getContent();
    assertThat(userContent).contains("你好");
  }

  @Test
  void questionClassifier_buildMessages_includesCategories() {
    List<String> categories = List.of("A", "B");

    List<Message> messages = QuestionClassifierHelper.buildMessages("你好", null, categories, null);

    // 系统模板含分类示例，UserMessage JSON 含传入的分类
    String userContent = (String) ((UserMessage) messages.get(1)).getContent();
    assertThat(userContent).contains("A");
    assertThat(userContent).contains("B");
  }

  @Test
  void questionClassifier_buildMessages_withInstruction() {
    List<String> categories = List.of("A", "B");

    List<Message> messages = QuestionClassifierHelper.buildMessages("你好", "只分两类", categories, null);

    // UserMessage JSON 中含 instruction
    String userContent = (String) ((UserMessage) messages.get(1)).getContent();
    assertThat(userContent).contains("只分两类");
  }

  @Test
  void questionClassifier_buildMessages_withHistory() {
    List<String> categories = List.of("A", "B");
    List<Message> history = List.of(new UserMessage("历史对话"));

    List<Message> messages = QuestionClassifierHelper.buildMessages("你好", null, categories, history);

    assertThat(messages).isNotEmpty().hasSize(2);
    // UserMessage JSON 中含历史消息内容
    String userContent = (String) ((UserMessage) messages.get(1)).getContent();
    assertThat(userContent).contains("历史对话");
  }
}
