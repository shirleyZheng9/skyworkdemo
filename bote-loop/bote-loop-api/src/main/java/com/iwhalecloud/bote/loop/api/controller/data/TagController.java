package com.iwhalecloud.bote.loop.api.controller.data;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.loop.client.data.tag.TagApplicationService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标签控制器
 * 对应Thrift: TagService
 */
@RestController
@RequestMapping("/api/data/v1")
@RequiredArgsConstructor
@IgnoreSession
@IgnoreSign
public class TagController {
  private final TagApplicationService tagService;

  /**
   * 获取标签规格
   */
  @GetMapping("/tag_spec")
  public GetTagSpecResponse getTagSpec(GetTagSpecRequest request) {
    return tagService.getTagSpec(request);
  }

  /**
   * 创建标签
   */
  @PostMapping("/tags")
  public CreateTagResponse createTag(@RequestBody CreateTagRequest request) {
    return tagService.createTag(request);
  }

  /**
   * 批量获取标签
   */
  @PostMapping("/tags/batch_get")
  public BatchGetTagsResponse batchGetTags(@RequestBody BatchGetTagsRequest request) {
    return tagService.batchGetTags(request);
  }

  /**
   * 批量更新标签状态
   */
  @PostMapping("/tags/batch_update_status")
  public BatchUpdateTagStatusResponse batchUpdateTagStatus(@RequestBody BatchUpdateTagStatusRequest request) {
    return tagService.batchUpdateTagStatus(request);
  }

  /**
   * 搜索标签
   */
  @PostMapping("/tags/search")
  public SearchTagsResponse searchTags(@RequestBody SearchTagsRequest request) {
    return tagService.searchTags(request);
  }

  /**
   * 更新标签
   */
  @PatchMapping("/tags/{tag_key_id}")
  public UpdateTagResponse updateTag(
    @PathVariable("tag_key_id") Long tagKeyId,
    @RequestBody UpdateTagRequest request) {
    request.setTagKeyId(tagKeyId);
    return tagService.updateTag(request);
  }

  /**
   * 获取标签详情
   */
  @PostMapping("/tags/{tag_key_id}/detail")
  public GetTagDetailResponse getTagDetail(
    @PathVariable("tag_key_id") Long tagKeyId,
    @RequestBody GetTagDetailRequest request) {
    request.setTagKeyId(tagKeyId);
    return tagService.getTagDetail(request);
  }
}
