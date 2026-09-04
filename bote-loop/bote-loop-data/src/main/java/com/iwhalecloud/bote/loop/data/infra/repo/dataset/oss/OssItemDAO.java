package com.iwhalecloud.bote.loop.data.infra.repo.dataset.oss;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import java.util.List;

/**
 * 数据集项目DAO接口
 * 迁移对应关系: Go语言item_dao.ItemDAO
 * - 功能: 提供数据集项目数据访问接口
 * - 方法定义: 各种数据集项目数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的item_dao.ItemDAO接口
 * - 使用Java接口定义，包含数据集项目数据访问方法
 * - 提供数据集项目数据存储操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface OssItemDAO {

  /**
   * 批量设置项目数据
   * 迁移对应关系: Go语言ItemDAO.MSetItemData
   * - 功能: 批量设置项目数据
   * - 参数: items - 项目列表
   * - 返回: 成功设置的项目数量
   * - 用途: 批量设置项目数据到对象存储
   */
  Integer mSetItemData(List<Item> items);

  /**
   * 批量获取项目数据
   * 迁移对应关系: Go语言ItemDAO.MGetItemData
   * - 功能: 批量获取项目数据
   * - 参数: items - 项目列表
   * - 用途: 从对象存储批量获取项目数据
   */
  void mGetItemData(List<Item> items);
}
