package com.iwhalecloud.bote.dto.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.app.WorkbenchAppAuthEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作台应用授权 DTO
 *
 * @author wang.tingyun
 * @since 2025-09-12
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_workbench_app_auth")
@JsonInclude(Include.NON_NULL)
public class WorkbenchAppAuthDTO extends WorkbenchAppAuthEntity {

}