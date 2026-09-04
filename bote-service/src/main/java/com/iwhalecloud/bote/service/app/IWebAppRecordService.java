package com.iwhalecloud.bote.service.app;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.app.WebAppRecordDTO;
import com.iwhalecloud.bote.dto.app.query.WebAppRecordQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import java.util.List;

/**
 * 网页应用访问记录服务
 *
 * @author wang.tingyun
 * @since 2025-09-22
 */
public interface IWebAppRecordService {

  /**
   * 分页查询应用访问记录列表
   *
   * @param params 查询参数
   * @return 应用访问记录列表
   */
  PageInfo<WebAppRecordDTO> queryRecordPage(WebAppRecordQueryParams params);

  /**
   * 查询应用访问记录列表
   *
   * @param params 查询参数
   * @return 常用应用列表
   */
  List<WebAppRecordDTO> queryRecordList(WebAppRecordQueryParams params);

  /**
   * 新增应用访问记录
   *
   * @param recordDTO 应用访问记录对象
   * @return 结果
   */
  ResultVO<Void> addAppRecord(WebAppRecordDTO recordDTO);

  /**
   * 移除某个应用访问记录
   *
   * @param recordId 访问记录ID
   * @return 结果
   */
  ResultVO<Void> removeAppRecord(Long recordId);

}