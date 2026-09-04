package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.base.query.AttrSpecQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 属性管理 controller
 *
 * @author auto
 * @since 2024-09-13
 */
@RestController
@RequestMapping(path = CommonConsts.API_PREFIX + "manager/attr", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：属性管理")
public class AttrManageController {

  private final AttrSpecCache attrSpecCache;

  @PostMapping("batchGetStaticAttr")
  @Operation(summary = "批量查询静态数据")
  public ResultVO<Map<String, List<SimpleAttrDTO>>> batchGetRuleStaticAttr(@RequestBody AttrSpecQueryParams params) {
    if (CollectionUtils.isEmpty(params.getAttrCodes())) {
      return ResultVO.success(Collections.emptyMap());
    }
    // 是否只查询平台级静态数据
    if (Boolean.TRUE.equals(params.getSystem())) {
      params.setTenantId(CommonConsts.PLATFORM_TENANT_ID);
    }

    // 并发查询
    Map<String, List<SimpleAttrDTO>> result = params.getAttrCodes().stream()
      .distinct()
      .parallel()
      .collect(Collectors.toMap(Function.identity(), attrCode -> ListUtils.emptyIfNull(attrSpecCache.get(params.getTenantId(), attrCode))));
    return ResultVO.success(result);
  }
}
