package com.iwhalecloud.bote.dto.knowledge.access.platform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识中台文档资源 DTO
 * @author bote
 */
@Data
@Schema(description = "知识中台文档资源")
public class DocResourceDTO {

    @Schema(description = "RESOURCE:个人资源、KNOW_BASE_RESOURCE:知识库资源")
    private String resourceType;

    @Schema(description = "资源wid，传知识库ID或文档ID")
    private String resourceWid;
}
