package com.iwhalecloud.bote.loop.data.infra.repo.tag;

import com.iwhalecloud.bote.entity.loop.data.tag.TagValueEntity;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagValueParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagValue;
import com.iwhalecloud.bote.loop.data.domain.tag.repo.tag.TagValueRepo;
import com.iwhalecloud.bote.loop.data.infra.repo.tag.convertor.TagValueConvertor;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bote.mapper.loop.data.tag.TagValueMapper;
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

/**
 * TagValue Repository实现类 - 简化版
 */
@Component("tagValueRepoImpl")
@RequiredArgsConstructor
public class TagValueRepoImpl implements TagValueRepo {
  private final TagValueMapper tagValueMapper;
  private final IIDGenerator idGenerator;

  /**
   * 批量创建TagValue
   */
  public void mCreateTagValues(List<TagValue> tagValues) {
    if (CollectionUtils.isEmpty(tagValues)) {
      return;
    }

    try {
      // 生成ID - 复用Go代码逻辑：每个TagValue需要2个ID（ID和TagValueID）

      // 设置ID - 复用Go代码逻辑
      for (TagValue tagValue : tagValues) {
        // 设置主ID
        tagValue.setId(idGenerator.genId());
        // 如果TagValueID为空或0，则设置TagValueID
        if (tagValue.getTagValueId() == null || tagValue.getTagValueId() == 0) {
          tagValue.setTagValueId(idGenerator.genId());
        }
      }

      // 转换为PO - 复用ToPO方法逻辑
      List<TagValueEntity> pos = tagValues.stream()
        .map(TagValue::toPO)
        .collect(Collectors.toList());

      // 批量插入
      tagValueMapper.batchInsert(pos);

    }
    catch (Exception e) {
      throw new BssException("MCreateTagValues failed", e);
    }
  }

  /**
   * 根据spaceID和id获取TagValue
   */
  public TagValue getTagValue(Long spaceId, Long id) {
    if (spaceId == null || id == null) {
      throw new BssException("space_id and id are required");
    }

    try {
      TagValueEntity po = tagValueMapper.selectBySpaceIdAndId(spaceId, id);
      if (po == null) {
        return null;
      }
      return TagValueConvertor.tagValuePO2DO(po);

    }
    catch (Exception e) {
      throw new BssException("GetTagValue failed", e);
    }
  }

  /**
   * 批量获取TagValue
   */
  public List<TagValue> mGetTagValue(MGetTagValueParam param) {
    if (param == null) {
      throw new BssException("param is null");
    }

    try {
      Map<String, Object> queryParams = buildQueryParams(param);
      List<TagValueEntity> pos = tagValueMapper.selectByCondition(queryParams);

      return pos.stream()
        .map(TagValueConvertor::tagValuePO2DO)
        .collect(Collectors.toList());

    }
    catch (Exception e) {
      throw new BssException("MGetTagValues failed", e);
    }
  }

  /**
   * 更新TagValue - 使用PO层更新
   */
  public void patchTagValue(Long spaceId, Long id, TagValue patch) {
    if (spaceId == null || id == null) {
      throw new BssException("space_id and id are required");
    }
    if (patch == null) {
      throw new BssException("patch is null");
    }

    try {
      // 先查询现有实体
      TagValueEntity existingEntity = tagValueMapper.selectBySpaceIdAndId(spaceId, id);
      if (existingEntity == null) {
        throw new BssException("tag value is not exist, space_id: " + spaceId + ", id: " + id);
      }

      TagValueEntity pp = patch.toPO();
      if (pp == null) {
        throw new BssException("patch is null");
      }
      pp.setId(id);
      pp.setSpaceId(spaceId);
      pp.setUpdatedAt(LocalDateTime.now());

      int updatedRows = tagValueMapper.updateByCondition(spaceId, id, pp);
      if (updatedRows == 0) {
        throw new BssException("tag value is not exist, space_id: " + spaceId + ", id: " + id);
      }

    }
    catch (Exception e) {
      throw new BssException("PatchTagValue failed", e);
    }
  }

  /**
   * 删除TagValue
   */
  public void deleteTagValue(Long spaceId, Long id) {
    if (spaceId == null || id == null) {
      throw new BssException("space_id and id are required");
    }

    try {
      int deletedRows = tagValueMapper.deleteBySpaceIdAndId(spaceId, id);
      if (deletedRows == 0) {
        throw new BssException("tag value not found, space_id: " + spaceId + ", id: " + id);
      }

    }
    catch (Exception e) {
      throw new BssException("DeleteTagValue failed", e);
    }
  }

  /**
   * 更新TagValues状态
   */
  public void updateTagValuesStatus(Long spaceId, Long tagKeyId, Integer versionNum, TagStatus toStatus, Boolean updateInfo) {
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

      tagValueMapper.updateStatusByCondition(spaceId, tagKeyId, versionNum, toStatus.name(), updatedAt, updatedBy);

    }
    catch (Exception e) {
      throw new BssException("UpdateTagValuesStatus failed", e);
    }
  }

  /**
   * 构建查询参数 - 简化版
   */
  private Map<String, Object> buildQueryParams(MGetTagValueParam param) {
    Map<String, Object> params = new HashMap<>();

    // 必须参数
    params.put("spaceId", param.getSpaceId());

    // 可选参数 - 直接传递，让XML处理判断
    params.put("ids", param.getIds());
    params.put("status", param.getStatus() != null ? param.getStatus().name() : null);
    params.put("tagKeyId", param.getTagKeyId());
    params.put("version", param.getVersion());
    params.put("tagValueIds", param.getTagValueId());

    return params;
  }
}
