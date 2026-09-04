package com.iwhalecloud.bote.loop.data.domain.tag.service;

import com.iwhalecloud.bote.loop.data.domain.tag.entity.GetTagDetailReq;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.GetTagDetailResp;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagKeyParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKey;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagSpec;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagValue;
import java.util.List;
import java.util.Map;

/**
 * 标签服务接口
 * 迁移对应关系: Go语言ITagService
 * - 功能: 提供标签业务逻辑接口
 * - 方法定义: 各种标签业务操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的ITagService接口
 * - 使用Java接口定义，包含标签业务操作方法
 * - 提供标签业务逻辑处理
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface ITagService {

  /**
   * 新建标签键和标签值
   * 迁移对应关系: Go语言ITagService.CreateTag
   * - 功能: 新建标签键和标签值
   * - 参数: spaceID - 空间ID, val - 标签键对象
   * - 返回: 标签键ID
   * - 用途: 创建新的标签键和标签值
   */
  Long createTag(Long spaceID, TagKey val);

  /**
   * 获取指定标签键ID的所有版本
   * 迁移对应关系: Go语言ITagService.GetAllTagKeyVersionsByKeyID
   * - 功能: 获取指定标签键ID的所有版本
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID
   * - 返回: 标签键版本列表
   * - 用途: 获取标签键的所有版本信息
   */
  List<TagKey> getAllTagKeyVersionsByKeyID(Long spaceID, Long tagKeyID);

  /**
   * 获取指定标签键ID和版本的选项，并建立树
   * 迁移对应关系: Go语言ITagService.GetAndBuildTagValues
   * - 功能: 获取指定标签键ID和版本的选项，并建立树
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID, versionNum - 版本号
   * - 返回: 标签值列表
   * - 用途: 获取标签值并建立树形结构
   */
  List<TagValue> getAndBuildTagValues(Long spaceID, Long tagKeyID, Integer versionNum);

  /**
   * 获取最新的标签键和标签值，并建树
   * 迁移对应关系: Go语言ITagService.GetLatestTag
   * - 功能: 获取最新的标签键和标签值，并建树
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID
   * - 返回: 最新的标签键对象
   * - 用途: 获取最新的标签键和标签值
   */
  TagKey getLatestTag(Long spaceID, Long tagKeyID);

  /**
   * 同时更新标签键和标签值，生成新版本
   * 迁移对应关系: Go语言ITagService.UpdateTag
   * - 功能: 同时更新标签键和标签值，生成新版本
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID, val - 标签键对象
   * - 用途: 更新标签键和标签值并生成新版本
   */
  void updateTag(Long spaceID, Long tagKeyID, TagKey val);

  /**
   * 改标签键和标签值的状态，不生成新版本
   * 迁移对应关系: Go语言ITagService.UpdateTagStatus
   * - 功能: 改标签键和标签值的状态，不生成新版本
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID, versionNum - 版本号, status - 状态, needLock - 是否需要锁定, updatedInfo - 是否更新信息
   * - 用途: 更新标签状态但不生成新版本
   */
  void updateTagStatus(Long spaceID, Long tagKeyID, Integer versionNum, TagStatus status, Boolean needLock, Boolean updatedInfo);

  /**
   * 改标签键和标签值的状态，生成新版本
   * 迁移对应关系: Go语言ITagService.UpdateTagStatusWithNewVersion
   * - 功能: 改标签键和标签值的状态，生成新版本
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID, status - 状态
   * - 用途: 更新标签状态并生成新版本
   */
  void updateTagStatusWithNewVersion(Long spaceID, Long tagKeyID, TagStatus status);

  /**
   * 获取标签规格
   * 迁移对应关系: Go语言ITagService.GetTagSpec
   * - 功能: 获取标签规格
   * - 参数: spaceID - 空间ID
   * - 返回: 最大高度、最大宽度、最大总数
   * - 用途: 获取标签的规格信息
   */
  TagSpec getTagSpec(Long spaceID);

  /**
   * 批量更新标签键状态
   * 迁移对应关系: Go语言ITagService.BatchUpdateTagStatus
   * - 功能: 批量更新标签键状态
   * - 参数: spaceID - 空间ID, tagKeyIDs - 标签键ID列表, toStatus - 目标状态
   * - 返回: 标签键ID到错误信息的映射
   * - 用途: 批量更新标签键状态
   */
  Map<Long, String> batchUpdateTagStatus(Long spaceID, List<Long> tagKeyIDs, TagStatus toStatus);

  /**
   * 搜索标签
   * 迁移对应关系: Go语言ITagService.SearchTags
   * - 功能: 搜索标签
   * - 参数: spaceID - 空间ID, param - 搜索参数
   * - 返回: 标签键列表和分页结果
   * - 用途: 搜索标签并返回分页结果
   * - 注意: 需要分页标注，后续单独处理
   */
  List<TagKey> searchTags(Long spaceID, MGetTagKeyParam param);

  /**
   * 获取标签详情
   * 迁移对应关系: Go语言ITagService.GetTagDetail
   * - 功能: 获取标签详情
   * - 参数: spaceID - 空间ID, param - 获取详情请求参数
   * - 返回: 获取详情响应
   * - 用途: 获取标签的详细信息
   */
  GetTagDetailResp getTagDetail(Long spaceID, GetTagDetailReq param);

  /**
   * 通过标签键ID批量获取标签键信息
   * 迁移对应关系: Go语言ITagService.BatchGetTagsByTagKeyIDs
   * - 功能: 通过标签键ID批量获取标签键信息
   * - 参数: spaceID - 空间ID, tagKeyIDs - 标签键ID列表
   * - 返回: 标签键列表
   * - 用途: 批量获取标签键信息
   */
  List<TagKey> batchGetTagsByTagKeyIds(Long spaceID, List<Long> tagKeyIDs);
}
