package com.iwhalecloud.bote.doc.module.control.base.request;

import com.iwhalecloud.bote.doc.module.control.base.ControlType;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder.ControlSubject;
import java.util.List;

/**
 * 权限控制的属性
 */
public interface ControlAttribute {

  /**
   * 被授权的对象主体
   *
   * @return 主体信息
   */
  List<ControlSubject> getControlSubjects();

  /**
   * 受控的对象ID
   *
   * @return 受控的资源对象ID
   */
  List<String> getControlIds();

  /**
   * 受控对象的类型
   *
   * @return control type
   */
  ControlType getType();
}
