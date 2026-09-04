package ${package.serviceimpl};

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.SessionUtil;
import ${package.dto}.${dtoName};
import ${package.query}.${queryParamsName};
import ${package.mapper}.${mapperName};
import ${package.service}.${serviceName};
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ${entityDesc}管理服务实现
 *
 * @author ${author}
 * @since ${currentTime}
 */
@Service
@RequiredArgsConstructor
public class ${serviceImplName} implements ${serviceName} {

  private final ${mapperName} ${mapperName?uncap_first};

  @Override
  public ${dtoName} find${entityCode}(${table.primaryKey.paramType} ${table.primaryKey.paramName}) {
    return ${mapperName?uncap_first}.find${entityCode}(${table.primaryKey.paramName});
  }

  @Override
  @Transactional
  public ResultVO<${dtoName}> save${entityCode}(${dtoName} ${entityCode?uncap_first}) {
    // 校验编码唯一性
    if (${mapperName?uncap_first}.exists${entityCode}Code(${entityCode?uncap_first})) {
      return BaseErrorConstant.CHECK_ATTR_NBR.toResult();
    }
    ${dtoName} old = ${entityCode?uncap_first}.get${table.primaryKey.paramName?cap_first}() ==  null ? null : find${entityCode}(${entityCode?uncap_first}.get${table.primaryKey.paramName?cap_first}());
    DataDifference<${dtoName}> difference = DataDifferenceStarter.computeSave(old, ${entityCode?uncap_first}, false, false);
    if (difference == null) {
    return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> delete${entityCode}(${table.primaryKey.paramType} ${table.primaryKey.paramName}) {
    ${mapperName?uncap_first}.delete${entityCode}(${table.primaryKey.paramName}, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public List<${dtoName}> query${entityCode}List(${queryParamsName} queryParams) {
    return ${mapperName?uncap_first}.select${entityCode}List(queryParams);
  }

  @Override
  public PageInfo<${dtoName}> query${entityCode}Page(${queryParamsName} queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    return ${mapperName?uncap_first}.select${entityCode}Page(queryParams, rowBounds).toPageInfo();
  }
}
