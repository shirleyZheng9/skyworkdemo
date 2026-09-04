package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.CorpusInfoManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentParameterDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentContentQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentParameterManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentContentManageService;
import com.iwhalecloud.bote.dto.knowledge.CorpusInfoDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.CorpusQueryParams;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.doc.module.knowledge.service.ICorpusInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语料基本信息管理服务实现
 *
 * @author auto
 * @since 2025-01-13
 */
@Service
@RequiredArgsConstructor
public class CorpusInfoManageServiceImpl implements ICorpusInfoManageService {

  private final CorpusInfoManageMapper corpusInfoManageMapper;

  private final DocumentParameterManageMapper parameterManageMapper;

  private final IDocumentContentManageService contentManageService;

  private final ICatalogManageService catalogManageService;

  @Override
  public CorpusInfoDTO findCorpusInfo(Long tenantId, Long corpusId) {
    return corpusInfoManageMapper.getCorpusInfo(corpusId, tenantId);
  }

  @Override
  @Transactional
  public ResultVO<CorpusInfoDTO> saveCorpusInfo(CorpusInfoDTO corpusInfo, MultipartFile file) {
    // 校验编码唯一性
    if (corpusInfoManageMapper.existsCorpusInfoCode(corpusInfo)) {
      return BaseErrorConstant.CHECK_NAME.toResult(corpusInfo.getCorpusName());
    }
    CorpusInfoDTO old = corpusInfo.getCorpusId() == null ? null : findCorpusInfo(corpusInfo.getTenantId(), corpusInfo.getCorpusId());
    DataDifference<CorpusInfoDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, corpusInfo, false, corpusInfo.getTenantId(),
      OperClassEnum.CORPUS);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    if (KnowledgeConsts.CORPUS_OPERATE_TYPE_IMPORT.equals(corpusInfo.getOperateType()) && old == null) {
      contentManageService.importDocumentContent(corpusInfo.getTenantId(), difference.getToSaveData().getCorpusId(), file, null,
        KnowledgeConsts.IMPORT_TYPE_OVERWRITE);
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteCorpusInfo(Long tenantId, Long corpusId) {
    corpusInfoManageMapper.deleteCorpusInfo(corpusId, SessionUtil.getLoginInfo().getUserId(), tenantId);
    ResourceElementFactory.get(OperClassEnum.CORPUS.name()).clear(tenantId, corpusId);
    return ResultVO.success();
  }

  @Override
  public List<CorpusInfoDTO> queryCorpusInfoList(CorpusQueryParams queryParams) {
    List<CorpusInfoDTO> list = corpusInfoManageMapper.selectCorpusInfoList(queryParams);
    if (CollectionUtils.isNotEmpty(list)) {
      List<Long> ids = list.stream().map(CorpusInfoDTO::getCorpusId).collect(Collectors.toList());
      DocumentContentQueryParams params = new DocumentContentQueryParams();
      params.setTenantId(queryParams.getTenantId());
      params.setDocumentIds(ids);
      Map<Long, List<DocumentParameterDTO>> group = CollectionUtils.emptyIfNull(parameterManageMapper.selectParameterList(params)).stream()
        .collect(Collectors.groupingBy(DocumentParameterDTO::getDocumentId));
      for (CorpusInfoDTO dto : list) {
        List<String> parameters = CollectionUtils.emptyIfNull(group.get(dto.getCorpusId())).stream().map(DocumentParameterDTO::getParameterName)
          .collect(Collectors.toList());
        dto.setParameters(parameters);
      }
    }
    return list;
  }

  @Override
  public PageInfo<CorpusInfoDTO> queryCorpusInfoPage(CorpusQueryParams queryParams) {
    queryParams.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(), CatalogConsts.TYPE_CORPUS));
    // noinspection resource
    return corpusInfoManageMapper.selectCorpusInfoPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }
}
