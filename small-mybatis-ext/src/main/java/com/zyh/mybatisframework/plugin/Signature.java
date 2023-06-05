package com.zyh.mybatisframework.plugin;

/**
 * @description: 方法签名，确定下拦截哪个方法
 * @author：zhanyh
 * @date: 2023/6/1
 */

public @interface Signature {

    /**
     * 被拦截类
     */
    Class<?> type();

    /**
     * 被拦截类的方法
     */
    String method();

    /**
     * 被拦截类的方法的参数
     */
    Class<?>[] args();

}
