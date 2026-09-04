package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取标签详情响应实体
 * 迁移对应关系: Go语言entity.GetTagDetailResp
 * - 功能: 获取标签详情的响应数据
 * - 字段定义:
 * * TagKeys: []*TagKey - 标签键列表
 * * Total: int64 - 总数
 * * NextPageToken: string - 下一页令牌
 * <p>
 * Java实现说明:
 * - 对应Go的entity.GetTagDetailResp结构体
 * - 使用Java类定义，包含响应数据字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go切片类型 -> Java列表
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTagDetailResp {

  /**
   * 标签键列表
   * 迁移对应关系: Go语言entity.GetTagDetailResp.TagKeys ([]*TagKey)
   * - 功能: 查询到的标签键列表
   * - 类型: Go的切片类型对应Java的列表
   * - 用途: 返回查询结果
   */
  @JsonProperty("tag_keys")
  private List<TagKey> tagKeys;

  /**
   * 总数
   * 迁移对应关系: Go语言entity.GetTagDetailResp.Total (int64)
   * - 功能: 符合条件的总记录数
   * - 类型: Go的int64类型对应Java的Long类型
   * - 用途: 分页信息
   */
  @JsonProperty("total")
  private Long total;

  /**
   * 下一页令牌
   * 迁移对应关系: Go语言entity.GetTagDetailResp.NextPageToken (string)
   * - 功能: 下一页的令牌
   * - 类型: Go的string类型对应Java的String类型
   * - 用途: 游标分页
   */
  @JsonProperty("next_page_token")
  private String nextPageToken;
}
