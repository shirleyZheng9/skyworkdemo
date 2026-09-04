package com.iwhalecloud.bote.service.chat.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.ChineseTranslateUtil;
import com.iwhalecloud.bote.dto.chat.ChatReplyThemeDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatReplyThemeQueryParams;
import com.iwhalecloud.bote.mapper.chat.ChatReplyThemeManageMapper;
import com.iwhalecloud.bote.service.chat.IChatReplyThemeManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 回复消息主题管理服务实现
 *
 * @author qian.sisheng
 * @since 2025-12-02
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ChatReplyThemeManageServiceImpl implements IChatReplyThemeManageService {

  private final Logger logger = LoggerFactory.getLogger(ChatReplyThemeManageServiceImpl.class);

  private final ChatReplyThemeManageMapper chatReplyThemeManageMapper;

  @Override
  public ChatReplyThemeDTO findChatReplyTheme(Long replyThemeId, Long tenantId) {
    return chatReplyThemeManageMapper.getChatReplyTheme(replyThemeId, tenantId);
  }

  @Override
  @Transactional
  public ResultVO<ChatReplyThemeDTO> saveChatReplyTheme(ChatReplyThemeDTO chatReplyTheme) {
    // 校验编码唯一性
    if (chatReplyThemeManageMapper.existsChatReplyThemeCode(chatReplyTheme)) {
      return BaseErrorConstant.CHECK_ATTR_NBR.toResult();
    }
    chatReplyTheme.setStatusCd(CommonConsts.STATUS_CD_VALID);
    ChatReplyThemeDTO old =
      chatReplyTheme.getReplyThemeId() == null ? null : findChatReplyTheme(chatReplyTheme.getReplyThemeId(), chatReplyTheme.getTenantId());
    DataDifference<ChatReplyThemeDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, chatReplyTheme, false, chatReplyTheme.getTenantId(),
      OperClassEnum.CHAT_REPLY_THEME);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteChatReplyTheme(Long replyThemeId, Long tenantId) {
    chatReplyThemeManageMapper.deleteChatReplyTheme(replyThemeId, SessionUtil.getLoginInfo().getUserId(), tenantId);
    return ResultVO.success();
  }

  @Override
  public List<ChatReplyThemeDTO> queryChatReplyThemeList(ChatReplyThemeQueryParams queryParams) {
    return chatReplyThemeManageMapper.selectChatReplyThemeList(queryParams);
  }

  @Override
  public PageInfo<ChatReplyThemeDTO> queryChatReplyThemePage(ChatReplyThemeQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return chatReplyThemeManageMapper.selectChatReplyThemePage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> importReplyTheme(MultipartFile file, Long tenantId, String replyThemeName) {
    try {
      String extension = FilenameUtils.getExtension(file.getOriginalFilename());
      if (!"css".equals(extension)) {
        throw new BssException("仅支持css文件");
      }
      ChatReplyThemeDTO chatReplyTheme = new ChatReplyThemeDTO();
      chatReplyTheme.setReplyThemeId(Sequences.CHAT_REPLY_THEME_ID.next());
      chatReplyTheme.setReplyThemeName(replyThemeName);
      chatReplyTheme.setReplyThemeCode(generateReplyThemeCode(tenantId, replyThemeName));
      chatReplyTheme.setThemeJson(new String(file.getBytes(), StandardCharsets.UTF_8));
      chatReplyTheme.setTenantId(tenantId);
      chatReplyTheme.setCreatedTime(new Date());
      chatReplyTheme.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
      chatReplyTheme.setCreatorId(SessionUtil.getLoginInfo().getUserId());
      chatReplyTheme.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
      chatReplyTheme.setStatusCd(CommonConsts.STATUS_CD_VALID);
      chatReplyThemeManageMapper.insertChatReplyTheme(chatReplyTheme);
    }
    catch (Exception e) {
      logger.error("Failed to read reply theme content, message={}", e.getMessage(), e);
      throw new BssException("导入消息主题失败，message=" + e.getMessage(), e);
    }
    return ResultVO.success();
  }

  /**
   * 生成编码
   */
  private String generateReplyThemeCode(Long tenantId, String replyThemeName) {
    String baseCode = ChineseTranslateUtil.translateToPinyin(replyThemeName);
    String replyThemeCode = baseCode;
    // 检查是否存在重复编码，如果存在则添加序号
    ChatReplyThemeDTO check = new ChatReplyThemeDTO();
    check.setReplyThemeCode(replyThemeCode);
    check.setTenantId(tenantId);
    int suffix = 1;
    while (chatReplyThemeManageMapper.existsChatReplyThemeCode(check)) {
      replyThemeCode = baseCode + "_" + suffix;
      check.setReplyThemeCode(replyThemeCode);
      suffix++;
    }
    return replyThemeCode;
  }
}
