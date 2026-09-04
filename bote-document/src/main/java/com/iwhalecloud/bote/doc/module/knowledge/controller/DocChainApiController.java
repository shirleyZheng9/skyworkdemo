package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocChainApiService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * docChain接口代理,用于业务系统调用
 *
 * @author qian.sisheng
 * @since 2025-05-15
 */
@RequestMapping(path = CommonConsts.API_PREFIX + "docchain/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
public class DocChainApiController {
  private final IDocChainApiService docChainApiService;

  @Operation(summary = "查询主题列表")
  @PostMapping("queryTopicList")
  public ResponseEntity<Object> queryTopicList(@RequestBody(required = false) Map<String, Object> params) {
    return docChainApiService.queryTopicList(params);
  }

  @Operation(summary = "更新主题")
  @PostMapping("updateTopic")
  public ResponseEntity<Object> updateTopic(@RequestBody Map<String, Object> params) {
    return docChainApiService.updateTopic(params);
  }

  @Operation(summary = "查询文档列表")
  @PostMapping("queryDocList")
  public ResponseEntity<Object> queryDocList(@RequestBody Map<String, Object> params) {
    return docChainApiService.queryDocList(params);
  }

  @Operation(summary = "查询文档列表(分页)")
  @PostMapping("queryDocPage")
  public ResponseEntity<Object> queryDocPage(@RequestBody Map<String, Object> params) {
    return docChainApiService.queryDocPage(params);
  }

  @Operation(summary = "同步切分")
  @PostMapping("syncSplit")
  public ResponseEntity<Object> syncSplit(@RequestBody Map<String, Object> params) {
    return docChainApiService.syncSplit(params);
  }

  @Operation(summary = "异步切分")
  @PostMapping("asyncSplit")
  public ResponseEntity<Object> asyncSplit(@RequestBody Map<String, Object> params) {
    return docChainApiService.asyncSplit(params);
  }

  @Operation(summary = "批量上传")
  @PostMapping("batchUpload")
  public ResponseEntity<Object> batchUpload(@RequestPart("files") MultipartFile[] files, @RequestParam Map<String, Object> params) {
    return docChainApiService.batchUpload(files, params);
  }

  @Operation(summary = "读取文件")
  @PostMapping("readDocument")
  public ResponseEntity<?> readDocument(@RequestBody Map<String, Object> params) {
    return docChainApiService.readDocument(params);
  }

  @Operation(summary = "查询切分参数")
  @PostMapping("querySplitParams")
  public ResponseEntity<Object> querySplitParams(@RequestBody Map<String, Object> params) {
    return docChainApiService.querySplitParams(params);
  }

  @Operation(summary = "查询文档详情")
  @PostMapping("queryDocumentDetail")
  public ResponseEntity<Object> queryDocumentDetail(@RequestBody Map<String, Object> params) {
    return docChainApiService.queryDocumentDetail(params);
  }

  @Operation(summary = "删除单个文档")
  @PostMapping("deleteDocument")
  public ResponseEntity<Object> deleteDocument(@RequestBody Map<String, Object> params) {
    return docChainApiService.deleteDocument(params);
  }

  @Operation(summary = "更新文档")
  @PostMapping("updateDocument")
  public ResponseEntity<Object> updateDocument(@RequestBody Map<String, Object> params) {
    return docChainApiService.updateDocument(params);
  }

  @Operation(summary = "查询切分列表")
  @PostMapping("queryChunksList")
  public ResponseEntity<Object> queryChunksList(@RequestBody Map<String, Object> params) {
    return docChainApiService.queryChunksList(params);
  }

  @Operation(summary = "添加切分")
  @PostMapping("addDocChunk")
  public ResponseEntity<Object> addDocChunk(@RequestBody Map<String, Object> params) {
    return docChainApiService.addDocChunk(params);
  }

  @Operation(summary = "删除切分")
  @PostMapping("deleteDocChunk")
  public ResponseEntity<Object> deleteDocChunk(@RequestBody Map<String, Object> params) {
    return docChainApiService.deleteDocChunk(params);
  }

  @Operation(summary = "更新切分")
  @PostMapping("updateDocChunk")
  public ResponseEntity<Object> updateDocChunk(@RequestBody Map<String, Object> params) {
    return docChainApiService.updateDocChunk(params);
  }

  @Operation(summary = "查询切分详情")
  @PostMapping("findDocChunk")
  public ResponseEntity<Object> findDocChunk(@RequestBody Map<String, Object> params) {
    return docChainApiService.findDocChunk(params);
  }

  @Operation(summary = "召回")
  @PostMapping("recall")
  public ResponseEntity<Object> recall(@RequestBody Map<String, Object> params) {
    return docChainApiService.recall(params);
  }

  @Operation(summary = "重排")
  @PostMapping("rerank")
  public ResponseEntity<Object> rerank(@RequestBody Map<String, Object> params) {
    return docChainApiService.rerank(params);
  }

  @Operation(summary = "上传图片")
  @PostMapping("chatUpload")
  public ResponseEntity<Object> chatUpload(@RequestPart("file") MultipartFile file, @RequestParam Map<String, Object> params) {
    return docChainApiService.chatUpload(file, params);
  }

  @Operation(summary = "图片搜索")
  @PostMapping("imgSearch")
  public ResponseEntity<Object> imgSearch(@RequestBody Map<String, Object> params) {
    return docChainApiService.imgSearch(params);
  }
}
