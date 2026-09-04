package com.iwhalecloud.bote.doc.module.knowledge.strategy;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.beyond.BeyondAuthHelper;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.config.properties.BeyondProperties;
import com.iwhalecloud.bote.dto.beyond.BeyondKnowledgeListResponse;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeAccessDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeCatalogDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeFileDTO;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeDocumentParams;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeFileQueryParams;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 百应知识库接入策略实现
 * @author 赵旭
 * @since 2025/07/22
 */
@Service
@ConditionalOnBooleanProperty("beyond.enabled")
@RequiredArgsConstructor
public class BeyondKnowledgeStrategy implements IKnowledgeAccessStrategy {
  private static final Logger logger = LoggerFactory.getLogger(BeyondKnowledgeStrategy.class);

  private final BeyondProperties properties;
  private final BeyondAuthHelper beyondAuthHelper;

  @Override
  public String getKnowledgeAccessType() {
    return KnowledgeConsts.KNOWLEDGE_TYPE_BEYOND;
  }

  @Override
  public ResultVO<List<KnowledgeCatalogDTO>> queryKnowledgeCatalog() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResultVO<PageInfo<KnowledgeAccessDTO>> queryKnowledgeInfoPage(KnowledgeQueryParams queryParams) {
    try {
      // 组装百应知识库列表查询请求参数
      Map<String, Object> requestData = new LinkedHashMap<>();
      requestData.put("pageIndex", queryParams.getPageNum());
      requestData.put("pageSize", queryParams.getPageSize());
      requestData.put("keyword", queryParams.getKnowledgeName() != null ? queryParams.getKnowledgeName() : "");
      // 0-有权限的，1-我创建的
      requestData.put("ownershipType", 0);
      requestData.put("resourceBizTypes", new String[]{"DOC"});
      // 构建百应API请求头
      HttpHeaders headers = beyondAuthHelper.buildHttpHeaders();
      BeyondKnowledgeListResponse beyondKnowledgeList = HttpUtil.post(properties.getQueryKnowledgeListApiUrl(), requestData, ParameterizedTypeReference.forType(BeyondKnowledgeListResponse.class), headers);
      Assert.notNull(beyondKnowledgeList, "百应API调用失败");
      Integer code = beyondKnowledgeList.getCode();
      if (code == null || code != 0) {
        String msg = beyondKnowledgeList.getMsg();
        return ResultVO.fail("百应API调用失败: " + msg);
      }
      // 转换响应结果
      PageInfo<KnowledgeAccessDTO> result = convertToPageInfo(beyondKnowledgeList, queryParams.getPageNum(), queryParams.getPageSize());
      return ResultVO.success(result);
    }
    catch (Exception e) {
      logger.error("分页查询百应知识库失败", e);
      return ResultVO.fail("分页查询百应知识库失败: " + e.getMessage());
    }
  }

  @Override
  public ResultVO<PageInfo<KnowledgeFileDTO>> queryKnowledgeFilePage(KnowledgeFileQueryParams queryParams) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResultVO<KnowledgeDocumentDTO> getKnowledgeDocument(KnowledgeDocumentParams documentParams) {
    return null;
  }

  @Override
  public void downloadKnowledgeDoc(Long docId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    throw new UnsupportedOperationException();
  }

  /**
   * 将百应响应转换为PageInfo
   */
  private PageInfo<KnowledgeAccessDTO> convertToPageInfo(BeyondKnowledgeListResponse response, Integer pageNum, Integer pageSize) {
    PageInfo<KnowledgeAccessDTO> result = new PageInfo<>();
    result.setPageNum(pageNum);
    result.setPageSize(pageSize);
    BeyondKnowledgeListResponse.DataWrapper data = response.getData();
    if (data != null) {
      Integer totalPage = data.getTotalPage();
      Long total = data.getTotal();
      List<BeyondKnowledgeListResponse.KnowledgeResource> rows = data.getRows();
      result.setPages(totalPage != null ? totalPage : 0);
      result.setTotal(total);
      result.setSize(rows != null ? rows.size() : 0);
      result.setPrePage(pageNum == 1 ? 0 : pageNum - 1);
      result.setNextPage(totalPage != null && totalPage > pageNum ? pageNum + 1 : 0);
      // 转换知识库列表
      List<KnowledgeAccessDTO> knowledgeList = getKnowledgeAccessDTOS(rows);
      result.setList(knowledgeList);
    }
    else {
      result.setPages(0);
      result.setTotal(0L);
      result.setSize(0);
      result.setList(new ArrayList<>());
    }
    return result;
  }

  private static List<KnowledgeAccessDTO> getKnowledgeAccessDTOS(List<BeyondKnowledgeListResponse.KnowledgeResource> rows) {
    List<KnowledgeAccessDTO> knowledgeList = new ArrayList<>();
    if (rows != null) {
      for (BeyondKnowledgeListResponse.KnowledgeResource resource : rows) {
        KnowledgeAccessDTO access = new KnowledgeAccessDTO();
        access.setKnowledgeId(resource.getResourceSourcePkId());
        access.setKnowledgeName(resource.getResourceName());
        access.setKnowledgeType(KnowledgeConsts.KNOWLEDGE_TYPE_BEYOND);
        access.setKnowledgeDesc(resource.getResourceDesc());
        access.setKnowledgeIcon(resource.getAvatar());
        // 百应不提供文档数量信息
        access.setFileCounts(0);
        knowledgeList.add(access);
      }
    }
    return knowledgeList;
  }
}
