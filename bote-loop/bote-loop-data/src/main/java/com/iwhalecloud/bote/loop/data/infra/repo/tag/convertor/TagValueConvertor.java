package com.iwhalecloud.bote.loop.data.infra.repo.tag.convertor;

import com.iwhalecloud.bote.entity.loop.data.tag.TagValueEntity;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagValue;

/**
 * TagValue转换器 - 保持与Go版本相同的转换逻辑
 */
public final class TagValueConvertor {
  private TagValueConvertor() {
  }

  /**
   * 将TagValueEntity转换为TagValue，保持与Go版本相同的转换逻辑
   *
   * @param val TagValueEntity对象
   * @return 转换后的TagValue对象
   */
  public static TagValue tagValuePO2DO(TagValueEntity val) {
    if (val == null) {
      return null;
    }

    TagValue.TagValueBuilder builder = TagValue.builder()
      .id(val.getId())
      .appId(val.getAppId())
      .spaceId(val.getSpaceId())
      .versionNum(val.getVersionNum())
      .tagKeyId(val.getTagKeyId())
      .tagValueId(val.getTagValueId())
      .tagValueName(val.getTagValueName())
      .description(val.getDescription())
      .status(TagStatus.fromValue(val.getStatus()))
      .parentValueId(val.getParentValueId())
      .createdBy(val.getCreatedBy())
      .createdAt(val.getCreatedAt())
      .updatedBy(val.getUpdatedBy())
      .updatedAt(val.getUpdatedAt());

    // 处理IsSystem字段 - 复用Go代码逻辑
    // Go代码: IsSystem: val.TagValueName == consts.FallbackTagValueDefaultName
    boolean isSystem = TagConsts.FALLBACK_TAG_VALUE_DEFAULT_NAME.equals(val.getTagValueName());
    builder.isSystem(isSystem);

    return builder.build();
  }
}
