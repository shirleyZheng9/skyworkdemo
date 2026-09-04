package com.iwhalecloud.bote.service.base;

import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.base.query.TableParams;
import java.util.List;

/**
 * 代码生成服务
 *
 * @author chen.linfa
 * @since 2024-09-12
 */
public interface ICodeGeneratorService {

  /**
   * 通过探测数据库表结构，生成代码
   *
   * @param params 条件
   */
  ResultVO<byte[]> execute(List<TableParams> params);
}
