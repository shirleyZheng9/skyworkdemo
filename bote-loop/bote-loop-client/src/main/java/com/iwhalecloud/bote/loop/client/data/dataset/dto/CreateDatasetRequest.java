package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetCategoryDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetFeaturesDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVisibilityDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldSchemaDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.SecurityLevelDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建数据集请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDatasetRequest {

  private Long workspaceId;

  private Integer appId;

  private String name;

  private String description;

  private DatasetCategoryDTO category;

  private String bizCategory;

  private List<FieldSchemaDTO> fields;

  private SecurityLevelDTO securityLevel;

  private DatasetVisibilityDTO visibility;

  private DatasetSpecDTO spec;

  private DatasetFeaturesDTO features;

  private Base base;

  private Long catalogItemId;
}
