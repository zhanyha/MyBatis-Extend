package com.zyh.mybatisframework.plugin;

import java.util.Properties;

/**
 * @description: 拦截器接口
 * @author：zhanyh
 * @date: 2023/6/1
 */
public interface Interceptor {


    // 拦截，使用方实现
    Object intercept(Invocation invocation) throws Throwable;

    // 代理
    default Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    // 设置属性
    default void setProperties(Properties properties) {
        // NOP
    }

}
