package com.iwhalecloud.bote.mapper.skill;

import com.iwhalecloud.bote.dto.scene.SimpleDslInfoDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;

/**
 * 流程查询相关数据库操作
 *
 * @author bianjp
 * @since 2024-09-19
 */
public interface FlowQueryMapper {

  /**
   * 根据流程 ID 查询 DSL 信息
   */
  @Nullable
  SimpleDslInfoDTO selectDslByFlowId(@Param("tenantId") Long tenantId, @Param("flowId") Long flowId);

  /**
   * 批量查询 DSL 信息
   */
  List<SimpleDslInfoDTO> selectDslByFlowIds(@Param("tenantId") Long tenantId, @Param("flowIds") List<Long> flowIds);

}
