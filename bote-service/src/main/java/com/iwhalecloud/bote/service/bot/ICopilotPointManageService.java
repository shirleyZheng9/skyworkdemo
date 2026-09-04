package com.iwhalecloud.bote.service.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.bot.CopilotPointDTO;
import com.iwhalecloud.bote.dto.bot.SimpleCopilotPointDTO;
import com.iwhalecloud.bote.dto.bot.query.CopilotPointParams;
import com.iwhalecloud.bote.dto.bot.query.PointQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 副驾指令管理服务
 *
 * @author chen.linfa
 * @since 2025-01-20
 */
public interface ICopilotPointManageService {
  /**
   * 查询单个副驾指令
   *
   * @param tenantId 租户 ID
   * @param pointId 主键
   * @return 副驾指令
   */
  CopilotPointDTO getPoint(Long tenantId, Long pointId);

  /**
   * 保存副驾指令
   *
   * @param point 副驾指令
   * @return 结果
   */
  ResultVO<CopilotPointDTO> savePoint(CopilotPointDTO point);

  /**
   * 删除副驾指令
   *
   * @param tenantId 租户 ID
   * @param pointId 主键
   * @return 结果
   */
  ResultVO<Void> deletePoint(Long tenantId, Long pointId);

  /**
   * 查询副驾指令列表（分页）
   *
   * @param queryParams 查询条件
   * @return 副驾指令分页列表
   */
  PageInfo<CopilotPointDTO> queryPointPage(PointQueryParams queryParams);

  /**
   * 查询副驾指令列表
   *
   * @param queryParams 查询条件
   * @return 副驾指令列表
   */
  List<CopilotPointDTO> queryPointList(PointQueryParams queryParams);

  /**
   * 副驾模式，根据指令编码，并返回组装后的指令
   *
   * @param params 副驾参数
   * @return 指令
   */
  ResultVO<SimpleCopilotPointDTO> auth(CopilotPointParams params);
}
