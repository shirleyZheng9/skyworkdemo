package com.iwhalecloud.bote.loop.data.domain.dataset.service;

/**
 * 数据集服务接口
 * 迁移对应关系: Go语言service.IDatasetAPI
 * - 功能: 提供数据集相关的业务逻辑服务
 * - 方法定义: 各种数据集操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的service.IDatasetAPI接口
 * - 使用Java接口定义，包含数据集服务方法
 * - 提供数据集CRUD和业务逻辑操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface DatasetDomainService extends DatasetService, SchemaService, VersionService, ItemService, ItemSnapshotService, FileStoreService, IOJobService {
}
