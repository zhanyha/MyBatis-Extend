package com.zyh.mybatisframework.executor.result;

import com.zyh.mybatisframework.reflection.factory.ObjectFactory;
import com.zyh.mybatisframework.session.ResultContext;
import com.zyh.mybatisframework.session.ResultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 默认结果处理器
 * @author：zhanyh
 * @date: 2023/5/29
 */
public class DefaultResultHandler implements ResultHandler {
    private final List<Object> list;

    public DefaultResultHandler() {
        this.list = new ArrayList<>();
    }
    /**
     * 通过 ObjectFactory 反射工具类，产生特定的 List
     */
    @SuppressWarnings("unchecked")
    public DefaultResultHandler(ObjectFactory objectFactory) {
        this.list = objectFactory.create(List.class);
    }

    public List<Object> getResultList() {
        return list;
    }

    @Override
    public void handleResult(ResultContext context) {
        list.add(context.getResultObject());
    }
}

