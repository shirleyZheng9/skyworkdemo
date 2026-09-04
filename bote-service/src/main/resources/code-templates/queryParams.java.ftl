package ${package.query};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ${entityDesc}查询参数
 *
 * @author ${author}
 * @since ${currentTime}
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "${entityDesc}查询参数")
public class ${queryParamsName} extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
}
