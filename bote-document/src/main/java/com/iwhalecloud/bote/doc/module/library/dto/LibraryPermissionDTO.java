package com.iwhalecloud.bote.doc.module.library.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.document.entity.LibraryPermissionEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档库权限
 *
 * @author yangran
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档库权限")
public class LibraryPermissionDTO extends LibraryPermissionEntity {
}
