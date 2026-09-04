package ${package.controller};

import com.github.pagehelper.PageInfo;
import ${package.dto}.${dtoName};
import ${package.query}.${queryParamsName};
import ${package.service}.${serviceName};
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ${entityDesc}管理 controller
 *
 * @author ${author}
 * @since ${currentTime}
 */
@RestController
@RequestMapping(value = "lcdp/manager/${entityCode?uncap_first}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "${entityDesc}管理")
public class ${controllerName} {

  private final ${serviceName} ${serviceName?substring(1)?uncap_first};

  @Operation(summary = "查询单个${entityDesc}")
  @GetMapping("find${entityCode}")
  public ResultVO<${dtoName}> find${entityCode}(@RequestParam(name = "${table.primaryKey.paramName}") ${table.primaryKey.paramType} ${table.primaryKey.paramName}) {
    Assert.notNull(${table.primaryKey.paramName}, "主键 ID 不能为空");
    return ResultVO.success(${serviceName?substring(1)?uncap_first}.find${entityCode}ById(${table.primaryKey.paramName}));
  }

  @Operation(summary = "保存${entityDesc}")
  @PostMapping("save${entityCode}")
  public ResultVO<Void> save${entityCode}(@RequestBody ${dtoName} ${entityCode}) {
    return ${serviceName?substring(1)?uncap_first}.save${entityCode}(${entityCode});
  }

  @Operation(summary = "删除${entityDesc}")
  @GetMapping("delete${entityCode}")
  public ResultVO<Void> delete${entityCode}(@RequestParam(name = "${table.primaryKey.paramName}") ${table.primaryKey.paramType} ${table.primaryKey.paramName}) {
    Assert.notNull(${table.primaryKey.paramName}, "主键 ID 不能为空");
    return ${serviceName?substring(1)?uncap_first}.delete${entityName}(${table.primaryKey.paramName});
  }

  @Operation(summary = "查询${entityDesc}列表")
  @PostMapping("query${entityCode}List")
  public ResultVO<List<${dtoName}>> query${entityCode}List(@RequestBody ${queryParamsName} queryParams) {
    return ResultVO.success(${serviceName?substring(1)?uncap_first}.query${entityCode}List(queryParams));
  }

  @Operation(summary = "分页查询${entityDesc}")
  @PostMapping("query${entityCode}Page")
  public ResultVO<PageInfo<${dtoName}>> query${entityCode}Page(@RequestBody ${queryParamsName} queryParams) {
    return ResultVO.success(${serviceName?substring(1)?uncap_first}.query${entityCode}Page(queryParams));
  }
}
