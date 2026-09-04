package com.iwhalecloud.bote.doc.module.control.base.request;

import com.iwhalecloud.bote.doc.module.control.base.ControlType;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder.ControlSubject;
import java.util.List;

/**
 * node control request factory.
 */
public class NodeControlRequestFactory implements ControlRequestFactory {

  @Override
  public ControlType getControlType() {
    return ControlType.NODE;
  }

  @Override
  public ControlRequest create(List<ControlSubject> subjects, List<String> controlIds) {
    return new NodeControlRequest(subjects, controlIds);
  }
}
