package com.iwhalecloud.bote.loop.data.application.convertor;


import com.iwhalecloud.bote.loop.client.data.domain.dataset.StorageProviderDTO;
import com.iwhalecloud.bote.loop.data.domain.entity.Provider;

/**
 * 数据集存储转换器
 * 迁移对应关系: Go语言backend/modules/data/application/convertor/dataset/storage.go
 * - 功能: 数据集存储相关的DO和DTO转换
 * - 主要方法:
 * * storageProviderDTO2DO - 存储提供者DTO转DO
 * * providerDO2DTO - 存储提供者DO转DTO
 * <p>
 * Java实现说明:
 * - 对应Go的storage.go文件
 * - 使用静态方法进行转换
 * - 处理存储提供者枚举转换
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go switch语句 -> Java switch表达式
 * - Go error返回 -> Java异常处理
 */
public final class DatasetStorageConvertor {

  private DatasetStorageConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 存储提供者DTO转DO
   * 迁移对应关系: Go语言StorageProviderDTO2DO方法
   *
   * @param storageProvider DTO层存储提供者
   * @return DO层存储提供者
   */
  public static Provider storageProviderDTO2DO(StorageProviderDTO storageProvider) {
    if (storageProvider == null) {
      return Provider.UNKNOWN;
    }

    return switch (storageProvider) {
      case TOS -> Provider.TOS;
      case VETOS -> Provider.VETOS;
      case HDFS -> Provider.HDFS;
      case IMAGEX -> Provider.IMAGE_X;
      case S3 -> Provider.S3;
      case LOCALFS -> Provider.LOCAL_FS;
      case ABASE -> Provider.ABASE;
      default -> Provider.UNKNOWN;
    };
  }

  /**
   * 存储提供者DO转DTO
   * 迁移对应关系: Go语言ProviderDO2DTO方法
   *
   * @param provider DO层存储提供者
   * @return DTO层存储提供者
   */
  public static StorageProviderDTO providerDO2DTO(Provider provider) {
    if (provider == null) {
      return null;
    }

    return switch (provider) {
      case TOS -> StorageProviderDTO.TOS;
      case VETOS -> StorageProviderDTO.VETOS;
      case HDFS -> StorageProviderDTO.HDFS;
      case IMAGE_X -> StorageProviderDTO.IMAGEX;
      case S3 -> StorageProviderDTO.S3;
      case LOCAL_FS -> StorageProviderDTO.LOCALFS;
      case ABASE -> StorageProviderDTO.ABASE;
      case RDS -> StorageProviderDTO.RDS;
      default -> null;
    };
  }
}
