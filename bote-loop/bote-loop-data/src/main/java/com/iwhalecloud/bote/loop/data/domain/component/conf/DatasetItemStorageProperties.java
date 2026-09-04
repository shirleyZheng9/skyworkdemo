package com.iwhalecloud.bote.loop.data.domain.component.conf;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("dataset.item.storage")
public class DatasetItemStorageProperties {

  List<DatasetItemProviderConfigProperties> providers;

}
