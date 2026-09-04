package com.iwhalecloud.bote.loop.data.application;

import com.iwhalecloud.bote.loop.client.data.domain.tag.TagInfoDTO;
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
import com.iwhalecloud.bote.loop.data.application.convertor.TagConvertor;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.GetTagDetailReq;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.GetTagDetailResp;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagKeyParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKey;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagSpec;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagTargetType;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagType;
import com.iwhalecloud.bote.loop.data.domain.tag.service.ITagService;
import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 标签管理服务实现类
 * 迁移对应关系: Go语言backend/modules/data/application/tag_app.go
 * - 功能: 标签管理相关的应用层服务
 * - 主要方法:
 * * createTag - 新增标签
 * * updateTag - 更新标签
 * * batchUpdateTagStatus - 批量更新标签状态
 * * searchTags - 搜索标签
 * * getTagDetail - 标签详情
 * * getTagSpec - 获取标签限制
 * * batchGetTags - 批量获取标签
 * <p>
 * Java实现说明:
 * - 对应Go的TagApplicationImpl结构体
 * - 使用Spring Service注解
 * - 依赖标签服务、仓库和鉴权组件
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go指针操作 -> Java对象操作
 */
@Service
@RequiredArgsConstructor
public class TagApplicationServiceImpl implements TagApplicationService {
  private static final Logger logger = LoggerFactory.getLogger(TagApplicationServiceImpl.class);

  private final ITagService tagService;

  @Override
  public CreateTagResponse createTag(CreateTagRequest request) {
    // 鉴权 - 对应Go代码第46-53行
    // TODO: 实现鉴权逻辑

    // 构建标签键实体 - 对应Go代码第55-69行
    TagKey tagKey = new TagKey();
    tagKey.setTagKeyName(request.getTagKeyName());
    tagKey.setDescription(request.getDescription());
    tagKey.setStatus(TagStatus.ACTIVE);
    tagKey.setTagType(TagType.TAG);
    tagKey.setTagContentType(TagConvertor.convertTagContentTypeDTOtoDO(request.getTagContentType()));
    tagKey.setTagTargetType(request.getTagDomainTypes().stream()
      .map(o -> TagTargetType.fromValue(o.getValue()))
      .collect(Collectors.toList()));
    tagKey.setTagValues(request.getTagValues().stream()
      .map(TagConvertor::convertTagValueDTOtoDO)
      .peek(tagValue -> tagValue.setStatus(TagStatus.ACTIVE))
      .collect(Collectors.toList()));
    tagKey.setContentSpec(TagConvertor.convertTagContentSpecDTOtoDO(request.getTagContentSpec()));
    tagKey.setVersion(request.getVersion());

    // 创建标签 - 对应Go代码第70-73行
    Long tagKeyId = tagService.createTag(request.getWorkspaceId(), tagKey);

    // 构建响应 - 对应Go代码第74-75行
    CreateTagResponse response = new CreateTagResponse();
    response.setTagKeyId(tagKeyId);

    return response;
  }

  @Override
  public UpdateTagResponse updateTag(UpdateTagRequest request) {
    // 鉴权 - 对应Go代码第82-89行
    // TODO: 实现鉴权逻辑

    // 构建标签键实体 - 对应Go代码第91-111行
    TagKey tagKey = new TagKey();
    tagKey.setTagKeyName(request.getTagKeyName());
    tagKey.setTagKeyId(request.getTagKeyId());
    tagKey.setDescription(request.getDescription());
    tagKey.setStatus(TagStatus.ACTIVE);
    tagKey.setTagType(TagType.TAG);
    tagKey.setTagContentType(TagConvertor.convertTagContentTypeDTOtoDO(request.getTagContentType()));
    tagKey.setTagTargetType(request.getTagDomainTypes().stream()
      .map(o -> TagTargetType.fromValue(o.getValue()))
      .collect(Collectors.toList()));
    tagKey.setTagValues(request.getTagValues().stream()
      .map(TagConvertor::convertTagValueDTOtoDO)
      .peek(tagValue -> {
        if (tagValue.getStatus() == TagStatus.UNDEFINED) {
          tagValue.setStatus(TagStatus.ACTIVE);
        }
      })
      .collect(Collectors.toList()));
    tagKey.setContentSpec(TagConvertor.convertTagContentSpecDTOtoDO(request.getTagContentSpec()));
    tagKey.setVersion(request.getVersion());

    // 更新标签 - 对应Go代码第112-115行
    tagService.updateTag(request.getWorkspaceId(), request.getTagKeyId(), tagKey);

    return new UpdateTagResponse();
  }

  @Override
  public BatchUpdateTagStatusResponse batchUpdateTagStatus(BatchUpdateTagStatusRequest request) {
    // 鉴权 - 对应Go代码第123-130行
    // TODO: 实现鉴权逻辑

    // 转换状态 - 对应Go代码第132行
    TagStatus toStatus = TagStatus.fromValue(request.getToStatus().getValue());

    // 批量更新状态 - 对应Go代码第133-136行
    Map<Long, String> errInfo = tagService.batchUpdateTagStatus(
      request.getWorkspaceId(),
      request.getTagKeyIds(),
      toStatus
    );

    // 构建响应 - 对应Go代码第137-138行
    BatchUpdateTagStatusResponse response = new BatchUpdateTagStatusResponse();
    response.setErrInfo(errInfo);

    return response;
  }

  @Override
  public SearchTagsResponse searchTags(SearchTagsRequest request) {
    // 参数验证 - 对应Go代码第144-146行
    if (request.getTagKeyName() != null && !request.getTagKeyName().isEmpty() &&
      request.getTagKeyNameLike() != null && !request.getTagKeyNameLike().isEmpty()) {
      throw new BssException("tag_key_name and tag_key_name_like can not be set at the same time");
    }

    // 鉴权 - 对应Go代码第148-156行
    // TODO: 实现鉴权逻辑

    // 构建查询参数 - 对应Go代码第158-175行
    List<TagStatus> status = Arrays.asList(TagStatus.ACTIVE, TagStatus.INACTIVE);

    Paginator paginator = new Paginator();
    paginator.setLimit(request.getPageSize());
    paginator.setOffset(request.getPageNumber());
    paginator.setIdColumn(request.getOrderBy().getField());
    paginator.setAsc(request.getOrderBy().getIsAsc());

    MGetTagKeyParam param = new MGetTagKeyParam();
    param.setPaginator(paginator);
    param.setSpaceId(request.getWorkspaceId());
    param.setTagType(TagType.TAG);
    param.setStatus(status);
    param.setCreatedBys(request.getCreatedBys());
    param.setTagKeyNameLike(request.getTagKeyNameLike());
    param.setTagKeyName(request.getTagKeyName());
    param.setTagDomainTypes(request.getDomainTypes().stream()
      .map(TagConvertor::convertTagContentTypeDTOtoDO)
      .collect(Collectors.toList()));
    param.setTagContentTypes(request.getContentTypes().stream()
      .map(TagConvertor::convertTagContentTypeDTOtoDO)
      .collect(Collectors.toList()));

    // 搜索标签 - 对应Go代码第177-181行
    List<TagKey> tagKeys = tagService.searchTags(request.getWorkspaceId(), param);
    if (tagKeys == null) {
      logger.warn("[SearchTagsHandler] get tag keys failed, param: {}", param);
      throw new BssException("search tags failed");
    }

    // 转换为DTO - 对应Go代码第182行
    List<TagInfoDTO> dtos = tagKeys.stream()
      .map(TagConvertor::convertTagKeyDOtoDTO)
      .collect(Collectors.toList());


    // 构建响应 - 对应Go代码第184-187行
    SearchTagsResponse response = new SearchTagsResponse();
    response.setTagInfos(dtos);

    return response;
  }

  @Override
  public GetTagDetailResponse getTagDetail(GetTagDetailRequest request) {
    // 鉴权 - 对应Go代码第194-201行
    // TODO: 实现鉴权逻辑

    // 构建查询参数 - 对应Go代码第203-210行
    GetTagDetailReq req = new GetTagDetailReq();
    req.setPageSize(request.getPageSize());
    req.setPageNum(request.getPageNumber());
    req.setPageToken(request.getPageToken());
    req.setTagKeyId(request.getTagKeyId());
    req.setOrderBy(request.getOrderBy().getField());
    req.setIsAsc(request.getOrderBy().getIsAsc());

    // 获取标签详情 - 对应Go代码第211-213行
    GetTagDetailResp detail = tagService.getTagDetail(request.getWorkspaceId(), req);
    if (detail == null) {
      throw new BssException("get tag detail failed");
    }

    // 转换为DTO - 对应Go代码第214行
    List<TagInfoDTO> dtos = detail.getTagKeys().stream()
      .map(TagConvertor::convertTagKeyDOtoDTO)
      .collect(Collectors.toList());


    // 构建响应 - 对应Go代码第216-219行
    GetTagDetailResponse response = new GetTagDetailResponse();
    response.setTags(dtos);
    response.setTotal(detail.getTotal());
    response.setNextPageToken(detail.getNextPageToken());

    return response;
  }

  @Override
  public GetTagSpecResponse getTagSpec(GetTagSpecRequest request) {
    // 鉴权 - 对应Go代码第225-232行
    // TODO: 实现鉴权逻辑

    // 获取标签规格 - 对应Go代码第234-237行
    TagSpec result = tagService.getTagSpec(request.getWorkspaceId());
    if (result == null) {
      throw new BssException("get tag spec failed");
    }

    // 构建响应 - 对应Go代码第238-241行
    GetTagSpecResponse response = new GetTagSpecResponse();
    response.setMaxHeight((long) result.getMaxHeight());
    response.setMaxWidth((long) result.getMaxWidth());

    return response;
  }

  @Override
  public BatchGetTagsResponse batchGetTags(BatchGetTagsRequest request) {
    // 鉴权 - 对应Go代码第247-254行
    // TODO: 实现鉴权逻辑

    // 批量获取标签 - 对应Go代码第255-258行
    List<TagKey> tagKeys = tagService.batchGetTagsByTagKeyIds(
      request.getWorkspaceId(),
      request.getTagKeyIds()
    );
    if (tagKeys == null) {
      throw new BssException("batch get tags failed");
    }

    // 转换为DTO - 对应Go代码第259行
    List<TagInfoDTO> dtos = tagKeys.stream()
      .map(TagConvertor::convertTagKeyDOtoDTO)
      .collect(Collectors.toList());


    // 构建响应 - 对应Go代码第261-262行
    BatchGetTagsResponse response = new BatchGetTagsResponse();
    response.setTagInfoList(dtos);

    return response;
  }
}
