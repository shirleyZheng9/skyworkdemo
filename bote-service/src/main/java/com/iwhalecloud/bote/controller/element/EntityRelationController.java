package com.iwhalecloud.bote.controller.element;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.query.EntityPageQueryParams;
import com.iwhalecloud.bote.dto.base.query.EntityRelationQueryParams;
import com.iwhalecloud.bote.dto.base.EntityRelationResultDTO;
import com.iwhalecloud.bote.dto.base.EntityInfoDTO;
import com.iwhalecloud.bote.service.element.IEntityRelationService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 元素资源 controller
 *
 * @author qian.sisheng
 * @since 2025-12-04
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/entity/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "元素关系查询服务")
public class EntityRelationController {
  private final IEntityRelationService entityRelationService;

  @PostMapping("queryEntityRelationTree")
  @Operation(summary = "查询元素关系树")
  public ResultVO<EntityRelationResultDTO> queryEntityRelationTree(@RequestBody EntityRelationQueryParams queryParam) {
    return ResultVO.success(entityRelationService.queryEntityRelationTree(queryParam));
  }

  @PostMapping("queryEntityDetail")
  @Operation(summary = "查询实体信息详情")
  public ResultVO<List<EntityInfoDTO>> queryEntityDetail(@RequestBody EntityRelationQueryParams queryParam) {
    return ResultVO.success(entityRelationService.queryEntityDetail(queryParam));
  }

  @PostMapping("queryEntityPage")
  @Operation(summary = "查询实体分页")
  public ResultVO<PageInfo<?>> queryEntityPage(@RequestBody EntityPageQueryParams queryParam) {
    return ResultVO.success(entityRelationService.queryEntityPage(queryParam));
  }
}
