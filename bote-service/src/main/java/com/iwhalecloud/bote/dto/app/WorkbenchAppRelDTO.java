package com.iwhalecloud.bote.dto.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.app.WorkbenchAppRelEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作台应用关联 DTO
 *
 * @author wang.tingyun
 * @since 2025-09-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_workbench_app_rel")
@JsonInclude(Include.NON_NULL)
public class WorkbenchAppRelDTO extends WorkbenchAppRelEntity {

}


