package com.iwhalecloud.bote.service.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.SensitiveWordDTO;
import com.iwhalecloud.bote.dto.base.query.SensitiveWordQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.lang.Nullable;

/**
 * 敏感词管理服务
 *
 * @author bianjp
 * @since 2025-01-15
 */
public interface ISensitiveWordService {

  /**
   * 保存敏感词
   *
   * @return 敏感词 ID
   */
  ResultVO<Long> saveSensitiveWord(SensitiveWordDTO sensitiveWord);

  /**
   * 删除敏感词
   *
   * @param wordId 敏感词 ID
   * @return 是否删除成功
   */
  boolean deleteSensitiveWord(Long wordId);

  /**
   * 查询敏感词
   */
  @Nullable
  SensitiveWordDTO findSensitiveWordById(Long wordId);

  /**
   * 分页查询敏感词
   */
  PageInfo<SensitiveWordDTO> qrySensitiveWordPage(SensitiveWordQueryParams queryParams);
}
