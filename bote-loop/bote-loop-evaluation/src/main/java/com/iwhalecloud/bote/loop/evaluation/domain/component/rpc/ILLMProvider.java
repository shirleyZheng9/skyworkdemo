package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.LLMCallParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ReplyItem;

public interface ILLMProvider {

  ReplyItem call(LLMCallParam llmCallParam);

}
