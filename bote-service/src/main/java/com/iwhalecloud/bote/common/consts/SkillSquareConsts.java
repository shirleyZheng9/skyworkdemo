package com.iwhalecloud.bote.common.consts;

import java.util.Set;

/**
 * 技能广场相关常量
 *
 * @author skill-square
 * @since 2026-03-18
 */
public final class SkillSquareConsts {
  /**
   * 允许的压缩包扩展名（小写，含点，如 .zip）
   */
  public static final Set<String> ALLOWED_ARCHIVE_EXTENSIONS = Set.of(".zip", ".skill");
  /**
   * 导出 zip 内技能目录前缀
   */
  public static final String ZIP_DIR_SKILLS = "skills/";
  /**
   * manifest.json 文件名
   */
  public static final String ZIP_FILE_MANIFEST = "manifest.json";
  /**
   * meta.json 文件名
   */
  public static final String ZIP_FILE_META = "meta.json";
  /**
   * SKILL.md 文件名
   */
  public static final String ZIP_FILE_SKILL_MD = "SKILL.md";
  /**
   * package.zip 文件名
   */
  public static final String ZIP_FILE_PACKAGE = "package.zip";
  /**
   * 上传子目录: 技能广场
   */
  public static final String UPLOAD_SUBFOLDER_SQUARE = "skill-square";

  /**
   * 异步导出时每个分包的技能条数上限
   */
  public static final int EXPORT_PACKAGE_MAX_RECORDS = 3000;
  /**
   * 导出参数 {@code top} 允许的最大值，防止过大查询
   */
  public static final int EXPORT_TOP_MAX = 500_000;
  /**
   * 上传子目录: 技能广场导出
   */
  public static final String UPLOAD_SUBFOLDER_EXPORT = "skill-square-export";
  /**
   * 广场查询类型：热门（按 install_count 降序，见 {@code AgentSkillSquareMapper.xml}）
   */
  public static final String QUERY_TYPE_HOT = "hot";
  /**
   * 热门列表最大条数（总量封顶；翻页时 offset/limit 不超出此窗口）
   */
  public static final int HOT_SKILL_QUERY_MAX = 50;
  /**
   * 批量导入 zip 内单个条目最大字节数（与 spring.servlet.multipart.max-file-size 对齐，防止 zip 炸弹/超大条目拖垮内存）
   */
  public static final long MAX_BULK_IMPORT_ENTRY_BYTES = 500L * 1024 * 1024;
  /**
   * 批量导入中 {@link #ZIP_FILE_META}、{@link #ZIP_FILE_SKILL_MD} 等文本条目最大字节数
   */
  public static final long MAX_BULK_IMPORT_TEXT_ENTRY_BYTES = 16L * 1024 * 1024;
  private SkillSquareConsts() {
  }

  /**
   * 检查文件名是否具有允许的压缩包扩展名（忽略大小写）
   *
   * @param filename 文件名
   * @return 是否允许
   */
  public static boolean hasAllowedArchiveExtension(String filename) {
    if (filename == null || filename.isEmpty()) {
      return false;
    }
    String lower = filename.toLowerCase();
    return ALLOWED_ARCHIVE_EXTENSIONS.stream().anyMatch(lower::endsWith);
  }

  /**
   * 校验失败时的格式提示（如：文件必须为 .zip 格式）
   */
  public static String getArchiveExtensionHint() {
    return "文件必须为 " + String.join("、", ALLOWED_ARCHIVE_EXTENSIONS) + " 格式";
  }
}
