package com.iwhalecloud.bote.doc.module.person.dto.share;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 我共享的文档 DTO
 *
 * @author lizuyin
 * @since 2025-08-20
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "我共享的文档")
public class SharedWithMeDataDTO {
  /** 文档ID */
  private List<String> documentIds;
  /** 文档权限 */
  private Map<String, String> documentPermissions;
  /** 文档分享者信息 */
  private Map<Long, PortalUserDTO> sharerUserMap;
}


