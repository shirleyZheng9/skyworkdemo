package ${package.diffcpersistimpl};

import ${package.dto}.${dtoName};
import ${package.mapper}.${mapperName};
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：${entityDesc}
 *
 * @author ${author}
 * @since ${currentTime}
 */
@Component
public class ${diffcPersistName} extends BaseRootPersistence<${dtoName}> {

  public ${diffcPersistName}(${mapperName} ${mapperName?uncap_first}) {
    setAddConsumer(${mapperName?uncap_first}::insert${entityCode});
    setBatchAddConsumer(${mapperName?uncap_first}::batchInsert${entityCode});
    setModifyConsumer(${mapperName?uncap_first}::update${entityCode});
  }

}
