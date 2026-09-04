package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.base.SimpleDcParamDTO;
import com.iwhalecloud.bote.mapper.base.DcParamQueryMapper;
import com.iwhalecloud.bss.litchi.cache.helper.BaseSecondaryCache;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 系统参数缓存
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
@Component
public class DcParamCache extends BaseSecondaryCache<SimpleDcParamDTO> {
  private final DcParamQueryMapper dcParamQueryMapper;

  public DcParamCache(DcParamQueryMapper dcParamQueryMapper) {
    super(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_DC_PARAM);
    this.dcParamQueryMapper = dcParamQueryMapper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_DC_PARAM;
  }

  @Nullable
  @Override
  protected SimpleDcParamDTO load(String key) {
    if (StringUtils.isEmpty(key)) {
      return null;
    }
    return dcParamQueryMapper.findDcParamByCode(key);
  }

  @Override
  protected void loadAll(Consumer<Map<String, SimpleDcParamDTO>> mapConsumer) {
    Objects.requireNonNull(mapConsumer);
    List<SimpleDcParamDTO> list = dcParamQueryMapper.selectDcParam();
    if (CollectionUtils.isEmpty(list)) {
      mapConsumer.accept(Collections.emptyMap());
    }
    else {
      Map<String, SimpleDcParamDTO> map = list.stream().collect(Collectors.toMap(SimpleDcParamDTO::getParamCode, d -> d));
      mapConsumer.accept(map);
    }
  }

  /**
   * 根据编码获取系统参数值
   *
   * @param paramCode 系统参数编码
   * @return 参数值。可能为空
   */
  @Nullable
  public String getDcParamValByCode(String paramCode) {
    SimpleDcParamDTO dto = this.get(paramCode);
    if (dto != null) {
      return dto.getParamVal();
    }
    return null;
  }

  /**
   * 根据编码获取系统参数值
   *
   * @param paramCode 系统参数编码
   * @param defaultValue 默认值
   * @return 参数值。为空时返回默认值
   */
  public String getDcParamValByCode(String paramCode, String defaultValue) {
    String paramVal = this.getDcParamValByCode(paramCode);
    return StringUtils.isNotEmpty(paramVal) ? paramVal : defaultValue;
  }
}
