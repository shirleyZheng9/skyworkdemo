package com.iwhalecloud.bote.loop.data.infra.repo.tag.convertor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.iwhalecloud.bote.entity.loop.data.tag.TagKeyEntity;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.ChangeLog;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagContentSpec;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagContentType;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKey;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagTargetType;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagType;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class TagKeyConvertor {

  private static final ObjectMapper objectMapper = JsonMapper.builder().build();

  private TagKeyConvertor() {
  }

  /**
   * 将TagKeyEntity转换为TagKey，保持与Go版本相同的转换逻辑
   *
   * @param val TagKeyEntity对象
   * @return 转换后的TagKey对象
   * @throws BssException 当JSON反序列化失败时抛出
   */
  public static TagKey tagKeyPO2DO(TagKeyEntity val) {
    if (val == null) {
      return null;
    }

    TagKey.TagKeyBuilder builder = TagKey.builder()
      .id(val.getId())
      .appId(val.getAppId())
      .spaceId(val.getSpaceId())
      .versionNum(val.getVersionNum())
      .tagKeyId(val.getTagKeyId())
      .tagKeyName(val.getTagKeyName())
      .description(val.getDescription())
      .status(TagStatus.fromValue(val.getStatus()))
      .tagType(TagType.fromValue(val.getTagType()))
      .parentKeyId(val.getParentKeyId())
      .createdBy(val.getCreatedBy())
      .createdAt(val.getCreatedAt())
      .updatedBy(val.getUpdatedBy())
      .updatedAt(val.getUpdatedAt())
      .tagContentType(TagContentType.fromValue(val.getContentType()));

    // 处理ChangeLog的JSON反序列化
    if (val.getChangeLog() != null) {
      try {
        List<ChangeLog> changeLogs = objectMapper.readValue(
          val.getChangeLog(),
          new TypeReference<>() {
          }
        );
        builder.changeLogs(changeLogs);
      }
      catch (Exception e) {
        throw new BssException("unmarshal tag key change log failed", e);
      }
    }

    // 处理TagTargetType的拆分与转换
    if (val.getTagTargetType() != null && !val.getTagTargetType().isEmpty()) {
      List<TagTargetType> targetTypes = Arrays.stream(val.getTagTargetType().split(","))
        .map(TagTargetType::fromValue)
        .collect(Collectors.toList());
      builder.tagTargetType(targetTypes);
    }

    // 处理Spec的JSON反序列化
    if (val.getSpec() != null) {
      try {
        TagContentSpec contentSpec = objectMapper.readValue(val.getSpec(), TagContentSpec.class);
        builder.contentSpec(contentSpec);
      }
      catch (Exception e) {
        throw new BssException("unmarshal tag key content spec failed", e);
      }
    }

    return builder.build();
  }

}
