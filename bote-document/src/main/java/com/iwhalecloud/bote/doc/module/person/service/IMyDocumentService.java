package com.iwhalecloud.bote.doc.module.person.service;

import com.iwhalecloud.bote.doc.module.person.dto.MyDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.MyDocumentQueryParams;

/**
 * 我的文档服务
 *
 * @author yangran
 * @since 2025-08-18
 */
public interface IMyDocumentService {

  /**
   * 分页查询我的文档
   *
   * @param queryParams 查询条件
   * @return 文档分页数据
   */
  MyDocumentDTO queryMyDocumentTree(MyDocumentQueryParams queryParams);

}
