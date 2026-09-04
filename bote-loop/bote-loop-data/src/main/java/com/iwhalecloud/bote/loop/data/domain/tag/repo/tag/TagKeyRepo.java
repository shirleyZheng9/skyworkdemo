package com.iwhalecloud.bote.loop.data.domain.tag.repo.tag;

import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagKeyParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKey;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import java.util.List;

/**
 * 标签键仓库接口
 * 迁移对应关系: Go语言repo.ITagKeyRepo
 * - 功能: 提供标签键数据访问接口
 * - 方法定义: 各种标签键数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的repo.ITagKeyRepo接口
 * - 使用Java接口定义，包含标签键数据访问方法
 * - 提供标签键数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface TagKeyRepo {

  /**
   * 批量创建标签键
   * 迁移对应关系: Go语言repo.ITagKeyRepo.MCreateTagKeys
   * - 功能: 批量创建标签键
   * - 参数: tagKeys - 标签键列表, options - 数据库选项
   * - 用途: 批量创建标签键
   */
  void mCreateTagKeys(List<TagKey> tagKeys);

  /**
   * 获取标签键
   * 迁移对应关系: Go语言repo.ITagKeyRepo.GetTagKey
   * - 功能: 获取标签键
   * - 参数: spaceId - 空间ID, id - ID, options - 数据库选项
   * - 返回: 标签键
   * - 用途: 获取单个标签键
   */
  TagKey getTagKey(Long spaceId, Long id);

  /**
   * 批量获取标签键
   * 迁移对应关系: Go语言repo.ITagKeyRepo.MGetTagKeys
   * - 功能: 批量获取标签键
   * - 参数: param - 查询参数, options - 数据库选项
   * - 返回: 标签键列表, 分页结果
   * - 用途: 批量查询标签键
   */
  List<TagKey> mGetTagKeys(MGetTagKeyParam param);

  /**
   * 更新标签键
   * 迁移对应关系: Go语言repo.ITagKeyRepo.PatchTagKey
   * - 功能: 更新标签键
   * - 参数: spaceId - 空间ID, id - ID, patch - 更新数据, options - 数据库选项
   * - 用途: 更新标签键
   */
  void patchTagKey(Long spaceId, Long id, TagKey patch);

  /**
   * 删除标签键
   * 迁移对应关系: Go语言repo.ITagKeyRepo.DeleteTagKey
   * - 功能: 删除标签键
   * - 参数: spaceId - 空间ID, id - ID, options - 数据库选项
   * - 用途: 删除标签键
   */
  void deleteTagKey(Long spaceId, Long id);

  /**
   * 更新标签键状态
   * 迁移对应关系: Go语言repo.ITagKeyRepo.UpdateTagKeysStatus
   * - 功能: 更新标签键状态
   * - 参数: spaceId - 空间ID, tagKeyId - 标签键ID, versionNum - 版本号, toStatus - 目标状态, updateInfo - 更新信息, options - 数据库选项
   * - 用途: 更新标签键状态
   */
  void updateTagKeysStatus(Long spaceId, Long tagKeyId, Integer versionNum, TagStatus toStatus, Boolean updateInfo);

  /**
   * 统计标签键数量
   * 迁移对应关系: Go语言repo.ITagKeyRepo.CountTagKeys
   * - 功能: 统计标签键数量
   * - 参数: param - 查询参数, options - 数据库选项
   * - 返回: 数量
   * - 用途: 统计标签键数量
   */
  Long countTagKeys(MGetTagKeyParam param);
}
