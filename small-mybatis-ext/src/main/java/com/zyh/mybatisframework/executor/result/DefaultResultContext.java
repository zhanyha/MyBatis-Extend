package com.zyh.mybatisframework.executor.result;

import com.zyh.mybatisframework.session.ResultContext;

/**
 * @description:
 * @author：zhanyh
 * @date: 2023/5/29
 */
public class DefaultResultContext implements ResultContext {
    private Object resultObject;
    private int resultCount;

    public DefaultResultContext() {
        this.resultObject = null;
        this.resultCount = 0;
    }

    @Override
    public Object getResultObject() {
        return resultObject;
    }

    @Override
    public int getResultCount() {
        return resultCount;
    }

    public void nextResultObject(Object resultObject) {
        resultCount++;
        this.resultObject = resultObject;
    }
}
