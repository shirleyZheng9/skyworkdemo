package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询Prompt基础信息参数
 * 迁移对应关系: Go语言ListPromptBasicParam
 * - 功能: 分页查询Prompt基础信息的参数类
 * - 字段定义:
 * * spaceId: Long - 空间ID
 * * keyWord: String - 关键词
 * * createdBys: List<String> - 创建人列表
 * * committedOnly: Boolean - 是否只查询已提交的
 * * offset: Integer - 偏移量
 * * limit: Integer - 限制数量
 * * orderBy: Integer - 排序字段
 * * asc: Boolean - 是否升序
 * <p>
 * Java实现说明:
 * - 对应Go的ListPromptBasicParam结构体
 * - 使用Lombok注解简化代码
 * - 用于分页查询时的参数传递
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go []string -> Java List<String>
 * - Go bool -> Java Boolean
 * - Go int -> Java Integer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListPromptBasicParam {

  /**
   * 空间ID
   * 迁移对应关系: Go语言ListPromptBasicParam.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   */
  private Long spaceId;

  /**
   * 关键词
   * 迁移对应关系: Go语言ListPromptBasicParam.KeyWord
   * - 功能: 搜索关键词
   * - 类型: Go的string对应Java的String
   */
  private String keyWord;

  /**
   * 创建人列表
   * 迁移对应关系: Go语言ListPromptBasicParam.CreatedBys
   * - 功能: 创建人过滤条件
   * - 类型: Go的[]string对应Java的List<String>
   */
  private List<String> createdBys;

  /**
   * 是否只查询已提交的
   * 迁移对应关系: Go语言ListPromptBasicParam.CommittedOnly
   * - 功能: 是否只查询已提交的Prompt
   * - 类型: Go的bool对应Java的Boolean
   */
  private Boolean committedOnly;

  /**
   * 偏移量
   * 迁移对应关系: Go语言ListPromptBasicParam.Offset
   * - 功能: 分页偏移量
   * - 类型: Go的int对应Java的Integer
   */
  private Integer offset;

  /**
   * 限制数量
   * 迁移对应关系: Go语言ListPromptBasicParam.Limit
   * - 功能: 分页限制数量
   * - 类型: Go的int对应Java的Integer
   */
  private Integer limit;

  /**
   * 排序字段
   * 迁移对应关系: Go语言ListPromptBasicParam.OrderBy
   * - 功能: 排序字段标识
   * - 类型: Go的int对应Java的Integer
   */
  private Integer orderBy;

  /**
   * 是否升序
   * 迁移对应关系: Go语言ListPromptBasicParam.Asc
   * - 功能: 排序方向
   * - 类型: Go的bool对应Java的Boolean
   */
  private Boolean asc;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "Prompt类型")
  private String promptType;
}
