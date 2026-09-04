package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取标签详情请求实体
 * 迁移对应关系: Go语言entity.GetTagDetailReq
 * - 功能: 获取标签详情的请求参数
 * - 字段定义:
 * * PageSize: int32 - 页面大小
 * * PageNum: int32 - 页码
 * * PageToken: string - 页面令牌
 * * TagKeyID: int64 - 标签键ID
 * * OrderBy: string - 排序字段
 * * IsAsc: bool - 是否升序
 * <p>
 * Java实现说明:
 * - 对应Go的entity.GetTagDetailReq结构体
 * - 使用Java类定义，包含请求参数字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go基本类型 -> Java基本类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTagDetailReq {

  /**
   * 页面大小
   * 迁移对应关系: Go语言entity.GetTagDetailReq.PageSize (int32)
   * - 功能: 每页返回的记录数
   * - 类型: Go的int32类型对应Java的Integer类型
   * - 用途: 控制分页大小
   */
  @JsonProperty("page_size")
  private Integer pageSize;

  /**
   * 页码
   * 迁移对应关系: Go语言entity.GetTagDetailReq.PageNum (int32)
   * - 功能: 当前页码
   * - 类型: Go的int32类型对应Java的Integer类型
   * - 用途: 分页定位
   */
  @JsonProperty("page_num")
  private Integer pageNum;

  /**
   * 页面令牌
   * 迁移对应关系: Go语言entity.GetTagDetailReq.PageToken (string)
   * - 功能: 分页令牌
   * - 类型: Go的string类型对应Java的String类型
   * - 用途: 游标分页
   */
  @JsonProperty("page_token")
  private String pageToken;

  /**
   * 标签键ID
   * 迁移对应关系: Go语言entity.GetTagDetailReq.TagKeyID (int64)
   * - 功能: 要查询的标签键ID
   * - 类型: Go的int64类型对应Java的Long类型
   * - 用途: 指定查询目标
   */
  @JsonProperty("tag_key_id")
  private Long tagKeyId;

  /**
   * 排序字段
   * 迁移对应关系: Go语言entity.GetTagDetailReq.OrderBy (string)
   * - 功能: 排序字段名
   * - 类型: Go的string类型对应Java的String类型
   * - 用途: 控制排序方式
   */
  @JsonProperty("order_by")
  private String orderBy;

  /**
   * 是否升序
   * 迁移对应关系: Go语言entity.GetTagDetailReq.IsAsc (bool)
   * - 功能: 排序方向标识
   * - 类型: Go的bool类型对应Java的Boolean类型
   * - 用途: 控制排序方向
   */
  @JsonProperty("is_asc")
  private Boolean isAsc;
}
