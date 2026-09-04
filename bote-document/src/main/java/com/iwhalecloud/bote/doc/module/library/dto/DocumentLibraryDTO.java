package com.iwhalecloud.bote.doc.module.library.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.library.entity.DocumentLibraryEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档库")
public class DocumentLibraryDTO extends DocumentLibraryEntity {
}


