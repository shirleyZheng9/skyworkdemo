package com.iwhalecloud.bote.doc.module.control.base;

import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import java.util.LinkedHashMap;

/**
 * Control role Dict.
 */
@SuppressWarnings("PMD.LooseCoupling")
public class ControlRoleDict extends LinkedHashMap<String, ControlRole> {

  static final float DEFAULT_LOAD_FACTOR = 0.75f;
  static final int DEFAULT_CAPACITY = 1 << 4;
  private static final long serialVersionUID = -7810769751186072489L;

  public ControlRoleDict() {
    this(DEFAULT_CAPACITY);
  }

  public ControlRoleDict(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  public ControlRoleDict(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public static ControlRoleDict create() {
    return new ControlRoleDict();
  }

  @Override
  public ControlRoleDict clone() {
    return (ControlRoleDict) super.clone();
  }
}
