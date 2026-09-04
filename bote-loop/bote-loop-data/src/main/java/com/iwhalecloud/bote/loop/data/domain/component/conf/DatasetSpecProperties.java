package com.iwhalecloud.bote.loop.data.domain.component.conf;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetCategory;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSpec;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("default.dataset.spec")
public class DatasetSpecProperties {

  private DatasetSpec spec;
  private Map<DatasetCategory, DatasetSpec> specsByCategory;

}
