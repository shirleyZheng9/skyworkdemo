package com.iwhalecloud.bote.loop.data.domain.component.conf;

import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagSpec;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("default.tag.spec")
public class TagSpecConfProperties {

  private TagSpec defaultSpec;
  private Map<Long, TagSpec> specsBySpace;

}
