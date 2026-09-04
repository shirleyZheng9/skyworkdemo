package com.iwhalecloud.bote.doc.module.control.base;

import com.iwhalecloud.bote.doc.module.control.base.request.ControlRequest;

/**
 * control request wrapper.
 */
public interface ControlRequestWrapper {

  /**
   * wrapper request.
   *
   * @param request ControlRequest
   */
  void doWrapper(ControlRequest request);
}
