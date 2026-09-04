package com.iwhalecloud.bote.doc.module.library.dto;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 文档库分页查询参数
 *
 * @author auto
 * @since 2025-09-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "文档库分页查询参数")
public class LibraryPageQueryParam extends PageParams {
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "组织ID列表")
    private List<Long> orgIds;
    @Schema(description = "状态码")
    private String statusCd;
    @Schema(description = "排序字段")
    private String sortBy;
    @Schema(description = "排序顺序")
    private String sortOrder;
    @Schema(description = "关键词")
    private String keywordLike;
    @Schema(description = "是否超级管理员")
    private Boolean isSuperAdmin;
    @Schema(description = "企业空间ID")
    private Long spaceId;
}
