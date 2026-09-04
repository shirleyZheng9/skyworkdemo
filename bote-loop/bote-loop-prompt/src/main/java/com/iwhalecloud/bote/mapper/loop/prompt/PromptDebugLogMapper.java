package com.iwhalecloud.bote.mapper.loop.prompt;

import com.iwhalecloud.bote.entity.loop.prompt.PromptDebugLogEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListParam;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.RowBounds;

/**
 * 调试日志Mapper接口
 * 对应Go代码中的PromptDebugLog表操作
 */
@Mapper
public interface PromptDebugLogMapper {

  /**
   * 插入调试日志记录
   *
   * @param debugLog 调试日志对象
   * @return 影响行数
   */
  int insert(PromptDebugLogEntity debugLog);

  /**
   * 根据条件查询调试日志列表
   *
   * @param param 查询参数
   * @return 调试日志列表
   */
  List<PromptDebugLogEntity> selectByCondition(ListParam param, RowBounds rowBounds);
}
