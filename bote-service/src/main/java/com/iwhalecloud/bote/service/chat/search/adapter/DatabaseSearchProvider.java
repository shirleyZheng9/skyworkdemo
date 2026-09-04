package com.iwhalecloud.bote.service.chat.search.adapter;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.SearchDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.query.SearchDocumentQueryParams;
import com.iwhalecloud.bote.doc.module.person.service.IHomepageService;
import com.iwhalecloud.bote.dto.app.SimpleWebAppDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.chat.SearchResultDTO;
import com.iwhalecloud.bote.dto.chat.query.SearchQueryParams;
import com.iwhalecloud.bote.mapper.app.WorkbenchAppMapper;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.service.chat.search.IChatSearchProvider;
import com.iwhalecloud.bote.service.organization.IOrganizationMemberService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 通用搜索数据库模式实现
 *
 * @author chen.linfa
 * @since 2025-10-14
 */
@Component
@RequiredArgsConstructor
public class DatabaseSearchProvider implements IChatSearchProvider {

  private final IOrganizationMemberService orgMemberService;
  private final WorkbenchAppMapper workbenchAppMapper;
  private final IHomepageService homepageService;
  private final BotQueryMapper botQueryMapper;

  @Override
  public ResultVO<SearchResultDTO> search(SearchQueryParams params) {
    // 补充当前登录用户信息（工号、组织）
    params.setUserId(SessionUtil.getLoginInfo().getUserId());
    params.setOrgIds(orgMemberService.queryUserOrgAndParentOrgIds(params.getSpaceId(), params.getUserId()));

    SearchResultDTO result = new SearchResultDTO();
    switch (params.getType()) {
      case ChatConsts.AI_SEARCH_TYPE_ALL -> {
        result.setBots(queryBot(params));
        result.setWebs(queryWeb(params));
        result.setDocuments(queryDocument(params.getSpaceId(), params.getSearchContent()));
      }
      case ChatConsts.AI_SEARCH_TYPE_BOT -> result.setBots(queryBot(params));
      case ChatConsts.AI_SEARCH_TYPE_WEB -> result.setWebs(queryWeb(params));
      case ChatConsts.AI_SEARCH_TYPE_DOC -> result.setDocuments(queryDocument(params.getSpaceId(), params.getSearchContent()));
      default -> throw new BssException("无效搜索类型" + params.getType());
    }
    return ResultVO.success(result);
  }

  /**
   * 查询授权的 AI 助理以及自定义 BoteClaw
   */
  private List<SimpleBotDTO> queryBot(SearchQueryParams params) {
    List<SimpleBotDTO> boteList = new ArrayList<>();
    boteList.addAll(botQueryMapper.selectBoteClawList(params));
    boteList.addAll(workbenchAppMapper.selectAuthBot(params));
    return boteList;
  }

  /**
   * 查询授权的网页应用
   */
  private List<SimpleWebAppDTO> queryWeb(SearchQueryParams params) {
    return workbenchAppMapper.selectAuthWeb(params);
  }

  /**
   * 查询授权的文档
   */
  private List<SearchDocumentDTO> queryDocument(Long spaceId, String searchContent) {
    SearchDocumentQueryParams queryParams = new SearchDocumentQueryParams();
    queryParams.setKeyword(searchContent);
    queryParams.setPageNum(1);
    queryParams.setPageSize(20);
    queryParams.setSpaceId(spaceId);
    PageInfo<SearchDocumentDTO> pages = homepageService.searchDocuments(queryParams);
    return pages.getList();
  }
}
