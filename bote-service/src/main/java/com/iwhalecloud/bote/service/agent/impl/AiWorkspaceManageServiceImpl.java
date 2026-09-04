package com.iwhalecloud.bote.service.agent.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.agent.memory.service.LongTermMemoryVectorService;
import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.AiWorkspaceDTO;
import com.iwhalecloud.bote.dto.agent.SimpleAiWorkspaceDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.entity.agent.AiWorkspaceEntity;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.mapper.agent.AiWorkspaceManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.service.agent.IAiWorkspaceManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 用户级的提示词管理服务实现
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Service
@RequiredArgsConstructor
public class AiWorkspaceManageServiceImpl implements IAiWorkspaceManageService {

  private static final String FILE_PATH = "agent/general/prompt/default/";

  private final AiWorkspaceManageMapper workspaceManageMapper;
  private final BotQueryMapper botQueryMapper;
  private final AttrSpecCache attrSpecCache;
  private final ModelClientCache modelClientCache;
  private final LongTermMemoryVectorService memoryVectorService;

  @Override
  public ResultVO<AiWorkspaceDTO> findAiWorkspace(Long id, String fileName, Long spaceId, Long botId) {
    AiWorkspaceDTO workspaceDTO;
    if (id != null) {
      workspaceDTO = workspaceManageMapper.getAiWorkspace(id);
    }
    else {
      workspaceDTO = new AiWorkspaceDTO();
      workspaceDTO.setFileContent(readWorkspaceFile(fileName));
      workspaceDTO.setFileName(fileName);
      workspaceDTO.setStatusCd(CommonConsts.STATUS_CD_VALID);
      workspaceDTO.setBotId(botId);
      workspaceDTO.setSpaceId(spaceId);
    }
    // 设置中文名
    List<SimpleAttrDTO> defaultPrompts = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, BaseConsts.BOTECLAW_SYSTEM_PROMPT);
    for (SimpleAttrDTO attr : CollectionUtils.emptyIfNull(defaultPrompts)) {
      if (attr.getAttrValue().equals(workspaceDTO.getFileName())) {
        workspaceDTO.setFileChineseName(attr.getAttrValueName());
      }
    }
    if (StringUtils.isEmpty(workspaceDTO.getFileChineseName())) {
      workspaceDTO.setFileChineseName(workspaceDTO.getFileName());
    }
    return ResultVO.success(workspaceDTO);
  }

  @Override
  @Transactional
  public ResultVO<AiWorkspaceDTO> saveAiWorkspace(AiWorkspaceDTO workspace) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    AiWorkspaceDTO old = workspace.getId() == null ? null : workspaceManageMapper.getAiWorkspace(workspace.getId());
    if (old != null && checkBotPermission(workspace.getBotId(), workspace.getSpaceId())) {
      old.setFileContent(workspace.getFileContent());
      old.setUpdatorId(userId);
      workspaceManageMapper.updateAiWorkspace(old);
      updateMemoryVector(old);
      return ResultVO.success(workspace);
    }
    workspace.setId(Sequences.AI_WORKSPACE_ID.next());
    workspace.setStatusCd(CommonConsts.STATUS_CD_VALID);
    workspace.setCreatorId(userId);
    workspaceManageMapper.insertAiWorkspace(workspace);
    return ResultVO.success(workspace);
  }

  /**
   * 更新记忆文件的向量存储
   */
  private void updateMemoryVector(AiWorkspaceDTO workspace) {
    if (StringUtils.isNotEmpty(workspace.getMemoryType())) {
      memoryVectorService.updateMemoryVector(workspace.getTenantId(), workspace.getCreatorId(), workspace.getBotId(),
        workspace.getSpaceId(), workspace.getFileName(), workspace.getFileContent());
    }
  }

  /**
   * 检查用户是否有权限编辑目标智能应用配置数据
   */
  private boolean checkBotPermission(Long botId, Long spaceId) {
    // 如果是通用智能应用，可以直接编辑配置数据
    if (BaseConsts.BOTE_AI_ID.equals(botId)) {
      return true;
    }
    // 查询智能应用所属创建人ID
    Long creatorId = getBotCreatorId(botId, spaceId);
    // 如果是自己创建的智能应用，可以直接编辑旧数据，否则私有化一份新的配置数据
    Long userId = SessionUtil.getLoginInfo().getUserId();
    return userId.equals(creatorId);
  }

  /**
   * 获取智能应用创建人ID
   */
  private Long getBotCreatorId(Long botId, Long spaceId) {
    SimpleBotDTO bot = botQueryMapper.selectSimpleBot(spaceId, botId);
    Assert.notNull(bot, "智能应用不存在");
    return bot.getCreatorId();
  }

  @Override
  @Transactional
  public List<AiWorkspaceDTO> queryAiWorkspaceList(AiQueryParams queryParams) {
    queryParams.setUserId(SessionUtil.getLoginInfo().getUserId());
    List<AiWorkspaceDTO> list = workspaceManageMapper.selectAiWorkspaceList(queryParams);

    // 如果是别人授权过来的智能应用，优先返回提供方的提示词配置数据
    if (!checkBotPermission(queryParams.getBotId(), queryParams.getSpaceId())) {
      queryParams.setUserId(getBotCreatorId(queryParams.getBotId(), queryParams.getSpaceId()));
      List<AiWorkspaceDTO> providerList = workspaceManageMapper.selectAiWorkspaceList(queryParams);
      // 合并提供方的提示词配置数据
      if (CollectionUtils.isNotEmpty(providerList)) {
        List<String> fileNames = list.stream().map(AiWorkspaceEntity::getFileName).toList();
        for (AiWorkspaceDTO workspace : providerList) {
          if (!fileNames.contains(workspace.getFileName())) {
            list.add(workspace);
          }
        }
      }
    }

    List<SimpleAttrDTO> defaultPrompts = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, BaseConsts.BOTECLAW_SYSTEM_PROMPT);
    if (CollectionUtils.isEmpty(list)) {
      // 初始化默认提示词数据
      list = initWorkspace(queryParams.getSpaceId(), queryParams.getBotId(), queryParams.getUserId(), defaultPrompts);
    }
    else {
      for (SimpleAttrDTO attr : CollectionUtils.emptyIfNull(defaultPrompts)) {
        AiWorkspaceDTO dto = IterableUtils.find(list, p -> attr.getAttrValue().equals(p.getFileName()));
        if (dto == null) {
          // 补充缺失的默认工作区提示词数据
          AiWorkspaceDTO workspace = new AiWorkspaceDTO();
          workspace.setCreatorId(queryParams.getUserId());
          workspace.setFileName(attr.getAttrValue());
          workspace.setFileChineseName(attr.getAttrValueName());
          workspace.setSpaceId(queryParams.getSpaceId());
          workspace.setBotId(queryParams.getBotId());
          workspace.setStatusCd(CommonConsts.STATUS_CD_VALID);
          workspace.setDesc(attr.getAttrValueDesc());
          list.add(workspace);
        }
        else {
          dto.setDesc(attr.getAttrValueDesc());
          dto.setFileChineseName(attr.getAttrValueName());
        }
      }
    }
    return list;
  }

  @Override
  @Transactional
  public void syncAiWorkspace(Long spaceId, Long botId, Long userId, List<SimpleAiWorkspaceDTO> list) {
    List<AiWorkspaceDTO> workspaces = new ArrayList<>(list.size());
    for (SimpleAiWorkspaceDTO dto : list) {
      AiWorkspaceDTO workspace = new AiWorkspaceDTO();
      workspace.setId(Sequences.AI_WORKSPACE_ID.next());
      workspace.setSpaceId(spaceId);
      workspace.setBotId(botId);
      workspace.setFileName(dto.getFileName());
      workspace.setFileContent(dto.getFileContent());
      workspace.setCreatorId(userId);
      workspace.setStatusCd(CommonConsts.STATUS_CD_VALID);
      workspaces.add(workspace);
    }
    workspaceManageMapper.batchInsertAiWorkspace(workspaces);
  }

  @Override
  public List<AiWorkspaceDTO> getSystemAiWorkspaceList() {
    List<SimpleAttrDTO> defaultPrompts = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, BaseConsts.BOTECLAW_SYSTEM_PROMPT);
    List<AiWorkspaceDTO> platformWorkspaceList = initWorkspace(null, null, SessionUtil.getLoginInfo().getUserId(), defaultPrompts);
    platformWorkspaceList.forEach(workspace -> workspace.setFileContent(readWorkspaceFile(workspace.getFileName())));
    return platformWorkspaceList;
  }

  @Override
  public ResultVO<String> generateAiWorkspace(AiQueryParams queryParams) {
    Assert.hasText(queryParams.getPromptContent(), "提示词内容不能为空");
    // 获取提示词内容
    String prompt = SystemParameter.GENERATE_AI_WORKSPACE_PROMPT.getValueFromDb();
    // 获取平台通用对话功能使用的大模型
    String modelIdStr = SystemParameter.COMMON_CHAT_MODEL_ID.getValueFromDb();
    Assert.hasLength(modelIdStr, "未配置平台通用对话功能使用的大模型 ID");
    LlmClient llmClient = modelClientCache.getLlmClient(BaseConsts.DEFAULT_TENANT_ID, Long.parseLong(modelIdStr));
    // 调用大模型生成智能体配置
    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addSystemMessage(prompt)
      .addUserMessage(queryParams.getPromptContent())
      .build();
    llmClient.chatCompletion(request);
    ChatCompletionResponse response = llmClient.chatCompletion(request);
    String content = response.getMessageContent();
    // 直接返回大模型生成的内容
    return ResultVO.success(content);
  }

  @Override
  public PageInfo<AiWorkspaceDTO> queryMemoryAiWorkspacePage(AiQueryParams queryParams) {
    queryParams.setUserId(SessionUtil.getLoginInfo().getUserId());
    // noinspection resource
    PageInfo<AiWorkspaceDTO> pageInfo = workspaceManageMapper.selectMemoryAiWorkspacePage(queryParams, queryParams.buildRowBounds()).toPageInfo();
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      pageInfo.getList().forEach(workspace -> workspace.setFileChineseName(workspace.getFileName()));
      // 将 MEMORY.md 文件置顶
      for (int i = 0; i < pageInfo.getList().size(); i++) {
        if ("MEMORY.md".equals(pageInfo.getList().get(i).getFileName())) {
          AiWorkspaceDTO memoryFile = pageInfo.getList().remove(i);
          pageInfo.getList().addFirst(memoryFile);
          break;
        }
      }
    }
    return pageInfo;
  }

  /**
   * 使用本地文件初始化默认工作区提示词
   *
   * @param spaceId
   *   空间 ID
   * @param userId
   *   用户 ID
   */
  private List<AiWorkspaceDTO> initWorkspace(Long spaceId, Long botId, Long userId, List<SimpleAttrDTO> defaultPrompts) {
    List<AiWorkspaceDTO> list = new ArrayList<>();
    for (SimpleAttrDTO attr : CollectionUtils.emptyIfNull(defaultPrompts)) {
      AiWorkspaceDTO workspace = new AiWorkspaceDTO();
      workspace.setCreatorId(userId);
      workspace.setFileName(attr.getAttrValue());
      workspace.setFileChineseName(attr.getAttrValueName());
      workspace.setSpaceId(spaceId);
      workspace.setBotId(botId);
      workspace.setStatusCd(CommonConsts.STATUS_CD_VALID);
      workspace.setDesc(attr.getAttrValueDesc());
      list.add(workspace);
    }
    return list;
  }

  /**
   * 从 classpath 读取默认提示词文件内容
   */
  private String readWorkspaceFile(String fileName) {
    String path = FILE_PATH + fileName;
    Resource resource = new ClassPathResource(path);
    if (!resource.exists()) {
      return null;
    }
    try (InputStream inputStream = resource.getInputStream()) {
      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      return null;
    }
  }
}
