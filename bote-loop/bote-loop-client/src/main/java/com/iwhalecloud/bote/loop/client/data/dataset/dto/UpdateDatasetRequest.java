package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 更新数据集请求DTO
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
public class UpdateDatasetRequest extends CreateDatasetRequest {
  private Long datasetId;
}
