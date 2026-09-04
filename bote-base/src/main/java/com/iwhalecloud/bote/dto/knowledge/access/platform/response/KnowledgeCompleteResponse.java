package com.iwhalecloud.bote.dto.knowledge.access.platform.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 知识问答响应
 *
 * @author lxs
 * @since 2025/7/12
 */
@Data
@Schema(description = "知识问答响应")
public class KnowledgeCompleteResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private MessageUsage usage;
    private List<StreamChoice> choices;

    @Data
    public static class MessageUsage {
        private Integer prompt_tokens;
        private Integer total_tokens;
        private Integer completion_tokens;
    }

    @Data
    public static class StreamChoice {
        private Integer index;
        private String logprobs;
        private String finish_reason;
        private String stop_reason;
        private StreamChoiceDelta delta;
        private StreamChoiceDelta message;
    }

    @Data
    public static class StreamChoiceDelta {
        private String role;
        private String content;
        private List<ToolCalls> tool_calls;
    }

    @Data
    public static class ToolCalls {
        private String id;
        private String type;
        private ToolCallsFunction function;
    }

    @Data
    public static class ToolCallsFunction {
        private String name;
        private String arguments;
    }
}
