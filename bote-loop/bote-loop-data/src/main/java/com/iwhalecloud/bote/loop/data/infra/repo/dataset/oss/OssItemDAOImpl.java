package com.iwhalecloud.bote.loop.data.infra.repo.dataset.oss;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.entity.Provider;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.oss.model.ItemDataPO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * 数据集项目DAO实现类
 * 迁移对应关系: Go语言ItemDAOImpl
 * - 功能: 实现数据集项目数据访问操作
 * - 方法实现: 各种数据集项目数据操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的ItemDAOImpl结构体
 * - 使用文件存储服务实现数据访问
 * - 提供数据集项目数据存储操作实现
 * <p>
 * 技术栈迁移:
 * - Go文件存储 -> Java文件存储服务
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Repository
@RequiredArgsConstructor
public class OssItemDAOImpl implements OssItemDAO {
  private final IFileStoreService fileStoreService;

  /**
   * 批量设置项目数据
   * 迁移对应关系: Go语言ItemDAOImpl.MSetItemData
   * - 功能: 批量设置项目数据
   * - 参数: items - 项目列表
   * - 返回: 成功设置的项目数量
   * - 用途: 批量设置项目数据到对象存储
   */
  @Override
  public Integer mSetItemData(List<Item> items) {
    List<Item> filteredItems = filterAbaseItems(items);
    if (CollectionUtils.isEmpty(filteredItems)) {
      return 0;
    }

    int count = 0;
    List<Exception> errors = new ArrayList<>();

    for (Item item : filteredItems) {
      try {
        // 序列化项目数据
        ItemDataPO itemDataPO = new ItemDataPO();
        itemDataPO.setData(item.getData());
        itemDataPO.setRepeatedData(item.getRepeatedData());

        byte[] data = JsonUtil.getObjectMapper().writeValueAsBytes(itemDataPO);
        String storageKey = item.getOrBuildProperties().getStorageKey();

        // 创建上传配置
        UploadConfigVO uploadConfig = new UploadConfigVO();
        uploadConfig.setStoreType("S3"); // 使用S3存储
        uploadConfig.setSaveName(storageKey);
        uploadConfig.setFileSize((long) data.length);
        uploadConfig.setFileType("application/json");

        // 上传文件
        fileStoreService.uploadFile(data, uploadConfig);
        count++;
      }
      catch (Exception e) {
        errors.add(new BssException("marshal item data, id=" + item.getId(), e));
      }
    }

    if (!errors.isEmpty()) {
      throw new BssException("MSetItemData failed: " + errors.size() + " errors", errors.get(0));
    }

    return count;
  }

  /**
   * 批量获取项目数据
   * 迁移对应关系: Go语言ItemDAOImpl.MGetItemData
   * - 功能: 批量获取项目数据
   * - 参数: items - 项目列表
   * - 用途: 从对象存储批量获取项目数据
   */
  @Override
  public void mGetItemData(List<Item> items) {
    List<Item> filteredItems = filterAbaseItems(items);
    if (CollectionUtils.isEmpty(filteredItems)) {
      return;
    }

    List<Exception> errors = new ArrayList<>();

    for (Item item : filteredItems) {
      try {
        String storageKey = item.getOrBuildProperties().getStorageKey();

        // 从文件存储服务获取文件信息
        // 这里需要根据storageKey获取FileInfoVO，可能需要自定义方法
        FileInfoVO fileInfo = getFileInfoByStorageKey(storageKey);
        if (fileInfo == null) {
          continue;
        }

        // 下载文件数据
        byte[] data = fileStoreService.downloadFile(fileInfo.getFileId());
        if (data == null || data.length == 0) {
          continue;
        }

        // 反序列化项目数据
        ItemDataPO itemDataPO = JsonUtil.getObjectMapper().readValue(data, ItemDataPO.class);
        item.setData(itemDataPO.getData());
        item.setRepeatedData(itemDataPO.getRepeatedData());

      }
      catch (Exception e) {
        errors.add(new BssException("unmarshal item data, id=" + item.getId(), e));
      }
    }

    if (!errors.isEmpty()) {
      throw new BssException("MGetItemData failed: " + errors.size() + " errors", errors.get(0));
    }
  }

  /**
   * 过滤基础项目
   * 迁移对应关系: Go语言ItemDAOImpl.filterAbaseItems
   * - 功能: 过滤基础项目
   * - 参数: items - 项目列表
   * - 返回: 过滤后的项目列表
   * - 用途: 过滤出需要存储到对象存储的项目
   */
  private List<Item> filterAbaseItems(List<Item> items) {
    if (CollectionUtils.isEmpty(items)) {
      return new ArrayList<>();
    }

    return items.stream()
      .filter(item -> item.getDataProperties() != null &&
        Objects.equals(item.getDataProperties().getStorage(), Provider.S3.getValue()) &&
        item.getDataProperties().getStorageKey() != null &&
        !item.getDataProperties().getStorageKey().isEmpty())
      .collect(Collectors.toList());
  }

  /**
   * 根据存储键获取文件信息
   * 这里需要根据实际的文件存储服务实现
   * 可能需要扩展IFileStoreService接口或使用其他方式
   */
  private FileInfoVO getFileInfoByStorageKey(String storageKey) {
    // 这里需要根据实际的文件存储服务实现
    // 可能需要扩展IFileStoreService接口或使用其他方式
    // 暂时返回null，需要根据实际需求实现
    FileInfoVO fileInfoVO = new FileInfoVO();
    fileInfoVO.setFileId(Long.parseLong(storageKey));
    return fileInfoVO;
  }
}
