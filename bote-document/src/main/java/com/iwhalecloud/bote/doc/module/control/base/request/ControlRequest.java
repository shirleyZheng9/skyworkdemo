package com.iwhalecloud.bote.doc.module.control.base.request;

import com.iwhalecloud.bote.doc.module.control.base.ControlRoleDict;

/**
 * control request api.
 */
public interface ControlRequest extends ControlAttribute {

  /**
   * execute control request.
   *
   * @return control role dict
   */
  ControlRoleDict execute();
}
