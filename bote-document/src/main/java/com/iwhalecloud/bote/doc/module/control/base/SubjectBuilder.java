package com.iwhalecloud.bote.doc.module.control.base;

import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.module.control.model.ControlRoleInfo;
import java.util.Objects;

/**
 * Control Subject Builder.
 */
public final class SubjectBuilder {
  private SubjectBuilder() {
  }

  public static ControlSubject userId(Long userId) {
    return new UserSubject(userId);
  }

  public static ControlSubject orgId(Long orgId) {
    return new OrgSubject(orgId);
  }

  public static ControlSubject roleId(Long roleId) {
    return new RoleSubject(roleId);
  }

  /**
   * 转换ControlSubject
   *
   * @param roleInfo 权限角色信息
   * @return controlSubject
   */
  public static ControlSubject fromControlRole(ControlRoleInfo roleInfo) {
    String subjectType = roleInfo.getSubjectType();
    SubjectTypeEnum subjectTypeEnum = SubjectTypeEnum.valueOf(subjectType);
    Long subjectId = roleInfo.getSubjectId();
    switch (subjectTypeEnum) {
      case USER:
        return userId(subjectId);
      case ORG:
        return orgId(subjectId);
      case ROLE:
        return roleId(subjectId);
      default:
        throw new UnsupportedOperationException("不支持的subjectType:" + subjectType);
    }
  }

  /**
   * subject type.
   */
  public interface ControlSubject {

    Long getSubjectId();

    SubjectTypeEnum getSubjectType();
  }

  /**
   * abstract subject.
   */
  private abstract static class AbstractSubject implements ControlSubject {

    private final Long subjectId;

    public AbstractSubject(Long subjectId) {
      this.subjectId = subjectId;
    }

    @Override
    public Long getSubjectId() {
      return this.subjectId;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.getSubjectId(), this.getSubjectType());
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ControlSubject that)) {
        return false;
      }
      return Objects.equals(this.getSubjectId(), that.getSubjectId())
        && Objects.equals(this.getSubjectType(), that.getSubjectType());
    }
  }


  /**
   * member subject.
   */
  public static class UserSubject extends AbstractSubject {

    public UserSubject(Long memberId) {
      super(memberId);
    }

    @Override
    public SubjectTypeEnum getSubjectType() {
      return SubjectTypeEnum.USER;
    }
  }

  /**
   * org subject.
   */
  public static class OrgSubject extends AbstractSubject {

    public OrgSubject(Long teamId) {
      super(teamId);
    }

    @Override
    public SubjectTypeEnum getSubjectType() {
      return SubjectTypeEnum.ORG;
    }
  }

  /**
   * role subject.
   */
  public static class RoleSubject extends AbstractSubject {

    public RoleSubject(Long roleId) {
      super(roleId);
    }

    @Override
    public SubjectTypeEnum getSubjectType() {
      return SubjectTypeEnum.ROLE;
    }
  }
}
