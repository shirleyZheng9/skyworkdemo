package com.iwhalecloud.bote.dto.knowledge.access.platform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库基本信息
 *
 * @author lxs
 * @since 2025/7/12
 */
@Data
@Schema(description = "知识库基本信息")
public class KnowledgeBasicDTO {
    @Schema(description = "知识ID")
    private String wid;

    @Schema(description = "知识库名称")
    private String name;

    @Schema(description = "知识库缩略图")
    private String icon;

    @Schema(description = "知识库简介")
    private String summary;

    @Schema(description = "知识库提示词")
    private String callWord;

    @Schema(description = "Docchain主题ID")
    private String topicId;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "知识库绑定资源数量")
    private Integer subResourceCount;

}
