package com.iwhalecloud.bote.doc.module.person.dto.share;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 共享目标构建结果 DTO
 *
 * @author lizuyin
 * @since 2025-08-20
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "共享目标构建结果")
public class ShareTargetResult {
  /** 共享目标列表 */
  private List<SharedTargetDTO> sharedTargets;
  /** 最新分享时间 */
  private Date latestShareTime;
}
