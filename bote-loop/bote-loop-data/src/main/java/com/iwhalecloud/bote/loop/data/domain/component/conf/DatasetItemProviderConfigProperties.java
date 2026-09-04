package com.iwhalecloud.bote.loop.data.domain.component.conf;

import com.iwhalecloud.bote.loop.data.domain.entity.Provider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatasetItemProviderConfigProperties {

  private Provider provider;
  private Long maxSize;

}
