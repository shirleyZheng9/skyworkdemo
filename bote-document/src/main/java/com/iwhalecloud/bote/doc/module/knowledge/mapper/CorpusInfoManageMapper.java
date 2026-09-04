package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.knowledge.CorpusInfoDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.CorpusQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 语料基本信息管理
 *
 * @author auto
 * @since 2025-03-10
 */
public interface CorpusInfoManageMapper {
  /**
   * 校验语料基本信息的编码唯一性
   *
   * @param corpusInfo 语料基本信息
   * @return 结果
   */
  boolean existsCorpusInfoCode(@Param("dto") CorpusInfoDTO corpusInfo);

  /**
   * 根据主键获取语料基本信息
   *
   * @param corpusId 语料基本信息主键
   * @return 语料基本信息
   */
  CorpusInfoDTO getCorpusInfo(@Param("id") Long corpusId, @Param("tenantId") Long tenantId);

  /**
   * 新增语料基本信息
   *
   * @param corpusInfo 语料基本信息
   * @return 结果
   */
  int insertCorpusInfo(@Param("dto") CorpusInfoDTO corpusInfo);

  /**
   * 批量新增语料基本信息
   *
   * @param corpusInfos 语料基本信息列表
   * @return 结果
   */
  int batchInsertCorpusInfo(@Param("list") List<CorpusInfoDTO> corpusInfos);

  /**
   * 修改语料基本信息
   *
   * @param corpusInfo 语料基本信息
   * @return 结果
   */
  int updateCorpusInfo(@Param("dto") CorpusInfoDTO corpusInfo);

  /**
   * 删除属性
   *
   * @param corpusId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteCorpusInfo(@Param("corpusId") Long corpusId, @Param("updatorId") Long updatorId, @Param("tenantId") Long tenantId);

  /**
   * 获取语料基本信息列表
   *
   * @param queryParams 查询条件
   * @return 语料基本信息列表
   */
  List<CorpusInfoDTO> selectCorpusInfoList(@Param("query") CorpusQueryParams queryParams);

  /**
   * 获取语料基本信息列表（分页）
   *
   * @param queryParams 查询条件
   * @return 语料基本信息分页列表
   */
  Page<CorpusInfoDTO> selectCorpusInfoPage(@Param("query") CorpusQueryParams queryParams, RowBounds rowBounds);

  /**
   * 修改语料基本信息时间
   *
   * @param corpusId 主键 ID
   * @return 影响行数
   */
  int updateCorpusInfoTime(@Param("corpusId") Long corpusId, @Param("updatorId") Long updatorId, @Param("tenantId") Long tenantId);
}
