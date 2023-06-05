package com.zyh.mybatisframework.session;

/**
 * @description: 结果处理器
 *               和结果集处理器处理的区别 :
 * @author：zhanyh
 * @date: 2023/5/26
 */
public interface ResultHandler {

    /**
     * 处理结果
     */
    void handleResult(ResultContext context);
}
