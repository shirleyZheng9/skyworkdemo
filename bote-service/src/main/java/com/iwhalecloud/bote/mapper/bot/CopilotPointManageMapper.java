package com.iwhalecloud.bote.mapper.bot;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.bot.CopilotPointDTO;
import com.iwhalecloud.bote.dto.bot.query.PointQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 副驾指令管理
 *
 * @author chen.linfa
 * @since 2025-01-20
 */
public interface CopilotPointManageMapper {

  /**
   * 校验副驾指令的编码唯一性
   *
   * @param point 副驾指令
   * @return 结果
   */
  boolean existsPointCode(@Param("dto") CopilotPointDTO point);

  /**
   * 根据主键获取副驾指令
   */
  CopilotPointDTO getPoint(@Param("tenantId") Long tenantId, @Param("id") Long pointId);

  /**
   * 新增副驾指令
   *
   * @param point 副驾指令
   * @return 结果
   */
  int insertPoint(@Param("dto") CopilotPointDTO point);

  /**
   * 修改副驾指令
   *
   * @param point 副驾指令
   * @return 结果
   */
  int updatePoint(@Param("dto") CopilotPointDTO point);

  /**
   * 删除副驾指令
   */
  int deletePoint(@Param("tenantId") Long tenantId, @Param("pointId") Long pointId, @Param("updatorId") Long updatorId);

  /**
   * 获取副驾指令列表（分页）
   *
   * @param queryParams 查询条件
   * @return 副驾指令分页列表
   */
  Page<CopilotPointDTO> selectPointPage(@Param("query") PointQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取副驾指令列表
   *
   * @param queryParams 查询条件
   * @return 副驾指令列表
   */
  List<CopilotPointDTO> selectPointList(@Param("query") PointQueryParams queryParams);

  /**
   * 根据指令编码，获取副驾指令
   */
  CopilotPointDTO getPointByCode(@Param("tenantId") Long tenantId, @Param("pointCode") String pointCode);
}
