package com.iwhalecloud.bote.doc.module.document.dto.request;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 查询文件夹子元素
 *
 * @author linmengfan
 * @version 1.0
 * @since 2026-06-02
 */
@Getter
@Setter
@ToString
public class DocFolderSubsQueryParams extends PageParams {

    @NotBlank(message = "文档ID不能为空")
    @Schema(description = "文档ID")
    private String documentId;

    @Schema(description = "环境租户ID")
    private Long envTenantId;

    @Schema(description = "文档名称搜索关键字")
    private String keyword;

}
