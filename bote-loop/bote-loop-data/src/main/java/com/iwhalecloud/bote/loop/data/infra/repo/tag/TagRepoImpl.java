package com.iwhalecloud.bote.loop.data.infra.repo.tag;

import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagKeyParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.MGetTagValueParam;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKey;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagStatus;
import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagValue;
import com.iwhalecloud.bote.loop.data.domain.tag.repo.tag.ITagAPI;
import com.iwhalecloud.bote.loop.data.domain.tag.repo.tag.TagKeyRepo;
import com.iwhalecloud.bote.loop.data.domain.tag.repo.tag.TagValueRepo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TagRepoImpl implements ITagAPI {
  @Qualifier("tagKeyRepoImpl")
  @Autowired
  private TagKeyRepo tagKeyRepo;
  @Qualifier("tagValueRepoImpl")
  @Autowired
  private TagValueRepo tagValueRepo;

  @Override
  public void mCreateTagKeys(List<TagKey> tagKeys) {
    tagKeyRepo.mCreateTagKeys(tagKeys);
  }

  @Override
  public TagKey getTagKey(Long spaceId, Long id) {
    return tagKeyRepo.getTagKey(spaceId, id);
  }

  @Override
  public List<TagKey> mGetTagKeys(MGetTagKeyParam param) {
    return tagKeyRepo.mGetTagKeys(param);
  }

  @Override
  public void patchTagKey(Long spaceId, Long id, TagKey patch) {
    tagKeyRepo.patchTagKey(spaceId, id, patch);
  }

  @Override
  public void deleteTagKey(Long spaceId, Long id) {
    tagKeyRepo.deleteTagKey(spaceId, id);
  }

  @Override
  public void updateTagKeysStatus(Long spaceId, Long tagKeyId, Integer versionNum, TagStatus toStatus, Boolean updateInfo) {
    tagKeyRepo.updateTagKeysStatus(spaceId, tagKeyId, versionNum, toStatus, updateInfo);
  }

  @Override
  public Long countTagKeys(MGetTagKeyParam param) {
    return tagKeyRepo.countTagKeys(param);
  }

  @Override
  public void mCreateTagValues(List<TagValue> tagValues) {
    tagValueRepo.mCreateTagValues(tagValues);
  }

  @Override
  public TagValue getTagValue(Long spaceId, Long id) {
    return tagValueRepo.getTagValue(spaceId, id);
  }

  @Override
  public List<TagValue> mGetTagValue(MGetTagValueParam param) {
    return tagValueRepo.mGetTagValue(param);
  }

  @Override
  public void patchTagValue(Long spaceId, Long id, TagValue patch) {
    tagValueRepo.patchTagValue(spaceId, id, patch);
  }

  @Override
  public void deleteTagValue(Long spaceId, Long id) {
    tagValueRepo.deleteTagValue(spaceId, id);
  }

  @Override
  public void updateTagValuesStatus(Long spaceId, Long tagKeyId, Integer versionNum, TagStatus toStatus, Boolean updateInfo) {
    tagValueRepo.updateTagValuesStatus(spaceId, tagKeyId, versionNum, toStatus, updateInfo);
  }
}
