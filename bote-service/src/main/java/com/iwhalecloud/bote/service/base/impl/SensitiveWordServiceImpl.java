package com.iwhalecloud.bote.service.base.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.SensitiveWordDTO;
import com.iwhalecloud.bote.dto.base.query.SensitiveWordQueryParams;
import com.iwhalecloud.bote.mapper.base.SensitiveWordMapper;
import com.iwhalecloud.bote.service.base.ISensitiveWordService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 敏感词管理服务
 *
 * @author bianjp
 * @since 2025-01-15
 */
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements ISensitiveWordService {
  private final SensitiveWordMapper sensitiveWordMapper;

  @Transactional
  @Override
  public ResultVO<Long> saveSensitiveWord(SensitiveWordDTO sensitiveWord) {
    Assert.hasLength(sensitiveWord.getIsBlack(), "是否是黑名单不能为空");
    Assert.isTrue(BaseConsts.TRUE.equals(sensitiveWord.getIsBlack()) || BaseConsts.FALSE.equals(sensitiveWord.getIsBlack()), "isBlack 值非法");
    Assert.hasLength(sensitiveWord.getWordContent(), "敏感词内容不能为空");
    sensitiveWord.setRemark(StringUtils.trimToNull(sensitiveWord.getRemark()));

    SensitiveWordDTO old;
    // 修改
    if (sensitiveWord.getWordId() != null) {
      old = sensitiveWordMapper.selectById(sensitiveWord.getWordId());
      Assert.notNull(old, "敏感词不存在");
      boolean isBlackChanged = !Objects.equals(sensitiveWord.getIsBlack(), old.getIsBlack());
      boolean contentChanged = !Objects.equals(sensitiveWord.getWordContent(), old.getWordContent());
      boolean remarkChanged = !Objects.equals(sensitiveWord.getRemark(), old.getRemark());
      if (!isBlackChanged && !contentChanged && !remarkChanged) {
        return BaseErrorConstant.NO_DIFFERENCE.toResult();
      }
      if (contentChanged) {
        Assert.isTrue(!sensitiveWordMapper.existsWord(sensitiveWord.getWordContent()), "敏感词已存在");
      }

      sensitiveWord.putFieldUpdateFlag("isBlack", isBlackChanged);
      sensitiveWord.putFieldUpdateFlag("wordContent", contentChanged);
      sensitiveWord.putFieldUpdateFlag("remark", remarkChanged);
      sensitiveWord.setUpdatorId(SessionUtil.getOptionalUserId());
      sensitiveWordMapper.update(sensitiveWord);
    }
    // 新增
    else {
      Assert.isTrue(!sensitiveWordMapper.existsWord(sensitiveWord.getWordContent()), "敏感词已存在");
      sensitiveWord.setWordId(Sequences.SENSITIVE_WORD_ID.next());
      sensitiveWord.setStatusCd(BaseConsts.STATUS_CD_VALID);
      sensitiveWord.setCreatorId(SessionUtil.getOptionalUserId());
      sensitiveWordMapper.insert(sensitiveWord);
    }

    return ResultVO.success(sensitiveWord.getWordId());
  }

  @Transactional
  @Override
  public boolean deleteSensitiveWord(Long wordId) {
    return sensitiveWordMapper.delete(wordId, SessionUtil.getOptionalUserId()) > 0;
  }

  @Nullable
  @Override
  public SensitiveWordDTO findSensitiveWordById(Long wordId) {
    return sensitiveWordMapper.selectById(wordId);
  }

  @Override
  public PageInfo<SensitiveWordDTO> qrySensitiveWordPage(SensitiveWordQueryParams queryParams) {
    //noinspection resource
    return sensitiveWordMapper.selectSensitiveWordPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }
}
