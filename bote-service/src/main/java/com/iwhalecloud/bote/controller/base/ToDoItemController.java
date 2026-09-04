package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.service.base.IToDoItemService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 待办事项 Controller
 *
 * @author wang.tingyun
 * @since 2025-09-24
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/todoItem", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：待办事项")
public class ToDoItemController {
    
    private final List<IToDoItemService> toDoItemServiceList;

    @Operation(summary = "查询用户的待办事项列表")
    @GetMapping("getUserToDoItems")
    public ResultVO<List<Map<String, Object>>> getUserToDoItems() {
        // 查询所有待办事项列表
        List<Map<String, Object>> itemMapList = new ArrayList<>();
        for (IToDoItemService toDoItemService : toDoItemServiceList) {
            Integer toDoItemCount = toDoItemService.getToDoItemCount();
            if (toDoItemCount > 0) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("todoItemType", toDoItemService.getToDoItemType());
                itemMap.put("todoItemCount", toDoItemCount);
                itemMapList.add(itemMap);
            }
        }
        return ResultVO.success(itemMapList);
    }

}
