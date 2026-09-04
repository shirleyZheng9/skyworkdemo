package com.iwhalecloud.bote.loop.data.application.convertor;

import com.iwhalecloud.bote.loop.client.base.BaseInfo;
import com.iwhalecloud.bote.loop.client.data.domain.tag.ChangeLogDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.ChangeTargetTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.ContinuousNumberSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.OperationTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagContentSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagContentTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagDomainTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagInfoDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagStatusDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagValueDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.UserInfoDTO;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.ChangeLog;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.ContinuousNumberSpec;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagChangeTargetType;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagContentSpec;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagContentType;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKey;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagOperationType;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagTargetType;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagType;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagValue;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

public final class TagConvertor {

  private TagConvertor() {
    // 工具类，禁止实例化
  }

  public static TagValue convertTagValueDTOtoDO(TagValueDTO dto) {
    TagValue tagValue = new TagValue();

    tagValue.setId(dto.getId());
    tagValue.setAppId(dto.getAppId());
    tagValue.setSpaceId(dto.getWorkspaceId());
    tagValue.setVersionNum(dto.getVersionNum());
    tagValue.setTagKeyId(dto.getTagKeyId());
    tagValue.setTagValueId(tagValue.getTagValueId());
    tagValue.setTagValueName(tagValue.getTagValueName());
    tagValue.setDescription(tagValue.getDescription());
    tagValue.setParentValueId(dto.getParentTagValueId());

    if (dto.getBaseInfo() != null) {
      BaseInfo baseInfo = dto.getBaseInfo();
      LocalDateTime createdAt = Instant.ofEpochMilli(baseInfo.getCreatedAt())
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
      LocalDateTime updatedAt = Instant.ofEpochMilli(baseInfo.getUpdatedAt())
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();

      tagValue.setCreatedAt(createdAt);
      tagValue.setUpdatedAt(updatedAt);
      final UserInfoDTO createdBy = baseInfo.getCreatedBy();
      final UserInfoDTO updatedBy = baseInfo.getUpdatedBy();
      if (createdBy != null) {
        tagValue.setCreatedBy(createdBy.getUserId());
      }
      if (updatedBy != null) {
        tagValue.setUpdatedBy(updatedBy.getUserId());
      }
    }

    return tagValue;
  }

  public static TagContentSpec convertTagContentSpecDTOtoDO(TagContentSpecDTO dto) {
    TagContentSpec tagContentSpec = new TagContentSpec();
    ContinuousNumberSpec continuousNumberSpec = convertContinuousNumberSpecDTOtoDO(dto.getContinuousNumberSpec());
    tagContentSpec.setContinuousNumberSpec(continuousNumberSpec);

    return tagContentSpec;
  }

  public static ContinuousNumberSpec convertContinuousNumberSpecDTOtoDO(ContinuousNumberSpecDTO dto) {
    ContinuousNumberSpec continuousNumberSpec = new ContinuousNumberSpec();
    continuousNumberSpec.setMinValue(dto.getMinValue());
    continuousNumberSpec.setMinValueDesc(dto.getMinValueDescription());
    continuousNumberSpec.setMaxValue(dto.getMaxValue());
    continuousNumberSpec.setMaxValueDesc(dto.getMaxValueDescription());
    return continuousNumberSpec;
  }

  public static TagContentType convertTagContentTypeDTOtoDO(TagContentTypeDTO dto) {
    return TagContentType.fromValue(dto.getValue());
  }

  public static TagTargetType convertTagContentTypeDTOtoDO(TagDomainTypeDTO dto) {
    switch (dto) {
      case DATA -> {
        return TagTargetType.DATASET_ITEM;
      }
      case OBSERVE -> {
        return TagTargetType.OBSERVE;
      }
      case EVALUATION -> {
        return TagTargetType.EVALUATION;
      }
      default -> {
        return TagTargetType.UNDEFINED;
      }
    }

  }

  public static TagInfoDTO convertTagKeyDOtoDTO(TagKey val) {
    TagInfoDTO dto = new TagInfoDTO();
    dto.setId(val.getId());
    dto.setAppID(val.getAppId());
    dto.setWorkspaceId(val.getSpaceId());
    dto.setVersion(val.getVersion());
    dto.setVersionNum(val.getVersionNum());
    dto.setTagKeyId(val.getTagKeyId());
    dto.setTagKeyName(val.getTagKeyName());
    dto.setDescription(val.getDescription());
    dto.setStatus(convertTagStatusDOtoDTO(val.getStatus()));
    dto.setTagType(convertTagTypeDOtoDTO(val.getTagType()));
    dto.setParentTagKeyId(val.getParentKeyId());
    dto.setChangeLogs(convertChangeLogDOtoDTO(val.getChangeLogs()));
    dto.setDomainTypeList(convertTagContentTypeDOtoDTO(val.getTagTargetType()));
    dto.setContentType(convertTagContentTypeDOtoDTO(val.getTagContentType()));
    dto.setContentSpec(convertTagContentSpecDOtoDTO(val.getContentSpec()));

    BaseInfo baseInfo = new BaseInfo();
    UserInfoDTO createdBy = new UserInfoDTO();
    createdBy.setUserId(val.getCreatedBy());
    UserInfoDTO updatedBy = new UserInfoDTO();
    updatedBy.setUserId(val.getUpdatedBy());
    baseInfo.setCreatedBy(createdBy);
    baseInfo.setUpdatedBy(updatedBy);
    baseInfo.setCreatedAt(val.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
    baseInfo.setUpdatedAt(val.getUpdatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
    dto.setBaseInfo(baseInfo);
    return dto;
  }

  public static List<ChangeLogDTO> convertChangeLogDOtoDTO(List<ChangeLog> valList) {
    return valList.stream().map(TagConvertor::convertChangeLogDOtoDTO).toList();
  }

  public static ChangeLogDTO convertChangeLogDOtoDTO(ChangeLog val) {
    ChangeLogDTO dto = new ChangeLogDTO();
    dto.setTarget(convertTagChangeTargetTypeDOtoDTO(val.getChangeTarget()));
    dto.setOperation(convertTagOperationTypeDOtoDTO(val.getOperation()));
    dto.setBeforeValue(val.getBeforeValue());
    dto.setTargetValue(val.getTargetValue());
    return dto;
  }

  public static OperationTypeDTO convertTagOperationTypeDOtoDTO(TagOperationType val) {
    return OperationTypeDTO.fromValue(val.getValue());
  }

  public static ChangeTargetTypeDTO convertTagChangeTargetTypeDOtoDTO(TagChangeTargetType val) {
    return ChangeTargetTypeDTO.fromValue(val.getValue());
  }

  public static TagStatusDTO convertTagStatusDOtoDTO(TagStatus val) {
    return TagStatusDTO.fromValue(val.getValue());
  }

  public static TagTypeDTO convertTagTypeDOtoDTO(TagType val) {
    return TagTypeDTO.fromValue(val.getValue());
  }

  public static List<TagDomainTypeDTO> convertTagContentTypeDOtoDTO(List<TagTargetType> valList) {
    return valList.stream().map(TagConvertor::convertTagContentTypeDOtoDTO).toList();
  }

  public static TagDomainTypeDTO convertTagContentTypeDOtoDTO(TagTargetType val) {
    switch (val) {
      case DATASET_ITEM -> {
        return TagDomainTypeDTO.DATA;
      }
      case OBSERVE -> {
        return TagDomainTypeDTO.OBSERVE;
      }
      case EVALUATION -> {
        return TagDomainTypeDTO.EVALUATION;
      }
      default -> {
        return null;
      }
    }
  }

  public static TagContentTypeDTO convertTagContentTypeDOtoDTO(TagContentType val) {
    return TagContentTypeDTO.fromValue(val.getValue());
  }

  public static TagContentSpecDTO convertTagContentSpecDOtoDTO(TagContentSpec val) {
    TagContentSpecDTO dto = new TagContentSpecDTO();
    ContinuousNumberSpecDTO continuousNumberSpecDTO = convertContinuousNumberSpecDOtoDTO(val.getContinuousNumberSpec());
    dto.setContinuousNumberSpec(continuousNumberSpecDTO);
    return dto;
  }

  public static ContinuousNumberSpecDTO convertContinuousNumberSpecDOtoDTO(ContinuousNumberSpec val) {
    ContinuousNumberSpecDTO dto = new ContinuousNumberSpecDTO();
    dto.setMinValue(val.getMinValue());
    dto.setMinValueDescription(val.getMinValueDesc());
    dto.setMaxValue(val.getMaxValue());
    dto.setMaxValueDescription(val.getMaxValueDesc());
    return dto;
  }

}
