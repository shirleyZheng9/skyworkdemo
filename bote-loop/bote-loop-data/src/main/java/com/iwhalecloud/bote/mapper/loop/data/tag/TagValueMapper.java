package com.iwhalecloud.bote.mapper.loop.data.tag;

import com.iwhalecloud.bote.entity.loop.data.tag.TagValueEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * TagValue MyBatis Mapper接口 - 使用PO层更新
 */

public interface TagValueMapper {

  /**
   * 根据spaceId和id查询
   */
  TagValueEntity selectBySpaceIdAndId(@Param("spaceId") Long spaceId, @Param("id") Long id);

  /**
   * 根据条件查询
   */
  List<TagValueEntity> selectByCondition(Map<String, Object> params);

  /**
   * 根据条件统计数量
   */
  Long countByCondition(Map<String, Object> params);

  /**
   * 批量插入
   */
  int batchInsert(@Param("list") List<TagValueEntity> tagValues);

  /**
   * 根据条件更新 - 使用PO层对象
   */
  int updateByCondition(@Param("spaceId") Long spaceId,
                        @Param("id") Long id,
                        @Param("updateEntity") TagValueEntity updateEntity);

  /**
   * 更新状态
   */
  int updateStatusByCondition(@Param("spaceId") Long spaceId,
                              @Param("tagKeyId") Long tagKeyId,
                              @Param("versionNum") Integer versionNum,
                              @Param("status") String status,
                              @Param("updatedAt") String updatedAt,
                              @Param("updatedBy") String updatedBy);

  /**
   * 根据spaceId和id删除
   */
  int deleteBySpaceIdAndId(@Param("spaceId") Long spaceId, @Param("id") Long id);
}
