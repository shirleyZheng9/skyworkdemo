package com.iwhalecloud.bote.doc.module.control.base.exception;

import com.iwhalecloud.bote.doc.module.control.base.ControlType;
import java.io.Serial;
import lombok.Getter;

/**
 * unknown control type exception.
 */
@Getter
public class UnknownControlTypeException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final ControlType controlType;

  public UnknownControlTypeException(ControlType controlType) {
    super("Unknown Control Type");
    this.controlType = controlType;
  }
}
