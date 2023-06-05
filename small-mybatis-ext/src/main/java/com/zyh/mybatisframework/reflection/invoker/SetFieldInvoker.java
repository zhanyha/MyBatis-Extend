package com.zyh.mybatisframework.reflection.invoker;

import java.lang.reflect.Field;

/**
 * @description: setter 调用者
 * @author：zhanyh
 * @date: 2023/5/26
 */
public class SetFieldInvoker implements Invoker {

    private Field field;

    public SetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        field.set(target, args[0]);
        return null;
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

}
