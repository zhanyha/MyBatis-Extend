package com.zyh.mybatisframework.cache.impl;

import com.alibaba.fastjson.JSON;
import com.zyh.mybatisframework.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 持久缓存
 * @author：zhanyh
 * @date: 2023/6/1
 */
public class PerpetualCache implements Cache {
    private Logger logger = LoggerFactory.getLogger(PerpetualCache.class);

    // 使用HashMap存放一级缓存数据，session 生命周期较短，正常情况下数据不会一直在缓存存放
    private Map<Object, Object> cache = new HashMap<>();

    private final String id;

    public PerpetualCache(String id) {
        this.id = id;
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return cache.get(key);
    }

    @Override
    public Object removeObject(Object key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int getSize() {
        return cache.size();
    }
}
