package com.iwhalecloud.bote.loop.prompt.infra.repo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.entity.loop.prompt.PromptBasicEntity;
import com.iwhalecloud.bote.entity.loop.prompt.PromptCommitEntity;
import com.iwhalecloud.bote.entity.loop.prompt.PromptUserDraftEntity;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.loop.prompt.domain.entity.CommitInfo;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DraftInfo;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptCommit;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDetail;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDraft;
import com.iwhalecloud.bote.loop.prompt.domain.repo.GetPromptBasicOptionFunc;
import com.iwhalecloud.bote.loop.prompt.domain.repo.GetPromptOptionFunc;
import com.iwhalecloud.bote.loop.prompt.domain.repo.IManageRepo;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.CommitDraftParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.GetPromptBasicOption;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.GetPromptOption;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.GetPromptParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListCommitInfoParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListCommitParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListCommitResult;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListPromptBasicParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListPromptParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListPromptResult;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptIDCommitVersionPair;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptIDUserIDPair;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.RedisPromptQuery;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.UpdatePromptParam;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.IPromptBasicDAO;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.IPromptCommitDAO;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.IPromptUserDraftDAO;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.convertor.ManageConverter;
import com.iwhalecloud.bote.loop.prompt.infra.repo.redis.IPromptBasicCacheDAO;
import com.iwhalecloud.bote.loop.prompt.infra.repo.redis.IPromptCacheDAO;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ManageRepoImpl implements IManageRepo {
  private final IPromptCacheDAO promptCacheDAO;
  private final IPromptBasicCacheDAO promptBasicCacheDAO;
  private final IPromptBasicDAO promptBasicDAO;
  private final IPromptUserDraftDAO promptUserDraftDAO;
  private final IPromptCommitDAO promptCommitDAO;
  private final IIDGenerator idGenerator;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public Long createPrompt(Prompt promptDO) {
    if (promptDO == null || promptDO.getPromptBasic() == null) {
      throw new BssException("promptDO or promptDO.PromptBasic is empty");
    }

    Long promptID = idGenerator.genId();
    Long draftID = null;
    if (promptDO.getPromptDraft() != null) {
      draftID = idGenerator.genId();
    }

    // 创建基础Prompt
    PromptBasicEntity basicPO = ManageConverter.promptDO2BasicPO(promptDO);
    basicPO.setId(promptID);
    promptBasicDAO.create(basicPO);

    // 如果有草稿，创建草稿
    if (promptDO.getPromptDraft() != null) {
      PromptUserDraftEntity draftPO = ManageConverter.promptDO2DraftPO(promptDO);
      draftPO.setId(draftID);
      draftPO.setPromptId(promptID);
      promptUserDraftDAO.create(draftPO);
    }

    return promptID;
  }

  @Override
  public ResultVO<Void> deletePrompt(Long promptId, Long spaceId) {
    if (promptId <= 0) {
      throw new BssException("promptID is invalid, promptID = " + promptId);
    }
    if (spaceId == null) {
      throw new BssException("spaceId is required for deletePrompt, prompt id = " + promptId);
    }

    // 先查询获取 promptKey
    PromptBasicEntity promptBasicPO = promptBasicDAO.get(promptId, spaceId, false);
    if (promptBasicPO == null) {
      throw new BssException("prompt is not found, prompt id = " + promptId + ", space id = " + spaceId);
    }

    if (resourceElementService.existsRelatedResource(promptBasicPO.getSpaceId(), promptId, DataSyncCodeEnum.PROMPT.getCode())) {
      return ResultVO.fail("数据已存在关联配置数据，不允许删除");
    }
    promptBasicDAO.delete(promptId, spaceId);
    promptBasicCacheDAO.delByPromptKey(spaceId, promptBasicPO.getPromptKey());
    return ResultVO.success();
  }

  @Override
  @Transactional
  public Prompt getPrompt(GetPromptParam param) {
    validateGetPromptParam(param);
    PromptBasicEntity basicPO = getPromptBasicEntity(param);
    PromptCommitEntity commitPO = getPromptCommitEntity(param);
    PromptUserDraftEntity draftPO = getPromptDraftEntity(param);

    return ManageConverter.promptPO2DO(basicPO, commitPO, draftPO);
  }

  private void validateGetPromptParam(GetPromptParam param) {
    validatePromptId(param);
    validateCommitVersion(param);
    validateUserId(param);
  }

  private void validatePromptId(GetPromptParam param) {
    if (param.getPromptId() <= 0) {
      throw new BssException("param.PromptID is invalid, param = " + param);
    }
  }

  private void validateCommitVersion(GetPromptParam param) {
    if (param.getWithCommit() && (param.getCommitVersion() == null || param.getCommitVersion().isEmpty())) {
      throw new BssException("Get with commit, but param.CommitVersion is empty, param = " + param);
    }
  }

  private void validateUserId(GetPromptParam param) {
    if (param.getWithDraft() != null && param.getWithDraft() && (param.getUserId() == null || param.getUserId().isEmpty())) {
      throw new BssException("Get with draft, but param.UserID is empty, param = " + param);
    }
  }

  private PromptBasicEntity getPromptBasicEntity(GetPromptParam param) {
    if (param.getSpaceId() == null) {
      throw new BssException("spaceId is required for GetPromptParam, prompt id = " + param.getPromptId());
    }
    List<Long> promptIds = Collections.singletonList(param.getPromptId());
    Map<Long, PromptBasicEntity> idPromptBasicPOMap = promptBasicDAO.mGet(param.getSpaceId(), promptIds);
    PromptBasicEntity basicPO = idPromptBasicPOMap.get(param.getPromptId());
    if (basicPO == null) {
      throw new BssException("resource not found prompt id = " + param.getPromptId() + ", space id = " + param.getSpaceId());
    }
    return basicPO;
  }

  private PromptCommitEntity getPromptCommitEntity(GetPromptParam param) {
    if (!param.getWithCommit()) {
      return null;
    }

    PromptCommitEntity commitPO = promptCommitDAO.get(param.getPromptId(), param.getCommitVersion());
    if (commitPO == null) {
      throw new BssException("Get with commit, but it's not found, prompt id = " +
        param.getPromptId() + ", commit version = " + param.getCommitVersion());
    }
    return commitPO;
  }

  private PromptUserDraftEntity getPromptDraftEntity(GetPromptParam param) {
    if (param.getWithDraft() == null || !param.getWithDraft()) {
      return null;
    }
    if (param.getSpaceId() == null) {
      throw new BssException("spaceId is required for GetPromptParam, prompt id = " + param.getPromptId());
    }
    return promptUserDraftDAO.get(param.getSpaceId(), param.getPromptId(), param.getUserId());
  }

  @Override
  public Map<GetPromptParam, Prompt> mGetPrompt(List<GetPromptParam> queries, GetPromptOptionFunc... opts) {
    if (queries.isEmpty()) {
      return null;
    }

    GetPromptOption options = buildGetPromptOptions(opts);
    Map<RedisPromptQuery, Prompt> cachedPromptMap = getCachedPrompts(queries, options);
    List<GetPromptParam> missedQueries = findMissedQueries(queries, cachedPromptMap);
    Map<GetPromptParam, Prompt> missedPromptMap = mGetPromptFromDB(missedQueries);

    Map<GetPromptParam, Prompt> result = buildResultMap(queries, cachedPromptMap, missedPromptMap);
    updateCacheIfEnabled(options, missedPromptMap);

    return result;
  }

  private GetPromptOption buildGetPromptOptions(GetPromptOptionFunc... opts) {
    GetPromptOption options = GetPromptOption.builder().build();
    for (GetPromptOptionFunc opt : opts) {
      opt.apply(options);
    }
    if (options.getCacheEnable() == null) {
      options.setCacheEnable(false);
    }
    return options;
  }

  private Map<RedisPromptQuery, Prompt> getCachedPrompts(List<GetPromptParam> queries, GetPromptOption options) {
    if (options.getCacheEnable() == null || !options.getCacheEnable()) {
      return null;
    }

    validateCacheRequirements(queries);
    List<RedisPromptQuery> cacheQueries = buildCacheQueries(queries);
    return promptCacheDAO.mGet(cacheQueries);
  }

  private void validateCacheRequirements(List<GetPromptParam> queries) {
    for (GetPromptParam query : queries) {
      if (query.getWithDraft() || !query.getWithCommit()) {
        throw new BssException("enable cache is allowed only when getting prompt with commit");
      }
    }
  }

  private List<RedisPromptQuery> buildCacheQueries(List<GetPromptParam> queries) {
    List<RedisPromptQuery> cacheQueries = Lists.newArrayList();
    for (GetPromptParam query : queries) {
      cacheQueries.add(RedisPromptQuery.builder()
        .promptId(query.getPromptId())
        .withCommit(query.getWithCommit())
        .commitVersion(query.getCommitVersion())
        .build());
    }
    return cacheQueries;
  }

  private List<GetPromptParam> findMissedQueries(List<GetPromptParam> queries, Map<RedisPromptQuery, Prompt> cachedPromptMap) {
    List<GetPromptParam> missedQueries = Lists.newArrayList();
    for (GetPromptParam query : queries) {
      if (cachedPromptMap == null) {
        missedQueries.add(query);
        continue;
      }
      Prompt prompt = getCachedPrompt(query, cachedPromptMap);
      if (prompt == null) {
        missedQueries.add(query);
      }
    }
    return missedQueries;
  }

  private Prompt getCachedPrompt(GetPromptParam query, Map<RedisPromptQuery, Prompt> cachedPromptMap) {
    return cachedPromptMap.get(RedisPromptQuery.builder()
      .promptId(query.getPromptId())
      .withCommit(query.getWithCommit())
      .commitVersion(query.getCommitVersion())
      .build());
  }

  private Map<GetPromptParam, Prompt> buildResultMap(List<GetPromptParam> queries,
                                                     Map<RedisPromptQuery, Prompt> cachedPromptMap, Map<GetPromptParam, Prompt> missedPromptMap) {
    Map<GetPromptParam, Prompt> result = Maps.newHashMap();

    for (GetPromptParam query : queries) {
      Prompt prompt = getPromptFromCacheOrMissed(query, cachedPromptMap, missedPromptMap);
      if (prompt != null) {
        result.put(query, prompt);
      }
    }

    return result;
  }

  private Prompt getPromptFromCacheOrMissed(GetPromptParam query, Map<RedisPromptQuery, Prompt> cachedPromptMap,
                                            Map<GetPromptParam, Prompt> missedPromptMap) {
    if (cachedPromptMap != null) {
      Prompt cachedPrompt = getCachedPrompt(query, cachedPromptMap);
      if (cachedPrompt != null) {
        return cachedPrompt;
      }
    }
    return missedPromptMap.get(query);
  }

  private void updateCacheIfEnabled(GetPromptOption options, Map<GetPromptParam, Prompt> missedPromptMap) {
    if (options.getCacheEnable()) {
      promptCacheDAO.mSet(missedPromptMap.values().stream().toList());
    }
  }

  private Map<GetPromptParam, Prompt> mGetPromptFromDB(List<GetPromptParam> queries) {
    if (queries.isEmpty()) {
      return Collections.emptyMap();
    }

    // 按 spaceId 分组查询
    Map<Long, List<GetPromptParam>> spaceIdQueriesMap = Maps.newHashMap();
    for (GetPromptParam query : queries) {
      if (query.getSpaceId() == null) {
        throw new BssException("spaceId is required for GetPromptParam, prompt id = " + query.getPromptId());
      }
      spaceIdQueriesMap.computeIfAbsent(query.getSpaceId(), k -> Lists.newArrayList()).add(query);
    }

    Map<Long, PromptBasicEntity> allIdPromptBasicPOMap = Maps.newHashMap();
    Map<GetPromptParam, Boolean> needDraftMap = Maps.newHashMap();
    Map<GetPromptParam, Boolean> needCommitMap = Maps.newHashMap();

    // 按 spaceId 分组批量查询
    for (Map.Entry<Long, List<GetPromptParam>> entry : spaceIdQueriesMap.entrySet()) {
      Long spaceId = entry.getKey();
      List<GetPromptParam> spaceQueries = entry.getValue();
      List<Long> promptIds = extractPromptIds(spaceQueries, needDraftMap, needCommitMap);
      Map<Long, PromptBasicEntity> idPromptBasicPOMap = promptBasicDAO.mGet(spaceId, promptIds);
      allIdPromptBasicPOMap.putAll(idPromptBasicPOMap);
    }

    Map<PromptIDUserIDPair, PromptUserDraftEntity> draftPOMap = getDraftEntities(needDraftMap);
    Map<PromptIDCommitVersionPair, PromptCommitEntity> commitPOMap = getCommitEntities(needCommitMap);

    return buildPromptResultMap(queries, allIdPromptBasicPOMap, draftPOMap, commitPOMap);
  }

  private List<Long> extractPromptIds(List<GetPromptParam> queries, Map<GetPromptParam, Boolean> needDraftMap,
                                      Map<GetPromptParam, Boolean> needCommitMap) {
    List<Long> allPromptIDs = Lists.newArrayList();
    for (GetPromptParam query : queries) {
      allPromptIDs.add(query.getPromptId());
      if (query.getWithDraft()) {
        needDraftMap.put(query, true);
      }
      if (query.getWithCommit()) {
        needCommitMap.put(query, true);
      }
    }
    return allPromptIDs;
  }

  private Map<PromptIDUserIDPair, PromptUserDraftEntity> getDraftEntities(Map<GetPromptParam, Boolean> needDraftMap) {
    if (needDraftMap.isEmpty()) {
      return Maps.newHashMap();
    }

    List<PromptIDUserIDPair> promptDraftQueries = buildDraftQueries(needDraftMap);
    return promptUserDraftDAO.mGet(promptDraftQueries);
  }

  private List<PromptIDUserIDPair> buildDraftQueries(Map<GetPromptParam, Boolean> needDraftMap) {
    List<PromptIDUserIDPair> promptDraftQueries = Lists.newArrayList();
    for (Map.Entry<GetPromptParam, Boolean> entry : needDraftMap.entrySet()) {
      GetPromptParam promptQuery = entry.getKey();
      promptDraftQueries.add(PromptIDUserIDPair.builder()
        .promptId(promptQuery.getPromptId())
        .userId(promptQuery.getUserId())
        .build());
    }
    return promptDraftQueries;
  }

  private Map<PromptIDCommitVersionPair, PromptCommitEntity> getCommitEntities(Map<GetPromptParam, Boolean> needCommitMap) {
    if (needCommitMap.isEmpty()) {
      return Maps.newHashMap();
    }

    List<PromptIDCommitVersionPair> promptCommitQueries = buildCommitQueries(needCommitMap);
    return promptCommitDAO.mGet(promptCommitQueries);
  }

  private List<PromptIDCommitVersionPair> buildCommitQueries(Map<GetPromptParam, Boolean> needCommitMap) {
    List<PromptIDCommitVersionPair> promptCommitQueries = Lists.newArrayList();
    for (Map.Entry<GetPromptParam, Boolean> entry : needCommitMap.entrySet()) {
      GetPromptParam promptQuery = entry.getKey();
      promptCommitQueries.add(PromptIDCommitVersionPair.builder()
        .promptId(promptQuery.getPromptId())
        .commitVersion(promptQuery.getCommitVersion())
        .build());
    }
    return promptCommitQueries;
  }

  private Map<GetPromptParam, Prompt> buildPromptResultMap(List<GetPromptParam> queries,
                                                           Map<Long, PromptBasicEntity> idPromptBasicPOMap,
                                                           Map<PromptIDUserIDPair, PromptUserDraftEntity> draftPOMap,
                                                           Map<PromptIDCommitVersionPair, PromptCommitEntity> commitPOMap) {
    Map<GetPromptParam, Prompt> promptDOMap = Maps.newHashMap();

    for (GetPromptParam query : queries) {
      Prompt prompt = buildPromptForQuery(query, idPromptBasicPOMap, draftPOMap, commitPOMap);
      promptDOMap.put(query, prompt);
    }

    return promptDOMap;
  }

  private Prompt buildPromptForQuery(GetPromptParam query, Map<Long, PromptBasicEntity> idPromptBasicPOMap,
                                     Map<PromptIDUserIDPair, PromptUserDraftEntity> draftPOMap,
                                     Map<PromptIDCommitVersionPair, PromptCommitEntity> commitPOMap) {
    PromptBasicEntity promptBasicPO = getPromptBasicEntity(query, idPromptBasicPOMap);
    PromptUserDraftEntity promptDraftPO = getPromptDraftEntity(query, draftPOMap);
    PromptCommitEntity promptCommitPO = getPromptCommitEntity(query, commitPOMap);

    return ManageConverter.promptPO2DO(promptBasicPO, promptCommitPO, promptDraftPO);
  }

  private PromptBasicEntity getPromptBasicEntity(GetPromptParam query, Map<Long, PromptBasicEntity> idPromptBasicPOMap) {
    PromptBasicEntity promptBasicPO = idPromptBasicPOMap.get(query.getPromptId());
    if (promptBasicPO == null) {
      throw new BssException("prompt not found, prompt_id=" + query.getPromptId());
    }
    return promptBasicPO;
  }

  private PromptUserDraftEntity getPromptDraftEntity(GetPromptParam query, Map<PromptIDUserIDPair, PromptUserDraftEntity> draftPOMap) {
    if (query.getWithDraft() == null || !query.getWithDraft()) {
      return null;
    }

    PromptUserDraftEntity promptDraftPO = draftPOMap.get(PromptIDUserIDPair.builder()
      .promptId(query.getPromptId()).userId(query.getUserId()).build());
    if (promptDraftPO == null) {
      throw new BssException("prompt draft not found, prompt_id=" + query.getPromptId() + ", user_id=" + query.getUserId());
    }
    return promptDraftPO;
  }

  private PromptCommitEntity getPromptCommitEntity(GetPromptParam query, Map<PromptIDCommitVersionPair, PromptCommitEntity> commitPOMap) {
    if (!query.getWithCommit()) {
      return null;
    }

    PromptCommitEntity promptCommitPO = commitPOMap.get(PromptIDCommitVersionPair.builder()
      .promptId(query.getPromptId())
      .commitVersion(query.getCommitVersion())
      .build());
    if (promptCommitPO == null) {
      throw new BssException("prompt commit not found, prompt_id=" + query.getPromptId() + ", commit_version=" + query.getCommitVersion());
    }
    return promptCommitPO;
  }

  @Override
  public List<Prompt> mGetPromptBasicByPromptKey(Long spaceId, List<String> promptKeys, GetPromptBasicOptionFunc... opts) {
    if (promptKeys.isEmpty()) {
      return null;
    }

    GetPromptBasicOption options = GetPromptBasicOption.builder().build();
    for (GetPromptBasicOptionFunc opt : opts) {
      opt.apply(options);
    }

    Map<String, Prompt> cacheResultMap = Maps.newHashMap();
    if (options.getCacheEnable()) {
      cacheResultMap = promptBasicCacheDAO.mGetByPromptKey(spaceId, promptKeys);
    }

    List<Prompt> promptDOs = Lists.newArrayList();
    List<String> missedPromptKeys = Lists.newArrayList();

    for (String promptKey : promptKeys) {
      Prompt promptDO = cacheResultMap.get(promptKey);
      if (promptDO != null) {
        promptDOs.add(promptDO);
      }
      else {
        missedPromptKeys.add(promptKey);
      }
    }

    // 从数据库获取
    List<Prompt> missedPrompts = mGetPromptBasicByPromptKeyFromDB(spaceId, missedPromptKeys);
    if (missedPrompts != null) {
      promptDOs.addAll(missedPrompts);
    }

    if (options.getCacheEnable()) {
      promptBasicCacheDAO.mSetByPromptKey(missedPrompts);
    }

    return promptDOs;
  }

  private List<Prompt> mGetPromptBasicByPromptKeyFromDB(Long spaceId, List<String> promptKeys) {
    if (promptKeys.isEmpty()) {
      return Collections.emptyList();
    }

    List<PromptBasicEntity> basicPOs = promptBasicDAO.mGetByPromptKey(spaceId, promptKeys);
    return ManageConverter.batchBasicPO2PromptDO(basicPOs);
  }

  @Override
  public ListPromptResult listPrompt(ListPromptParam param) {
    ListPromptBasicParam listBasicParam = ListPromptBasicParam.builder()
      .spaceId(param.getSpaceId())
      .keyWord(param.getKeyWord())
      .createdBys(param.getCreatedBys())
      .committedOnly(param.getCommittedOnly())
      .offset((param.getPageNum() - 1) * param.getPageSize())
      .limit(param.getPageSize())
      .orderBy(param.getOrderBy())
      .asc(param.getAsc())
      .catalogItemId(param.getCatalogItemId())
      .promptType(param.getPromptType())
      .build();

    List<PromptBasicEntity> basicPOs = promptBasicDAO.list(listBasicParam);
    Long total = promptBasicDAO.countByCondition(listBasicParam);

    Map<PromptIDUserIDPair, PromptUserDraftEntity> draftPOMap = Maps.newHashMap();
    if (!basicPOs.isEmpty()) {
      List<PromptIDUserIDPair> promptDraftQueries = Lists.newArrayList();
      for (PromptBasicEntity basicPO : basicPOs) {
        promptDraftQueries.add(PromptIDUserIDPair.builder()
          .promptId(basicPO.getId())
          .userId(param.getUserId())
          .build());
      }
      draftPOMap = promptUserDraftDAO.mGet(promptDraftQueries);
    }

    return ListPromptResult.builder()
      .total(total)
      .promptDOs(ManageConverter.batchBasicAndDraftPO2PromptDO(basicPOs, draftPOMap, param.getUserId()))
      .build();
  }

  @Override
  public void updatePrompt(UpdatePromptParam param) {
    if (param.getSpaceId() == null) {
      throw new BssException("spaceId is required for UpdatePromptParam, prompt id = " + param.getPromptId());
    }

    promptBasicDAO.update(param.getPromptId(), param.getSpaceId(), PromptBasicEntity.builder()
      .updatedBy(param.getUpdatedBy())
      .name(param.getPromptName())
      .description(param.getPromptDescription())
      .catalogItemId(param.getCatalogItemId())
      .promptType(param.getPromptType())
      .updatedAt(new Date())
      .build());
    // 删除缓存 - 需要先查询获取 promptKey
    PromptBasicEntity basicPO = promptBasicDAO.get(param.getPromptId(), param.getSpaceId(), false);
    if (basicPO != null) {
      promptBasicCacheDAO.delByPromptKey(param.getSpaceId(), basicPO.getPromptKey());
    }
  }

  @Override
  @Transactional
  public DraftInfo saveDraft(Prompt promptDO) {
    validateSaveDraftInput(promptDO);
    if (promptDO.getSpaceId() == null) {
      throw new BssException("Prompt.spaceId is required for saveDraft, prompt id = " + promptDO.getId());
    }
    Long spaceId = promptDO.getSpaceId();
    PromptBasicEntity basicPO = getPromptBasicEntity(promptDO.getId(), spaceId);
    PromptCommitEntity baseCommitPO = getBaseCommitEntity(promptDO);
    PromptUserDraftEntity originalDraftPO = getOriginalDraftEntity(promptDO);

    if (originalDraftPO == null) {
      return createNewDraft(promptDO, basicPO);
    }

    return updateExistingDraft(promptDO, originalDraftPO, baseCommitPO);
  }

  private void validateSaveDraftInput(Prompt promptDO) {
    if (promptDO == null || promptDO.getPromptDraft() == null) {
      throw new BssException("promptDO or promptDO.PromptDraft is empty");
    }
  }

  public PromptBasicEntity getPromptBasicEntity(Long promptId, Long spaceId) {
    if (spaceId == null) {
      throw new BssException("spaceId is required for getPromptBasicEntity, prompt id = " + promptId);
    }
    // 如果需要加锁，调用带锁的查询
    PromptBasicEntity basicPO = promptBasicDAO.get(promptId, spaceId, true);
    if (basicPO == null) {
      throw new BssException("Prompt is not found, prompt id = " + promptId);
    }
    return basicPO;
  }

  private PromptCommitEntity getBaseCommitEntity(Prompt promptDO) {
    String savingBaseVersion = promptDO.getPromptDraft().getDraftInfo().getBaseVersion();
    if (savingBaseVersion == null || savingBaseVersion.isEmpty()) {
      return null;
    }

    PromptCommitEntity baseCommitPO = promptCommitDAO.get(promptDO.getId(), savingBaseVersion);
    if (baseCommitPO == null) {
      throw new BssException("Draft's base prompt commit is not found, prompt id = " +
        promptDO.getId() + ", base commit version = " + savingBaseVersion);
    }
    return baseCommitPO;
  }

  private PromptUserDraftEntity getOriginalDraftEntity(Prompt promptDO) {
    if (promptDO.getSpaceId() == null) {
      throw new BssException("Prompt.spaceId is required for getOriginalDraftEntity, prompt id = " + promptDO.getId());
    }
    String userID = promptDO.getPromptDraft().getDraftInfo().getUserId();
    return promptUserDraftDAO.get(promptDO.getSpaceId(), promptDO.getId(), userID);
  }

  private DraftInfo createNewDraft(Prompt promptDO, PromptBasicEntity basicPO) {
    promptDO.getPromptDraft().getDraftInfo().setIsModified(true);
    PromptUserDraftEntity creatingDraftPO = buildNewDraftEntity(promptDO, basicPO);
    promptUserDraftDAO.create(creatingDraftPO);

    PromptUserDraftEntity createdDraftPO = promptUserDraftDAO.getById(creatingDraftPO.getId());
    if (createdDraftPO != null) {
      return ManageConverter.draftPO2DO(createdDraftPO).getDraftInfo();
    }
    return null;
  }

  private PromptUserDraftEntity buildNewDraftEntity(Prompt promptDO, PromptBasicEntity basicPO) {
    PromptUserDraftEntity creatingDraftPO = ManageConverter.promptDO2DraftPO(promptDO);
    creatingDraftPO.setId(idGenerator.genId());
    creatingDraftPO.setSpaceId(basicPO.getSpaceId());
    return creatingDraftPO;
  }

  private DraftInfo updateExistingDraft(Prompt promptDO, PromptUserDraftEntity originalDraftPO, PromptCommitEntity baseCommitPO) {
    PromptDetail originalDraftDetailDO = ManageConverter.draftPO2DO(originalDraftPO).getPromptDetail();
    PromptDetail updatingDraftDetailDO = promptDO.getPromptDraft().getPromptDetail();

    if (updatingDraftDetailDO.equals(originalDraftDetailDO)) {
      return null;
    }

    setDraftModifiedStatus(promptDO, updatingDraftDetailDO, baseCommitPO);
    updateDraftEntity(promptDO, originalDraftPO);

    return getUpdatedDraftInfo(originalDraftPO.getId());
  }

  private void setDraftModifiedStatus(Prompt promptDO, PromptDetail updatingDraftDetailDO, PromptCommitEntity baseCommitPO) {
    if (baseCommitPO == null) {
      promptDO.getPromptDraft().getDraftInfo().setIsModified(true);
    }
    else {
      PromptDetail baseCommitDetailDO = ManageConverter.commitPO2DO(baseCommitPO).getPromptDetail();
      promptDO.getPromptDraft().getDraftInfo().setIsModified(!updatingDraftDetailDO.equals(baseCommitDetailDO));
    }
  }

  private void updateDraftEntity(Prompt promptDO, PromptUserDraftEntity originalDraftPO) {
    PromptUserDraftEntity updatingDraftPO = ManageConverter.promptDO2DraftPO(promptDO);
    updatingDraftPO.setId(originalDraftPO.getId());
    updatingDraftPO.setUpdatedAt(new Date());
    promptUserDraftDAO.update(updatingDraftPO);
  }

  private DraftInfo getUpdatedDraftInfo(Long draftId) {
    PromptUserDraftEntity updatedDraftPO = promptUserDraftDAO.getById(draftId);
    if (updatedDraftPO != null) {
      return ManageConverter.draftPO2DO(updatedDraftPO).getDraftInfo();
    }
    return null;
  }

  @Override
  @Transactional
  public void commitDraft(CommitDraftParam param) {
    if (param.getSpaceId() == null) {
      throw new BssException("spaceId is required for CommitDraftParam, prompt id = " + param.getPromptId());
    }
    Long commitID = idGenerator.genId();
    Long spaceID = param.getSpaceId();

    // 如果需要加锁，调用带锁的查询
    PromptBasicEntity basicPO = promptBasicDAO.get(param.getPromptId(), spaceID, true);
    if (basicPO == null) {
      throw new BssException("Prompt is not found, prompt id = " + param.getPromptId() + ", space id = " + spaceID);
    }
    String promptKey = basicPO.getPromptKey();

    PromptUserDraftEntity draftPO = promptUserDraftDAO.get(spaceID, param.getPromptId(), param.getUserId());
    if (draftPO == null) {
      throw new BssException("Prompt draft is not found, prompt id = " + param.getPromptId() + ", user id = " + param.getUserId());
    }

    PromptDraft draftDO = ManageConverter.draftPO2DO(draftPO);
    PromptCommit commitDO = PromptCommit.builder()
      .commitInfo(CommitInfo.builder()
        .version(param.getCommitVersion())
        .baseVersion(draftPO.getBaseVersion())
        .description(param.getCommitDescription())
        .committedBy(param.getUserId())
        .build())
      .promptDetail(draftDO.getPromptDetail())
      .build();

    Prompt promptDO = ManageConverter.promptPO2DO(basicPO, null, null);
    promptDO.setPromptCommit(commitDO);
    PromptCommitEntity commitPO = ManageConverter.promptDO2CommitPO(promptDO);
    commitPO.setId(commitID);
    promptCommitDAO.create(commitPO, new Date());
    promptUserDraftDAO.delete(draftPO.getId());

    PromptBasicEntity updatePromptBasicPO = PromptBasicEntity.builder().latestCommitTime(new Date()).updatedAt(new Date()).latestVersion(param.getCommitVersion()).build();

    promptBasicDAO.update(basicPO.getId(), spaceID, updatePromptBasicPO);

    // 删除缓存
    promptBasicCacheDAO.delByPromptKey(spaceID, promptKey);
  }

  @Override
  public ListCommitResult listCommitInfo(ListCommitInfoParam param) {
    if (param.getPromptId() <= 0 || param.getPageSize() <= 0) {
      throw new BssException("Param(PromptID or PageSize) is invalid, param = " + param);
    }

    ListCommitParam listCommitParam = ListCommitParam.builder()
      .promptId(param.getPromptId())
      .cursor(param.getPageToken())
      .limit(param.getPageSize() + 1)
      .asc(param.getAsc())
      .build();

    List<PromptCommitEntity> commitPOs = promptCommitDAO.list(listCommitParam);
    if (commitPOs.isEmpty()) {
      return null;
    }

    ListCommitResult result = ListCommitResult.builder().build();
    List<PromptCommit> commitDOs = ManageConverter.batchCommitPO2DO(commitPOs);
    List<CommitInfo> commitInfoDOs = ManageConverter.batchGetCommitInfoDOFromCommitDO(commitDOs);

    if (commitPOs.size() <= param.getPageSize()) {
      result.setCommitInfoDOs(commitInfoDOs);
      return result;
    }

    result.setNextPageToken(commitPOs.get(param.getPageSize()).getId());
    result.setCommitInfoDOs(commitInfoDOs.subList(0, commitPOs.size() - 1));
    return result;
  }

  @Override
  public boolean existsPromptKey(Long workspaceId, String promptKey) {
    return promptBasicDAO.existsPromptKey(workspaceId, promptKey);
  }

  @Override
  public boolean existsPromptName(Long workspaceId, String promptName) {
    return promptBasicDAO.existsPromptName(workspaceId, promptName);
  }
}
