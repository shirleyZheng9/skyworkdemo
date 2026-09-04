package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVersionDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 列表数据集版本响应DTO
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDatasetVersionsResponse {
  private List<DatasetVersionDTO> versions;
  private String nextPageToken;
  private BaseResp baseResp;
  private Long total;
}
