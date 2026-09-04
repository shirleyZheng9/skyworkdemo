package ${package.service};

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import ${package.dto}.${dtoName};
import ${package.query}.${queryParamsName};
import java.util.List;

/**
 * ${entityDesc}管理服务
 *
 * @author ${author}
 * @since ${currentTime}
 */
public interface ${serviceName} {

  /**
   * 查询单个${entityDesc}
   *
   * @param ${table.primaryKey.paramName} ${entityDesc}主键
   * @return ${entityDesc}
   */
  ${dtoName} find${entityCode}(${table.primaryKey.paramType} ${table.primaryKey.paramName});

  /**
   * 保存${entityDesc}
   *
   * @param ${entityCode?uncap_first} ${entityDesc}
   * @return 结果
   */
  ResultVO<${dtoName}> save${entityCode}(${dtoName} ${entityCode?uncap_first});

  /**
   * 删除${entityDesc}
   *
   * @param ${table.primaryKey.paramName} ${entityDesc}主键
   * @return 结果
   */
  ResultVO<Void> delete${entityCode}(${table.primaryKey.paramType} ${table.primaryKey.paramName});

  /**
   * 查询${entityDesc}列表
   *
   * @param queryParams 查询条件
   * @return ${entityDesc}列表
   */
  List<${dtoName}> query${entityCode}List(${queryParamsName} queryParams);

  /**
   * 查询${entityDesc}列表（分页）
   *
   * @param queryParams 查询条件
   * @return ${entityDesc}分页列表
   */
  PageInfo<${dtoName}> query${entityCode}Page(${queryParamsName} queryParams);

}
