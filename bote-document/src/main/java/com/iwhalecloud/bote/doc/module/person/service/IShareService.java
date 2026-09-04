package com.iwhalecloud.bote.doc.module.person.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.person.dto.share.MyShareDTO;
import com.iwhalecloud.bote.doc.module.person.dto.share.SharedWithMeDTO;

/**
 * 共享模块 Service 接口
 * <p>提供“与我共享”和“我共享的”分页查询。</p>
 *
 * @author lizuyin
 * @since 2025-08-20
 */
public interface IShareService {

  /**
   * 获取与我共享的列表。
   *
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @param fileType 文件类型过滤
   * @param sortBy 排序字段：shareTime、modifyTime
   * @param sortOrder 排序：asc、desc
   * @return 分页数据
   */
  PageInfo<SharedWithMeDTO> getSharedWithMe(Integer pageNum, Integer pageSize, String fileType, String sortBy,
                                            String sortOrder, Long tenantId, Long spaceId, String platform);

  /**
   * 获取我共享的列表。
   *
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @param fileType 文件类型过滤
   * @param sortBy 排序字段：shareTime、modifyTime
   * @param sortOrder 排序：asc、desc
   * @return 分页数据
   */
  PageInfo<MyShareDTO> getMyShares(Integer pageNum, Integer pageSize, String fileType, String sortBy, String sortOrder, Long tenantId, Long spaceId, String platform);
}


