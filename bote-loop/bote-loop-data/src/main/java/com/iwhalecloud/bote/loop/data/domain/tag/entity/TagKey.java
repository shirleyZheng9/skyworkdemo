package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签键实体类
 * 迁移对应关系: Go语言TagKey
 * - 功能: 标签键实体
 * - 字段定义: 各种标签键字段
 * <p>
 * Java实现说明:
 * - 对应Go的TagKey结构体
 * - 使用Lombok注解简化代码
 * - 提供标签键数据模型
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go JSON标签 -> Java Jackson注解
 * - Go指针 -> Java对象引用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagKey {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("version")
  private String version;

  @JsonProperty("version_num")
  private Integer versionNum;

  @JsonProperty("tag_key_id")
  private Long tagKeyId;

  @JsonProperty("tag_key_name")
  private String tagKeyName;

  @JsonProperty("description")
  private String description;

  @JsonProperty("status")
  private TagStatus status;

  @JsonProperty("tag_type")
  private TagType tagType;

  @JsonProperty("tag_target_type")
  private List<TagTargetType> tagTargetType;

  @JsonProperty("parent_key_id")
  private Long parentKeyId;

  @JsonProperty("tag_values")
  private List<TagValue> tagValues;

  @JsonProperty("change_logs")
  private List<ChangeLog> changeLogs;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("updated_at")
  private LocalDateTime updatedAt;

  @JsonProperty("tag_content_type")
  private TagContentType tagContentType;

  @JsonProperty("tag_content_spec")
  private TagContentSpec contentSpec;

  // Setter方法
  public void setVersionNum(Integer versionNum) {
    this.versionNum = versionNum;
    if (tagValues != null) {
      for (TagValue tagValue : tagValues) {
        tagValue.setVersionNum(versionNum);
        setVersionNumRecursive(tagValue.getChildren(), versionNum);
      }
    }
  }

  public void setSpaceId(Long spaceId) {
    this.spaceId = spaceId;
    if (tagValues != null) {
      for (TagValue tagValue : tagValues) {
        tagValue.setSpaceId(spaceId);
        setSpaceIdRecursive(tagValue.getChildren(), spaceId);
      }
    }
  }

  public void setAppId(Integer appId) {
    this.appId = appId;
    if (tagValues != null) {
      for (TagValue tagValue : tagValues) {
        tagValue.setAppId(appId);
        setAppIdRecursive(tagValue.getChildren(), appId);
      }
    }
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    if (tagValues != null) {
      for (TagValue tagValue : tagValues) {
        tagValue.setCreatedBy(createdBy);
        setCreatedByRecursive(tagValue.getChildren(), createdBy);
      }
    }
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    if (tagValues != null) {
      for (TagValue tagValue : tagValues) {
        tagValue.setUpdatedBy(updatedBy);
        setUpdatedByRecursive(tagValue.getChildren(), updatedBy);
      }
    }
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    if (tagValues != null) {
      for (TagValue tagValue : tagValues) {
        tagValue.setCreatedAt(createdAt);
        setCreatedAtRecursive(tagValue.getChildren(), createdAt);
      }
    }
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    if (tagValues != null) {
      for (TagValue tagValue : tagValues) {
        tagValue.setUpdatedAt(updatedAt);
        setUpdatedAtRecursive(tagValue.getChildren(), updatedAt);
      }
    }
  }

  public void setStatus(TagStatus status) {
    this.status = status;
    if (tagValues != null) {
      for (TagValue tagValue : tagValues) {
        tagValue.setStatus(status);
        setStatusRecursive(tagValue.getChildren(), status);
      }
    }
  }

  public void setTagKeyId(Long tagKeyId) {
    this.tagKeyId = tagKeyId;
    if (tagValues != null) {
      for (TagValue tagValue : tagValues) {
        tagValue.setTagKeyId(tagKeyId);
        setTagKeyIdRecursive(tagValue.getChildren(), tagKeyId);
      }
    }
  }

  // 递归设置方法
  private void setVersionNumRecursive(List<TagValue> children, Integer versionNum) {
    if (children != null) {
      for (TagValue child : children) {
        child.setVersionNum(versionNum);
        setVersionNumRecursive(child.getChildren(), versionNum);
      }
    }
  }

  private void setSpaceIdRecursive(List<TagValue> children, Long spaceId) {
    if (children != null) {
      for (TagValue child : children) {
        child.setSpaceId(spaceId);
        setSpaceIdRecursive(child.getChildren(), spaceId);
      }
    }
  }

  private void setAppIdRecursive(List<TagValue> children, Integer appId) {
    if (children != null) {
      for (TagValue child : children) {
        child.setAppId(appId);
        setAppIdRecursive(child.getChildren(), appId);
      }
    }
  }

  private void setCreatedByRecursive(List<TagValue> children, String createdBy) {
    if (children != null) {
      for (TagValue child : children) {
        child.setCreatedBy(createdBy);
        setCreatedByRecursive(child.getChildren(), createdBy);
      }
    }
  }

  private void setUpdatedByRecursive(List<TagValue> children, String updatedBy) {
    if (children != null) {
      for (TagValue child : children) {
        child.setUpdatedBy(updatedBy);
        setUpdatedByRecursive(child.getChildren(), updatedBy);
      }
    }
  }

  private void setCreatedAtRecursive(List<TagValue> children, LocalDateTime createdAt) {
    if (children != null) {
      for (TagValue child : children) {
        child.setCreatedAt(createdAt);
        setCreatedAtRecursive(child.getChildren(), createdAt);
      }
    }
  }

  private void setUpdatedAtRecursive(List<TagValue> children, LocalDateTime updatedAt) {
    if (children != null) {
      for (TagValue child : children) {
        child.setUpdatedAt(updatedAt);
        setUpdatedAtRecursive(child.getChildren(), updatedAt);
      }
    }
  }

  private void setStatusRecursive(List<TagValue> children, TagStatus status) {
    if (children != null) {
      for (TagValue child : children) {
        child.setStatus(status);
        setStatusRecursive(child.getChildren(), status);
      }
    }
  }

  private void setTagKeyIdRecursive(List<TagValue> children, Long tagKeyId) {
    if (children != null) {
      for (TagValue child : children) {
        child.setTagKeyId(tagKeyId);
        setTagKeyIdRecursive(child.getChildren(), tagKeyId);
      }
    }
  }
}
