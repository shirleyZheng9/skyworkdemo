package ${package.entity};

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
<#if table.containDateColumn>
import java.util.Date;
</#if>
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ${entityDesc} Entity
 *
 * @author ${author}
 * @since ${currentTime}
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "${table.name?upper_case}")
public class ${entityName} extends BaseEntity {
<#if table.primaryKey??>

  @DiffId
  @Schema(description = "主键")
  private ${table.primaryKey.paramType} ${table.primaryKey.paramName};
</#if>
<#list table.fieldsWithoutPrimaryKey as field>
<#if field.isBaseKey == false>
  @DiffField(name = "${field.columnName?upper_case}")
  @Schema(description = "${field.comment!''}")
  private ${field.paramType} ${field.paramName};
</#if>
</#list>
}
