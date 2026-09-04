package com.iwhalecloud.bote.service.chat.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.chat.ChatThemeDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatThemeQueryParams;
import com.iwhalecloud.bote.mapper.chat.ChatThemeMapper;
import com.iwhalecloud.bote.service.chat.IChatThemeService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 聊天主题服务实现
 *
 * @author tingyun.wang
 * @since 2025-07-23
 */
@Service
@RequiredArgsConstructor
public class ChatThemeServiceImpl implements IChatThemeService {

  private final ChatThemeMapper chatThemeMapper;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<ChatThemeDTO> addTheme(ChatThemeDTO chatThemeDTO) {
    // 补充数据
    chatThemeDTO.setThemeId(Sequences.CHAT_THEME_ID.next());
    chatThemeDTO.setThemeScope(BaseConsts.CHAT_THEME_SCOPE_APP);
    chatThemeDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);
    chatThemeDTO.setIsUsing(BaseConsts.FALSE);
    chatThemeDTO.setThemeJson(JsonUtil.toJsonString(chatThemeDTO.getThemeJsonMap()));
    // 补充回复消息主题ID
    chatThemeDTO.setReplyThemeId(MapUtils.getLong(chatThemeDTO.getThemeJsonMap(), "--botchat-reply-message-theme"));
    // 新增主题数据
    DataDifference<ChatThemeDTO> difference = DataDifferenceStarter.computeSaveAndLog(null, chatThemeDTO, true,
      chatThemeDTO.getTenantId(),  OperClassEnum.CHAT_THEME);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    // 添加应用关联会话血缘关系
    resourceElementService.batchAdd(chatThemeDTO.getTenantId(), chatThemeDTO.getBotId(), DataSyncCodeEnum.BOT.getCode(),
        Collections.singletonList(Long.valueOf(difference.getId())), DataSyncCodeEnum.CHAT_THEME.getCode());
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<ChatThemeDTO> updateTheme(ChatThemeDTO chatThemeDTO) {
    // 补充主题数据
    Long userId = SessionUtil.getLoginInfo().getUserId();
    chatThemeDTO.setUpdatorId(userId);
    chatThemeDTO.setThemeJson(JsonUtil.toJsonString(chatThemeDTO.getThemeJsonMap()));
    // 补充回复消息主题ID
    chatThemeDTO.setReplyThemeId(MapUtils.getLong(chatThemeDTO.getThemeJsonMap(), "--botchat-reply-message-theme"));
    // 查找旧主题
    ChatThemeDTO oldTheme = chatThemeMapper.getChatTheme(chatThemeDTO.getBotId(), chatThemeDTO.getThemeId(), chatThemeDTO.getTenantId());
    Assert.notNull(oldTheme, "主题不存在");
    // 更新主题数据
    DataDifference<ChatThemeDTO> difference = DataDifferenceStarter.computeSaveAndLog(oldTheme, chatThemeDTO, true,
      chatThemeDTO.getTenantId(), OperClassEnum.CHAT_THEME);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public void renameTheme(ChatThemeDTO chatThemeDTO) {
    chatThemeMapper.updateThemeName(chatThemeDTO);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteChatTheme(ChatThemeDTO chatThemeDTO) {
    // 查询主题是否存在
    ChatThemeDTO theme = chatThemeMapper.getThemeBasicInfo(chatThemeDTO.getBotId(), chatThemeDTO.getThemeId(), chatThemeDTO.getTenantId());
    if (theme == null) {
      throw new BssException("主题不存在");
    }
    // 删除主题
    chatThemeMapper.deleteChatTheme(chatThemeDTO.getBotId(), chatThemeDTO.getThemeId(), chatThemeDTO.getTenantId());
    // 移除应用关联会话血缘关系
    resourceElementService.remove(chatThemeDTO.getTenantId(), chatThemeDTO.getBotId(), theme.getId(), DataSyncCodeEnum.CHAT_THEME.getCode());
    return ResultVO.success();
  }

  @Override
  public PageInfo<ChatThemeDTO> queryChatThemePage(ChatThemeQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    PageInfo<ChatThemeDTO> pageInfo = chatThemeMapper.selectChatThemePage(queryParams, rowBounds).toPageInfo();
    // 主题数据转对象
    CollectionUtils.emptyIfNull(pageInfo.getList()).forEach(this::convertThemeJson);
    return pageInfo;
  }

  @Override
  public List<ChatThemeDTO> getThemeList(Long botId, Long tenantId) {
    List<ChatThemeDTO> resultList = new ArrayList<>();

    // 查询平台级主题数据
    ChatThemeQueryParams queryParams = new ChatThemeQueryParams();
    queryParams.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
    queryParams.setBotId(-1L);
    queryParams.setThemeScope(BaseConsts.CHAT_THEME_SCOPE_PLATFORM);
    List<ChatThemeDTO> platformThemeList = processThemeList(chatThemeMapper.selectChatThemeList(queryParams));
    List<Long> platformThemeIds = platformThemeList.stream().map(ChatThemeDTO::getThemeId).collect(Collectors.toList());
    resultList.addAll(platformThemeList);

    // 查询应用级主题数据
    queryParams.setTenantId(tenantId);
    queryParams.setBotId(botId);
    queryParams.setThemeScope(BaseConsts.CHAT_THEME_SCOPE_APP);
    List<ChatThemeDTO> appThemeList = processThemeList(chatThemeMapper.selectChatThemeList(queryParams));

    // 处理正在使用的并且时从使用平台主题时新增的应用主题：设置对应的平台主题 isUsing 为 T
    Optional<ChatThemeDTO> usedPlatformTheme = appThemeList.stream()
      .filter(p -> platformThemeIds.contains(p.getThemeId()) && BaseConsts.TRUE.equals(p.getIsUsing())).findFirst();
    usedPlatformTheme.ifPresent(theme -> {
      for (ChatThemeDTO dto : resultList) {
        if (dto.getThemeId().equals(theme.getThemeId())) {
          dto.setIsUsing(BaseConsts.TRUE);
          break;
        }
      }
    });

    // 过滤使用平台主题时新增的所有应用主题
    appThemeList = appThemeList.stream().filter(p -> !platformThemeIds.contains(p.getThemeId()))
      .collect(Collectors.toList());
    resultList.addAll(appThemeList);

    return resultList;
  }

  /**
   * 处理主题列表数据
   */
  private List<ChatThemeDTO> processThemeList(List<ChatThemeDTO> themeDTOList) {
    List<ChatThemeDTO> resultList = ListUtils.emptyIfNull(themeDTOList);
    for (ChatThemeDTO themeDTO : resultList) {
      String themeJson = themeDTO.getThemeJson();
      // 从 themeJson 中提取 primary-color
      if (StringUtils.isNotEmpty(themeJson)) {
        Map<String, Object> map = JsonUtil.parseJson(themeJson, new TypeReference<Map<String, Object>>() {
        });
        if (map != null) {
          themeDTO.setPrimaryColor(String.valueOf(map.get("--botchat-primary-color")));
        }
        themeDTO.setThemeJson(null);
      }
    }
    return resultList;
  }

  @Override
  @Nullable
  public ChatThemeDTO getThemeDetail(Long botId, Long themeId, Long tenantId) {
    // botId 为 -1 时，查询的是平台模板，平台模板的租户ID为 -1
    if (botId == -1L) {
      tenantId = BaseConsts.PLATFORM_TENANT_ID;
    }
    ChatThemeDTO chatTheme = chatThemeMapper.getChatTheme(botId, themeId, tenantId);
    return convertThemeJson(chatTheme);
  }

  @Override
  @Nullable
  public ChatThemeDTO getUsedTheme(Long botId, Long tenantId) {
    ChatThemeDTO chatTheme = chatThemeMapper.getBotUsedTheme(botId, tenantId);
    if (chatTheme == null) {
      // 存量bot缺少主题时，返回平台预置默认主题
      return chatThemeMapper.getDefaultPlatformTheme();
    }
    // 如果使用的主题是平台主题则替换为平台主题数据
    String platformThemeJson = chatThemeMapper.getPlatformThemeJson(chatTheme.getThemeId());
    if (StringUtils.isNotEmpty(platformThemeJson)) {
      chatTheme.setThemeJson(platformThemeJson);
    }
    return convertThemeJson(chatTheme);
  }

  @Override
  @Transactional
  public ResultVO<Void> switchTheme(ChatThemeDTO chatThemeDTO) {
    Long tenantId = chatThemeDTO.getTenantId();
    Long botId = chatThemeDTO.getBotId();
    Long themeId = chatThemeDTO.getThemeId();

    // 检查 botId+themeId+tenantId 主题是否已存在并且正在使用
    if (chatThemeMapper.checkIsThemeUsed(tenantId, botId, themeId)) {
      return ResultVO.success();
    }

    // 将该 botId+tenantId 下的所有主题设置为未使用
    chatThemeMapper.batchUpdateThemeNotUsing(tenantId, botId);

    // 检查 botId+themeId+tenantId 是否存在
    ChatThemeDTO theme = chatThemeMapper.getChatTheme(botId, themeId, tenantId);
    if (theme == null) {
      // 不存在则判断 themeId 是否为平台级的主题
      ChatThemeDTO platformTheme = chatThemeMapper.getChatTheme(-1L, themeId, BaseConsts.PLATFORM_TENANT_ID);
      if (platformTheme == null || !BaseConsts.CHAT_THEME_SCOPE_PLATFORM.equals(platformTheme.getThemeScope())) {
        throw new BssException("主题不存在");
      }
      // 如果是平台级则新增一条应用级主题数据，并设置为使用中
      createThemeFormPlatform(botId, tenantId, platformTheme);
    }
    else {
      // 存在则设置为状态使用中
      chatThemeMapper.updateThemeUsingStatus(tenantId, botId, themeId, BaseConsts.TRUE);
    }

    return ResultVO.success();
  }

  @Override
  @Transactional
  public void createDefaultTheme(Long botId, Long tenantId) {
    // 参数校验
    Assert.notNull(botId, "智能应用ID不能为空");
    Assert.notNull(tenantId, "租户ID不能为空");
    // 获取默认平台主题
    ChatThemeDTO defaultPlatformTheme = chatThemeMapper.getDefaultPlatformTheme();
    if (defaultPlatformTheme == null) {
      throw new BssException("缺少平台默认主题，请联系管理员。");
    }
    // 创建对应的应用主题
    createThemeFormPlatform(botId, tenantId, defaultPlatformTheme);
  }

  /**
   * 从平台主题中创建应用主题
   */
  private void createThemeFormPlatform(Long botId, Long tenantId, ChatThemeDTO platformTheme) {
    ChatThemeDTO newTheme = new ChatThemeDTO();
    newTheme.setBotId(botId);
    newTheme.setTenantId(tenantId);
    newTheme.setThemeId(platformTheme.getThemeId());
    newTheme.setThemeName(platformTheme.getThemeName());
    newTheme.setIsUsing(BaseConsts.TRUE);
    newTheme.setThemeScope(BaseConsts.CHAT_THEME_SCOPE_APP);
    newTheme.setStatusCd(BaseConsts.STATUS_CD_VALID);
    DataDifference<ChatThemeDTO> difference = DataDifferenceStarter.computeSaveAndLog(null, newTheme, true, tenantId, OperClassEnum.CHAT_THEME);
    if (difference == null) {
      throw BaseErrorConstant.NO_DIFFERENCE.toException();
    }
    // 添加应用关联会话主题的血缘关系
    resourceElementService.batchAdd(tenantId, botId, DataSyncCodeEnum.BOT.getCode(),
        Collections.singletonList(Long.valueOf(difference.getId())), DataSyncCodeEnum.CHAT_THEME.getCode());
  }

  /**
   * 转换 themeJson 数据
   */
  @Nullable
  private ChatThemeDTO convertThemeJson(@Nullable ChatThemeDTO chatThemeDTO) {
    if (chatThemeDTO == null || StringUtils.isEmpty(chatThemeDTO.getThemeJson())) {
      return chatThemeDTO;
    }
    // 将 themeJson 字符串数据转换成对象返回给前端
    chatThemeDTO.setThemeJsonMap(JsonUtil.parseJson(chatThemeDTO.getThemeJson(), new TypeReference<Map<String, Object>>() {
    }));
    chatThemeDTO.setThemeJson(null);
    return chatThemeDTO;
  }

}
