package com.iwhalecloud.bote.loop.data.infra.repo.tag;

import com.iwhalecloud.bote.entity.loop.data.tag.TagKeyEntity;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagKeyParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKey;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKeyUtil;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.repo.tag.TagKeyRepo;
import com.iwhalecloud.bote.loop.data.infra.repo.tag.convertor.TagKeyConvertor;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bote.mapper.loop.data.tag.TagKeyMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * TagKey Repository实现类 - 简化版
 */
@Component("tagKeyRepoImpl")
@RequiredArgsConstructor
public class TagKeyRepoImpl implements TagKeyRepo {
  private final TagKeyMapper tagKeyMapper;
  private final IIDGenerator idGenerator;

  /**
   * 批量创建TagKey
   */
  public void mCreateTagKeys(List<TagKey> tagKeys) {
    if (CollectionUtils.isEmpty(tagKeys)) {
      return;
    }

    try {
      // 生成ID

      // 设置ID
      for (TagKey tagKey : tagKeys) {
        tagKey.setId(idGenerator.genId());
        if (tagKey.getTagKeyId() == null || tagKey.getTagKeyId() == 0) {
          tagKey.setTagKeyId(idGenerator.genId());
        }
      }

      // 转换为PO
      List<TagKeyEntity> pos = tagKeys.stream()
        .map(TagKeyUtil::toPO)
        .collect(Collectors.toList());

      // 批量插入
      tagKeyMapper.batchInsert(pos);

    }
    catch (Exception e) {
      throw new BssException("MCreateTagKeys failed", e);
    }
  }

  /**
   * 根据spaceID和id获取TagKey
   */
  public TagKey getTagKey(Long spaceId, Long id) {
    if (spaceId == null || id == null) {
      throw new BssException("space_id and id are required");
    }

    try {
      TagKeyEntity po = tagKeyMapper.selectBySpaceIdAndId(spaceId, id);
      if (po == null) {
        return null;
      }
      return TagKeyConvertor.tagKeyPO2DO(po);

    }
    catch (Exception e) {
      throw new BssException("GetTagKey failed", e);
    }
  }

  /**
   * 批量获取TagKey
   */
  public List<TagKey> mGetTagKeys(MGetTagKeyParam param) {
    if (param == null) {
      throw new BssException("param is null");
    }

    try {
      Map<String, Object> queryParams = buildQueryParams(param);
      List<TagKeyEntity> pos = tagKeyMapper.selectByCondition(queryParams);

      return pos.stream()
        .map(TagKeyConvertor::tagKeyPO2DO)
        .collect(Collectors.toList());

    }
    catch (Exception e) {
      throw new BssException("MGetTagKeys failed", e);
    }
  }

  /**
   * 更新TagKey - 使用PO层更新
   */
  public void patchTagKey(Long spaceId, Long id, TagKey patch) {
    if (spaceId == null || id == null) {
      throw new BssException("space_id and id are required");
    }
    if (patch == null) {
      throw new BssException("patch is null");
    }

    try {
      // 先查询现有实体
      TagKeyEntity existingEntity = tagKeyMapper.selectBySpaceIdAndId(spaceId, id);
      if (existingEntity == null) {
        throw new BssException("tagKey not found, space_id: " + spaceId + ", id: " + id);
      }

      TagKeyEntity pp = TagKeyUtil.toPO(patch);
      pp.setId(id);
      pp.setSpaceId(spaceId);
      pp.setUpdatedAt(LocalDateTime.now());

      int updatedRows = tagKeyMapper.updateByCondition(spaceId, id, pp);
      if (updatedRows == 0) {
        throw new BssException("tagKey not found, space_id: " + spaceId + ", id: " + id);
      }

    }
    catch (Exception e) {
      throw new BssException("PatchTagKey failed", e);
    }
  }

  /**
   * 删除TagKey
   */
  public void deleteTagKey(Long spaceId, Long id) {
    if (spaceId == null || id == null) {
      throw new BssException("space_id and id are required");
    }

    try {
      int deletedRows = tagKeyMapper.deleteBySpaceIdAndId(spaceId, id);
      if (deletedRows == 0) {
        throw new BssException("tagKey not found, space_id: " + spaceId + ", id: " + id);
      }

    }
    catch (Exception e) {
      throw new BssException("DeleteTagKey failed", e);
    }
  }

  /**
   * 更新TagKeys状态
   */
  public void updateTagKeysStatus(Long spaceId, Long tagKeyId, Integer versionNum, TagStatus toStatus, Boolean updateInfo) {
    if (spaceId == null || tagKeyId == null) {
      throw new BssException("space_id and tagKeyID are required");
    }

    try {
      String updatedAt = null;
      String updatedBy = null;

      if (updateInfo) {
        updatedAt = DateUtil.format();
        updatedBy = SessionContext.getCurrentUserId();
      }

      tagKeyMapper.updateStatusByCondition(spaceId, tagKeyId, versionNum, toStatus.name(), updatedAt, updatedBy);

    }
    catch (Exception e) {
      throw new BssException("UpdateTagKeysStatus failed", e);
    }
  }

  /**
   * 统计TagKeys数量
   */
  public Long countTagKeys(MGetTagKeyParam param) {
    if (param == null) {
      throw new BssException("param is null");
    }

    try {
      Map<String, Object> queryParams = buildQueryParams(param);
      return tagKeyMapper.countByCondition(queryParams);

    }
    catch (Exception e) {
      throw new BssException("CountTagKeys failed", e);
    }
  }

  /**
   * 构建查询参数 - 简化版
   */
  private Map<String, Object> buildQueryParams(MGetTagKeyParam param) {
    Map<String, Object> params = new HashMap<>();

    // 必须参数
    params.put("spaceId", param.getSpaceId());

    // 可选参数 - 直接传递，让XML处理判断
    params.put("ids", param.getIds());
    params.put("tagType", param.getTagType() != null ? param.getTagType().name() : null);

    if (!CollectionUtils.isEmpty(param.getStatus())) {
      List<String> statusNames = param.getStatus().stream()
        .map(Enum::name)
        .collect(Collectors.toList());
      params.put("statusList", statusNames);
    }

    params.put("tagKeyIds", param.getTagKeyIds());
    params.put("createdBys", param.getCreatedBys());

    if (!CollectionUtils.isEmpty(param.getTagContentTypes())) {
      List<String> contentTypeNames = param.getTagContentTypes().stream()
        .map(Enum::name)
        .collect(Collectors.toList());
      params.put("contentTypes", contentTypeNames);
    }

    params.put("tagKeyName", param.getTagKeyName());
    params.put("tagKeyNameLike", StringUtils.hasText(param.getTagKeyNameLike()) ?
      "%" + param.getTagKeyNameLike() + "%" : null);

    return params;
  }
}
