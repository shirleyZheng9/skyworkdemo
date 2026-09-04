package com.iwhalecloud.bote.controller.database;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.DataTableCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.dto.database.DataSaveDTO;
import com.iwhalecloud.bote.dto.database.DataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.dto.database.SimpleDataTableDTO;
import com.iwhalecloud.bote.dto.database.query.DataTableQueryParams;
import com.iwhalecloud.bote.service.database.IDataTableService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * 数据表控制器
 *
 * @author wangtingyun
 * @since  2025-11-18
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/dataTable", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "业务数据表管理")
public class DataTableController {

  private final IDataTableService dataTableService;
  private final DataTableCache tableCache;

  @Operation(summary = "保存数据表")
  @PostMapping("saveDataTable")
  public ResultVO<DataTableDTO> saveDataTable(@RequestBody DataTableDTO tableDTO) {
    boolean isUpdate = tableDTO.getTableId() != null;
    ResultVO<DataTableDTO> resultVO = dataTableService.saveDataTable(tableDTO);
    if (isUpdate && resultVO.isSuccess()) {
      // 刷新表定义缓存
      tableCache.refreshTable(tableDTO.getTenantId(), tableDTO.getTableId());
    }
    return resultVO;
  }

  @Operation(summary = "批量导入保存数据表")
  @PostMapping("batchImportDataTable")
  public ResultVO<Void> batchImportDataTable(@RequestBody List<DataTableDTO> tableDTO) {
    dataTableService.batchImportDataTable(tableDTO);
    return ResultVO.success();
  }

  @Operation(summary = "校验表编码是否可用")
  @GetMapping("checkTableCode")
  public ResultVO<String> checkTableCode(@RequestParam("tenantId") Long tenantId, @RequestParam("tableCode") String tableCode) {
    dataTableService.checkTableCode(tenantId, tableCode);
    return ResultVO.success();
  }

  @Operation(summary = "查询数据表详情")
  @GetMapping("getDataTableDetail")
  public ResultVO<DataTableDTO> getDataTableDetail(@RequestParam("tableId") Long tableId, @RequestParam("tenantId") Long tenantId) {
    return ResultVO.success(dataTableService.getDataTable(tableId, tenantId));
  }

  @Operation(summary = "分页查询业务数据表")
  @PostMapping("getDataTablePage")
  public ResultVO<PageInfo<DataTableDTO>> getDataTablePage(@RequestBody DataTableQueryParams query) {
    return ResultVO.success(dataTableService.getDataTablePage(query));
  }

  @Operation(summary = "编辑数据表基本信息")
  @PostMapping("editDataTableInfo")
  public ResultVO<DataTableDTO> editDataTableInfo(@RequestBody DataTableDTO tableDTO) {
    ResultVO<DataTableDTO> resultVO = dataTableService.editDataTableInfo(tableDTO);
    if (resultVO.isSuccess()) {
      // 刷新表定义缓存
      tableCache.refreshTable(tableDTO.getTenantId(), tableDTO.getTableId());
    }
    return resultVO;
  }

  @Operation(summary = "删除数据表")
  @GetMapping("deleteDataTable")
  public ResultVO<Void> deleteDataTable(@RequestParam("tableId") Long tableId, @RequestParam("tenantId") Long tenantId) {
    ResultVO<Integer> delResult = dataTableService.deleteDataTable(tableId, tenantId);
    if (delResult.isSuccess() && delResult.getResultObject() > 0) {
      // 刷新表定义缓存
      tableCache.refreshTable(tenantId, tableId);
    }
    return ResultVO.success();
  }

  @Operation(summary = "检查是否开启平台数据库")
  @GetMapping("checkPlatformDatabaseEnabled")
  public ResultVO<Boolean> checkPlatformDatabaseEnabled() {
    return ResultVO.success(dataTableService.checkPlatformDatabaseEnabled());
  }

  @IgnoreSign
  @IgnoreSession
  @Operation(summary = "获取数据表图标")
  @GetMapping("getDataTableIcon")
  @RequestCacheable(sql = "SELECT updated_time FROM bt_data_table WHERE table_id = #{param1} AND tenant_id = #{param2} ", cacheOnNotFound = true)
  public void getDataTableIcon(@RequestParam("tableId") Long tableId, @RequestParam("tenantId") Long tenantId,
                               HttpServletResponse response) throws IOException {
    Assert.notNull(tableId, "数据表 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    String tableIcon = dataTableService.getDataTableIcon(tableId, tenantId);
    IconUtil.sendBase64Icon(response, tableIcon);
  }

  @Operation(summary = "获取系统预置的数据表字段")
  @GetMapping("getPlatformColumns")
  public ResultVO<List<DataTableColumnDTO>> getPlatformColumns() {
    return ResultVO.success(dataTableService.getPlatformColumns());
  }

  @Operation(summary = "分页查询数据表测试数据")
  @PostMapping("queryTestDataPage")
  public ResultVO<PageInfo<Map<String, Object>>> queryTestDataPage(@RequestBody DataTableQueryParams query) {
    return ResultVO.success(dataTableService.getTestDataPage(query));
  }

  @Operation(summary = "保存数据表测试数据")
  @PostMapping("saveTestData")
  public ResultVO<Void> saveTestData(@RequestBody DataSaveDTO dataSaveDTO) {
    dataTableService.saveTestData(dataSaveDTO);
    return ResultVO.success();
  }

  @Operation(summary = "分页查询表定义", description = "用于其他模块引用")
  @PostMapping("querySimpleDataTablePage")
  public ResultVO<PageInfo<SimpleDataTableDTO>> querySimpleDataTablePage(@RequestBody DataTableQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    return ResultVO.success(dataTableService.querySimpleDataTablePage(params));
  }

  @Operation(summary = "查询表字段列表信息")
  @GetMapping("queryTableColumns")
  public ResultVO<List<DataTableColumnDTO>> queryTableColumns(@RequestParam("tenantId") Long tenantId,
                                                              @RequestParam("tableId") Long tableId) {
    return ResultVO.success(dataTableService.getTableColumns(tenantId, tableId));
  }

  @Operation(summary = "查询数据表基本信息")
  @GetMapping("queryTableBasicInfo")
  public ResultVO<DataTableDTO> queryTableBasicInfo(@RequestParam("tenantId") Long tenantId,
                                                    @RequestParam("tableId") Long tableId) {
    return ResultVO.success(dataTableService.getTableBasicInfo(tenantId, tableId));
  }

}
