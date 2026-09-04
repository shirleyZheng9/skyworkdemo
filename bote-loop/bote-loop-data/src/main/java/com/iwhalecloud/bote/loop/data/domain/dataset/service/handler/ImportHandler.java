package com.iwhalecloud.bote.loop.data.domain.dataset.service.handler;

import com.iwhalecloud.bote.loop.data.domain.component.IFileReader;
import com.iwhalecloud.bote.loop.data.domain.component.IUnionFS;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOFile;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOJobProgress;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldData;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.DeltaDatasetIOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.IndexedItem;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.MAddItemOpt;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.impl.ItemServiceImpl;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.util.ItemUtils;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 导入处理器
 * 迁移对应关系: Go语言importHandler
 * - 功能: 处理文件导入的核心逻辑
 * - 方法实现: 各种导入处理方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的importHandler结构体
 * - 使用Component注解标记为Spring组件
 * - 提供导入处理功能
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go切片 -> Java List
 * - Go映射 -> Java Map
 * - Go错误处理 -> Java异常处理
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ImportHandler {
  private static final Logger logger = LoggerFactory.getLogger(ImportHandler.class);

  private IOJob job;
  private Map<String, List<String>> fieldMapping;
  private DatasetWithSchema ds;
  private IUnionFS fsUnion;
  private IDatasetAPI repo;
  private ItemServiceImpl itemService;
  private ImportUnit currentUnit;

  private static final int BULK_SIZE = 100;

  /**
   * 处理导入
   * 迁移对应关系: Go语言Handle
   * - 功能: 处理导入任务
   * - 用途: 执行导入任务
   */
  public void handle() {
    try {
      run();
    }
    catch (Exception e) {
      logger.error("import handler failed, job_id={}", job.getId(), e);
      throw new BssException("import handler failed", e);
    }
  }

  /**
   * 运行导入
   * 迁移对应关系: Go语言run
   * - 功能: 运行导入任务
   * - 用途: 执行导入流程
   */
  private void run() {
    ImportWorkspace workspace = newImportWorkspace(job, fsUnion);

    boolean started = startJob();
    if (!started) {
      return;
    }

    while (!isJobTerminal(currentUnit.getStatus())) {
      IFileReader fr = nextFile(workspace);
      if (fr == null) {
        break;
      }
      importFile(workspace, fr);

      if (currentUnit.getStatus() == JobStatus.COMPLETED && !currentUnit.getErrors().isEmpty()) {
        // 若任务完成时包含错误，需要扫描剩下的文件内容更新总行数
        scanFileWithoutSave(workspace, fr);
      }
    }
  }

  /**
   * 启动任务
   * 迁移对应关系: Go语言startJob
   * - 功能: 启动导入任务
   * - 返回: 是否启动成功
   * - 用途: 启动任务并处理清空数据集
   */
  private boolean startJob() {
    if (currentUnit.getStatus() == JobStatus.RUNNING) {
      return true;
    }

    currentUnit.setStartedAt(java.time.LocalDateTime.now());
    currentUnit.setStatus(JobStatus.RUNNING);

    boolean overwrite = job.getOption() != null && job.getOption().getOverwriteDataset();
    if (!overwrite) {
      return true;
    }

    // 清空数据集
    try {
      itemService.clearDataset(ds);
      logger.info("clear dataset, job_id={}, dataset_id={}", job.getId(), ds.getDataset().getId());
    }
    catch (Exception e) {
      logger.error("clear dataset failed, job_id={}, dataset_id={}, err={}",
        job.getId(), ds.getDataset().getId(), e.getMessage());
      endJobWithError(e.getMessage());
      return false;
    }

    return true;
  }

  /**
   * 获取下一个文件
   * 迁移对应关系: Go语言nextFile
   * - 功能: 获取下一个要处理的文件
   * - 参数: workspace - 工作空间
   * - 返回: 文件读取器
   * - 用途: 文件遍历
   */
  private IFileReader nextFile(ImportWorkspace workspace) {
    try {
      return workspace.nextFile();
    }
    catch (Exception e) {
      currentUnit.onInternalErr(e, e.getMessage());
      saveCurrentUnit();
      return null;
    }
  }

  /**
   * 导入文件
   * 迁移对应关系: Go语言importFile
   * - 功能: 导入文件内容
   * - 参数: workspace - 工作空间, fr - 文件读取器
   * - 用途: 处理文件内容
   */
  private void importFile(ImportWorkspace workspace, IFileReader fr) {
    ImportUnit unit = currentUnit;
    unit.setFilename(fr.getName());
    long lastCursor = fr.getCursor();

    while (true) {
      Map<String, Object> kv;
      try {
        kv = fr.next();
        if (kv == null) {
          break;
        }
      }
      catch (IOException e) {
        if (e.getMessage().contains("EOF")) {
          break;
        }
        unit.onInternalErr(e, "read next item from file failed");
        break;
      }

      unit.getItems().add(IndexedItem.builder()
        .item(kv2Item(kv))
        .index((int) fr.getCursor())
        .build());

      if (unit.getItems().size() < BULK_SIZE) {
        continue;
      }

      unit.setProcessed(fr.getCursor() - lastCursor);
      lastCursor = fr.getCursor();
      saveCurrentUnit();

      if (unit.getStatus() != JobStatus.RUNNING) {
        break;
      }
    }

    unit.setProcessed(fr.getCursor() - lastCursor);
    if (workspace.noMoreFile()) {
      unit.setStatus(JobStatus.COMPLETED);
      unit.setTotal(unit.getProcessed() + unit.getPreProcessed());
    }
    saveCurrentUnit();
  }

  /**
   * 键值对转换为项目
   * 迁移对应关系: Go语言kv2Item
   * - 功能: 将键值对转换为项目对象
   * - 参数: kv - 键值对
   * - 返回: 项目对象
   * - 用途: 数据转换
   */
  private Item kv2Item(Map<String, Object> kv) {
    Item item = Item.builder()
      .data(new ArrayList<>())
      .build();

    for (Map.Entry<String, Object> entry : kv.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      List<String> names = fieldMapping.get(key);
      if (names == null) {
        continue;
      }

      for (String name : names) {
        item.getData().add(FieldData.builder()
          .name(name)
          .content(String.valueOf(value))
          .build());
      }
    }

    return item;
  }

  /**
   * 保存当前处理单元
   * 迁移对应关系: Go语言saveCurrentUnit
   * - 功能: 保存当前处理单元的状态
   * - 用途: 更新进度和状态
   */
  private void saveCurrentUnit() {
    ImportUnit unit = currentUnit;
    if (unit.isEmpty()) {
      logger.info("nothing to save");
      return;
    }

    try {
      // 保存项目
      List<Item> items = unit.getItems().stream()
        .map(IndexedItem::getItem)
        .toList();

      ItemUtils.sanitizeInputItem(ds, items.toArray(new Item[0]));
      ItemUtils.ValidationResult result = ItemUtils.validateIndexedItems(ds, unit.getItems());
      unit.onBadItems(result.bad().toArray(new ItemErrorGroup[0]));

      List<IndexedItem> added = itemService.batchCreateItems(ds, result.good(),
        MAddItemOpt.builder().partialAdd(true).build());

      unit.setAdded((long) added.size());
      if (added.size() < result.good().size()) {
        logger.info("batch create items got dataset full, job will be ended, job_id={}, dataset_id={}, added={}, good={}",
          job.getId(), ds.getDataset().getId(), added.size(), result.good().size());
        unit.onDatasetFull();
      }

      // 保存任务进度
      DeltaDatasetIOJob delta = unit.toDeltaDatasetIOJob();
      repo.updateIOJob(job.getId(), delta);

      logger.info("import unit saved, processed_item={}, added={}, pre_processed={}",
        unit.getProcessed(), added.size(), unit.getPreProcessed());
    }
    catch (Exception e) {
      unit.onInternalErr(e, "batch create items failed");
      logger.error("save current unit failed", e);
    }
    finally {
      unit.onFlush();
    }
  }

  /**
   * 结束任务并记录错误
   * 迁移对应关系: Go语言endJobWithError
   * - 功能: 结束任务并记录错误
   * - 参数: errMsg - 错误消息
   * - 用途: 错误处理
   */
  private void endJobWithError(String errMsg) {
    DeltaDatasetIOJob delta = DeltaDatasetIOJob.builder()
      .status(JobStatus.FAILED.toString())
      .errors(List.of(ItemErrorGroup.builder()
        .type(ItemErrorType.INTERNAL_ERROR)
        .summary(errMsg)
        .build()))
      .build();
    repo.updateIOJob(job.getId(), delta);
  }

  /**
   * 扫描文件但不保存
   * 迁移对应关系: Go语言scanFileWithoutSave
   * - 功能: 仅更新文件的总行数，不写入项目
   * - 参数: workspace - 工作空间, fr - 文件读取器
   * - 用途: 更新总行数
   */
  private void scanFileWithoutSave(ImportWorkspace workspace, IFileReader fr) {
    ImportUnit unit = currentUnit;
    while (true) {
      unit.setFilename(fr.getName());
      long lastCursor = fr.getCursor();

      while (true) {
        try {
          Map<String, Object> kv = fr.next();
          if (kv == null) {
            break;
          }
        }
        catch (IOException e) {
          if (e.getMessage().contains("EOF")) {
            break;
          }
          logger.warn("get next failed, err={}", e.getMessage());
          break;
        }
      }

      unit.setProcessed(unit.getProcessed() + fr.getCursor() - lastCursor);
      if (unit.getProgresses().containsKey(unit.getFilename())) {
        DatasetIOJobProgress prog = unit.getProgresses().get(unit.getFilename());
        prog.setTotal(fr.getCursor());
        prog.setProcessed(fr.getCursor());
      }

      if (workspace.noMoreFile()) {
        break;
      }

      IFileReader nextFR = nextFile(workspace);
      if (nextFR == null) {
        return;
      }
      fr = nextFR;
    }

    DeltaDatasetIOJob delta = DeltaDatasetIOJob.builder()
      .total(unit.getProcessed() + unit.getPreProcessed())
      .deltaProcessed(unit.getProcessed())
      .subProgresses(new ArrayList<>(unit.getProgresses().values()))
      .build();
    repo.updateIOJob(job.getId(), delta);
  }

  /**
   * 检查任务是否已结束
   * 迁移对应关系: Go语言IsJobTerminal
   * - 功能: 检查任务是否已结束
   * - 参数: status - 任务状态
   * - 返回: 是否已结束
   * - 用途: 任务状态检查
   */
  private boolean isJobTerminal(JobStatus status) {
    return status == JobStatus.COMPLETED || status == JobStatus.FAILED || status == JobStatus.CANCELLED;
  }

  /**
   * 创建导入工作空间
   * 迁移对应关系: Go语言newImportWorkspace
   * - 功能: 创建导入工作空间
   * - 参数: job - IO任务, fs - 文件系统
   * - 返回: 导入工作空间
   * - 用途: 创建工作空间
   */
  private ImportWorkspace newImportWorkspace(IOJob job, IUnionFS fs) {
    DatasetIOFile source = job.getSource() != null ? job.getSource().getFile() : null;
    if (source == null) {
      throw new BssException("source file is null");
    }

    List<DatasetIOJobProgress> subProgresses = job.getProgress() != null ?
      job.getProgress().getSubProgresses() : new ArrayList<>();

    ImportWorkspace workspace = new ImportWorkspace();
    workspace.setSource(source);
    workspace.setFs(fs);
    workspace.setFiles(List.of(source.getPath()));

    if (subProgresses != null) {
      Map<String, DatasetIOJobProgress> progress = subProgresses.stream()
        .collect(Collectors.toMap(
          DatasetIOJobProgress::getName,
          prog -> prog
        ));
      workspace.setProgress(progress);
    }

    return workspace;
  }
}
