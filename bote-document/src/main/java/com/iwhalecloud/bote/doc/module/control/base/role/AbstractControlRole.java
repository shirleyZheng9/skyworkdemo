package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.module.control.base.permission.PermissionDefinition;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.MapUtils;

/**
 * base control role.
 */
abstract class AbstractControlRole implements ControlRole {

  private final boolean inherit;
  protected volatile Set<PermissionDefinition> permissions = new LinkedHashSet<>();

  public AbstractControlRole(boolean inherit) {
    this.inherit = inherit;
  }

  @Override
  public boolean isInherit() {
    return this.inherit;
  }

  @Override
  public Set<PermissionDefinition> getPermissions() {
    return this.permissions;
  }

  @Override
  public Map<Integer, Long> getGroupPermissionBit() {
    Map<Integer, List<PermissionDefinition>> mapGroups = permissions.stream().distinct()
      .sorted(Comparator.comparing(PermissionDefinition::getGroup))
      .collect(Collectors.groupingBy(PermissionDefinition::getGroup, Collectors.toList()));
    return mapGroups.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey,
        entry ->
          entry.getValue().stream()
            .mapToLong(PermissionDefinition::getValue)
            .reduce(0L, (left, right) -> left | right)));
  }

  @Override
  public long getBits() {
    return getGroupPermissionBit().values().stream().reduce(0L, (left, right) -> left | right);
  }

  @Override
  public boolean hasPermission(PermissionDefinition permission) {
    int group = permission.getGroup();
    long value = permission.getValue();
    Map<Integer, Long> groupPermissionBit = getGroupPermissionBit();
    if (MapUtils.isEmpty(groupPermissionBit)) {
      return false;
    }
    Long permVal = groupPermissionBit.get(group);
    if (permVal == null) {
      return false;
    }
    return (permVal & value) != 0;
  }

  @Override
  public <T> T permissionToBean(Class<T> beanClass) {
    Map<String, Boolean> map = getPermissions().stream()
      .collect(HashMap::new, (m, v) -> m.put(v.getCode(), true), HashMap::putAll);

    try {
      T instance = beanClass.getDeclaredConstructor().newInstance();
      BeanUtils.populate(instance, map);
      return instance;
    }
    catch (Exception e) {
      throw new RuntimeException("无法将权限映射到Bean: " + beanClass.getName(), e);
    }
  }

  @Override
  public int compareTo(ControlRole other) {
    return Long.compare(getBits(), other.getBits());
  }

  @Override
  public boolean isEqualTo(ControlRole other) {
    return this.compareTo(other) == 0;
  }

  @Override
  public boolean isGreaterThan(ControlRole other) {
    return this.compareTo(other) > 0;
  }

  @Override
  public boolean isGreaterThanOrEqualTo(ControlRole other) {
    return this.compareTo(other) >= 0;
  }

  @Override
  public boolean isLessThan(ControlRole other) {
    return this.compareTo(other) < 0;
  }

  @Override
  public boolean isLessThanOrEqualTo(ControlRole other) {
    return this.compareTo(other) <= 0;
  }

}
