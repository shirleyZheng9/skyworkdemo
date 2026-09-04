package com.iwhalecloud.bote.doc.module.knowledge.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * docChain接口代理服务
 *
 * @author qian.sisheng
 * @since 2025-05-14
 */
public interface IDocChainApiService {
  /**
   * 查询用户主题
   *
   * @return 结果
   */
  ResponseEntity<Object> queryTopicList(Map<String, Object> params);

  /**
   * 更新主题
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> updateTopic(Map<String, Object> params);

  /**
   * 查询文档列表
   *
   * @param params 参数
   * @return 列表
   */
  ResponseEntity<Object> queryDocList(Map<String, Object> params);

  /**
   * 查询文档列表(分页)
   *
   * @param params 参数
   * @return 列表
   */
  ResponseEntity<Object> queryDocPage(Map<String, Object> params);

  /**
   * 同步拆分
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> syncSplit(Map<String, Object> params);

  /**
   * 异步拆分
   *
   * @param params 参数
   * @return 响应
   */
  ResponseEntity<Object> asyncSplit(Map<String, Object> params);

  /**
   * 批量上传
   *
   * @param files 文件
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> batchUpload(MultipartFile[] files, Map<String, Object> params);

  /**
   * 读取文档
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<?> readDocument(Map<String,  Object> params);

  /**
   * 拆分参数
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> querySplitParams(Map<String, Object> params);

  /**
   * 查询文档详情
   *
   * @param params 参数
   * @return 详情
   */
  ResponseEntity<Object> queryDocumentDetail(Map<String, Object> params);

  /**
   * 删除文档
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> deleteDocument(Map<String, Object> params);

  /**
   * 更新文档
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> updateDocument(Map<String, Object> params);

  /**
   * 查询文档列表
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> queryChunksList(Map<String, Object> params);

  /**
   * 添加文档块
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> addDocChunk(Map<String, Object> params);

  /**
   * 删除文档块
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> deleteDocChunk(Map<String, Object> params);

  /**
   * 更新文档块
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> updateDocChunk(Map<String, Object> params);

  /**
   * 查询文档块
   *
   * @param params 文档ID
   * @return 块
   */
  ResponseEntity<Object> findDocChunk(Map<String, Object> params);

  /**
   * 知识招回
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> recall(Map<String, Object> params);

  /**
   * 重排接口
   *
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> rerank(Map<String, Object> params);

  /**
   * 图片上传
   *
   * @param file 文件
   * @param params 参数
   * @return 结果
   */
  ResponseEntity<Object> chatUpload(MultipartFile file, Map<String, Object> params);

  /**
   * 图片检索
   *
   * @param params 参数
   * @return 检索结果
   */
  ResponseEntity<Object> imgSearch(Map<String, Object> params);
}
