package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import lombok.Data;
import java.util.Date;

/**
 * 文档访问记录
 *
 * @author system
 * @since 2025/01/27
 */
@Data
public class DocumentActivityDTO {
  private String documentId;
  private Long userId;
  private String actionType;
  private Date createdTime;
  private String username;
}
