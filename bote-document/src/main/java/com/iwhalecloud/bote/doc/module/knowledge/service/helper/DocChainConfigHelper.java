package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainUploadFileDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.request.DocChainTopicRequest;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryDocmentResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryDocmentResponse.DocmentInfo;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantDocChainAccountSettingDTO;
import com.iwhalecloud.bote.mapper.portal.TenantQueryMapper;
import com.iwhalecloud.bote.service.portal.ITenantSettingInfoManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * DocChain 知识库配置辅助类
 *
 * @author chen.linfa
 * @since 2024-10-16
 */
@Component
@RequiredArgsConstructor
public class DocChainConfigHelper {
  private final DocChainLoginHelper loginHelper;

  private final DocChainTopicHelper topicHelper;

  private final DocChainDocumentHelper documentHelper;

  private final TenantQueryMapper tenantQueryMapper;

  private final ITenantSettingInfoManageService settingInfoManageService;

  /**
   * 创建 DocChain 账号，已有账号时不处理
   * <p>
   * 注意: 不要放在事务中执行；创建了账号时需要刷新租户设置缓存。
   * </p>
   *
   * @param tenantId 租户 ID
   * @return 是否创建了账号。已有账号时返回 false
   */
  public boolean createDocChainAccount(Long tenantId) {
    // 查询账号
    TenantSettingInfoDTO setting = settingInfoManageService.findTenantSettingInfo(tenantId, CommonConsts.FUNC_TYPE_KNOWLEDGE);
    TenantDocChainAccountSettingDTO account;
    if (setting == null) {
      setting = new TenantSettingInfoDTO();
      setting.setTenantId(tenantId);
      setting.setFuncType(CommonConsts.FUNC_TYPE_KNOWLEDGE);
      account = new TenantDocChainAccountSettingDTO();
    }
    else {
      account = JsonUtil.parseJson(setting.getSettingInfo(), TenantDocChainAccountSettingDTO.class);
      if (account == null) {
        account = new TenantDocChainAccountSettingDTO();
      }
    }

    // 已有账号：兼容旧 password 字段，并补齐 token / apiKey / enabled
    String existingSecret = StringUtils.defaultIfEmpty(account.getToken(), account.getPassword());
    if (StringUtils.isNotEmpty(account.getUserName()) && StringUtils.isNotEmpty(existingSecret)) {
      boolean needUpdate = false;
      if (StringUtils.isEmpty(account.getToken()) && StringUtils.isNotEmpty(account.getPassword())) {
        account.setToken(account.getPassword());
        needUpdate = true;
      }
      if (StringUtils.isEmpty(account.getApiKey())) {
        String apiKey = loginHelper.generateApiKey(CommonConsts.COPILOT_TENANT_ID, account.getUserName());
        if (StringUtils.isNotEmpty(apiKey)) {
          account.setApiKey(apiKey);
          needUpdate = true;
        }
      }
      if (!Boolean.TRUE.equals(account.getEnabled())) {
        account.setEnabled(true);
        needUpdate = true;
      }
      if (needUpdate) {
        setting.setSettingInfo(JsonUtil.toJsonString(account));
        settingInfoManageService.saveTenantSettingInfo(setting);
        return true;
      }
      return false;
    }

    // 创建 DocChain 账号
    String userName = "bote_" + tenantQueryMapper.getTenantCode(tenantId);
    String password = BaseSystemParameter.DOC_CHAIN_USER_PASSWORD.getValueFromDb();
    loginHelper.register(tenantId, userName, password);

    // base64 解密
    password = new String(Base64.decodeBase64(password), StandardCharsets.UTF_8);
    // AES 加密
    password = AesUtil.aesEncrypt(password, BaseSystemParameter.ENCRYPTION_AES.getValueFromDb());
    // 保存账号信息
    account.setUserName(userName);
    account.setToken(password);
    account.setEnabled(true);
    String apiKey = loginHelper.generateApiKey(CommonConsts.COPILOT_TENANT_ID, userName);
    account.setApiKey(apiKey);
    setting.setSettingInfo(JsonUtil.toJsonString(account));
    settingInfoManageService.saveTenantSettingInfo(setting);
    return true;
  }

  /**
   * 保存知识库场景，DocChain 功能对接
   * <p>
   * 2.知识库新增场景，需要创建主题
   * </p>
   * <p>
   * 3.修改场景，如果知识库名称、备注发生变化，需要修改主题信息
   * </p>
   * <p>
   * 4.如果带有新的文档数据，需要创建文档
   * </p>
   */
  public void saveKnowledge(KnowledgeBaseDTO dto, KnowledgeBaseDTO old) {
    Long tenantId = dto.getTenantId();
    DocChainTopicRequest request = new DocChainTopicRequest(dto.getKnowledgeName(), dto.getKnowledgeDesc(),
      dto.getKnowledgeStrategy());
    if (old == null) {
      // 新增场景
      Long topicId = topicHelper.createTopic(tenantId, request);
      dto.setTopicId(topicId);
    }
    else {
      // 修改场景
      if (!Objects.equals(dto.getKnowledgeName(), old.getKnowledgeName())
        || !Objects.equals(dto.getKnowledgeDesc(), old.getKnowledgeDesc())
        || !Objects.equals(dto.getKnowledgeStrategy(), old.getKnowledgeStrategy())) {
        // request.setTopicId(old.getTopicId());
        // topicHelper.modifyTopic(tenantId, request);
        // 如果在修改知识库的时候，docchain这边并没有存在主题那么就修改不了，就需要走创建，因为如果名字不规范，在更新学习知识库就会报知识库名称不规范，创建不了dochain的知识库
        dto.setTopicId(rebuildKnowledge(dto));
      }
    }
    // 处理文档
    saveDocument(tenantId, dto.getTopicId(), dto.getDocuments(), old == null ? null : old.getDocuments());
  }
  public void deleteKnowledgeBase(Long tenantId, Long topicId) {
    topicHelper.deleteTopic(tenantId, topicId);
  }

  public void existsTopicId(Long tenantId, Long topicId) {
    List<Map<String, Object>> topics = topicHelper.queryTopic(tenantId);
    boolean exists = IterableUtils.matchesAny(CollectionUtils.emptyIfNull(topics),
      p -> Objects.equals(topicId, MapUtils.getLong(p, "id")));
    if (!exists) {
      throw new BssException("关联知识库主题不存在！主题id为：" + topicId);
    }
  }

  public Long rebuildKnowledge(KnowledgeBaseDTO dto) {
    Long tenantId = dto.getTenantId();
    List<Map<String, Object>> topics = topicHelper.queryTopic(tenantId);
    boolean exists = IterableUtils.matchesAny(CollectionUtils.emptyIfNull(topics),
      p -> Objects.equals(dto.getTopicId(), MapUtils.getLong(p, "id")));
    Long topicId = dto.getTopicId();
    boolean shouldCreate = false;
    if (!exists) {
      // 新增情况，如果已存在名称相同的主题，采用修改方式
      Map<String, Object> data = IterableUtils.find(CollectionUtils.emptyIfNull(topics),
        p -> Objects.equals(dto.getKnowledgeName(), MapUtils.getString(p, "name")));
      if (MapUtils.isNotEmpty(data)) {
        topicId = MapUtils.getLong(data, "id");
      }
      else {
        shouldCreate = true;
      }
    }
    DocChainTopicRequest request = new DocChainTopicRequest(dto.getKnowledgeName(), dto.getKnowledgeDesc(), dto.getKnowledgeStrategy());
    if (shouldCreate) {
      return topicHelper.createTopic(tenantId, request);
    }
    request.setTopicId(topicId);
    topicHelper.modifyTopic(tenantId, request);
    return topicId;
  }

  public void redoDocuments(Long tenantId, Long topicId) {
    topicHelper.redoDocuments(tenantId, topicId);
  }

  public Map<Long, Long> rebuildDocument(Long tenantId, Long topicId, List<Long> fileIds) {
    return documentHelper.uploadDocument(tenantId, topicId, fileIds);
  }

  public void saveDocument(Long tenantId, Long topicId, List<DocumentDTO> documents, List<DocumentDTO> oldDocuments) {
    if (CollectionUtils.isEmpty(documents)) {
      return;
    }
    List<Long> fileIds = new ArrayList<>();
    // 只处理新增的文档
    for (DocumentDTO dto : CollectionUtils.emptyIfNull(documents)) {
      DocumentDTO document = IterableUtils.find(CollectionUtils.emptyIfNull(oldDocuments),
        p -> Objects.equals(p.getFileId(), dto.getFileId()));
      if (document == null && dto.getFileId() != null) {
        fileIds.add(dto.getFileId());
      }
    }
    if (CollectionUtils.isEmpty(fileIds)) {
      return;
    }
    Map<Long, Long> mapping = documentHelper.uploadDocument(tenantId, topicId, fileIds);
    for (DocumentDTO document : documents) {
      document.setParseStartedTime(new Date());
      document.setExtSystemId(mapping.get(document.getFileId()));
      document.setDocStatus(KnowledgeConsts.DOCUMENT_STATUS_ANALYZING);
    }
  }

  public void saveDocumentNew(Long tenantId, Long topicId, List<DocumentDTO> documents, List<DocumentDTO> oldDocuments) {
    if (CollectionUtils.isEmpty(documents)) {
      return;
    }
    List<String> dcDocmentIds = new ArrayList<>();
    // 只处理新增的文档
    for (DocumentDTO dto : CollectionUtils.emptyIfNull(documents)) {
      DocumentDTO document = IterableUtils.find(CollectionUtils.emptyIfNull(oldDocuments),
        p -> Objects.equals(p.getDcDocumentId(), dto.getDcDocumentId()));
      if (document == null && dto.getDcDocumentId() != null) {
        dcDocmentIds.add(dto.getDcDocumentId());
      }
    }
    if (CollectionUtils.isEmpty(dcDocmentIds)) {
      return;
    }
    DocChainUploadFileDTO mapping = documentHelper.uploadDocumentNew(tenantId, topicId, dcDocmentIds);
    Date date = new Date();
    for (DocumentDTO document : documents) {
      document.setParseStartedTime(date);
      document.setFileSize(mapping.getFileSizes().get(document.getDcDocumentId()));
      document.setExtSystemId(mapping.getDocIds().get(document.getDcDocumentId()));
      document.setDocStatus(KnowledgeConsts.DOCUMENT_STATUS_ANALYZING);
      document.setHasUpdate("0");
    }
  }
  public void deleteDocument(Long tenantId, Long docId) {
    documentHelper.deleteDocument(tenantId, docId);
  }

  public void redoDocument(Long tenantId, Long docId) {
    documentHelper.redoDocument(tenantId, docId);
  }

  public void summaryDocument(Long tenantId, Long docId) {
    documentHelper.summaryDocument(tenantId, docId);
  }

  public List<String> readDocument(Long tenantId, Long docId) {
    return Arrays.asList(StringUtils.split(documentHelper.readDocument(tenantId, docId, null), '\n'));
  }

  public List<QueryDocmentResponse.DocmentInfo> queryDocument(Long tenantId, Long topicId) {
    return documentHelper.queryDocument(tenantId, topicId);
  }

  public QueryDocmentResponse.DocmentPageInfo queryDocumentPage(Long tenantId, Long topicId, String keyword,
    int pageNum, int pageSize) {
    return documentHelper.queryDocumentPage(tenantId, topicId, keyword, pageNum, pageSize);
  }

  public boolean existsTopic(Long tenantId, Long topicId) {
    List<Map<String, Object>> topics = topicHelper.queryTopic(tenantId);
    return IterableUtils.matchesAny(CollectionUtils.emptyIfNull(topics), p -> Objects.equals(topicId, MapUtils.getLong(p, "id")));
  }

  public boolean existsTopicDocId(Long tenantId, Long topicId, Long docId) {
    List<DocmentInfo> docmentInfos = queryDocument(tenantId, topicId);
    return IterableUtils.matchesAny(CollectionUtils.emptyIfNull(docmentInfos), p -> Objects.equals(docId, p.getId()));
  }

}
