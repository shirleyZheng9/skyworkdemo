package com.iwhalecloud.bote.common.diffc.persist.impl.knowledge;

import com.iwhalecloud.bote.dto.knowledge.CorpusInfoDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.CorpusInfoManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 文档基本信息
 *
 * @author qian.sisheng
 * @since 2025-3-10
 */
@Component
public final class CorpusInfoDifferencePersistence extends BaseRootPersistence<CorpusInfoDTO> {
    public CorpusInfoDifferencePersistence(CorpusInfoManageMapper corpusInfoManageMapper) {
      setAddConsumer(corpusInfoManageMapper::insertCorpusInfo);
      setModifyConsumer(corpusInfoManageMapper::updateCorpusInfo);
    }
}
