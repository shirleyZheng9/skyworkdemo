package com.iwhalecloud.bote.service;

import com.iwhalecloud.bote.dto.base.DocumentPreviewDTO;
import com.iwhalecloud.bote.dto.base.FilePreviewDTO;
import com.iwhalecloud.bote.dto.base.FilePreviewRequestParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 文件预览服务
 *
 * @author qian.sisheng
 * @since 2025-11-26
 */
public interface IFilePreviewService {

  /**
   * 获取文件预览信息
   *
   * @param params 文件预览请参数
   * @return 文件预览信息
   */
  ResultVO<FilePreviewDTO> getFilePreviewInfo(FilePreviewRequestParams params);

  /**
   * 获取文件预览信息
   *
   * @param documentPreview 文档预览信息
   * @return 文件预览
   */
  ResultVO<FilePreviewDTO> getFilePreviewInfo(DocumentPreviewDTO documentPreview);
}
