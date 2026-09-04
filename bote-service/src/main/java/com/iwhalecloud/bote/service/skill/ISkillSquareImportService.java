package com.iwhalecloud.bote.service.skill;

import com.iwhalecloud.bote.dto.skill.AdminImportRequest;
import com.iwhalecloud.bote.dto.skill.ImportAllResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 技能广场导入服务
 * <p>
 * 负责单技能 ZIP 导入及全量导入
 *
 * @author skill-square
 * @since 2026-03-19
 */
public interface ISkillSquareImportService {

  /**
   * 单技能导入（ZIP 包上传）
   *
   * @param request 导入请求参数
   */
  ResultVO<Long> importOne(AdminImportRequest request);

  /**
   * 全量导入（同步处理）
   *
   * @param zipFile 上传的 zip 文件
   * @return created/updated/failed 统计
   */
  ResultVO<ImportAllResponse> importAll(MultipartFile zipFile);
}
