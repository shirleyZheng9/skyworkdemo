package ${package.dto};

import ${package.entity}.${entityName};
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ${entityDesc} DTO
 *
 * @author ${author}
 * @since ${currentTime}
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ${dtoName} extends ${entityName} {
}
