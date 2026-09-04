package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceVersionResult;
import java.util.ArrayList;
import java.util.List;

/**
 * 源评估目标操作服务抽象基类
 * 提供公共方法实现
 */
public abstract class AbstractSourceEvalTargetServiceImpl implements com.iwhalecloud.bote.loop.evaluation.domain.service.ISourceEvalTargetOperateService {

  /**
   * 构建分页结果
   */
  protected <T> ListSourceResult buildListSourceResult(List<EvalTarget> targets, PageInfo<T> pageInfo, int page) {
    String nextCursor = String.valueOf(page + 1);
    boolean hasMore = pageInfo.isHasNextPage();

    ListSourceResult result = new ListSourceResult();
    result.setEvalTargets(targets);
    result.setNextCursor(nextCursor);
    result.setHasMore(hasMore);
    result.setTotal(pageInfo.getTotal());
    return result;
  }

  /**
   * 从游标构建页码
   */
  protected int buildPageByCursor(String cursor) {
    if (cursor == null || cursor.isEmpty()) {
      return 1;
    }
    try {
      return Integer.parseInt(cursor);
    }
    catch (NumberFormatException e) {
      return 1;
    }
  }

  @Override
  public ListSourceVersionResult listSourceVersion(ListSourceVersionParam param) {
    // 默认实现：返回空结果
    ListSourceVersionResult result = new ListSourceVersionResult();
    result.setVersions(new ArrayList<>());
    result.setNextCursor("");
    result.setHasMore(false);
    return result;
  }

  @Override
  public void packSourceInfo(Long spaceId, List<EvalTarget> targets) {
    // 默认实现：空操作
  }

  @Override
  public void packSourceVersionInfo(Long spaceId, List<EvalTarget> targets) {
    // 默认实现：空操作
  }

  @Override
  public List<EvalTarget> batchGetSource(Long spaceId, List<String> ids) {
    // 默认实现：返回空列表
    return new ArrayList<>();
  }
}

