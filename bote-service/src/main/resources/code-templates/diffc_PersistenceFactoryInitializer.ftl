package ${package.diffcfactory};

<#list tableParams as tableParam>
import ${tableParam.package.diffcpersistimpl}.${tableParam.diffcPersistName};
</#list>
<#list tableParams as tableParam>
import ${tableParam.package.dto}.${tableParam.dtoName};
</#list>
import com.cmsr.tiangong.diffc.factory.BatchPersistenceFactory;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务工厂初始化
 *
 * @author ${author}
 * @since ${currentTime}
 */
@Component
public class PersistenceFactoryInitializer {

  public BatchPersistenceFactoryInitializer() {
  <#list tableParams as tableParam>
    // ${tableParam.entityName}
    BatchPersistenceFactory.register(${tableParam.dtoName}.class, ${tableParam.diffcPersistName}.class);
  </#list>
  }

}
