package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.skill.PromptContentDTO;
import com.iwhalecloud.bote.dto.skill.PromptDTO;
import com.iwhalecloud.bote.dto.skill.SimplePromptDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.PromptManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.skill.IPromptManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 提示词管理服务
 *
 * @author qian.sisheng
 * @since 2024/8/2
 */
@Service
@RequiredArgsConstructor
public class PromptManageServiceImpl implements IPromptManageService {

  private final PromptManageMapper promptMapper;
  private final ICatalogManageService catalogManageService;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<PromptDTO> savePrompt(PromptDTO prompt) {
    prompt.setStatusCd(BaseConsts.STATUS_CD_VALID);
    PromptDTO old = prompt.getPromptId() == null ? null : findPrompt(prompt.getTenantId(), prompt.getPromptId());
    DataDifference<PromptDTO> dataDifference = DataDifferenceStarter.computeSaveAndLog(old, prompt, true, prompt.getTenantId(), OperClassEnum.PROMPT);
    if (dataDifference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(dataDifference.getToSaveData());
  }

  @Override
  @Nullable
  public PromptDTO findPrompt(Long tenantId, Long promptId) {
    PromptDTO prompt = promptMapper.getPrompt(tenantId, promptId);
    if (prompt == null) {
      return null;
    }
    prompt.setPromptContents(promptMapper.selectPromptContentList(tenantId, Collections.singletonList(promptId), null));
    return prompt;
  }

  @Override
  public List<PromptDTO> queryPromptList(SkillQueryParams params) {
    List<PromptDTO> prompts = promptMapper.selectPromptList(params);
    if (CollectionUtils.isEmpty(prompts)) {
      return prompts;
    }
    List<Long> promptsIds = prompts.stream().map(PromptDTO::getPromptId).collect(Collectors.toList());
    fillPromptContentList(params.getTenantId(), promptsIds, prompts);
    return prompts;
  }

  private void fillPromptContentList(Long tenantId, List<Long> promptsIds, List<PromptDTO> prompts) {
    List<PromptContentDTO> promptContents = promptMapper.selectPromptContentList(tenantId, promptsIds, null);
    if (CollectionUtils.isEmpty(promptContents)) {
      return;
    }
    prompts.forEach(prompt -> prompt.setPromptContents(
      promptContents.stream().filter(content -> content.getPromptId().equals(prompt.getPromptId())).collect(Collectors.toList())));
  }

  @Override
  public PageInfo<PromptDTO> queryPromptPage(SkillQueryParams params) {
    if (!BaseConsts.FALSE.equals(params.getConfigFlag())) {
      params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_PROMPT));
    }
    // noinspection resource
    PageInfo<PromptDTO> pageInfo = promptMapper.selectPromptPage(params, params.buildRowBounds()).toPageInfo();
    if (CollectionUtils.isEmpty(pageInfo.getList())) {
      return pageInfo;
    }
    List<Long> promptsIds = pageInfo.getList().stream().map(PromptDTO::getPromptId).collect(Collectors.toList());
    fillPromptContentList(params.getTenantId(), promptsIds, pageInfo.getList());
    return pageInfo;
  }

  @Override
  public String findPromptContent(Long tenantId, @Nullable Long modelId, String title, String catalogName) {
    if (modelId == null) {
      modelId = tenantSettingInfoCache.getModelId(tenantId);
    }
    return promptMapper.selectPromptContent(tenantId, modelId, title, catalogName);
  }

  @Override
  @Transactional
  public ResultVO<Void> deletePrompt(Long tenantId, Long promptId) {
    if (resourceElementService.existsRelatedResource(tenantId, promptId, "prompt")) {
      return ResultVO.fail("提示词已存在关联配置数据，不允许删除");
    }
    promptMapper.deletePrompt(tenantId, promptId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.PROMPT.name()).clear(tenantId, promptId);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> syncPrompt(SimplePromptDTO simplePrompt) {
    Long tenantId = simplePrompt.getTenantId();
    if (simplePrompt.getSourceModelId() == null) {
      simplePrompt.setSourceModelId(tenantSettingInfoCache.getModelId(tenantId));
    }
    if (simplePrompt.getSourceModelId().equals(simplePrompt.getTargetModelId())) {
      return ResultVO.fail("源模型与目标模型不能相同");
    }
    List<PromptContentDTO> sourceContents = promptMapper.selectPromptContentList(tenantId, simplePrompt.getPromptIds(),
      simplePrompt.getSourceModelId());
    List<PromptContentDTO> targetContents = promptMapper.selectPromptContentList(tenantId, null, simplePrompt.getTargetModelId());
    savePrompt(targetContents, sourceContents, simplePrompt);
    return ResultVO.success();
  }

  private void savePrompt(List<PromptContentDTO> targetPrompts, List<PromptContentDTO> sourcePrompts, SimplePromptDTO simplePrompt) {
    List<PromptContentDTO> existingContents = new ArrayList<>();
    List<PromptContentDTO> addPromptContents = new ArrayList<>();
    for (PromptContentDTO sourceContent : CollectionUtils.emptyIfNull(sourcePrompts)) {
      PromptContentDTO targetPrompt = IterableUtils.find(CollectionUtils.emptyIfNull(targetPrompts),
        p -> p.getPromptId().equals(sourceContent.getPromptId()));
      if (BaseConsts.TRUE.equals(simplePrompt.getCoverFlag()) && targetPrompt != null) {
        targetPrompt.setUpdatedTime(new Date());
        targetPrompt.setUpdatorId(SessionUtil.getOptionalUserId());
        targetPrompt.setPromptId(sourceContent.getPromptId());
        targetPrompt.setPromptContent(sourceContent.getPromptContent());
        existingContents.add(targetPrompt);
      }
      else if (targetPrompt == null) {
        PromptContentDTO prompt = createPromptContent(sourceContent, simplePrompt.getTargetModelId(), simplePrompt.getTenantId());
        addPromptContents.add(prompt);
      }
    }
    if (CollectionUtils.isNotEmpty(existingContents)) {
      existingContents.forEach(promptMapper::batchUpdatePromptContent);
    }
    if (CollectionUtils.isNotEmpty(addPromptContents)) {
      promptMapper.batchInsertPromptContent(addPromptContents);
    }
  }

  private PromptContentDTO createPromptContent(PromptContentDTO sourceContent, Long modelId, Long tenantId) {
    PromptContentDTO newPromptContent = new PromptContentDTO();
    newPromptContent.setContentId(Sequences.CONTENT_ID.next());
    newPromptContent.setPromptContent(sourceContent.getPromptContent());
    newPromptContent.setModelId(modelId);
    newPromptContent.setPromptId(sourceContent.getPromptId());
    newPromptContent.setStatusCd(BaseConsts.STATUS_CD_VALID);
    newPromptContent.setTenantId(tenantId);
    newPromptContent.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    return newPromptContent;
  }

  @Override
  public String checkPrompt(SimplePromptDTO prompt) {
    Long tenantId = prompt.getTenantId();
    if (prompt.getSourceModelId() == null) {
      prompt.setSourceModelId(tenantSettingInfoCache.getModelId(tenantId));
    }
    List<PromptContentDTO> sourceContentList = promptMapper.selectPromptContentList(tenantId, prompt.getPromptIds(), prompt.getSourceModelId());
    List<PromptContentDTO> targetContentList = promptMapper.selectPromptContentList(tenantId, null, prompt.getTargetModelId());
    for (PromptContentDTO sourceContent : sourceContentList) {
      if (targetContentList.stream().anyMatch(p -> p.getPromptId().equals(sourceContent.getPromptId()))) {
        return BaseConsts.TRUE;
      }
    }
    return BaseConsts.FALSE;
  }
}
