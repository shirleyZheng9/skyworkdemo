UPDATE bt_docchain_extra_cfg SET updated_time = TIMESTAMP '2026-05-20 00:00:00',setter_options = '\\{"label":"0","value":"0"},\\{"label":"10","value":"10"},\\{"label":"20","value":"20"},\\{"label":"40","value":"40"},\\{"label":"80","value":"80"},\\{"label":"100","value":"100"},\\{"label":"200","value":"200"},\\{"label":"500","value":"500"},\\{"label":"1000","value":"1000"}' WHERE id = 3003;

update bt_skill_attr_spec set status_cd='00A' where attr_id = 2025031202;

update bt_skill_attr_value set status_cd='00A' where attr_id = 2025031202;


update bt_skill_attr_value set attr_value_name='outer_split_model' where attr_value_id = 2026030901;

update bt_docchain_extra_cfg set tip ='chunk_size:根据文本块大小拆分,不管内容，只看长度，强行切成差不多大的块，该方式对应的长度与下方的chunk_size参数设置的值为准；
semantics:按语义拆分,根据AI自己理解的内容来来切分，保证每一块都是完整意思，不切断逻辑；
custom_split:自定义拆分,按照字符拆分，以下方的“自定义拆分符”参数设定的字符为切分点；
paragraph：按段落拆分(docx)，Word 里你按回车分段，一段就是一块，不拆碎；
chapter：按章拆分(docx)，按 Word 的章节、标题切来切分，按这个方式切分出来的块最大；
layout：按版面分析结果拆分(pdf)，按视觉区域切，PDF 里可能有：标题、正文、分栏、表格、图片，在切分时就会识别哪里是标题、哪里是正文、哪里是表格、然后按区域切块；
outer_split_model: 通过外部接口拆分markdown' where id = '3014';