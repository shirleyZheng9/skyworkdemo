package ${package.mapper};

import com.github.pagehelper.Page;
import ${package.dto}.${dtoName};
import ${package.query}.${queryParamsName};
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * ${entityDesc}管理
 *
 * @author ${author}
 * @since ${currentTime}
 */
public interface ${mapperName} {
  /**
   * 校验${entityDesc}的编码唯一性
   *
   * @param ${entityCode?uncap_first} ${entityDesc}
   * @return 结果
   */
  boolean exists${entityCode}Code(@Param("dto") ${dtoName} ${entityCode?uncap_first});

  /**
   * 根据主键获取${entityDesc}
   *
   * @param ${table.primaryKey.paramName} ${entityDesc}主键
   * @return ${entityDesc}
   */
  ${dtoName} get${entityCode}(@Param("id") ${table.primaryKey.paramType} ${table.primaryKey.paramName});

  /**
   * 新增${entityDesc}
   *
   * @param ${entityCode?uncap_first} ${entityDesc}
   * @return 结果
   */
  int insert${entityCode}(@Param("dto") ${dtoName} ${entityCode?uncap_first});

  /**
   * 批量新增${entityDesc}
   *
   * @param ${entityCode?uncap_first}s ${entityDesc}列表
   * @return 结果
   */
  int batchInsert${entityCode}(@Param("list") List<${dtoName}> ${entityCode?uncap_first}s);

  /**
   * 修改${entityDesc}
   *
   * @param ${entityCode?uncap_first} ${entityDesc}
   * @return 结果
   */
  int update${entityCode}(@Param("dto") ${dtoName} ${entityCode?uncap_first});

  /**
   * 删除属性
   *
   * @param ${table.primaryKey.paramName} 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int delete${entityCode}(@Param("${table.primaryKey.paramName}") Long ${table.primaryKey.paramName}, @Param("updatorId") Long updatorId);

  /**
   * 获取${entityDesc}列表
   *
   * @param queryParams 查询条件
   * @return ${entityDesc}列表
   */
  List<${dtoName}> select${entityCode}List(@Param("query") ${entityCode}QueryParams queryParams);

  /**
   * 获取${entityDesc}列表（分页）
   *
   * @param queryParams 查询条件
   * @return ${entityDesc}分页列表
   */
  Page<${dtoName}> select${entityCode}Page(@Param("query") ${entityCode}QueryParams queryParams, RowBounds rowBounds);
}
