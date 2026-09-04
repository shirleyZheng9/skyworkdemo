package com.iwhalecloud.bote.loop.data.domain.tag.service.impl;

import com.iwhalecloud.bote.loop.data.domain.tag.entity.ChangeLog;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.GetTagDetailReq;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.GetTagDetailResp;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagKeyParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagValueParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKey;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKeyUtil;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagSpec;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagType;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagValue;
import com.iwhalecloud.bote.loop.data.domain.tag.repo.tag.ITagAPI;
import com.iwhalecloud.bote.loop.data.domain.tag.service.ITagService;
import com.iwhalecloud.bote.loop.data.domain.tag.service.util.VersionUtils;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 标签服务实现类
 * 迁移对应关系: Go语言TagServiceImpl
 * - 功能: 实现标签业务逻辑
 * - 方法实现: 各种标签业务操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的TagServiceImpl结构体
 * - 使用Repository层实现数据访问
 * - 提供标签业务逻辑处理
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements ITagService {
  private static final Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);

  private final ITagAPI tagRepo;

  /**
   * 新建标签键和标签值
   * 迁移对应关系: Go语言TagServiceImpl.CreateTag
   * - 功能: 新建标签键和标签值
   * - 参数: spaceID - 空间ID, val - 标签键对象
   * - 返回: 标签键ID
   * - 用途: 创建新的标签键和标签值
   */
  @Override
  @Transactional
  public Long createTag(Long spaceID, TagKey val) {

    // 计算更新日志
    List<ChangeLog> changeLogs = TagKeyUtil.calculateChangeLogs(val, null);
    val.setChangeLogs(changeLogs);

    String userID = SessionContext.getCurrentUserId();
    Integer appID = SessionContext.getAppId();
    LocalDateTime now = LocalDateTime.now();
    val.setCreatedAt(now);
    val.setUpdatedAt(now);
    val.setAppId(appID);
    val.setCreatedBy(userID);
    val.setUpdatedBy(userID);
    val.setVersionNum(0);
    val.setSpaceId(spaceID);

    // 加锁
    // 只有tag类型才加锁 & 检测是否有同名
    if (val.getTagType() == TagType.TAG) {
      // check tag key name
      boolean exist = isTagNameExisted(spaceID, 0L, val.getTagKeyName());
      if (exist) {
        logger.error("[CreateTag] tag name is already existed");
        throw new BssException("tag name is already existed");
      }
      // check version
      if (val.getVersion() != null) {
        if (validateVersion("", val.getVersion())) {
          logger.error("[CreateTag] validate version failed");
          throw new BssException("validate version failed");
        }
      }
    }

    // insert tag key and tag value
    Long tagKeyID;
    try {
      // 创建标签键
      tagRepo.mCreateTagKeys(List.of(val));
      tagKeyID = val.getTagKeyId();

      // 创建标签值（分层处理）
      List<TagValue> current = val.getTagValues();
      while (!CollectionUtils.isEmpty(current)) {
        // assign tag key id
        for (TagValue v : current) {
          v.setTagKeyId(tagKeyID);
        }
        // insert level tag values
        tagRepo.mCreateTagValues(current);

        // get next level & assigned parent tag value id
        List<TagValue> next = new ArrayList<>();
        for (TagValue v : current) {
          for (TagValue vv : v.getChildren()) {
            vv.setParentValueId(v.getTagValueId());
          }
          next.addAll(v.getChildren());
        }
        current = next;
      }
    }
    catch (Exception e) {
      logger.error("[CreateTag] insert tag key and tag value failed", e);
      throw new BssException("CreateTag failed", e);
    }

    return tagKeyID;
  }

  /**
   * 检查标签名是否存在
   * 迁移对应关系: Go语言TagServiceImpl.isTagNameExisted
   * - 功能: 检查标签名是否存在
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID, tagName - 标签名
   * - 返回: 是否存在
   * - 用途: 检查标签名是否已存在
   */
  private boolean isTagNameExisted(Long spaceID, Long tagKeyID, String tagName) {
    try {
      MGetTagKeyParam param = new MGetTagKeyParam();
      param.setSpaceId(spaceID);
      param.setTagKeyName(tagName);
      param.setStatus(List.of(TagStatus.INACTIVE, TagStatus.ACTIVE));

      List<TagKey> tagKeys = tagRepo.mGetTagKeys(param);
      if (tagKeyID == null || tagKeyID == 0) {
        return !CollectionUtils.isEmpty(tagKeys);
      }
      if (tagKeys.size() > 1) {
        return true;
      }
      else if (tagKeys.size() == 1) {
        return !tagKeys.getFirst().getTagKeyId().equals(tagKeyID);
      }
      return false;
    }
    catch (Exception e) {
      logger.error("[isTagNameExisted] MGetTagKeys failed", e);
      throw new BssException("isTagNameExisted failed", e);
    }
  }

  /**
   * 获取指定标签键ID的所有版本
   * 迁移对应关系: Go语言TagServiceImpl.GetAllTagKeyVersionsByKeyID
   * - 功能: 获取指定标签键ID的所有版本
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID
   * - 返回: 标签键版本列表
   * - 用途: 获取标签键的所有版本信息
   */
  @Override
  public List<TagKey> getAllTagKeyVersionsByKeyID(Long spaceID, Long tagKeyID) {
    MGetTagKeyParam param = new MGetTagKeyParam();
    param.setSpaceId(spaceID);
    param.setTagKeyIds(List.of(tagKeyID));

    return tagRepo.mGetTagKeys(param);
  }

  /**
   * 获取指定标签键ID和版本的选项，并建立树
   * 迁移对应关系: Go语言TagServiceImpl.GetAndBuildTagValues
   * - 功能: 获取指定标签键ID和版本的选项，并建立树
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID, versionNum - 版本号
   * - 返回: 标签值列表
   * - 用途: 获取标签值并建立树形结构
   */
  @Override
  public List<TagValue> getAndBuildTagValues(Long spaceID, Long tagKeyID, Integer versionNum) {

    MGetTagValueParam param = new MGetTagValueParam();
    param.setSpaceId(spaceID);
    param.setTagKeyId(tagKeyID);
    param.setVersion(versionNum);

    List<TagValue> values = tagRepo.mGetTagValue(param);
    List<TagValue> tagValues = new ArrayList<>(values);

    // build tree
    List<TagValue> result = new ArrayList<>();
    Map<Long, TagValue> valueMap = tagValues.stream()
      .collect(Collectors.toMap(TagValue::getTagValueId, v -> v));

    for (TagValue item : tagValues) {
      if (item.getParentValueId() == null || item.getParentValueId() == 0) {
        result.add(item);
      }
      else {
        TagValue parent = valueMap.get(item.getParentValueId());
        if (parent != null) {
          parent.getChildren().add(item);
        }
      }
    }

    return result;
  }

  /**
   * 获取最新的标签键和标签值，并建树
   * 迁移对应关系: Go语言TagServiceImpl.GetLatestTag
   * - 功能: 获取最新的标签键和标签值，并建树
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID
   * - 返回: 最新的标签键对象
   * - 用途: 获取最新的标签键和标签值
   */
  @Override
  public TagKey getLatestTag(Long spaceID, Long tagKeyID) {
    try {
      MGetTagKeyParam param = new MGetTagKeyParam();
      param.setSpaceId(spaceID);
      param.setTagKeyIds(List.of(tagKeyID));
      param.setStatus(List.of(TagStatus.ACTIVE, TagStatus.INACTIVE));

      List<TagKey> tagKeys = tagRepo.mGetTagKeys(param);
      if (CollectionUtils.isEmpty(tagKeys)) {
        logger.warn("[GetLatestTag] tag key is not exist, spaceID: {}, tagKeyID: {}", spaceID, tagKeyID);
        throw new BssException("tag key is not exist");
      }

      // get tag values
      TagKey result = tagKeys.getFirst();
      List<TagValue> values = getAndBuildTagValues(spaceID, tagKeyID, result.getVersionNum());
      result.setTagValues(values);
      return result;
    }
    catch (Exception e) {
      logger.error("[GetLatestTag] get tag key failed, spaceID: {}, tagKeyID: {}", spaceID, tagKeyID, e);
      throw new BssException("GetLatestTag failed", e);
    }
  }

  /**
   * 同时更新标签键和标签值，生成新版本
   * 迁移对应关系: Go语言TagServiceImpl.UpdateTag
   * - 功能: 同时更新标签键和标签值，生成新版本
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID, val - 标签键对象
   * - 用途: 更新标签键和标签值并生成新版本
   */
  @Override
  @Transactional
  public void updateTag(Long spaceID, Long tagKeyID, TagKey val) {

    val.setUpdatedAt(LocalDateTime.now());
    val.setUpdatedBy(SessionContext.getCurrentUserId());
    val.setAppId(SessionContext.getAppId());
    val.setSpaceId(spaceID);

    boolean nameExisted = isTagNameExisted(spaceID, tagKeyID, val.getTagKeyName());
    if (nameExisted) {
      logger.error("[UpdateTag] tag name is already existed");
      throw new BssException("tag name is already existed");
    }

    // 加锁

    // get latest tag
    TagKey preTagKey = getLatestTag(spaceID, tagKeyID);

    // 检查version
    if (val.getVersion() != null && preTagKey.getVersion() != null) {
      if (validateVersion(preTagKey.getVersion(), val.getVersion())) {
        logger.error("[UpdateTag] validate version failed");
        throw new BssException("validate version failed");
      }
    }

    val.setCreatedBy(preTagKey.getCreatedBy());
    val.setCreatedAt(preTagKey.getCreatedAt());

    // 计算更新日志
    List<ChangeLog> changeLogs = TagKeyUtil.calculateChangeLogs(val, preTagKey);
    val.setChangeLogs(changeLogs);

    // 更新tag key和 tag value的版本信息
    val.setVersionNum(preTagKey.getVersionNum() + 1);

    // 落库
    // disable old tag
    updateTagStatus(spaceID, tagKeyID, preTagKey.getVersionNum(), TagStatus.DEPRECATED, false, false);

    // insert tag keys
    tagRepo.mCreateTagKeys(List.of(val));

    // insert tag values
    List<TagValue> current = val.getTagValues();
    while (!CollectionUtils.isEmpty(current)) {
      List<TagValue> next = new ArrayList<>();
      for (TagValue v : current) {
        v.setTagKeyId(val.getTagKeyId());
      }
      tagRepo.mCreateTagValues(current);

      for (TagValue v : current) {
        for (TagValue vv : v.getChildren()) {
          vv.setParentValueId(v.getTagValueId());
        }
        next.addAll(v.getChildren());
      }
      current = next;
    }
  }

  /**
   * 改标签键和标签值的状态，不生成新版本
   * 迁移对应关系: Go语言TagServiceImpl.UpdateTagStatus
   * - 功能: 改标签键和标签值的状态，不生成新版本
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID, versionNum - 版本号, status - 状态, needLock - 是否需要锁定, updatedInfo - 是否更新信息
   * - 用途: 更新标签状态但不生成新版本
   */
  @Override
  @Transactional
  public void updateTagStatus(Long spaceID, Long tagKeyID, Integer versionNum, TagStatus status, Boolean needLock, Boolean updatedInfo) {
    // 更新标签状态
    tagRepo.updateTagKeysStatus(spaceID, tagKeyID, versionNum, status, updatedInfo);
    tagRepo.updateTagValuesStatus(spaceID, tagKeyID, versionNum, status, updatedInfo);
  }

  /**
   * 改标签键和标签值的状态，生成新版本
   * 迁移对应关系: Go语言TagServiceImpl.UpdateTagStatusWithNewVersion
   * - 功能: 改标签键和标签值的状态，生成新版本
   * - 参数: spaceID - 空间ID, tagKeyID - 标签键ID, status - 状态
   * - 用途: 更新标签状态并生成新版本
   */
  @Override
  @Transactional
  public void updateTagStatusWithNewVersion(Long spaceID, Long tagKeyID, TagStatus status) {
    // 加锁


    // get latest tag
    TagKey preTagKey = getLatestTag(spaceID, tagKeyID);
    if (preTagKey.getStatus() == status) {
      logger.error("[UpdateTagStatusWithNewVersion] no need to update status");
      throw new BssException("no need to update status");
    }

    TagKey nowTagKey = new TagKey();
    // 复制属性
    copyTagKey(preTagKey, nowTagKey);

    nowTagKey.setUpdatedAt(LocalDateTime.now());
    nowTagKey.setUpdatedBy(SessionContext.getCurrentUserId());
    nowTagKey.setAppId(SessionContext.getAppId());
    nowTagKey.setSpaceId(spaceID);
    nowTagKey.setStatus(status);

    if (nowTagKey.getVersion() != null) {
      String nextVersion = simpleIncrementVersion(nowTagKey.getVersion());
      nowTagKey.setVersion(nextVersion);
    }

    List<ChangeLog> changeLogs = TagKeyUtil.calculateChangeLogs(nowTagKey, preTagKey);
    nowTagKey.setChangeLogs(changeLogs);
    nowTagKey.setVersionNum(preTagKey.getVersionNum() + 1);

    // disable old tag
    updateTagStatus(spaceID, tagKeyID, preTagKey.getVersionNum(), TagStatus.DEPRECATED, false, false);

    // insert tag keys
    tagRepo.mCreateTagKeys(List.of(nowTagKey));

    // insert tag values
    List<TagValue> current = nowTagKey.getTagValues();
    while (!CollectionUtils.isEmpty(current)) {
      List<TagValue> next = new ArrayList<>();
      for (TagValue v : current) {
        v.setTagKeyId(nowTagKey.getTagKeyId());
      }
      tagRepo.mCreateTagValues(current);

      for (TagValue v : current) {
        for (TagValue vv : v.getChildren()) {
          vv.setParentValueId(v.getTagValueId());
        }
        next.addAll(v.getChildren());
      }
      current = next;
    }
  }

  /**
   * 获取标签规格
   * 迁移对应关系: Go语言TagServiceImpl.GetTagSpec
   * - 功能: 获取标签规格
   * - 参数: spaceID - 空间ID
   * - 返回: 标签规格
   * - 用途: 获取标签的规格信息
   */
  @Override
  public TagSpec getTagSpec(Long spaceID) {
    return TagSpec.builder()
      .maxHeight(1)
      .maxWidth(20)
      .build();
  }

  /**
   * 批量更新标签键状态
   * 迁移对应关系: Go语言TagServiceImpl.BatchUpdateTagStatus
   * - 功能: 批量更新标签键状态
   * - 参数: spaceID - 空间ID, tagKeyIDs - 标签键ID列表, toStatus - 目标状态
   * - 返回: 标签键ID到错误信息的映射
   * - 用途: 批量更新标签键状态
   */
  @Override
  @Transactional
  public Map<Long, String> batchUpdateTagStatus(Long spaceID, List<Long> tagKeyIDs, TagStatus toStatus) {
    if (toStatus == TagStatus.UNDEFINED || toStatus == TagStatus.DEPRECATED) {
      logger.error("[BatchUpdateTagStatus] toStatus is illegal: {}", toStatus);
      throw new BssException("to_status is illegal");
    }
    if (CollectionUtils.isEmpty(tagKeyIDs)) {
      logger.error("[BatchUpdateTagStatus] tag key ids is empty");
      throw new BssException("tag key ids is empty");
    }

    Map<Long, String> errInfo = new HashMap<>();
    for (Long tagKeyID : tagKeyIDs) {
      try {
        updateTagStatusWithNewVersion(spaceID, tagKeyID, toStatus);
      }
      catch (Exception e) {
        logger.warn("[BatchUpdateTagStatus] update tag status failed, spaceID: {}, tagKeyID: {}", spaceID, tagKeyID, e);
        errInfo.put(tagKeyID, e.getMessage());
      }
    }
    return errInfo;
  }

  /**
   * 搜索标签
   * 迁移对应关系: Go语言TagServiceImpl.SearchTags
   * - 功能: 搜索标签
   * - 参数: spaceID - 空间ID, param - 搜索参数
   * - 返回: 标签键列表
   * - 用途: 搜索标签并返回结果
   * - 注意: 需要分页标注，后续单独处理
   */
  @Override
  public List<TagKey> searchTags(Long spaceID, MGetTagKeyParam param) {
    if (param == null) {
      logger.error("[SearchTags] param is null");
      throw new BssException("param is null");
    }

    try {
      List<TagKey> tagKeys = tagRepo.mGetTagKeys(param);
      for (TagKey item : tagKeys) {
        List<TagValue> tagValues = getAndBuildTagValues(spaceID, item.getTagKeyId(), item.getVersionNum());
        item.setTagValues(tagValues);
      }
      return tagKeys;
    }
    catch (Exception e) {
      logger.warn("[SearchTags] get tag keys failed, param: {}", param, e);
      throw new BssException("SearchTags failed", e);
    }
  }

  /**
   * 获取标签详情
   * 迁移对应关系: Go语言TagServiceImpl.GetTagDetail
   * - 功能: 获取标签详情
   * - 参数: spaceID - 空间ID, param - 获取详情请求参数
   * - 返回: 获取详情响应
   * - 用途: 获取标签的详细信息
   */
  @Override
  public GetTagDetailResp getTagDetail(Long spaceID, GetTagDetailReq param) {
    if (param == null) {
      logger.error("[GetTagDetail] param is null");
      throw new BssException("param is null");
    }

    GetTagDetailResp resp = new GetTagDetailResp();
    List<TagKey> tagKeys;
    Long total;

    // 兼容逻辑
    if (param.getPageSize() == null || param.getPageSize() == 0) {
      tagKeys = getAllTagKeyVersionsByKeyID(spaceID, param.getTagKeyId());
      total = (long) tagKeys.size();
    }
    else {
      MGetTagKeyParam queryParam = new MGetTagKeyParam();
      queryParam.setSpaceId(spaceID);
      queryParam.setTagKeyIds(List.of(param.getTagKeyId()));

      tagKeys = tagRepo.mGetTagKeys(queryParam);
      total = tagRepo.countTagKeys(queryParam);
    }

    resp.setTotal(total);
    resp.setTagKeys(tagKeys);

    // 填充tag values
    for (TagKey item : tagKeys) {
      List<TagValue> tagValues = getAndBuildTagValues(spaceID, item.getTagKeyId(), item.getVersionNum());
      item.setTagValues(tagValues);
    }

    return resp;
  }

  /**
   * 通过标签键ID批量获取标签键信息
   * 迁移对应关系: Go语言TagServiceImpl.BatchGetTagsByTagKeyIDs
   * - 功能: 通过标签键ID批量获取标签键信息
   * - 参数: spaceID - 空间ID, tagKeyIDs - 标签键ID列表
   * - 返回: 标签键列表
   * - 用途: 批量获取标签键信息
   */
  @Override
  public List<TagKey> batchGetTagsByTagKeyIds(Long spaceID, List<Long> tagKeyIDs) {
    if (CollectionUtils.isEmpty(tagKeyIDs)) {
      logger.error("[BatchGetTagsByTagKeyIDs] tag key list is empty");
      throw new BssException("tag key list is empty");
    }

    MGetTagKeyParam params = new MGetTagKeyParam();
    params.setSpaceId(spaceID);
    params.setStatus(List.of(TagStatus.ACTIVE, TagStatus.INACTIVE));
    params.setTagKeyIds(tagKeyIDs);

    List<TagKey> tagKeys = tagRepo.mGetTagKeys(params);
    for (TagKey item : tagKeys) {
      List<TagValue> tagValues = getAndBuildTagValues(spaceID, item.getTagKeyId(), item.getVersionNum());
      item.setTagValues(tagValues);
    }
    return tagKeys;
  }

  // 辅助方法

  private boolean validateVersion(String oldVersion, String newVersion) {
    // 实现版本验证逻辑
    return !VersionUtils.validateVersion(oldVersion, newVersion);
  }

  private String simpleIncrementVersion(String version) {
    // 实现版本递增逻辑
    return VersionUtils.simpleIncrementVersion(version);
  }

  private void copyTagKey(TagKey source, TagKey target) {
    // 实现标签键复制逻辑
    target.setId(source.getId());
    target.setAppId(source.getAppId());
    target.setSpaceId(source.getSpaceId());
    target.setVersionNum(source.getVersionNum());
    target.setTagKeyId(source.getTagKeyId());
    target.setTagKeyName(source.getTagKeyName());
    target.setDescription(source.getDescription());
    target.setStatus(source.getStatus());
    target.setTagType(source.getTagType());
    target.setParentKeyId(source.getParentKeyId());
    target.setCreatedBy(source.getCreatedBy());
    target.setCreatedAt(source.getCreatedAt());
    target.setUpdatedBy(source.getUpdatedBy());
    target.setUpdatedAt(source.getUpdatedAt());
    target.setTagContentType(source.getTagContentType());
    target.setChangeLogs(source.getChangeLogs());
    target.setTagTargetType(source.getTagTargetType());
    target.setContentSpec(source.getContentSpec());
    target.setVersion(source.getVersion());
    target.setTagValues(source.getTagValues());
  }
}
