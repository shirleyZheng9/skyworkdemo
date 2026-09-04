package com.iwhalecloud.bote.mapper.loop.prompt;

import com.iwhalecloud.bote.entity.loop.prompt.PromptDebugContextEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 调试上下文Mapper接口
 * 对应Go代码中的PromptDebugContext表操作
 */
@Mapper
public interface PromptDebugContextMapper {

  /**
   * 插入调试上下文记录
   *
   * @param debugContext 调试上下文对象
   * @return 影响行数
   */
  int insert(PromptDebugContextEntity debugContext);

  /**
   * 根据条件查询调试上下文
   *
   * @param promptId 提示词ID
   * @param userId 用户ID
   * @return 调试上下文列表
   */
  List<PromptDebugContextEntity> selectByCondition(@Param("promptId") Long promptId,
                                                   @Param("userId") String userId);

  /**
   * 根据ID更新调试上下文
   *
   * @param debugContext 调试上下文对象
   * @return 影响行数
   */
  int updateById(PromptDebugContextEntity debugContext);
}
