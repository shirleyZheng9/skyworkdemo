package com.iwhalecloud.bote.doc.module.knowledge.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.knowledge.CorpusInfoDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.CorpusQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语料基本信息管理服务
 *
 * @author auto
 * @since 2025-01-13
 */
public interface ICorpusInfoManageService {

  /**
   * 查询单个语料基本信息
   *
   * @param tenantId 租户 ID
   * @param corpusId 语料基本信息主键
   * @return 语料基本信息
   */
  CorpusInfoDTO findCorpusInfo(Long tenantId, Long corpusId);

  /**
   * 保存语料基本信息
   *
   * @param corpusInfo 语料基本信息
   * @return 结果
   */
  ResultVO<CorpusInfoDTO> saveCorpusInfo(CorpusInfoDTO corpusInfo, MultipartFile file);

  /**
   * 删除语料基本信息
   *
   * @param tenantId 租户 ID
   * @param corpusId 语料基本信息主键
   * @return 结果
   */
  ResultVO<Void> deleteCorpusInfo(Long tenantId, Long corpusId);

  /**
   * 查询语料基本信息列表
   *
   * @param queryParams 查询条件
   * @return 语料基本信息列表
   */
  List<CorpusInfoDTO> queryCorpusInfoList(CorpusQueryParams queryParams);

  /**
   * 查询语料基本信息列表（分页）
   *
   * @param queryParams 查询条件
   * @return 语料基本信息分页列表
   */
  PageInfo<CorpusInfoDTO> queryCorpusInfoPage(CorpusQueryParams queryParams);
}
