package com.iwhalecloud.bote.doc.module.control.base;

import java.io.Serializable;

/**
 * Control Request Executor option.
 */
public class ControlRequestOption implements Serializable {

  private static final long serialVersionUID = -2590606512902541554L;

  protected boolean nodeTreeRoleBuilding;

  protected boolean shareNodeTreeRoleBuilding;

  protected boolean rubbishRoleBuilding;

  protected boolean internalBuilding;

  public static ControlRequestOption create() {
    return new ControlRequestOption();
  }

  public ControlRequestOption setNodeTreeRoleBuilding(boolean nodeTreeRoleBuilding) {
    this.nodeTreeRoleBuilding = nodeTreeRoleBuilding;
    return this;
  }

  public ControlRequestOption setShareNodeTreeRoleBuilding(boolean shareNodeTreeRoleBuilding) {
    this.shareNodeTreeRoleBuilding = shareNodeTreeRoleBuilding;
    return this;
  }

  public ControlRequestOption setRubbishRoleBuilding(boolean rubbishRoleBuilding) {
    this.rubbishRoleBuilding = rubbishRoleBuilding;
    return this;
  }

  public ControlRequestOption setInternalBuilding(boolean internalBuilding) {
    this.internalBuilding = internalBuilding;
    return this;
  }
}
