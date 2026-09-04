package com.iwhalecloud.bote.loop.client.data.tag;

import com.iwhalecloud.bote.loop.client.data.tag.dto.BatchGetTagsRequest;
import com.iwhalecloud.bote.loop.client.data.tag.dto.BatchGetTagsResponse;
import com.iwhalecloud.bote.loop.client.data.tag.dto.BatchUpdateTagStatusRequest;
import com.iwhalecloud.bote.loop.client.data.tag.dto.BatchUpdateTagStatusResponse;
import com.iwhalecloud.bote.loop.client.data.tag.dto.CreateTagRequest;
import com.iwhalecloud.bote.loop.client.data.tag.dto.CreateTagResponse;
import com.iwhalecloud.bote.loop.client.data.tag.dto.GetTagDetailRequest;
import com.iwhalecloud.bote.loop.client.data.tag.dto.GetTagDetailResponse;
import com.iwhalecloud.bote.loop.client.data.tag.dto.GetTagSpecRequest;
import com.iwhalecloud.bote.loop.client.data.tag.dto.GetTagSpecResponse;
import com.iwhalecloud.bote.loop.client.data.tag.dto.SearchTagsRequest;
import com.iwhalecloud.bote.loop.client.data.tag.dto.SearchTagsResponse;
import com.iwhalecloud.bote.loop.client.data.tag.dto.UpdateTagRequest;
import com.iwhalecloud.bote.loop.client.data.tag.dto.UpdateTagResponse;

/**
 * Tag Service Interface
 * 对应Thrift: TagService
 */
public interface TagApplicationService {

  /**
   * 新增标签
   * 对应Thrift方法: CreateTag
   */
  CreateTagResponse createTag(CreateTagRequest request);

  /**
   * 更新标签
   * 对应Thrift方法: UpdateTag
   */
  UpdateTagResponse updateTag(UpdateTagRequest request);

  /**
   * 批量更新标签状态
   * 对应Thrift方法: BatchUpdateTagStatus
   */
  BatchUpdateTagStatusResponse batchUpdateTagStatus(BatchUpdateTagStatusRequest request);

  /**
   * 搜索标签
   * 对应Thrift方法: SearchTags
   */
  SearchTagsResponse searchTags(SearchTagsRequest request);

  /**
   * 标签详情
   * 对应Thrift方法: GetTagDetail
   */
  GetTagDetailResponse getTagDetail(GetTagDetailRequest request);

  /**
   * 获取标签限制
   * 对应Thrift方法: GetTagSpec
   */
  GetTagSpecResponse getTagSpec(GetTagSpecRequest request);

  /**
   * 批量获取标签
   * 对应Thrift方法: BatchGetTags
   */
  BatchGetTagsResponse batchGetTags(BatchGetTagsRequest request);
}
