package com.iwhalecloud.bote.loop.evaluation.application.convertor.common;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDisplayFormatDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ArgsSchemaDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.AudioDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ImageDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.MessageDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ModelConfigDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.OrderByDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.RoleDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.SessionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.UserInfoDTO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ArgsSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Audio;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Content;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ContentType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldDisplayFormat;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Image;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Message;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ModelConfig;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Role;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * 通用转换器
 * 对应Go: common/common.go
 * <p>
 * 注意：所有方法都是静态方法，不使用Spring Bean注解
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class CommonConvertor {

  private CommonConvertor() {
    // 工具类，禁止实例化
  }

  private static final Logger logger = LoggerFactory.getLogger(CommonConvertor.class);

  /**
   * 内容类型DTO转DO
   * 对应Go: ConvertContentTypeDTO2DO
   */
  public static ContentType convertContentTypeDTO2DO(String ct) {
    if (ct == null) {
      return null;
    }
    return ContentType.fromString(ct);
  }

  /**
   * 内容类型DO转DTO
   * 对应Go: ConvertContentTypeDO2DTO
   */
  public static String convertContentTypeDO2DTO(ContentType ct) {
    if (ct == null) {
      return null;
    }
    return ct.getValue();
  }

  /**
   * 图片DTO转DO
   * 对应Go: ConvertImageDTO2DO
   */
  public static Image convertImageDTO2DO(ImageDTO img) {
    if (img == null) {
      return null;
    }
    return Image.builder()
      .name(img.getName())
      .url(img.getUrl())
      .uri(img.getUri())
      .thumbUrl(img.getThumbUrl())
      .build();
  }

  /**
   * 图片DO转DTO
   * 对应Go: ConvertImageDO2DTO
   */
  public static ImageDTO convertImageDO2DTO(Image img) {
    if (img == null) {
      return null;
    }
    return ImageDTO.builder()
      .name(img.getName())
      .url(img.getUrl())
      .uri(img.getUri())
      .thumbUrl(img.getThumbUrl())
      .build();
  }

  /**
   * 音频DTO转DO
   * 对应Go: ConvertAudioDTO2DO
   */
  public static Audio convertAudioDTO2DO(AudioDTO audio) {
    if (audio == null) {
      return null;
    }
    return Audio.builder()
      .format(audio.getFormat())
      .url(audio.getUrl())
      .build();
  }

  /**
   * 音频DO转DTO
   * 对应Go: ConvertAudioDO2DTO
   */
  public static AudioDTO convertAudioDO2DTO(Audio audio) {
    if (audio == null) {
      return null;
    }
    return AudioDTO.builder()
      .format(audio.getFormat())
      .url(audio.getUrl())
      .build();
  }

  /**
   * 内容DTO转DO
   * 对应Go: ConvertContentDTO2DO
   */
  public static Content convertContentDTO2DO(ContentDTO content) {
    if (content == null) {
      return null;
    }

    ContentType contentType = null;
    if (content.getContentType() != null) {
      contentType = ContentType.fromString(content.getContentType());
    }

    FieldDisplayFormat format = null;
    if (content.getFormat() != null) {
      format = FieldDisplayFormat.fromValue(content.getFormat().getValue());
    }

    List<Content> multiPart = null;
    if (!CollectionUtils.isEmpty(content.getMultiPart())) {
      multiPart = content.getMultiPart().stream()
        .map(CommonConvertor::convertContentDTO2DO)
        .collect(Collectors.toList());
    }

    return Content.builder()
      .contentType(contentType)
      .format(format)
      .text(content.getText())
      .image(convertImageDTO2DO(content.getImage()))
      .multiPart(multiPart)
      .audio(convertAudioDTO2DO(content.getAudio()))
      .build();
  }

  /**
   * 内容DO转DTO
   * 对应Go: ConvertContentDO2DTO
   */
  public static ContentDTO convertContentDO2DTO(Content content) {
    if (content == null) {
      return null;
    }

    String contentTypeStr = null;
    if (content.getContentType() != null) {
      contentTypeStr = content.getContentType().getValue();
    }

    FieldDisplayFormatDTO formatDTO = null;
    if (content.getFormat() != null) {
      formatDTO = FieldDisplayFormatDTO.fromValue(content.getFormat().getValue());
    }

    List<ContentDTO> multiPart = null;
    if (!CollectionUtils.isEmpty(content.getMultiPart())) {
      multiPart = content.getMultiPart().stream()
        .map(CommonConvertor::convertContentDO2DTO)
        .collect(Collectors.toList());
    }

    return ContentDTO.builder()
      .contentType(contentTypeStr)
      .format(formatDTO)
      .text(content.getText())
      .image(convertImageDO2DTO(content.getImage()))
      .multiPart(multiPart)
      .audio(convertAudioDO2DTO(content.getAudio()))
      .build();
  }

  /**
   * 排序DTO列表转DO列表
   * 对应Go: ConvertOrderByDTO2DOs
   */
  public static List<OrderBy> convertOrderByDTO2DOs(List<OrderByDTO> orders) {
    if (CollectionUtils.isEmpty(orders)) {
      return null;
    }
    return orders.stream()
      .map(CommonConvertor::convertOrderByDTO2DO)
      .collect(Collectors.toList());
  }

  /**
   * 排序DTO转DO
   * 对应Go: ConvertOrderByDTO2DO
   */
  public static OrderBy convertOrderByDTO2DO(OrderByDTO order) {
    if (order == null) {
      return null;
    }
    return OrderBy.builder()
      .field(order.getField())
      .isAsc(order.getIsAsc())
      .build();
  }

  /**
   * 排序DO转DTO
   * 对应Go: ConvertOrderByDO2DTO
   */
  public static OrderByDTO convertOrderByDO2DTO(OrderBy order) {
    if (order == null) {
      return null;
    }
    return OrderByDTO.builder()
      .field(order.getField())
      .isAsc(order.getIsAsc())
      .build();
  }

  /**
   * 角色DTO转DO
   * 对应Go: ConvertRoleDTO2DO
   */
  public static Role convertRoleDTO2DO(Long role) {
    if (role == null) {
      return null;
    }
    return Role.fromValue(role.intValue());
  }

  /**
   * 角色DO转DTO
   * 对应Go: ConvertRoleDO2DTO
   */
  public static Long convertRoleDO2DTO(Role role) {
    if (role == null) {
      return null;
    }
    return (long) role.getValue();
  }

  /**
   * 消息DTO转DO
   * 对应Go: ConvertMessageDTO2DO
   */
  public static Message convertMessageDTO2DO(MessageDTO msg) {
    if (msg == null) {
      return null;
    }

    Role role = Role.UNDEFINED;
    if (msg.getRole() != null) {
      role = Role.fromValue(msg.getRole().getValue());
    }

    return Message.builder()
      .role(role)
      .content(convertContentDTO2DO(msg.getContent()))
      .ext(msg.getExt())
      .build();
  }

  /**
   * 消息DO转DTO
   * 对应Go: ConvertMessageDO2DTO
   */
  public static MessageDTO convertMessageDO2DTO(Message msg) {
    if (msg == null) {
      return null;
    }

    RoleDTO role = null;
    if (msg.getRole() != Role.UNDEFINED) {
      role = RoleDTO.fromValue(msg.getRole().getValue());
    }

    return MessageDTO.builder()
      .role(role)
      .content(convertContentDO2DTO(msg.getContent()))
      .ext(msg.getExt())
      .build();
  }

  /**
   * 参数模式DTO转DO
   * 对应Go: ConvertArgsSchemaDTO2DO
   */
  public static ArgsSchema convertArgsSchemaDTO2DO(ArgsSchemaDTO schema) {
    if (schema == null) {
      return null;
    }

    List<ContentType> contentTypes = schema.getSupportContentTypes().stream()
      .map(ContentType::fromString)
      .collect(Collectors.toList());

    return ArgsSchema.builder()
      .key(schema.getKey())
      .supportContentTypes(contentTypes)
      .jsonSchema(schema.getJsonSchema())
      .build();
  }

  /**
   * 参数模式DO转DTO
   * 对应Go: ConvertArgsSchemaDO2DTO
   */
  public static ArgsSchemaDTO convertArgsSchemaDO2DTO(ArgsSchema schema) {
    if (schema == null) {
      return null;
    }

    List<String> contentTypes = schema.getSupportContentTypes().stream()
      .map(ContentType::getValue)
      .collect(Collectors.toList());

    return ArgsSchemaDTO.builder()
      .key(schema.getKey())
      .supportContentTypes(contentTypes)
      .jsonSchema(schema.getJsonSchema())
      .build();
  }

  /**
   * 用户信息DTO转DO
   * 对应Go: ConvertUserInfoDTO2DO
   */
  public static UserInfo convertUserInfoDTO2DO(UserInfoDTO info) {
    if (info == null) {
      return null;
    }
    return UserInfo.builder()
      .name(info.getName())
      .enName(info.getEnName())
      .avatarUrl(info.getAvatarUrl())
      .avatarThumb(info.getAvatarThumb())
      .openId(info.getOpenId())
      .unionId(info.getUnionId())
      .userId(info.getUserId())
      .email(info.getEmail())
      .build();
  }

  /**
   * 用户信息DO转DTO
   * 对应Go: ConvertUserInfoDO2DTO
   */
  public static UserInfoDTO convertUserInfoDO2DTO(UserInfo info) {
    if (info == null) {
      return null;
    }
    return UserInfoDTO.builder()
      .name(info.getName())
      .enName(info.getEnName())
      .avatarUrl(info.getAvatarUrl())
      .avatarThumb(info.getAvatarThumb())
      .openId(info.getOpenId())
      .unionId(info.getUnionId())
      .userId(info.getUserId())
      .email(info.getEmail())
      .build();
  }

  /**
   * 基础信息DTO转DO
   * 对应Go: ConvertBaseInfoDTO2DO
   */
  public static BaseInfo convertBaseInfoDTO2DO(BaseInfoDTO info) {
    if (info == null) {
      return null;
    }
    return BaseInfo.builder()
      .createdBy(convertUserInfoDTO2DO(info.getCreatedBy()))
      .updatedBy(convertUserInfoDTO2DO(info.getUpdatedBy()))
      .createdAt(info.getCreatedAt())
      .updatedAt(info.getUpdatedAt())
      .deletedAt(info.getDeletedAt())
      .build();
  }

  /**
   * 基础信息DO转DTO
   * 对应Go: ConvertBaseInfoDO2DTO
   */
  public static BaseInfoDTO convertBaseInfoDO2DTO(BaseInfo info) {
    if (info == null) {
      return null;
    }
    return BaseInfoDTO.builder()
      .createdBy(convertUserInfoDO2DTO(info.getCreatedBy()))
      .updatedBy(convertUserInfoDO2DTO(info.getUpdatedBy()))
      .createdAt(info.getCreatedAt())
      .updatedAt(info.getUpdatedAt())
      .deletedAt(info.getDeletedAt())
      .build();
  }

  /**
   * 模型配置DTO转DO
   * 对应Go: ConvertModelConfigDTO2DO
   */
  public static ModelConfig convertModelConfigDTO2DO(ModelConfigDTO config) {
    if (config == null) {
      return null;
    }

    return ModelConfig.builder()
      .modelId(config.getModelId())
      .modelName(config.getModelName())
      .temperature(config.getTemperature())
      .maxTokens(config.getMaxTokens())
      .topP(config.getTopP())
      .build();
  }

  /**
   * 模型配置DO转DTO
   * 对应Go: ConvertModelConfigDO2DTO
   */
  public static ModelConfigDTO convertModelConfigDO2DTO(ModelConfig config) {
    if (config == null) {
      return null;
    }

    ModelConfigDTO.ModelConfigDTOBuilder builder = ModelConfigDTO.builder()
      .modelName(config.getModelName())
      .temperature(config.getTemperature())
      .maxTokens(config.getMaxTokens())
      .topP(config.getTopP());

    if (config.getModelId() != null) {
      builder.modelId(config.getModelId());
    }
    else if (config.getProviderModelId() != null && !config.getProviderModelId().isEmpty()) {
      try {
        Long pModelId = Long.parseLong(config.getProviderModelId());
        builder.modelId(pModelId);
      }
      catch (NumberFormatException e) {
        // 日志记录解析失败，但不抛出异常
        logger.warn("failed to parse provider model id: {}, err: {}", config.getProviderModelId(), e.getMessage());
      }
    }

    return builder.build();
  }

  /**
   * 字段显示格式DTO转DO
   * 对应Go: ConvertFieldDisplayFormatDTO2DO
   */
  public static FieldDisplayFormat convertFieldDisplayFormatDTO2DO(Long fdf) {
    if (fdf == null) {
      return null;
    }
    return FieldDisplayFormat.fromValue(fdf.intValue());
  }

  /**
   * 字段显示格式DO转DTO
   * 对应Go: ConvertFieldDisplayFormatDO2DTO
   */
  public static Long convertFieldDisplayFormatDO2DTO(FieldDisplayFormat fdf) {
    if (fdf == null) {
      return null;
    }
    return (long) fdf.getValue();
  }

  /**
   * Session DTO转DO
   * 对应Go: Session相关转换
   */
  public static Session convertSessionDTO2DO(SessionDTO sessionDTO) {
    if (sessionDTO == null) {
      return null;
    }
    return Session.builder()
      .userId(sessionDTO.getUserId() != null ? String.valueOf(sessionDTO.getUserId()) : null)
      .appId(sessionDTO.getAppId())
      .build();
  }

  /**
   * Session DO转DTO
   * 对应Go: Session相关转换
   */
  public static SessionDTO convertSessionDO2DTO(Session session) {
    if (session == null) {
      return null;
    }

    Long userId = null;
    if (session.getUserId() != null) {
      try {
        userId = Long.parseLong(session.getUserId());
      }
      catch (NumberFormatException e) {
        // 处理用户ID格式错误
        logger.warn("Invalid userId format: {}", session.getUserId());
      }
    }

    return SessionDTO.builder()
      .userId(userId)
      .appId(session.getAppId())
      .build();
  }

  /**
   * 内容类型字符串转枚举
   * 辅助方法
   */
  public static ContentType convertContentTypeStringToDO(String contentTypeStr) {
    if (contentTypeStr == null || contentTypeStr.isEmpty()) {
      return null;
    }
    return ContentType.fromString(contentTypeStr);
  }

  /**
   * 内容类型枚举转字符串
   * 辅助方法
   */
  public static String convertContentTypeDOToString(ContentType contentType) {
    if (contentType == null) {
      return null;
    }
    return contentType.getValue();
  }
}
