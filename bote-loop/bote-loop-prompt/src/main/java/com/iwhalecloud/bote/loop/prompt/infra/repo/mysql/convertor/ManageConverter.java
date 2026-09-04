package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.entity.loop.prompt.PromptBasicEntity;
import com.iwhalecloud.bote.entity.loop.prompt.PromptCommitEntity;
import com.iwhalecloud.bote.entity.loop.prompt.PromptUserDraftEntity;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptType;
import com.iwhalecloud.bote.loop.prompt.domain.entity.CommitInfo;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DraftInfo;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Message;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ModelConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptBasic;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptCommit;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDetail;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDraft;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptTemplate;
import com.iwhalecloud.bote.loop.prompt.domain.entity.TemplateType;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Tool;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ToolCallConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.VariableDef;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptIDUserIDPair;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Prompt转换器类
 * 对应Go中的convertor包
 */
public final class ManageConverter {

  private ManageConverter() {
    // 工具类，禁止实例化
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 批量转换Basic和Draft PO到Prompt DO
   */
  public static List<Prompt> batchBasicAndDraftPO2PromptDO(
    List<PromptBasicEntity> basicPOs,
    Map<PromptIDUserIDPair, PromptUserDraftEntity> draftPOMap,
    String userId) {

    if (basicPOs == null || basicPOs.isEmpty()) {
      return null;
    }

    List<Prompt> promptDOs = new ArrayList<>();
    for (PromptBasicEntity basicPO : basicPOs) {
      PromptIDUserIDPair key = new PromptIDUserIDPair(basicPO.getId(), userId);
      PromptUserDraftEntity draftPO = draftPOMap.get(key);

      Prompt promptDO = promptPO2DO(basicPO, null, draftPO);
      if (promptDO != null) {
        promptDOs.add(promptDO);
      }
    }

    return promptDOs.isEmpty() ? null : promptDOs;
  }

  /**
   * 批量转换Basic PO到Prompt DO
   */
  public static List<Prompt> batchBasicPO2PromptDO(List<PromptBasicEntity> basicPOs) {
    if (basicPOs == null || basicPOs.isEmpty()) {
      return Collections.emptyList();
    }

    List<Prompt> promptDOs = new ArrayList<>();
    for (PromptBasicEntity basicPO : basicPOs) {
      Prompt promptDO = promptPO2DO(basicPO, null, null);
      if (promptDO != null) {
        promptDOs.add(promptDO);
      }
    }

    return promptDOs.isEmpty() ? Collections.emptyList() : promptDOs;
  }

  /**
   * 转换Prompt PO到DO
   */
  public static Prompt promptPO2DO(PromptBasicEntity basicPO, PromptCommitEntity commitPO, PromptUserDraftEntity draftPO) {
    if (basicPO == null) {
      return null;
    }

    return new Prompt(
      basicPO.getId(),
      basicPO.getSpaceId(),
      basicPO.getPromptKey(),
      basicPO2DO(basicPO),
      draftPO2DO(draftPO),
      commitPO2DO(commitPO),
      basicPO.getCatalogItemId()
    );
  }

  /**
   * 转换Basic PO到DO
   */
  public static PromptBasic basicPO2DO(PromptBasicEntity promptPO) {
    if (promptPO == null) {
      return null;
    }

    PromptBasic basic = new PromptBasic();
    basic.setDisplayName(promptPO.getName());
    basic.setDescription(promptPO.getDescription());
    basic.setLatestVersion(promptPO.getLatestVersion());
    basic.setCreatedBy(promptPO.getCreatedBy());
    basic.setUpdatedBy(promptPO.getUpdatedBy());
    basic.setCreatedAt(promptPO.getCreatedAt());
    basic.setUpdatedAt(promptPO.getUpdatedAt());
    basic.setLatestCommittedAt(promptPO.getLatestCommitTime());
    // 转换 promptType: String -> PromptType 枚举
    if (promptPO.getPromptType() != null) {
      try {
        basic.setPromptType(PromptType.fromValue(promptPO.getPromptType()));
      } catch (IllegalArgumentException e) {
        // 如果字符串值无效，设置为 null
        basic.setPromptType(null);
      }
    }

    return basic;
  }

  /**
   * 批量获取CommitInfo DO
   */
  public static List<CommitInfo> batchGetCommitInfoDOFromCommitDO(List<PromptCommit> commitDOs) {
    if (commitDOs == null || commitDOs.isEmpty()) {
      return null;
    }

    List<CommitInfo> commitInfoDOs = new ArrayList<>();
    for (PromptCommit commitDO : commitDOs) {
      if (commitDO != null && commitDO.getCommitInfo() != null) {
        commitInfoDOs.add(commitDO.getCommitInfo());
      }
    }

    return commitInfoDOs.isEmpty() ? null : commitInfoDOs;
  }

  /**
   * 批量转换Commit PO到DO
   */
  public static List<PromptCommit> batchCommitPO2DO(List<PromptCommitEntity> commitPOs) {
    if (commitPOs == null) {
      return null;
    }

    List<PromptCommit> commitDOs = new ArrayList<>();
    for (PromptCommitEntity commitPO : commitPOs) {
      commitDOs.add(commitPO2DO(commitPO));
    }

    return commitDOs.isEmpty() ? null : commitDOs;
  }

  /**
   * 转换Commit PO到DO
   */
  public static PromptCommit commitPO2DO(PromptCommitEntity commitPO) {
    if (commitPO == null) {
      return null;
    }

    CommitInfo commitInfo = new CommitInfo(
      commitPO.getVersion(),
      commitPO.getBaseVersion(),
      commitPO.getDescription(),
      commitPO.getCommittedBy(),
      commitPO.getCreatedAt()
    );

    PromptDetail promptDetail = promptCommitPO2PromptDetailDO(commitPO);

    return new PromptCommit(promptDetail, commitInfo);
  }

  /**
   * 转换Prompt DO到Basic PO
   */
  public static PromptBasicEntity promptDO2BasicPO(Prompt prompt) {
    if (prompt == null || prompt.getPromptBasic() == null) {
      return null;
    }

    PromptBasicEntity basic = new PromptBasicEntity();
    basic.setId(prompt.getId());
    basic.setSpaceId(prompt.getSpaceId());
    basic.setPromptKey(prompt.getPromptKey());
    basic.setName(prompt.getPromptBasic().getDisplayName());
    basic.setDescription(prompt.getPromptBasic().getDescription());
    basic.setCreatedBy(prompt.getPromptBasic().getCreatedBy());
    basic.setUpdatedBy(prompt.getPromptBasic().getUpdatedBy());
    // 新增字段：提交状态，默认为0（未提交）
    basic.setCommitStatus(0);
    String latestVersion = prompt.getPromptBasic().getLatestVersion();
    basic.setLatestVersion(latestVersion == null ? "" : latestVersion);
    Date createdAt = prompt.getPromptBasic().getCreatedAt();
    Date updatedAt = prompt.getPromptBasic().getUpdatedAt();
    Date now = new Date();
    basic.setCreatedAt(createdAt == null ? now : createdAt);
    basic.setUpdatedAt(updatedAt == null ? now : updatedAt);
    // 新增字段：删除时间，默认为0（未删除）
    basic.setDeletedAt(0L);
    basic.setCatalogItemId(prompt.getCatalogItemId());
    // 转换 promptType: PromptType 枚举 -> String
    if (prompt.getPromptBasic().getPromptType() != null) {
      basic.setPromptType(prompt.getPromptBasic().getPromptType().getValue());
    }
    return basic;
  }

  /**
   * 转换Prompt DO到Commit PO
   */
  public static PromptCommitEntity promptDO2CommitPO(Prompt prompt) {
    if (prompt == null) {
      return null;
    }

    PromptCommitEntity commit = buildBasicCommitEntity(prompt);
    setCommitInfo(commit, prompt);
    setPromptDetail(commit, prompt);

    return commit;
  }

  private static PromptCommitEntity buildBasicCommitEntity(Prompt prompt) {
    PromptCommitEntity commit = new PromptCommitEntity();
    // 注意：id字段在调用此方法前已经通过commitPO.setId(commitID)设置
    commit.setSpaceId(prompt.getSpaceId());
    commit.setPromptId(prompt.getId());
    commit.setPromptKey(prompt.getPromptKey());
    return commit;
  }

  private static void setCommitInfo(PromptCommitEntity commit, Prompt prompt) {
    if (prompt.getPromptCommit() != null && prompt.getPromptCommit().getCommitInfo() != null) {
      CommitInfo commitInfo = prompt.getPromptCommit().getCommitInfo();
      commit.setVersion(commitInfo.getVersion());
      commit.setBaseVersion(commitInfo.getBaseVersion());
      commit.setCommittedBy(commitInfo.getCommittedBy());
      commit.setCreatedAt(commitInfo.getCommittedAt());
      commit.setUpdatedAt(commitInfo.getCommittedAt());
      commit.setDescription(commitInfo.getDescription());
    }
  }

  private static void setPromptDetail(PromptCommitEntity commit, Prompt prompt) {
    if (prompt.getPromptCommit() != null && prompt.getPromptCommit().getPromptDetail() != null) {
      PromptDetail detail = prompt.getPromptCommit().getPromptDetail();
      setModelConfig(commit, detail);
      setTools(commit, detail);
      setToolCallConfig(commit, detail);
      setPromptTemplate(commit, detail);
    }
  }

  private static void setModelConfig(PromptCommitEntity commit, PromptDetail detail) {
    if (detail.getModelConfig() != null) {
      commit.setModelConfig(jsonify(detail.getModelConfig()));
    }
  }

  private static void setTools(PromptCommitEntity commit, PromptDetail detail) {
    if (detail.getTools() != null) {
      commit.setTools(jsonify(detail.getTools()));
    }
  }

  private static void setToolCallConfig(PromptCommitEntity commit, PromptDetail detail) {
    if (detail.getToolCallConfig() != null) {
      commit.setToolCallConfig(jsonify(detail.getToolCallConfig()));
    }
  }

  private static void setPromptTemplate(PromptCommitEntity commit, PromptDetail detail) {
    if (detail.getPromptTemplate() != null) {
      commit.setTemplateType(detail.getPromptTemplate().getTemplateType().getValue());
      setTemplateMessages(commit, detail.getPromptTemplate());
      setTemplateVariableDefs(commit, detail.getPromptTemplate());
    }
  }

  private static void setTemplateMessages(PromptCommitEntity commit, PromptTemplate template) {
    if (template.getMessages() != null) {
      commit.setMessages(jsonify(template.getMessages()));
    }
  }

  private static void setTemplateVariableDefs(PromptCommitEntity commit, PromptTemplate template) {
    if (template.getVariableDefs() != null) {
      commit.setVariableDefs(jsonify(template.getVariableDefs()));
    }
  }

  /**
   * 转换Prompt DO到Draft PO
   */
  public static PromptUserDraftEntity promptDO2DraftPO(Prompt prompt) {
    if (prompt == null) {
      return null;
    }

    PromptUserDraftEntity draft = buildBasicDraftEntity(prompt);
    setDraftDetail(draft, prompt);
    setDraftInfo(draft, prompt);

    return draft;
  }

  private static PromptUserDraftEntity buildBasicDraftEntity(Prompt prompt) {
    PromptUserDraftEntity draft = new PromptUserDraftEntity();
    draft.setSpaceId(prompt.getSpaceId());
    draft.setPromptId(prompt.getId());
    return draft;
  }

  private static void setDraftDetail(PromptUserDraftEntity draft, Prompt prompt) {
    if (prompt.getPromptDraft() != null && prompt.getPromptDraft().getPromptDetail() != null) {
      PromptDetail detailDO = prompt.getPromptDraft().getPromptDetail();
      setDraftTemplate(draft, detailDO);
      setDraftModelConfig(draft, detailDO);
      setDraftTools(draft, detailDO);
      setDraftToolCallConfig(draft, detailDO);
    }
  }

  private static void setDraftTemplate(PromptUserDraftEntity draft, PromptDetail detailDO) {
    if (detailDO.getPromptTemplate() != null) {
      draft.setTemplateType(detailDO.getPromptTemplate().getTemplateType().getValue());
      setDraftTemplateMessages(draft, detailDO.getPromptTemplate());
      setDraftTemplateVariableDefs(draft, detailDO.getPromptTemplate());
    }
  }

  private static void setDraftTemplateMessages(PromptUserDraftEntity draft, PromptTemplate template) {
    if (template.getMessages() != null) {
      draft.setMessages(jsonify(template.getMessages()));
    }
  }

  private static void setDraftTemplateVariableDefs(PromptUserDraftEntity draft, PromptTemplate template) {
    if (template.getVariableDefs() != null) {
      draft.setVariableDefs(jsonify(template.getVariableDefs()));
    }
  }

  private static void setDraftModelConfig(PromptUserDraftEntity draft, PromptDetail detailDO) {
    if (detailDO.getModelConfig() != null) {
      draft.setModelConfig(jsonify(detailDO.getModelConfig()));
    }
  }

  private static void setDraftTools(PromptUserDraftEntity draft, PromptDetail detailDO) {
    if (detailDO.getTools() != null) {
      draft.setTools(jsonify(detailDO.getTools()));
    }
  }

  private static void setDraftToolCallConfig(PromptUserDraftEntity draft, PromptDetail detailDO) {
    if (detailDO.getToolCallConfig() != null) {
      draft.setToolCallConfig(jsonify(detailDO.getToolCallConfig()));
    }
  }

  private static void setDraftInfo(PromptUserDraftEntity draft, Prompt prompt) {
    if (prompt.getPromptDraft() != null && prompt.getPromptDraft().getDraftInfo() != null) {
      DraftInfo infoDO = prompt.getPromptDraft().getDraftInfo();
      draft.setUserId(infoDO.getUserId());
      draft.setBaseVersion(infoDO.getBaseVersion() == null ? "" : infoDO.getBaseVersion());
      draft.setIsDraftEdited(marshalBool(infoDO.getIsModified()));
    }
  }

  /**
   * 转换Draft PO到DO
   */
  public static PromptDraft draftPO2DO(PromptUserDraftEntity draftPO) {
    if (draftPO == null) {
      return null;
    }

    DraftInfo draftInfo = new DraftInfo(
      draftPO.getUserId(),
      draftPO.getBaseVersion(),
      unmarshalBool(draftPO.getIsDraftEdited()),
      draftPO.getCreatedAt(),
      draftPO.getUpdatedAt()
    );

    PromptDetail promptDetail = promptUserDraftPO2PromptDetailDO(draftPO);

    return new PromptDraft(promptDetail, draftInfo);
  }

  /**
   * 转换PromptUserDraft PO到PromptDetail DO
   */
  public static PromptDetail promptUserDraftPO2PromptDetailDO(PromptUserDraftEntity draftPO) {
    if (draftPO == null) {
      return null;
    }

    PromptTemplate promptTemplate = new PromptTemplate();
    promptTemplate.setMessages(unmarshalMessageDOs(draftPO.getMessages()));
    promptTemplate.setVariableDefs(unmarshalVariableDefDOs(draftPO.getVariableDefs()));
    promptTemplate.setTemplateType(unmarshalTemplateType(draftPO.getTemplateType()));

    return new PromptDetail(
      promptTemplate,
      unmarshalToolDOs(draftPO.getTools()),
      unmarshalToolCallConfig(draftPO.getToolCallConfig()),
      unmarshalModelConfig(draftPO.getModelConfig())
    );
  }

  /**
   * 转换PromptCommit PO到PromptDetail DO
   */
  public static PromptDetail promptCommitPO2PromptDetailDO(PromptCommitEntity commitPO) {
    if (commitPO == null) {
      return null;
    }

    PromptTemplate promptTemplate = new PromptTemplate();
    promptTemplate.setMessages(unmarshalMessageDOs(commitPO.getMessages()));
    promptTemplate.setVariableDefs(unmarshalVariableDefDOs(commitPO.getVariableDefs()));
    promptTemplate.setTemplateType(unmarshalTemplateType(commitPO.getTemplateType()));

    return new PromptDetail(
      promptTemplate,
      unmarshalToolDOs(commitPO.getTools()),
      unmarshalToolCallConfig(commitPO.getToolCallConfig()),
      unmarshalModelConfig(commitPO.getModelConfig())
    );
  }

  // 辅助方法
  private static List<Message> unmarshalMessageDOs(String text) {
    if (text == null) {
      return null;
    }
    try {
      return objectMapper.readValue(text, new TypeReference<>() {
      });
    }
    catch (JsonProcessingException e) {
      return new ArrayList<>();
    }
  }

  private static List<VariableDef> unmarshalVariableDefDOs(String text) {
    if (text == null) {
      return null;
    }
    try {
      return objectMapper.readValue(text, new TypeReference<>() {
      });
    }
    catch (JsonProcessingException e) {
      return new ArrayList<>();
    }
  }

  private static TemplateType unmarshalTemplateType(String text) {
    return TemplateType.fromValue(text);
  }

  private static ToolCallConfig unmarshalToolCallConfig(String text) {
    if (text == null) {
      return null;
    }
    try {
      return objectMapper.readValue(text, ToolCallConfig.class);
    }
    catch (JsonProcessingException e) {
      return null;
    }
  }

  private static ModelConfig unmarshalModelConfig(String text) {
    if (text == null) {
      return null;
    }
    try {
      return objectMapper.readValue(text, ModelConfig.class);
    }
    catch (JsonProcessingException e) {
      return null;
    }
  }

  private static List<Tool> unmarshalToolDOs(String text) {
    if (text == null) {
      return null;
    }
    try {
      return objectMapper.readValue(text, new TypeReference<List<Tool>>() {
      });
    }
    catch (JsonProcessingException e) {
      return new ArrayList<>();
    }
  }

  private static Boolean unmarshalBool(Integer val) {
    return val != null && val != 0;
  }

  private static Integer marshalBool(Boolean val) {
    return val != null && val ? 1 : 0;
  }

  private static String jsonify(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    }
    catch (JsonProcessingException e) {
      return null;
    }
  }

}
