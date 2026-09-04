package com.iwhalecloud.bote.doc.module.control.base;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * control id builder.
 */
public final class ControlIdBuilder {

  public static final String SYMBOL = "-";

  private ControlIdBuilder() {
    throw new IllegalStateException();
  }

  public static ControlId nodeId(String nodeId, String libraryId) {
    return nodeIds(Collections.singletonList(nodeId), libraryId);
  }

  public static ControlId nodeIds(List<String> nodeIds, String libraryId) {
    return new NodeControlId(libraryId, nodeIds);
  }

  /**
   * control id.
   */
  public interface ControlId {

    String getLibraryId();

    List<String> getControlIds();

    ControlType getControlType();

    List<String> toRealIdList();
  }

  /**
   * control id abstract class.
   */
  private abstract static class AbstractControlId implements ControlId {

    private final List<String> controlIds;
    private final String libraryId;

    public AbstractControlId(String libraryId, List<String> controlIds) {
      this.libraryId = libraryId;
      this.controlIds = controlIds;
    }

    @Override
    public String getLibraryId() {
      return this.libraryId;
    }

    @Override
    public List<String> getControlIds() {
      return this.controlIds;
    }

    @Override
    public List<String> toRealIdList() {
      if (getControlType() == ControlType.NODE) {
        return getControlIds();
      }
      return getControlIds().stream().map(
          controlId -> controlId.substring(controlId.indexOf(SYMBOL) + 1))
        .collect(Collectors.toList());
    }

    @Override
    public String toString() {
      if (controlIds != null) {
        if (controlIds.size() == 1) {
          return controlIds.get(0);
        }
        return controlIds.stream().map(String::toString)
          .reduce("", (s, s2) -> s.concat(",").concat(s2));
      }
      return "";
    }
  }

  /**
   * node control id.
   */
  public static class NodeControlId extends AbstractControlId {

    public NodeControlId(String libraryId, List<String> controlIds) {
      super(libraryId, controlIds);
    }

    @Override
    public ControlType getControlType() {
      return ControlType.NODE;
    }
  }
}
