package com.iwhalecloud.bote.mapper.loop.prompt;

import com.iwhalecloud.bote.entity.loop.prompt.PromptBasicEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListPromptBasicParam;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * Prompt基础信息Mapper接口
 * 对应Go代码中的PromptBasic表操作
 */
@Mapper
public interface PromptBasicMapper {

  /**
   * 插入Prompt基础信息
   *
   * @param basicPO Prompt基础信息对象
   * @return 影响行数
   */
  int insert(PromptBasicEntity basicPO);

  /**
   * 根据ID和空间ID删除Prompt
   *
   * @param promptId Prompt ID
   * @param spaceId 空间ID
   * @return 影响行数
   */
  int deleteById(@Param("promptId") Long promptId, @Param("spaceId") Long spaceId);

  /**
   * 根据ID和空间ID查询Prompt
   *
   * @param promptId Prompt ID
   * @param spaceId 空间ID
   * @param lock 是否加锁
   * @return Prompt基础信息
   */
  PromptBasicEntity selectById(@Param("promptId") Long promptId, @Param("spaceId") Long spaceId, @Param("lock") boolean lock);

  /**
   * 根据空间ID和ID列表批量查询Prompt
   *
   * @param spaceId 空间ID
   * @param promptIds Prompt ID列表
   * @return Prompt基础信息列表
   */
  List<PromptBasicEntity> selectByIds(@Param("spaceId") Long spaceId, @Param("promptIds") List<Long> promptIds);

  /**
   * 根据PromptKey列表查询Prompt
   *
   * @param spaceId 空间ID
   * @param promptKeys PromptKey列表
   * @return Prompt基础信息列表
   */
  List<PromptBasicEntity> selectByPromptKeys(@Param("spaceId") Long spaceId,
                                             @Param("promptKeys") List<String> promptKeys);

  /**
   * 是否存在promptKey
   *
   * @param spaceId 空间ID
   * @param promptKey PromptKey
   * @return Prompt基础信息列表
   */
  boolean existsPromptKey(@Param("spaceId") Long spaceId,
                          @Param("promptKey") String promptKey);
  /**
   * 是否存在promptName
   *
   * @param spaceId 空间ID
   * @param promptName PromptName
   * @return Prompt基础信息列表
   */
  boolean existsPromptName(@Param("spaceId") Long spaceId,
                          @Param("promptName") String promptName);

  /**
   * 根据条件查询Prompt列表
   *
   * @param param 查询参数
   * @return Prompt基础信息列表
   */
  List<PromptBasicEntity> selectByCondition(ListPromptBasicParam param, RowBounds rowBounds);

  /**
   * 根据条件统计Prompt数量
   *
   * @param param 查询参数
   * @return 总数
   */
  long countByCondition(ListPromptBasicParam param);

  /**
   * 根据ID和空间ID更新Prompt
   *
   * @param promptId Prompt ID
   * @param spaceId 空间ID
   * @param updatePromptBasicPO 更新字段映射
   * @return 影响行数
   */
  int updateById(@Param("promptId") Long promptId,
                 @Param("spaceId") Long spaceId,
                 @Param("updatePromptBasicPO") PromptBasicEntity updatePromptBasicPO);
}
