package com.iwhalecloud.bote.loop.prompt.infra.repo;

/**
 * Prompt基础信息DAO常量
 * 迁移对应关系: Go语言mysql包中的常量
 * - 功能: 定义排序字段常量
 * - 常量:
 * * LIST_PROMPT_BASIC_ORDER_BY_ID - 按ID排序
 * * LIST_PROMPT_BASIC_ORDER_BY_CREATED_AT - 按创建时间排序
 * * LIST_PROMPT_BASIC_ORDER_BY_LATEST_COMMITTED_AT - 按最新提交时间排序
 * <p>
 * Java实现说明:
 * - 对应Go的常量定义
 * - 使用Java类定义常量
 * - 提供排序字段标识
 * <p>
 * 技术栈迁移:
 * - Go常量 -> Java常量
 * - Go int -> Java Integer
 */
public class PromptBasicDAOConstants {

  /**
   * 按ID排序
   * 迁移对应关系: Go语言ListPromptBasicOrderByID
   * - 功能: 按Prompt ID排序
   * - 值: 1
   */
  public static final Integer LIST_PROMPT_BASIC_ORDER_BY_ID = 1;

  /**
   * 按创建时间排序
   * 迁移对应关系: Go语言ListPromptBasicOrderByCreatedAt
   * - 功能: 按创建时间排序
   * - 值: 2
   */
  public static final Integer LIST_PROMPT_BASIC_ORDER_BY_CREATED_AT = 2;

  /**
   * 按最新提交时间排序
   * 迁移对应关系: Go语言ListPromptBasicOrderByLatestCommittedAt
   * - 功能: 按最新提交时间排序
   * - 值: 3
   */
  public static final Integer LIST_PROMPT_BASIC_ORDER_BY_LATEST_COMMITTED_AT = 3;
}
