package com.iwhalecloud.bote.service.chat.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.chat.ChatGroupCfgDTO;
import com.iwhalecloud.bote.mapper.chat.ChatGroupCfgMapper;
import com.iwhalecloud.bote.service.chat.IChatGroupCfgService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对话分组服务实现
 *
 * @author chen.linfa
 * @since 2025-09-08
 */
@Service
@RequiredArgsConstructor
public class ChatGroupCfgServiceImpl implements IChatGroupCfgService {

  /** 词典 - 对话分组属性 */
  private static final String CHAT_GROUP_ATTR = "CHAT_GROUP_ATTR";

  private final ChatGroupCfgMapper chatGroupCfgMapper;

  private final AttrSpecCache attrSpecCache;

  @Override
  public ChatGroupCfgDTO getGroupCfg(Long spaceId, Long userId) {
    List<String> attrs = CollectionUtils.emptyIfNull(attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, CHAT_GROUP_ATTR)).stream()
      .map(SimpleAttrDTO::getAttrValue).collect(Collectors.toList());
    List<String> showAttrs = new ArrayList<>();
    List<String> hideAttrs = new ArrayList<>();
    ChatGroupCfgDTO group = chatGroupCfgMapper.getChatGroupCfg(spaceId, userId);
    if (group == null || StringUtils.isEmpty(group.getSettingJson())) {
      group = new ChatGroupCfgDTO();
      group.setSpaceId(spaceId);
      group.setUserId(userId);
      showAttrs = attrs;
    }
    else {
      List<String> list = JsonUtil.parseJson(group.getSettingJson(), new TypeReference<>() {
      });
      for (String attr : CollectionUtils.emptyIfNull(attrs)) {
        boolean exists = IterableUtils.matchesAny(CollectionUtils.emptyIfNull(list), p -> Objects.equals(p, attr));
        if (exists) {
          showAttrs.add(attr);
        }
        else {
          hideAttrs.add(attr);
        }
      }
    }
    Map<String, List<String>> setting = new HashMap<>();
    setting.put("show", showAttrs);
    setting.put("hide", hideAttrs);
    group.setSetting(setting);
    group.setSettingJson(null);
    return group;
  }

  @Override
  @Transactional
  public ResultVO<ChatGroupCfgDTO> saveGroupCfg(ChatGroupCfgDTO group) {
    String settingJson = null;
    if (MapUtils.isNotEmpty(group.getSetting()) && group.getSetting().containsKey("show")) {
      List<String> show = group.getSetting().get("show");
      if (CollectionUtils.isNotEmpty(show)) {
        settingJson = JsonUtil.toJsonString(show);
      }
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    group.setSettingJson(settingJson);
    group.setUserId(userId);
    group.setStatusCd(BaseConsts.STATUS_CD_VALID);
    group.setCreatorId(userId);
    group.setUpdatorId(userId);

    ChatGroupCfgDTO data = chatGroupCfgMapper.getChatGroupCfg(group.getSpaceId(), userId);
    if (data != null) {
      group.setId(data.getId());
      chatGroupCfgMapper.updateChatGroupCfg(group);
    }
    else {
      group.setId(IDUtils.nextId());
      chatGroupCfgMapper.insertChatGroupCfg(group);
    }
    return ResultVO.success(group);
  }
}
