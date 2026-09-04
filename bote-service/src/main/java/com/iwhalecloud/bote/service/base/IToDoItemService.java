package com.iwhalecloud.bote.service.base;

/**
 * 待办事项服务接口
 * 
 * @author wang.tingyun
 * @since 2025-09-24
 */
public interface IToDoItemService {
    
    /**
     * 获取待办事项类型
     */
    String getToDoItemType();

    /**
     * 查询用户的代表事项数
     *
     * @return 待办事项数量
     */
    Integer getToDoItemCount();

}