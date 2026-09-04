package com.iwhalecloud.bote.dto.knowledge.access.platform.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识召回响应
 *
 * @author lxs
 * @since 2025/7/12
 */
@Getter
@Setter
@ToString
@Schema(description = "知识召回响应")
public class KnowledgeDocContentResponse {
    @Schema(description = "文档id")
    private String docId;

    @Schema(description = "文档内容格式")
    private String redFormat;

    @Schema(description = "文档内容")
    private String content;

}
