package com.iwhalecloud.bote.dto.knowledge.access.platform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库资源
 *
 * @author lxs
 * @since 2025/7/12
 */
@Data
@Schema(description = "知识库资源")
public class KnowledgeResourceDTO {
    @Schema(description = "wid")
    private String wid;

    @Schema(description = "助手wid")
    private String assistantWid;

    @Schema(description = "资源wid")
    private String resourceWid;

    @Schema(description = "关联时的资源版本ID")
    private String resourceVersionId;

    @Schema(description = "资源来源(01文档)")
    private String resourceFrom;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "资源类型(字典)")
    private String resourceType;

    @Schema(description = "资源大小")
    private Integer resourceSize;

    @Schema(description = "文件后缀名")
    private String fileSuffix;

    @Schema(description = "docchain文档ID")
    private String docId;

    @Schema(description = "创建人ID")
    private String createUserId;

    @Schema(description = "创建人姓名")
    private String createUserName;

    @Schema(description = "创建时间")
    private String createTime;
}
