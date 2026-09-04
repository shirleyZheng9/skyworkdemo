package com.iwhalecloud.bote.doc.module.knowledge.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcKbPermissionDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseBatchPermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBasePermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcKbPermissionQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 知识库权限表管理服务
 *
 * @author linmengfan
 * @since 2025-08-23
 */
public interface IBtDcKbPermissionManageService {

  /**
   * 查询知识库权限表列表
   *
   * @param queryParams 查询条件
   * @return 知识库权限表列表
   */
  List<BtDcKbPermissionDTO> queryBtDcKbPermissionList(BtDcKbPermissionQueryParams queryParams);

  /**
   * 查询知识库权限表列表（分页）
   *
   * @param queryParams 查询条件
   * @return 知识库权限表分页列表
   */
  PageInfo<BtDcKbPermissionDTO> queryBtDcKbPermissionPage(BtDcKbPermissionQueryParams queryParams);

  ResultVO<Void> updateKbPermissions(Long knowledgeId, @Valid KnowledgeBasePermissionRequestDTO request);

  ResultVO<Void> batchKbPermissions(Long knowledgeId, @Valid KnowledgeBaseBatchPermissionRequestDTO request);
}
