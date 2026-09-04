package com.iwhalecloud.bote.loop.data.domain.dataset.repo;

/**
 * 数据集API接口
 * 迁移对应关系: Go语言repo.IDatasetAPI
 * - 功能: 提供数据集数据访问接口
 * - 方法定义: 各种数据集数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的repo.IDatasetAPI接口
 * - 使用Java接口定义，包含数据集数据访问方法
 * - 提供数据集数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IDatasetAPI extends IDatasetRepo, ISchemaRepo, IVersionRepo, IOperationRepo, IItemRepo, IItemSnapshotRepo, IIOJobRepo {
}
