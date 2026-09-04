package com.iwhalecloud.bote.loop.data.domain.tag.service;

import com.iwhalecloud.bote.loop.data.domain.tag.entity.GetTagDetailReq;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.GetTagDetailResp;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagKeyParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKey;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagSpec;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagValue;
import com.iwhalecloud.bote.loop.data.domain.tag.service.dto.TagSearchResult;
import java.util.List;
import java.util.Map;

/**
 * 标签服务接口
 * 迁移对应关系: Go语言service.ITagService
 * - 功能: 提供标签相关的业务逻辑服务
 * - 方法定义: 各种标签操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的service.ITagService接口
 * - 使用Java接口定义，包含标签服务方法
 * - 提供标签CRUD和业务逻辑操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface TagService {

  /**
   * 创建标签
   * 迁移对应关系: Go语言service.ITagService.CreateTag
   * - 功能: 新建 tag key & tag value
   * - 参数: spaceId - 空间ID, tagKey - 标签键, options - 数据库选项
   * - 返回: 标签ID
   * - 用途: 创建新标签
   */
  Long createTag(Long spaceId, TagKey tagKey, Object... options);

  /**
   * 获取指定tagKeyID的所有版本
   * 迁移对应关系: Go语言service.ITagService.GetAllTagKeyVersionsByKeyID
   * - 功能: 获取指定tagKeyID的所有版本
   * - 参数: spaceId - 空间ID, tagKeyId - 标签键ID, options - 数据库选项
   * - 返回: 标签键版本列表
   * - 用途: 版本管理
   */
  List<TagKey> getAllTagKeyVersionsByKeyId(Long spaceId, Long tagKeyId, Object... options);

  /**
   * 获取指定tagKeyID和版本的选项，并建立树
   * 迁移对应关系: Go语言service.ITagService.GetAndBuildTagValues
   * - 功能: 获取指定tagKeyID和版本的选项，并建立树
   * - 参数: spaceId - 空间ID, tagKeyId - 标签键ID, versionNum - 版本号, options - 数据库选项
   * - 返回: 标签值树列表
   * - 用途: 构建标签值树结构
   */
  List<TagValue> getAndBuildTagValues(Long spaceId, Long tagKeyId, Integer versionNum, Object... options);

  /**
   * 获取最新的tagKey和tagValue，并建树
   * 迁移对应关系: Go语言service.ITagService.GetLatestTag
   * - 功能: 获取最新的tagKey和tagValue，并建树
   * - 参数: spaceId - 空间ID, tagKeyId - 标签键ID, options - 数据库选项
   * - 返回: 最新标签键
   * - 用途: 获取最新版本标签
   */
  TagKey getLatestTag(Long spaceId, Long tagKeyId, Object... options);

  /**
   * 同时更新 tag key 和 tag value，生成新版本
   * 迁移对应关系: Go语言service.ITagService.UpdateTag
   * - 功能: 同时更新 tag key 和 tag value，生成新版本
   * - 参数: spaceId - 空间ID, tagKeyId - 标签键ID, tagKey - 标签键, options - 数据库选项
   * - 用途: 更新标签并生成新版本
   */
  void updateTag(Long spaceId, Long tagKeyId, TagKey tagKey, Object... options);

  /**
   * 改 tag key 和 tag value 的状态, 不生成新版本
   * 迁移对应关系: Go语言service.ITagService.UpdateTagStatus
   * - 功能: 改 tag key 和 tag value 的状态, 不生成新版本
   * - 参数: spaceId - 空间ID, tagKeyId - 标签键ID, versionNum - 版本号, status - 状态, needLock - 需要锁, updatedInfo - 更新信息, options - 数据库选项
   * - 用途: 更新标签状态
   */
  void updateTagStatus(Long spaceId, Long tagKeyId, Integer versionNum, TagStatus status, Boolean needLock, Boolean updatedInfo, Object... options);

  /**
   * 改 tag key 和 tag value 的状态, 生成新版本
   * 迁移对应关系: Go语言service.ITagService.UpdateTagStatusWithNewVersion
   * - 功能: 改 tag key 和 tag value 的状态, 生成新版本
   * - 参数: spaceId - 空间ID, tagKeyId - 标签键ID, status - 状态
   * - 用途: 更新标签状态并生成新版本
   */
  void updateTagStatusWithNewVersion(Long spaceId, Long tagKeyId, TagStatus status);

  /**
   * 获取标签规格
   * 迁移对应关系: Go语言service.ITagService.GetTagSpec
   * - 功能: 获取标签规格
   * - 参数: spaceId - 空间ID
   * - 返回: 最大高度, 最大宽度, 最大总数
   * - 用途: 获取标签配置规格
   */
  TagSpec getTagSpec(Long spaceId);

  /**
   * 批量更新tag key状态
   * 迁移对应关系: Go语言service.ITagService.BatchUpdateTagStatus
   * - 功能: 批量更新tag key状态
   * - 参数: spaceId - 空间ID, tagKeyIds - 标签键ID列表, toStatus - 目标状态
   * - 返回: 更新结果映射
   * - 用途: 批量更新标签状态
   */
  Map<Long, String> batchUpdateTagStatus(Long spaceId, List<Long> tagKeyIds, TagStatus toStatus);

  /**
   * 搜索Tag
   * 迁移对应关系: Go语言service.ITagService.SearchTags
   * - 功能: 搜索Tag
   * - 参数: spaceId - 空间ID, param - 搜索参数
   * - 返回: 标签键列表, 分页结果
   * - 用途: 搜索标签
   */
  TagSearchResult searchTags(Long spaceId, MGetTagKeyParam param);

  /**
   * 获取Tag详情
   * 迁移对应关系: Go语言service.ITagService.GetTagDetail
   * - 功能: 获取Tag详情
   * - 参数: spaceId - 空间ID, param - 详情请求参数
   * - 返回: 标签详情响应
   * - 用途: 获取标签详细信息
   */
  GetTagDetailResp getTagDetail(Long spaceId, GetTagDetailReq param);

  /**
   * 通过tagKeyID获取tag key信息
   * 迁移对应关系: Go语言service.ITagService.BatchGetTagsByTagKeyIDs
   * - 功能: 通过tagKeyID获取tag key信息
   * - 参数: spaceId - 空间ID, tagKeyIds - 标签键ID列表
   * - 返回: 标签键列表
   * - 用途: 批量获取标签信息
   */
  List<TagKey> batchGetTagsByTagKeyIds(Long spaceId, List<Long> tagKeyIds);
}
