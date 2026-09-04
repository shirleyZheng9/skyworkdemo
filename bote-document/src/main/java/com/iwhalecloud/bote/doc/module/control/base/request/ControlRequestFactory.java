package com.iwhalecloud.bote.doc.module.control.base.request;

import com.iwhalecloud.bote.doc.module.control.base.ControlType;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder.ControlSubject;
import java.util.List;

/**
 * control request factory.
 */
public interface ControlRequestFactory {

  /**
   * control type.
   *
   * @return control type.
   */
  ControlType getControlType();

  /**
   * create control request.
   *
   * @param subjects 授权主体.
   * @param controlIds 被管控字段ID
   * @return control request.
   */
  ControlRequest create(List<ControlSubject> subjects, List<String> controlIds);
}
