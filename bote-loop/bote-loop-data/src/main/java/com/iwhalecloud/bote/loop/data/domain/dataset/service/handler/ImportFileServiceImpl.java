package com.iwhalecloud.bote.loop.data.domain.dataset.service.handler;

import com.iwhalecloud.bote.loop.data.domain.component.IUnionFS;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOJobProgress;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldMapping;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorType;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.impl.ItemServiceImpl;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 导入文件服务实现类
 * 迁移对应关系: Go语言import_file.go
 * - 功能: 实现文件导入相关的业务逻辑
 * - 方法实现: 各种文件导入操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的import_file.go文件
 * - 使用Repository层实现数据访问
 * - 提供文件导入管理功能
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Service
@RequiredArgsConstructor
public class ImportFileServiceImpl {
  private final IDatasetAPI repo;
  private final IUnionFS fsUnion;
  private final ItemServiceImpl itemService;

  /**
   * 创建导入处理器
   * 迁移对应关系: Go语言newImportHandler
   * - 功能: 创建导入处理器
   * - 参数: job - IO任务, ds - 数据集和模式
   * - 返回: 导入处理器
   * - 用途: 创建导入处理器
   */
  public ImportHandler newImportHandler(IOJob job, DatasetWithSchema ds) {
    // 按源字段分组
    Map<String, List<FieldMapping>> groupBySource = job.getFieldMappings().stream()
      .collect(Collectors.groupingBy(FieldMapping::getSource));

    // 构建字段映射
    Map<String, List<String>> fieldMapping = groupBySource.entrySet().stream()
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        entry -> entry.getValue().stream()
          .map(FieldMapping::getTarget)
          .distinct()
          .collect(Collectors.toList())
      ));

    // 创建当前处理单元
    ImportUnit currentUnit = newImportUnit(job);

    return new ImportHandler(job, fieldMapping, ds, fsUnion, repo, itemService, currentUnit);
  }

  /**
   * 创建导入单元
   * 迁移对应关系: Go语言newImportUnit
   * - 功能: 创建导入单元
   * - 参数: job - IO任务
   * - 返回: 导入单元
   * - 用途: 创建处理单元
   */
  public ImportUnit newImportUnit(IOJob job) {
    ImportUnit unit = new ImportUnit();
    unit.setStatus(job.getStatus());

    if (job.getProgress() != null) {
      unit.setPreProcessed(job.getProgress().getProcessed());
      if (job.getProgress().getSubProgresses() != null) {
        Map<String, DatasetIOJobProgress> progresses = job.getProgress().getSubProgresses().stream()
          .collect(Collectors.toMap(
            DatasetIOJobProgress::getName,
            prog -> prog
          ));
        unit.setProgresses(progresses);
      }
    }

    if (job.getErrors() != null) {
      Map<ItemErrorType, ItemErrorGroup> errors = job.getErrors().stream()
        .collect(Collectors.toMap(
          ItemErrorGroup::getType,
          error -> error
        ));
      unit.setErrors(errors);
    }

    return unit;
  }
}
