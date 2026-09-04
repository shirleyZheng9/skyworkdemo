package com.iwhalecloud.bote.controller.cutover;

import com.github.pagehelper.PageHelper;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.FieldEncryptUtil;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.cutover.CutOverMapper;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 存储加解密数据割接服务：
 * 1. 对使用 @EncryptField 注解的存量字段数据进行加密；
 * 2. 对使用 @EncryptField 注解的存量字段数据进行解密。
 *
 * @author tingyun.wang
 * @since 2025-07-31
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cutOver/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Hidden
@IgnoreSign
@SuppressWarnings("PMD.GuardLogStatement")
public class MigrateEncryptDataController {

  private static final Logger logger = LoggerFactory.getLogger(MigrateEncryptDataController.class);

  private final CutOverMapper cutOverMapper;

  /**
   * 加密存量数据
   */
  @PostMapping("encryptExistingData")
  public void encryptExistingData(HttpServletResponse response) throws IOException {
    // 加密用户实体数据
    processUserData(response, true);
  }

  /**
   * 解密存量数据
   */
  @PostMapping("decryptExistingData")
  public void decryptExistingData(HttpServletResponse response) throws IOException {
    // 解密用户实体数据
    processUserData(response, false);
  }

  /**
   * 处理用户数据的加解密
   */
  private void processUserData(HttpServletResponse response, boolean isEncrypt) throws IOException {
    String operationType = isEncrypt ? "加密" : "解密";
    response.setContentType("text/plain;charset=UTF-8");
    try (PrintWriter writer = response.getWriter()) {
      long startTime = System.currentTimeMillis();
      addLog(writer, "开始执行%s存量用户数据任务\n", operationType);

      // 统计需要处理的数据总量
      long totalCount = PageHelper.count(cutOverMapper::selectUsersForEncryption);
      if (totalCount == 0) {
        addLog(writer, "没有需要%s的用户数据\n", operationType);
        return;
      }
      addLog(writer, "需要%s的用户数据总量: %d\n", operationType, totalCount);

      // 分页处理数据
      int limit = 100;
      int successCount = 0;
      for (int offset = 0; offset < totalCount; offset += limit) {
        //noinspection resource
        PageHelper.offsetPage(offset, limit, false);
        List<UserEntity> userList = cutOverMapper.selectUsersForEncryption();
        for (UserEntity user : userList) {
          boolean needUpdate = isEncrypt ? encryptUserFields(user) : decryptUserFields(user);
          if (needUpdate) {
            // 执行用户数据更新并统计成功数
            boolean isSuccess = execUpdateUser(writer, user);
            successCount += isSuccess ? 1 : 0;
          }
        }
      }

      addLog(writer, "数据%s完成，耗时: %sms, 总数: %s, 更新: %s\n", operationType, System.currentTimeMillis() - startTime, totalCount, successCount);
    }
    finally {
      PageHelper.clearPage();
    }
  }

  /**
   * 加密用户字段策略
   */
  private boolean encryptUserFields(UserEntity user) {
    boolean needUpdate = false;
    // 加密手机号
    if (StringUtils.isNotEmpty(user.getPhoneNo()) && !user.getPhoneNo().startsWith(FieldEncryptUtil.ENCRYPT_FIELD_PRE)) {
      user.setPhoneNo(FieldEncryptUtil.encryptData(user.getPhoneNo()));
      needUpdate = true;
    }
    // 加密邮箱
    if (StringUtils.isNotEmpty(user.getEmail()) && !user.getEmail().startsWith(FieldEncryptUtil.ENCRYPT_FIELD_PRE)) {
      user.setEmail(FieldEncryptUtil.encryptData(user.getEmail()));
      needUpdate = true;
    }
    return needUpdate;
  }

  /**
   * 解密用户字段策略
   */
  private boolean decryptUserFields(UserEntity user) {
    boolean needUpdate = false;
    // 解密手机号
    if (StringUtils.isNotEmpty(user.getPhoneNo()) && user.getPhoneNo().startsWith(FieldEncryptUtil.ENCRYPT_FIELD_PRE)) {
      user.setPhoneNo(FieldEncryptUtil.decryptData(user.getPhoneNo()));
      needUpdate = true;
    }
    // 解密邮箱
    if (StringUtils.isNotEmpty(user.getEmail()) && user.getEmail().startsWith(FieldEncryptUtil.ENCRYPT_FIELD_PRE)) {
      user.setEmail(FieldEncryptUtil.decryptData(user.getEmail()));
      needUpdate = true;
    }
    return needUpdate;
  }

  /**
   * 执行用户数据更新
   */
  private boolean execUpdateUser(PrintWriter writer, UserEntity user) {
    try {
      TransactionUtil.executeNew(() -> cutOverMapper.updateUserEncryptFields(user));
      return true;
    }
    catch (Exception e) {
      logger.error("Failed to update user. userId = {}", user.getUserId(), e);
      addLog(writer, "\t执行更新用户数据失败: %s\n", ExpUtil.getMsg(e));
      return false;
    }
  }

  /**
   * 打印日志到 HTTP 响应
   */
  @SuppressFBWarnings("XSS_SERVLET")
  private void addLog(PrintWriter writer, String msg, Object... args) {
    writer.printf(msg, args);
    writer.flush();
  }

}
