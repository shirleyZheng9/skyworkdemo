package com.iwhalecloud.bote.dto.beyond;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应会话结束事件对象
 *
 * @author bianjp
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString
public class BeyondChatEndEventDTO {
  /** 关联问题列表 */
  private List<String> relatedQuestions;
  /** 参考文档列表 */
  private List<BeyondReferenceDocumentDTO> relatedResources;
}
