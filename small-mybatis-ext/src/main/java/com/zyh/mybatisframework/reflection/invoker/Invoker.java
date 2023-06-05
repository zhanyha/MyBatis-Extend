package com.zyh.mybatisframework.reflection.invoker;

/**
 * @description: 反射调用者
 * @author：zhanyh
 * @date: 2023/5/26
 */
public interface Invoker {

    Object invoke(Object target, Object[] args) throws Exception;

    Class<?> getType();
}
