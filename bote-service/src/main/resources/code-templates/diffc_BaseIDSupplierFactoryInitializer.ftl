package ${package.diffcfactory};

<#list tableParams as tableParam>
import ${tableParam.package.dto}.${tableParam.dtoName};
</#list>
import com.iwhalecloud.bote.common.enums.Sequences;
import com.cmsr.tiangong.diffc.factory.IDSupplierFactory;
import org.springframework.stereotype.Component;

/**
 * 主键提供者工厂初始化
 *
 * @author ${author}
 * @since ${currentTime}
 */
@Component
public class BaseIDSupplierFactoryInitializer {

  public IDSupplierFactoryInitializer() {
  <#list tableParams as tableParam>
    // ${tableParam.entityName}
    IDSupplierFactory.register(DemoAttrSpecDTO.class, Sequences.${tableParam.sequenceName}::next);
  </#list>
  }

}
