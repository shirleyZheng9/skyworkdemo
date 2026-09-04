package com.iwhalecloud.bote.dto.dashboard;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库文件大类数量查询结果。
 *
 * <p>该 DTO 只承接数据库聚合结果，不直接作为接口响应。Mapper 已经将文件扩展名归并为
 * document（文档）、spreadsheet（表格）、image（图片）、audioVideo（音视频）和 other（其他）五个页面大类，
 * Service 再负责补齐数据库中不存在的类别、计算占比以及保证占比合计为 100.00%。</p>
 *
 * @author zhengxueli
 * @since 2026-08-28
 */
@Getter
@Setter
@ToString
public class KnowledgeFileTypeCountDTO {

  /** 页面文件大类编码：document、spreadsheet、image、audioVideo 或 other。 */
  private String code;

  /** 当前租户有效知识库中属于该大类的有效文档数量。 */
  private Long count;
}
