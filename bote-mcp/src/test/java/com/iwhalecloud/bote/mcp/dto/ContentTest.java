package com.iwhalecloud.bote.mcp.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.iwhalecloud.bote.mcp.dto.response.CallToolResult;
import com.cmsr.tiangong.util.JsonUtil;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link Content} 多态反序列化单元测试
 *
 * <p>复现并验证线上问题：MCP 服务端返回 content 类型为 resource_link 时，
 * 反序列化 {@link CallToolResult} 抛出 InvalidTypeIdException
 * （Could not resolve type id 'resource_link' as a subtype of Content）。</p>
 */
class ContentTest {

  private static CallToolResult parseResult(String contentJson) {
    String json = "{\"content\": [" + contentJson + "]}";
    return JsonUtil.convert(JsonUtil.readTree(json), CallToolResult.class);
  }

  @Test
  void deserialize_textContent() {
    CallToolResult result = parseResult("{\"type\": \"text\", \"text\": \"hello\"}");

    assertThat(result.getContent()).hasSize(1);
    Content content = result.getContent().getFirst();
    assertThat(content).isInstanceOf(TextContent.class);
    assertThat(content.getType()).isEqualTo("text");
    assertThat(((TextContent) content).getText()).isEqualTo("hello");
  }

  @Test
  void deserialize_imageContent() {
    CallToolResult result = parseResult("{\"type\": \"image\", \"data\": \"aGVsbG8=\", \"mimeType\": \"image/png\"}");

    Content content = result.getContent().getFirst();
    assertThat(content).isInstanceOf(ImageContent.class);
    assertThat(((ImageContent) content).getData()).isEqualTo("aGVsbG8=");
    assertThat(((ImageContent) content).getMimeType()).isEqualTo("image/png");
  }

  @Test
  void deserialize_audioContent() {
    CallToolResult result = parseResult("{\"type\": \"audio\", \"data\": \"aGVsbG8=\", \"mimeType\": \"audio/wav\"}");

    Content content = result.getContent().getFirst();
    assertThat(content).isInstanceOf(AudioContent.class);
    assertThat(content.getType()).isEqualTo("audio");
    assertThat(((AudioContent) content).getData()).isEqualTo("aGVsbG8=");
    assertThat(((AudioContent) content).getMimeType()).isEqualTo("audio/wav");
  }

  @Test
  void deserialize_embeddedTextResourceContent() {
    CallToolResult result = parseResult(
      "{\"type\": \"resource\", \"resource\": {\"uri\": \"file:///a.txt\", \"mimeType\": \"text/plain\", \"text\": \"content\"}}");

    Content content = result.getContent().getFirst();
    assertThat(content).isInstanceOf(EmbeddedResourceContent.class);
    assertThat(content.getType()).isEqualTo("resource");
    ResourceContent resource = ((EmbeddedResourceContent) content).getResource();
    assertThat(resource.getUri()).isEqualTo("file:///a.txt");
    assertThat(resource.getMimeType()).isEqualTo("text/plain");
    assertThat(resource.getText()).isEqualTo("content");
    assertThat(resource.getBlob()).isNull();
  }

  @Test
  void deserialize_embeddedBlobResourceContent() {
    CallToolResult result = parseResult(
      "{\"type\": \"resource\", \"resource\": {\"uri\": \"file:///a.png\", \"mimeType\": \"image/png\", \"blob\": \"aGVsbG8=\"}}");

    Content content = result.getContent().getFirst();
    assertThat(content).isInstanceOf(EmbeddedResourceContent.class);
    ResourceContent resource = ((EmbeddedResourceContent) content).getResource();
    assertThat(resource.getBlob()).isEqualTo("aGVsbG8=");
    assertThat(resource.getText()).isNull();
  }

  @Test
  void deserialize_resourceLinkContent() {
    // 与线上 dust-detector-mcp 的 upload_image 工具返回的报文结构一致
    CallToolResult result = parseResult(
      "{\"type\": \"resource_link\", \"uri\": \"detector-image://images/abc/image.png\", \"name\": \"uploaded-image\", "
        + "\"mimeType\": \"image/png\", \"size\": 1024}, {\"type\": \"text\", \"text\": \"{}\"}");

    assertThat(result.getContent()).hasSize(2);
    Content content = result.getContent().getFirst();
    assertThat(content).isInstanceOf(ResourceLinkContent.class);
    assertThat(content.getType()).isEqualTo("resource_link");
    ResourceLinkContent link = (ResourceLinkContent) content;
    assertThat(link.getUri()).isEqualTo("detector-image://images/abc/image.png");
    assertThat(link.getName()).isEqualTo("uploaded-image");
    assertThat(link.getMimeType()).isEqualTo("image/png");
    assertThat(link.getSize()).isEqualTo(1024L);
  }

  @Test
  void deserialize_unknownContentType_fallbackToUnknownContent() {
    CallToolResult result = parseResult("{\"type\": \"future_type\", \"foo\": \"bar\", \"num\": 1}");

    Content content = result.getContent().getFirst();
    assertThat(content).isInstanceOf(UnknownContent.class);
    assertThat(content.getType()).isEqualTo("future_type");
    UnknownContent unknown = (UnknownContent) content;
    assertThat(unknown.getFields()).containsEntry("foo", "bar");
    assertThat(unknown.getFields()).containsEntry("num", 1);
  }

  @Test
  void deserialize_missingType_fallbackToUnknownContent() {
    CallToolResult result = parseResult("{\"text\": \"hello\"}");

    Content content = result.getContent().getFirst();
    assertThat(content).isInstanceOf(UnknownContent.class);
    assertThat(((UnknownContent) content).getFields()).containsEntry("text", "hello");
  }

  @Test
  void serialize_includesTypeProperty() {
    ResourceLinkContent link = new ResourceLinkContent();
    link.setUri("detector-image://images/abc/image.png");
    link.setName("uploaded-image");
    link.setMimeType("image/png");
    link.setSize(1024L);
    CallToolResult result = new CallToolResult();
    result.setContent(List.of(link));

    String json = JsonUtil.toJsonString(result);

    assertThat(json).contains("\"type\":\"resource_link\"");
    assertThat(json).contains("\"uri\":\"detector-image://images/abc/image.png\"");
    assertThat(json).contains("\"size\":1024");
  }

  @Test
  void serialize_unknownContent_preservesRawFields() {
    CallToolResult result = parseResult("{\"type\": \"future_type\", \"foo\": \"bar\"}");

    String json = JsonUtil.toJsonString(result);

    assertThat(json).contains("\"type\":\"future_type\"");
    assertThat(json).contains("\"foo\":\"bar\"");
  }
}
