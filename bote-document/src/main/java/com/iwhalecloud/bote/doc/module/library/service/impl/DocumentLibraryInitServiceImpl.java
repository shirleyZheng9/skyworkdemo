package com.iwhalecloud.bote.doc.module.library.service.impl;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.consts.VisibilityScopeEnum;
import com.iwhalecloud.bote.doc.module.person.mapper.MyDocumentMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.mapper.DocumentLibraryMapper;
import com.iwhalecloud.bote.doc.module.library.service.IDocumentLibraryInitService;
import com.iwhalecloud.bote.doc.common.utils.DcIdUtils;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文档库初始化服务
 *
 * @author yangran
 * @since 2025-08-18
 */
@Service
@RequiredArgsConstructor
public class DocumentLibraryInitServiceImpl implements IDocumentLibraryInitService {
  //@formatter:off
  private final MyDocumentMapper myDocumentMapper;
  private final DocumentLibraryMapper documentLibraryMapper;
  private final IDocumentService documentService;
  //@formatter:on
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
  public String initializeUserDocumentLibrary(Long userId, Long tenantId, Long spaceId) {
    // 检查用户个人文档库是否已存在
    String string = existsUserDocumentLibrary(userId, spaceId);
    if (StringUtils.isNotEmpty(string)) {
      return string;
    }


    // 创建用户个人文档库
    DocumentLibraryDTO library = new DocumentLibraryDTO();
    library.setId(IDUtils.nextId());
    library.setLibraryId(DcIdUtils.createLibraryId());
    library.setLibraryName("我的文档");
    library.setVisibilityScope(VisibilityScopeEnum.PRIVATE.getCode());
    library.setSortOrder(0);
    library.setAccessCount(0);
    library.setOwnerId(userId);
    library.setTenantId(tenantId);
    library.setCreatorId(1L);
    library.setUpdatorId(1L);
    library.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    library.setIsBuiltin(DocBaseConsts.TRUE);
    library.setLibraryIcon(DocBaseConsts.DEFAULT_LIBRARY_ICON);
    library.setColor(DocBaseConsts.DEFAULT_LIBRARY_COLOR);
    library.setSpaceId(spaceId);
    documentLibraryMapper.insert(library);

    // 初始化文档库根节点
//    String rootNodeId =
      documentService.initLibraryRootDocumentNode(library.getLibraryId(),
      library.getLibraryName(), tenantId, spaceId);

    // 创建内置文件夹
//    ensureBuiltinFoldersExist(userId, tenantId, library.getLibraryId(), rootNodeId, spaceId);
    return library.getLibraryId();
  }

  @Override
  public String existsUserDocumentLibrary(Long userId, Long spaceId) {
    return myDocumentMapper.existsUserDocumentLibrary(userId, spaceId);
  }

  @Override
  public boolean existsBuiltinFolder(Long userId, Long spaceId, String builtinType, String libraryId) {
    return !myDocumentMapper.existsBuiltinFolder(userId, spaceId, builtinType, libraryId);
  }

  // private void ensureBuiltinFoldersExist(Long userId, Long tenantId, String libraryId, String rootNodeId, Long
  // spaceId) {
  // // 确保对话上传文件夹存在 TODO : 临时关闭
  // if (existsBuiltinFolder(userId, spaceId, BuiltinTypeEnum.DIALOG_FOLDER.getCode(), libraryId)) {
  // documentService.createBuiltinFolder(userId, spaceId, tenantId, BuiltinTypeEnum.DIALOG_FOLDER.getCode(),
  // FolderTypeEnum.DIALOG.getDescription(), libraryId, rootNodeId);
  // }
  //
  // // 确保应用上传文件夹存在
  // if (existsBuiltinFolder(userId, spaceId, BuiltinTypeEnum.APP_FOLDER.getCode(), libraryId)) {
  // documentService.createBuiltinFolder(userId, spaceId, tenantId, BuiltinTypeEnum.APP_FOLDER.getCode(),
  // FolderTypeEnum.APP.getDescription(), libraryId, rootNodeId);
  // }
  // }
}
