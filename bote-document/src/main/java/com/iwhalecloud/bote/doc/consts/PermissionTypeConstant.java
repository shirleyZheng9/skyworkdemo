package com.iwhalecloud.bote.doc.consts;

import java.util.Arrays;
import lombok.Getter;

/**
 * 权限类型枚举
 */
public final class PermissionTypeConstant {

  @Getter
  public enum LibraryRoleEnum {
    // 文档库/知识库权限
    OWNER("OWNER", "所有者", 0),
    /**
     * 增删改查、权限管理、设置
     */
    MANAGE("MANAGE", "可管理", 1),

    /**
     * 增删改查、协作编辑
     */
    EDIT("EDIT", "可编辑", 2),

    /**
     * 可修订
     */
    CORRECTION("CORRECTION", "可修订", 3),

    /**
     * 可查看和下载
     */
    DOWNLOAD("DOWNLOAD", "可查看和下载", 4),

    /**
     * 查看、下载、复制
     */
    READ("READ", "只读", 5),

    /**
     * 匿名者
     */
    ANONYMOUS("ANONYMOUS", "匿名者", 99);

    private final String code;
    private final String description;
    private final int level;

    LibraryRoleEnum(String code, String description, int level) {
      this.code = code;
      this.description = description;
      this.level = level;
    }

    public static LibraryRoleEnum getByCode(String code) {
      return Arrays.stream(values())
        .filter(item -> item.getCode().equals(code))
        .findFirst()
        .orElse(null);
    }
  }

  @Getter
  public enum DocRoleEnum {

    /**
     * 增删改查、权限管理、分享
     */
    DOC_MANAGE("MANAGE", "可管理", true, 0),

    /**
     * 所有者--文档的创建者
     */
    DOC_OWNER("OWNER", "所有者", false, 1),

    /**
     * 修改内容、协作编辑
     */
    DOC_EDIT("EDIT", "可编辑", true, 2),

    /**
     * 可修订
     */
    DOC_CORRECTION("CORRECTION", "可修订", true, 3),

    /**
     * 查看内容、下载文档
     */
    DOWNLOAD("DOWNLOAD", "可查看和下载", true, 4),

    /**
     * 仅查看内容
     */
    DOC_READ("READ", "可查看", true, 5),

    /**
     * 匿名访问
     */
    ANONYMOUS("ANONYMOUS", "匿名者", false, 99);

    private final String code;
    private final String description;
    /**
     * 是否可分配
     */
    private final boolean assignable;
    /**
     * 权限等级
     */
    private final int level;

    DocRoleEnum(String code, String description, boolean assignable, int level) {
      this.code = code;
      this.description = description;
      this.assignable = assignable;
      this.level = level;
    }

    public static DocRoleEnum getByCode(String code) {
      return Arrays.stream(values())
        .filter(item -> item.getCode().equals(code))
        .findFirst()
        .orElse(null);
    }
  }
}
