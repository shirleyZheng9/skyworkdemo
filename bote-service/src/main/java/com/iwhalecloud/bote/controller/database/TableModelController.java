package com.iwhalecloud.bote.controller.database;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.service.database.ITableModelService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 表模型控制器
 *
 * @author wangtingyun
 * @since  2025-11-28
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/tableModel", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "表模型管理")
public class TableModelController {

  private final ITableModelService tableModelService;

  @Operation(summary = "根据数据源获取表定义列表(不含表字段)")
  @GetMapping("queryTableListByDataSource")
  public ResultVO<List<DataTableDTO>> queryTableListByDataSource(@RequestParam("tenantId") Long tenantId,
                                                                 @RequestParam("dataSourceId") Long dataSourceId) {
    return ResultVO.success(tableModelService.getTableListByDataSource(tenantId, dataSourceId));
  }

  @Operation(summary = "根据表名获取单个表的定义信息")
  @GetMapping("queryTableByDataSource")
  public ResultVO<DataTableDTO> queryTableByDataSource(@RequestParam("tenantId") Long tenantId,
                                                       @RequestParam("dataSourceId") Long dataSourceId,
                                                       @RequestParam("tableCode") String tableCode) {
    return ResultVO.success(tableModelService.getTableByDataSource(tenantId, dataSourceId, tableCode));
  }

  @Operation(summary = "刷新数据源表结构列表缓存")
  @GetMapping("refreshDataSourceTableList")
  public ResultVO<List<DataTableDTO>> refreshAppDataSourceTableList(@RequestParam("tenantId") Long tenantId,
                                                                    @RequestParam("dataSourceId") Long dataSourceId) {
    tableModelService.refreshDataSourceTableList(tenantId, dataSourceId);
    return queryTableListByDataSource(tenantId, dataSourceId);
  }

}
