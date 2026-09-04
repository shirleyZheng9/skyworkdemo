package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 实验错误控制配置
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "expt.err.ctrl")
public class ExptErrCtrl {

  private ErrRetryCtrl errRetryCtrl;
  private Map<Long, ErrRetryCtrl> spaceErrRetryCtrl = Maps.newHashMap();
  private List<ResultErrConvert> resultErrConverts = Lists.newArrayList();

  public ErrRetryCtrl getErrRetryCtrlBySpaceId(Long spaceId) {
    return spaceErrRetryCtrl.containsKey(spaceId) ? spaceErrRetryCtrl.get(spaceId) : errRetryCtrl;
  }

  public String convertErrMsg(String msg) {
    if (msg == null) {
      return "";
    }
    ResultErrConvert defaultConf = new ResultErrConvert();
    for (ResultErrConvert conf : resultErrConverts) {
      if (conf.getAsDefault()) {
        defaultConf = conf;
        continue;
      }
      ResultErrConvert.ConvertErrMsgResult convertErrMsgResult = conf.convertErrMsg(msg);
      if (convertErrMsgResult.isMatched()) {
        return convertErrMsgResult.getMsg();
      }
    }
    return defaultConf.convertErrMsg(msg).getMsg();
  }

}
