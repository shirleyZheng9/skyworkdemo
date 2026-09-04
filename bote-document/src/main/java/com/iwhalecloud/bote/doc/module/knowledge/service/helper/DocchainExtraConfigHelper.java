package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainExtraCfgDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainExtraDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeBaseManageMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Comparator;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * DocChain 知识信息策略设置
 *
 * @author chen.linfa
 * @since 2025-07-14
 */
@Component
@RequiredArgsConstructor
public class DocchainExtraConfigHelper {
  private final KnowledgeBaseManageMapper mapper;
  private final DocumentManageMapper documentManageMapper;
  private final DocChainConfigHelper docChainConfigHelper;

  public List<DocChainExtraDTO> getDocChainExtraConfig() {
    List<DocChainExtraCfgDTO> cfgs = mapper.selectAllDocChainExtraCfg();
    DocChainExtraCfgDTO root = IterableUtils.find(cfgs, p -> DocBaseConsts.TRUE.equals(p.getIsRoot()));
    List<DocChainExtraDTO> roots = JsonUtil.parseJson(root.getTip(), new TypeReference<List<DocChainExtraDTO>>() {
    });
    Assert.notEmpty(roots, "预置的 DocChain 主题扩展参数定义异常，请联系系统管理员");
    // 获取默认的策略设置
    Map<String, Object> defaultExtra = getDefaultExtra();
    Map<String, List<DocChainExtraCfgDTO>> groupCfgs = cfgs.stream().filter(p -> !DocBaseConsts.TRUE.equals(p.getIsRoot()))
      .collect(Collectors.groupingBy(DocChainExtraCfgDTO::getExtGroupName));
    for (DocChainExtraDTO extra : roots) {
      fillGroupAttrsAndDefaults(extra.getGroups(), groupCfgs, defaultExtra);
    }
    return roots;
  }

  /**
   * 递归处理分组树：tip 中可嵌套 groups，需按 groupName 挂载 attrs 并写入默认值。
   */
  private void fillGroupAttrsAndDefaults(List<DocChainExtraDTO.DocChainExtraGroupDTO> groups,
    Map<String, List<DocChainExtraCfgDTO>> groupCfgs, Map<String, Object> defaultExtra) {
    if (CollectionUtils.isEmpty(groups)) {
      return;
    }
    for (DocChainExtraDTO.DocChainExtraGroupDTO group : groups) {
      List<DocChainExtraCfgDTO> attrs = groupCfgs.get(group.getGroupName());
      if (CollectionUtils.isNotEmpty(attrs)) {
        attrs.sort(Comparator.comparing(DocChainExtraCfgDTO::getSortby));
        group.setAttrs(attrs);
        setDefaultValue(group, defaultExtra);
      }
      fillGroupAttrsAndDefaults(group.getGroups(), groupCfgs, defaultExtra);
    }
  }

  /**
   * 修改场景，如果某些策略参数发生变动，需要自动触发重新构建知识下所有文档
   */
  public void processRedoDocument(KnowledgeBaseDTO knowledge, @Nullable KnowledgeBaseDTO oldKnowledge) {
    if (shouldSkip(knowledge, oldKnowledge)) {
      return;
    }
    boolean isRedo = StringUtils.isEmpty(oldKnowledge.getKnowledgeStrategy());
    if (!isRedo) {
      // 分析变动的策略参数
      Map<String, Object> newStrategy = JsonUtil.parseJson(knowledge.getKnowledgeStrategy(), new TypeReference<Map<String, Object>>() {
      });
      Map<String, Object> oldStrategy = JsonUtil.parseJson(oldKnowledge.getKnowledgeStrategy(), new TypeReference<Map<String, Object>>() {
      });
      List<DocChainExtraCfgDTO> cfgs = mapper.selectAllDocChainExtraCfg().stream().filter(p -> DocBaseConsts.TRUE.equals(p.getIsRedo()))
        .collect(Collectors.toList());
      for (DocChainExtraCfgDTO cfg : CollectionUtils.emptyIfNull(cfgs)) {
        isRedo = equals(cfg.getGroupCode(), cfg.getCode(), newStrategy, oldStrategy);
        if (isRedo) {
          break;
        }
      }
    }
    if (isRedo) {
      docChainConfigHelper.redoDocuments(knowledge.getTenantId(), knowledge.getTopicId());
      // 成功后，需要把所有文档状态重置为处理中
      Long userId = SessionUtil.getLoginInfo().getUserId();
      documentManageMapper.updateDocStatusByKnowledgeId(knowledge.getTenantId(), knowledge.getKnowledgeId(),
        KnowledgeConsts.DOCUMENT_STATUS_ANALYZING, userId);
    }
  }

  private boolean shouldSkip(KnowledgeBaseDTO knowledge, @Nullable KnowledgeBaseDTO oldKnowledge) {
    // 例外场景：新增、策略没有发生变化、关联类型的知识库
    return oldKnowledge == null
      || checkStrategyEmptyState(knowledge, oldKnowledge)
      || Objects.equals(knowledge.getKnowledgeStrategy(), oldKnowledge.getKnowledgeStrategy())
      || DocBaseConsts.TRUE.equals(knowledge.getIsExist());
  }

  private boolean checkStrategyEmptyState(KnowledgeBaseDTO knowledge, KnowledgeBaseDTO oldKnowledge) {
    if (StringUtils.isEmpty(oldKnowledge.getKnowledgeStrategy())) {
      String template = BaseSystemParameter.DOCCHAIN_TOPIC_API_REQUEST.getValueFromDb();
      Map<String, Object> params = new HashMap<>(4);
      params.put("topicId", oldKnowledge.getKnowledgeId());
      params.put("topicName", oldKnowledge.getKnowledgeName());
      params.put("comment", oldKnowledge.getKnowledgeDesc());
      params.put("operation", "modify");
      String request = FreemarkerUtil.process(template, params);
      Map<String, Object> content = JsonUtil.parseJsonRequired(request, new TypeReference<>() {
      });
      oldKnowledge.setKnowledgeStrategy(JsonUtil.toJsonString(content.get("extra")));
    }
    return StringUtils.isAnyEmpty(knowledge.getKnowledgeStrategy(), oldKnowledge.getKnowledgeStrategy());
  }

  @SuppressWarnings("unchecked")
  private boolean equals(String groupCode, String attrCode, Map<String, Object> newStrategy, Map<String, Object> oldStrategy) {
    if (StringUtils.isEmpty(groupCode)) {
      return !Objects.equals(newStrategy.get(attrCode), oldStrategy.get(attrCode));
    }
    Map<String, Object> newMap = (Map<String, Object>) newStrategy.get(groupCode);
    Map<String, Object> oldMap = (Map<String, Object>) oldStrategy.get(groupCode);
    return !Objects.equals(newMap.get(attrCode), oldMap.get(attrCode));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getDefaultExtra() {
    String template = BaseSystemParameter.DOCCHAIN_TOPIC_API_REQUEST.getValueFromDb();
    Map<String, Object> params = new HashMap<>(4);
    params.put("operation", "1");
    params.put("topicId", "1");
    params.put("topicName", "1");
    params.put("comment", "1");
    String content = FreemarkerUtil.process(template, params);
    Map<String, Object> args = JsonUtil.parseJsonRequired(content, new TypeReference<Map<String, Object>>() {
    });
    return MapUtils.isEmpty(args) ? null : (Map<String, Object>) args.get("extra");
  }

  @SuppressWarnings("unchecked")
  private void setDefaultValue(DocChainExtraDTO.DocChainExtraGroupDTO group, Map<String, Object> defaultExtra) {
    for (DocChainExtraCfgDTO attr : group.getAttrs()) {
      if (StringUtils.isEmpty(attr.getGroupCode())) {
        attr.setDefaultValue(MapUtils.getString(defaultExtra, attr.getCode(), attr.getDefaultValue()));
      }
      else {
        Map<String, Object> map = (Map<String, Object>) defaultExtra.get(attr.getGroupCode());
        if (MapUtils.isEmpty(map)) {
          continue;
        }
        attr.setDefaultValue(MapUtils.getString(map, attr.getCode(), attr.getDefaultValue()));
      }
    }
  }
}
