package com.zyh.mybatisframework.cache.decorators;

import com.zyh.mybatisframework.cache.Cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @description: 事务缓存， 每个mapper下的查询结果  每个mapper对应不同的二级cache
 *               当会话关闭后会刷新到二级缓存中
 * @author：zhanyh
 * @date: 2023/6/2
 */
public class TransactionalCache implements Cache{

    // mapper对应的二级缓存
    private Cache delegate;

    // commit 时要不要清缓存
    private boolean clearOnCommit;

    // commit 时要添加的元素
    private Map<Object, Object> entriesToAddOnCommit;

    private Set<Object> entriesMissedInCache;

    public TransactionalCache(Cache delegate) {
        // delegate = FifoCache
        this.delegate = delegate;
        // 默认 commit 时不清缓存
        this.clearOnCommit = false;
        this.entriesToAddOnCommit = new HashMap<>();
        this.entriesMissedInCache = new HashSet<>();
    }


    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void putObject(Object key, Object value) {
        // 不能直接存入二级缓存， 先暂存到事务缓存中
        entriesToAddOnCommit.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        // 去mapper对应的二级缓存查
        Object object = delegate.getObject(key);
        if (object == null) {
            entriesMissedInCache.add(key);
        }
        return object;
    }

    @Override
    public Object removeObject(Object key) {
        return null;
    }

    @Override
    public void clear() {
        clearOnCommit = true;
        entriesToAddOnCommit.clear();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    public void commit() {
        if (clearOnCommit) {
            delegate.clear();
        }
        flushPendingEntries();
        reset();
    }

    public void rollback() {
        unlockMissedEntries();
        reset();
    }

    private void reset() {
        clearOnCommit = false;
        entriesToAddOnCommit.clear();
        entriesMissedInCache.clear();
    }

    /**
     * 刷新数据到 MappedStatement#Cache 中，也就是把数据填充到 Mapper XML 级别下。
     * flushPendingEntries 方法把事务缓存下的数据，填充到 FifoCache 中。
     */
    private void flushPendingEntries() {
        for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
            delegate.putObject(entry.getKey(), entry.getValue());
        }
        for (Object entry : entriesMissedInCache) {
            if (!entriesToAddOnCommit.containsKey(entry)) {
                delegate.putObject(entry, null);
            }
        }
    }

    private void unlockMissedEntries() {
        for (Object entry : entriesMissedInCache) {
            delegate.putObject(entry, null);
        }
    }
}
