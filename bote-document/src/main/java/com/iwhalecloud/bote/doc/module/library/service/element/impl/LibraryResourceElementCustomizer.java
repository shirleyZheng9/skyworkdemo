package com.iwhalecloud.bote.doc.module.library.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentMapper;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.mapper.DocumentLibraryMapper;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.service.element.impl.AbstractResourceElementCustomizer;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 知识库
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.LIBRARY)
public class LibraryResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final DocumentMapper documentMapper;
  private final DocumentLibraryMapper documentLibraryMapper;


  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.LIBRARY.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long libraryId) {
    DocumentLibraryDTO library = documentLibraryMapper.queryByPrimaryKey(libraryId, tenantId);
    if (library == null) {
      throw new IllegalArgumentException("文档库不存在: libraryId=" + libraryId);
    }
    List<ResourceElementDTO> elements = new ArrayList<>();
    // 关联的文档库
    List<DcDocumentDTO> dcDocumentDTOS = documentMapper.selectDocumentsByLibraryId(library.getLibraryId());
    for (DcDocumentDTO dto : CollectionUtils.emptyIfNull(dcDocumentDTOS)) {
      // 添加文件的血缘关系
      if (dto.getFileInfoId() != null) {
        elements.add(createElement(tenantId, libraryId, dto.getFileInfoId(), DataSyncCodeEnum.DOCUMENT_FILE.getCode()));
      }
    }
    return elements;
  }
}
