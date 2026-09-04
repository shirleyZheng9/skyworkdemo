package com.iwhalecloud.bote.loop.data.domain.tag.repo.tag;

import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagValueParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagValue;
import java.util.List;

/**
 * 标签值仓库接口
 * 迁移对应关系: Go语言repo.ITagValueRepo
 * - 功能: 提供标签值数据访问接口
 * - 方法定义: 各种标签值数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的repo.ITagValueRepo接口
 * - 使用Java接口定义，包含标签值数据访问方法
 * - 提供标签值数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface TagValueRepo {

  /**
   * 批量创建标签值
   * 迁移对应关系: Go语言repo.ITagValueRepo.MCreateTagValues
   * - 功能: 批量创建标签值
   * - 参数: tagValues - 标签值列表, options - 数据库选项
   * - 用途: 批量创建标签值
   */
  void mCreateTagValues(List<TagValue> tagValues);

  /**
   * 获取标签值
   * 迁移对应关系: Go语言repo.ITagValueRepo.GetTagValue
   * - 功能: 获取标签值
   * - 参数: spaceId - 空间ID, id - ID, options - 数据库选项
   * - 返回: 标签值
   * - 用途: 获取单个标签值
   */
  TagValue getTagValue(Long spaceId, Long id);

  /**
   * 批量获取标签值
   * 迁移对应关系: Go语言repo.ITagValueRepo.MGetTagValue
   * - 功能: 批量获取标签值
   * - 参数: param - 查询参数, options - 数据库选项
   * - 返回: 标签值列表, 分页结果
   * - 用途: 批量查询标签值
   */
  List<TagValue> mGetTagValue(MGetTagValueParam param);

  /**
   * 更新标签值
   * 迁移对应关系: Go语言repo.ITagValueRepo.PatchTagValue
   * - 功能: 更新标签值
   * - 参数: spaceId - 空间ID, id - ID, patch - 更新数据, options - 数据库选项
   * - 用途: 更新标签值
   */
  void patchTagValue(Long spaceId, Long id, TagValue patch);

  /**
   * 删除标签值
   * 迁移对应关系: Go语言repo.ITagValueRepo.DeleteTagValue
   * - 功能: 删除标签值
   * - 参数: spaceId - 空间ID, id - ID, options - 数据库选项
   * - 用途: 删除标签值
   */
  void deleteTagValue(Long spaceId, Long id);

  /**
   * 更新标签值状态
   * 迁移对应关系: Go语言repo.ITagValueRepo.UpdateTagValuesStatus
   * - 功能: 更新标签值状态
   * - 参数: spaceId - 空间ID, tagKeyId - 标签键ID, versionNum - 版本号, toStatus - 目标状态, updateInfo - 更新信息, options - 数据库选项
   * - 用途: 更新标签值状态
   */
  void updateTagValuesStatus(Long spaceId, Long tagKeyId, Integer versionNum, TagStatus toStatus, Boolean updateInfo);
}
