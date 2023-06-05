package com.zyh.mybatisframework.cache.decorators;

import com.zyh.mybatisframework.cache.Cache;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @description: 先进先出的缓存
 * @author：zhanyh
 * @date: 2023/6/1
 */
public class FifoCache implements Cache{

    private final Cache delegate;

    private Deque<Object> keyList;
    private int size;
    public FifoCache(Cache delegate) {
        this.delegate = delegate;
        this.keyList = new LinkedList<>();
        this.size = 1024;
    }

    @Override
    public void putObject(Object key, Object value) {
        keyList.addLast(key);
        if (keyList.size() > size) {
            Object oldestKey = keyList.removeFirst();
            delegate.removeObject(oldestKey);
        }
        delegate.putObject(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void clear() {
        delegate.clear();
        keyList.clear();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }
    public void setSize(int size) {
        this.size = size;
    }
}
