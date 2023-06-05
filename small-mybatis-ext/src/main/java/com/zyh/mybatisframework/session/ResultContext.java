package com.zyh.mybatisframework.session;

/**
 * @description: 结果上下文
 * @author：zhanyh
 * @date: 2023/5/29
 */
public interface ResultContext {

    /**
     * 获取结果
     */
    Object getResultObject();

    /**
     * 获取记录数
     */
    int getResultCount();

}
