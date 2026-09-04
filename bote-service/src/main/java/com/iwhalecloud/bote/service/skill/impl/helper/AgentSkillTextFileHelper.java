package com.iwhalecloud.bote.service.skill.impl.helper;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * Agent Skill 可编辑文本文件判定
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
public final class AgentSkillTextFileHelper {

  private AgentSkillTextFileHelper() {
  }

  /**
   * 从文件名解析小写后缀（不含点），无后缀返回空串
   */
  public static String normalizeExtension(@Nullable String fileName) {
    if (StringUtils.isBlank(fileName) || !fileName.contains(".")) {
      return "";
    }
    int dot = fileName.lastIndexOf('.');
    if (dot < 0 || dot >= fileName.length() - 1) {
      return "";
    }
    return fileName.substring(dot + 1).toLowerCase(Locale.ROOT).trim();
  }

  /**
   * 是否属于可在线编辑的文本类型
   */
  public static boolean isTextFile(@Nullable String fileName) {
    String ext = normalizeExtension(fileName);
    return StringUtils.isNotEmpty(ext) && SystemParameter.AGENT_SKILL_EDIT_FILE_TYPE.getListValueFromDb().contains(ext);
  }
}
