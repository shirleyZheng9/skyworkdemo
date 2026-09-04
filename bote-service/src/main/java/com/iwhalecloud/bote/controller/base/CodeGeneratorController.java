package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.query.TableParams;
import com.iwhalecloud.bote.service.base.ICodeGeneratorService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代码生成服务
 *
 * @author chen.linfa
 * @since 2024-09-12
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/codeGenerate", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：代码生成服务")
public class CodeGeneratorController {

  private final Logger logger = LoggerFactory.getLogger(CodeGeneratorController.class);

  private final ICodeGeneratorService codeGeneratorService;

  @PostMapping("execute")
  @Schema(description = "通过探测数据库表结构，生成代码")
  public ResponseEntity<?> generateByDatabase(@RequestBody List<TableParams> params) {
    Assert.notEmpty(params, "条件不能为空");
    for (TableParams param : params) {
      Assert.isTrue(!StringUtils.isAnyEmpty(param.getTableCode(), param.getEntityDesc(), param.getEntityCode()), "条件不能为空");
    }
    ResultVO<byte[]> result = codeGeneratorService.execute(params);
    try {
      if (!result.isSuccess()) {
        return ResponseEntity.ok().contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)).body(result);
      }
      String contentDisposition = ContentDisposition.attachment().filename("代码生成器", StandardCharsets.UTF_8).build().toString();
      MediaType contentType = MediaType.parseMediaType("application/zip");
      return ResponseEntity.ok().contentType(contentType).header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition).body(result.getResultObject());
    }
    catch (Exception e) {
      logger.error("Failed to code generate.", e);
      return ResponseEntity.badRequest().contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)).body("代码生成异常, " + e.getMessage());
    }
  }
}
