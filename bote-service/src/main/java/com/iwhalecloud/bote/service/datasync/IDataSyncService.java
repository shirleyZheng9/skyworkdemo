package com.iwhalecloud.bote.service.datasync;

import com.iwhalecloud.bote.dto.datasync.DataSyncGroupDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncNodeDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.query.CopyDataParams;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncQueryParams;
import com.iwhalecloud.bote.dto.datasync.query.ExportDataParams;
import com.iwhalecloud.bote.dto.datasync.query.ImportDataParams;
import com.iwhalecloud.bote.dto.datasync.query.OnlinePublishParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.io.File;
import java.util.List;

/**
 * 数据导出、导出服务
 *
 * @author chen.linfa
 * @since 2024-10-21
 */
public interface IDataSyncService {

  /**
   * 发布导出数据流程
   *
   * @param params 入参
   * @return 结果
   */
  ResultVO<Long> publishExport(ExportDataParams params);

  /**
   * 发布导出数据流程，可指定发布类型
   *
   * @param params 入参
   * @param publishType 发布类型
   * @return 结果
   */
  ResultVO<Long> publishExport(ExportDataParams params, String publishType);

  /**
   * 发布导入数据流程
   *
   * @param params 入参
   * @return 结果
   */
  ResultVO<Long> publishImport(ImportDataParams params);

  /**
   * 发布导入数据流程，可指定发布类型
   *
   * @param params 入参
   * @param publishType 发布类型
   * @return 结果
   */
  ResultVO<Long> publishImport(ImportDataParams params, String publishType);

  /**
   * 发布复制数据流程
   *
   * @param params 入参
   * @return 结果
   */
  ResultVO<Long> publishCopy(CopyDataParams params);

  /**
   * 在线发布数据流程
   *
   * @param params 入参
   * @return 结果
   */
  ResultVO<Long> publishOnline(OnlinePublishParams params);

  /**
   * 查询数据同步表定义列表
   *
   * @return 表定义列表
   */
  List<DataSyncTableDefinition> queryTableDefinition();

  /**
   * 解析导入的数据同步文件
   *
   * @param params 入参
   * @param tempFile 文件
   * @return 结果
   */
  ResultVO<Void> parseImportFile(DataSyncParams params, File tempFile);

  /**
   * 查询数据同步节点列表
   *
   * @param queryParams 条件
   * @return 数据同步列表
   */
  List<DataSyncGroupDTO> queryDataSyncNode(DataSyncQueryParams queryParams);

  /**
   * 查询选中项关联的配置，用于增量导出
   *
   * @param queryParams 条件
   * @return 关联的配置
   */
  ResultVO<List<DataSyncNodeDTO>> queryRelatedResource(DataSyncQueryParams queryParams);
}
