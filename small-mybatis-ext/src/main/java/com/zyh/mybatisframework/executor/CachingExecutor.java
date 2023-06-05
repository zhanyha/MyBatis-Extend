package com.zyh.mybatisframework.executor;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.zyh.mybatisframework.cache.Cache;
import com.zyh.mybatisframework.cache.CacheKey;
import com.zyh.mybatisframework.cache.TransactionalCacheManager;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.DBRouter;
import com.zyh.mybatisframework.mapping.MappedStatement;
import com.zyh.mybatisframework.plugin.impl.tableselect.TableSelect;
import com.zyh.mybatisframework.reflection.MetaObject;
import com.zyh.mybatisframework.reflection.SystemMetaObject;
import com.zyh.mybatisframework.session.ResultHandler;
import com.zyh.mybatisframework.session.RowBounds;
import com.zyh.mybatisframework.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: 二级缓存执行器
 * @author：zhanyh
 * @date: 2023/6/2
 */
public class CachingExecutor implements Executor {

    private Logger logger = LoggerFactory.getLogger(CachingExecutor.class);

    /* 包装了SimpleExecutor */
    private Executor delegate;

    /* 事务缓存管理器—————— 管理不同mapper下的查询结果*/
    /*  不同mapper对于不同的cache */
    private TransactionalCacheManager tcm = new TransactionalCacheManager();

    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
        delegate.setExecutorWrapper(this);
    }


    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        // 拿到该mapper的二级缓存
        Cache cache = ms.getCache();
        if (cache != null) {
            flushCacheIfRequired(ms);
            if (ms.isUseCache() && resultHandler == null) {
                // 从该二级缓存下尝试取数据
                @SuppressWarnings("unchecked")
                List<E> list = (List<E>) tcm.getObject(cache, key);
                if (list == null) {
                    // 二级缓存为空，走一级缓存的逻辑
                    list = delegate.<E>query(ms, parameter, rowBounds, resultHandler, key, boundSql);
                    // 查询结果暂存到事务缓存管理器中， 等到后面sqlSession.close()的
                    // 时候刷新到二级缓存中
                    tcm.putObject(cache, key, list);
                }
                // 打印调试日志，记录二级缓存获取数据
                if (logger.isDebugEnabled() && cache.getSize() > 0) {
                    logger.debug("二级缓存：{}", JSON.toJSONString(list));
                }
                return list;
            }
        }
        // 没有配置二级缓存，直接走一级缓存逻辑
        return delegate.<E>query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    private void flushCacheIfRequired(MappedStatement ms) {
        Cache cache = ms.getCache();
        if (cache != null && ms.isFlushCacheRequired()) {
            tcm.clear(cache);
        }
    }

    /**
     * 查询的统一入口，所有查询都先走这个方法
     */
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        // 1. 获取绑定SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        if(ms.getDBRouter() != null){
            rewriteSql(ms, boundSql);
        }
        // 2. 创建缓存Key
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    /**
     * 配置分表，需要改写sql
     * @param boundSql
     */
    private void rewriteSql(MappedStatement ms, BoundSql boundSql) {
        try {
            String id = ms.getId();
            String className = id.substring(0, id.lastIndexOf("."));
            String methodName = id.substring(id.lastIndexOf(".") + 1);
            Class<?> clazz = Class.forName(className);
            // 3 根据key得到要路由到的哪张表
            Method method = getMethod(clazz, methodName);
            if (method == null) {
                throw new RuntimeException("no such method called" + methodName + " ！");
            }
            TableSelect tableSelect = method.getAnnotation(TableSelect.class);
            if (tableSelect == null) {
                return;
            }
            // 获取SQL
            Object parameterObject = boundSql.getParameterObject();
            Pattern pattern = Pattern.compile("(from|into|update)[\\s]{1,}(\\w{1,})", Pattern.CASE_INSENSITIVE);
            String sql = boundSql.getSql();
            DBRouter dbRouter = ms.getDBRouter();

            String dbKey = tableSelect.key();
            if (StringUtils.isEmpty(dbKey)) {
                throw new RuntimeException("annotation TableSelect key is null！");
            }
            Object value = getAttrValue(parameterObject, dbKey);
            int tbIdx = (dbRouter.getSize() - 1) & (value.hashCode() ^ (value.hashCode() >>> 16));
            // 替换SQL表名
            Matcher matcher = pattern.matcher(sql);
            String tableName = null;
            if (matcher.find()) {
                tableName = matcher.group().trim();
            }
            assert null != tableName;
            String replaceSql = matcher.replaceAll(tableName + dbRouter.getJoin() +
                    String.format(dbRouter.getFormat(), tbIdx));
            boundSql.setSql(replaceSql);
            logger.info("原始sql : " + sql);
            logger.info("路由表索引 : " + tbIdx);
            logger.info("最终执行的sql : " + replaceSql);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        tcm.clear(ms.getCache());
        return delegate.update(ms, parameter);
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        // 一级缓存提交逻辑，清空一级缓存
        delegate.commit(required);
        // 执行commit， 把数据全部刷入二级缓存中
        tcm.commit();
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        try {
            delegate.rollback(required);
        } finally {
            if (required) {
                tcm.rollback();
            }
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            if (forceRollback) {
                tcm.rollback();
            } else {
                tcm.commit();
            }
        } finally {
            delegate.close(forceRollback);
        }
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public void setExecutorWrapper(Executor cachingExecutor) {
        throw new UnsupportedOperationException("This method should not be called");
    }

    private Method getMethod(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private Object getAttrValue(Object parameterObject, String attr) {
        if (parameterObject instanceof String) {
            return parameterObject.toString();
        }
        if (parameterObject instanceof Long) {
            return parameterObject;
        }
        Object filedValue;
        try {
            MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
            filedValue = metaObject.getValue(attr);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取路由属性值失败 attr：{"+attr+"}");
        }
        return filedValue;
    }

}
