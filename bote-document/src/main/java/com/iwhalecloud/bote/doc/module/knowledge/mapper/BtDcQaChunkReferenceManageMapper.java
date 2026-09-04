package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaChunkReferenceDTO;

/**
 * 知识库权限表管理
 *
 * @author linmengfan
 * @since 2025-09-13
 */
public interface BtDcQaChunkReferenceManageMapper {

  /**
   * 根据主键获取知识库权限表
   *
   * @param referenceId 知识库权限表主键
   * @return 知识库权限表
   */
  BtDcQaChunkReferenceDTO getBtDcQaChunkReference(@Param("id") Long referenceId);

  /**
   * 新增知识库权限表
   *
   * @param btDcQaChunkReference 知识库权限表
   * @return 结果
   */
  int insertBtDcQaChunkReference(@Param("dto") BtDcQaChunkReferenceDTO btDcQaChunkReference);

  /**
   * 批量新增知识库权限表
   *
   * @param btDcQaChunkReferences 知识库权限表列表
   * @return 结果
   */
  int batchInsertBtDcQaChunkReference(@Param("list") List<BtDcQaChunkReferenceDTO> btDcQaChunkReferences);

  /**
   * 修改知识库权限表
   *
   * @param btDcQaChunkReference 知识库权限表
   * @return 结果
   */
  int updateBtDcQaChunkReference(@Param("dto") BtDcQaChunkReferenceDTO btDcQaChunkReference);

  /**
   * 获取知识库权限表列表
   *
   * @param qaId 问答记录id
   * @return 知识库权限表列表
   */
  List<BtDcQaChunkReferenceDTO> selectBtDcQaChunkReferenceList(@Param("qaId") Long qaId, @Param("tenantId") Long tenantId);

  /**
   * 失效文档知识库引用片段
   * @param qaId
   * @param tenantId
   * @return
   */
  int deleteBtDcQaChunkReference(@Param("qaId")Long qaId, @Param("tenantId")Long tenantId);

  List<BtDcQaChunkReferenceDTO> selectBtDcQaChunkReferenceListByknowledgeId(@Param("extSystemId")Long extSystemId, @Param("tenantId")Long tenantId);

  int deleteBtDcQaChunkReferenceByExtSystemId(@Param("extSystemId")Long extSystemId, @Param("tenantId")Long tenantId, @Param("qaId")Long qaId);
}
