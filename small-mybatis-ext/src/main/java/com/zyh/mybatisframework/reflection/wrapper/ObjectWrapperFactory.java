package com.zyh.mybatisframework.reflection.wrapper;

import com.zyh.mybatisframework.reflection.MetaObject;

/**
 * @description: 对象包装工厂
 * @author：zhanyh
 * @date: 2023/5/27
 */
public interface ObjectWrapperFactory {

    /**
     * 判断有没有包装器
     */
    boolean hasWrapperFor(Object object);

    /**
     * 得到包装器
     */
    ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);

}
