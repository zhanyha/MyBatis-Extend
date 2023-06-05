package com.zyh.mybatisframework.cache;

import com.zyh.mybatisframework.cache.decorators.TransactionalCache;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 事务缓存，管理器
 * @author：zhanyh
 * @date: 2023/6/2
 */
public class TransactionalCacheManager {

    private Map<Cache, TransactionalCache> transactionalCaches = new HashMap<>();

    public void clear(Cache cache) {
        // 获取事务缓存——————保存着该mapper下的查询结果，还未提交到二级缓存中
        Cache txCache = getTransactionalCache(cache);
        // 清除
        txCache.clear();
    }


    /**
     * 走事务缓存也是去二级缓存中查询，为什么不直接从二级缓存中拿？
     * 因为，需要创建该二级缓存对应的事务缓存，下次put进来的时候，能够找到对应的事务缓存。
     * @param cache 二级缓存
     * @param key   缓存key
     * @return
     */
    public Object getObject(Cache cache, CacheKey key) {
        Cache txCache = getTransactionalCache(cache);
        return txCache.getObject(key);
    }

    /**
     * put事务缓存也是去二级缓存中put，为什么不直接保存到二级缓存中？
     * 因为，如果发生回滚操作，二级缓存中不应该保存值。所以要先放到事务缓存中。
     * 等close,commit的时候在刷入到二级缓存中.
     * @param cache 二级缓存
     * @param key   缓存key
     * @param value 查询结果
     */
    public void putObject(Cache cache, CacheKey key, Object value) {
        Cache txCache = getTransactionalCache(cache);
        txCache.putObject(key, value);
    }

    /**
     * 提交时全部提交
     */
    public void commit() {
        for (TransactionalCache txCache : transactionalCaches.values()) {
            txCache.commit();
        }
    }

    /**
     * 回滚时全部回滚
     */
    public void rollback() {
        for (TransactionalCache txCache : transactionalCaches.values()) {
            txCache.rollback();
        }
    }

    private Cache getTransactionalCache(Cache cache) {
        TransactionalCache txCache = transactionalCaches.get(cache);
        if (txCache == null) {
            txCache = new TransactionalCache(cache);
            transactionalCaches.put(cache, txCache);
        }
        return txCache;
    }

}
