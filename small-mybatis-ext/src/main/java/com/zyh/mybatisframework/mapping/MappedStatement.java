package com.zyh.mybatisframework.mapping;

import com.zyh.mybatisframework.cache.Cache;
import com.zyh.mybatisframework.executor.keygen.KeyGenerator;
import com.zyh.mybatisframework.mapping.result.ResultMap;
import com.zyh.mybatisframework.scripting.LanguageDriver;
import com.zyh.mybatisframework.session.Configuration;

import java.util.List;

/**
 * @description: [select|insert|update|delete]封装成一个MappedStatement对象
 * @author：zhanyh
 * @date: 2023/5/25
 */
public class MappedStatement {
    private Configuration configuration;
    private String id;
    private SqlCommandType sqlCommandType;
    /*
       1 最开始：用BoundSql代替
            private String parameterType;
            private String resultType;
            private String sql;
            private Map<Integer, String> parameter;
        */

    /*
       2 再后来：
        SqlSource(可以返回SqlBound) 和 resultType
        private BoundSql boundSql;
    */

    private SqlSource sqlSource;
    private Class<?> resultType;
    private List<ResultMap> resultMaps;
    private LanguageDriver languageDriver;

    /** step 14 add -start */
    private String[] keyProperties;
    private String[] keyColumns;
    private String resource;
    private KeyGenerator keyGenerator;
    /** step 14 add  -end*/

    private boolean flushCacheRequired;
    private boolean useCache;
    private Cache cache;
    private DBRouter dBRouter;

    MappedStatement() {
        // constructor disabled
    }

    public LanguageDriver getLanguageDriver() {
        return languageDriver;
    }

    public List<ResultMap> getResultMaps() {
        return resultMaps;
    }

    public BoundSql getBoundSql(Object parameterObject) {
        // 调用 SqlSource#getBoundSql
        return sqlSource.getBoundSql(parameterObject);
    }

    public String[] getKeyProperties() {
        return keyProperties;
    }

    public String[] getKeyColumns() {
        return keyColumns;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public boolean isFlushCacheRequired() {
        return flushCacheRequired;
    }

    /**
     * 返回该mapper下的二级缓存
     * @return
     */
    public Cache getCache() {
        return cache;
    }

    /**
     *  分表
     * @return
     */
    public DBRouter getDBRouter() {
        return dBRouter;
    }

    public boolean isUseCache() {
        return useCache;
    }


    /**
     * 建造者
     */
    public static class Builder {

        private MappedStatement mappedStatement = new MappedStatement();


        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, SqlSource sqlSource, Class<?> resultType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.resultType = resultType;
            mappedStatement.languageDriver = configuration.getDefaultScriptingLanguageInstance();
        }

        public MappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            return mappedStatement;
        }

        public String id() {
            return mappedStatement.id;
        }

        public Builder resultMaps(List<ResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            return this;
        }

        public Builder resource(String resource) {
            mappedStatement.resource = resource;
            return this;
        }

        public Builder keyGenerator(KeyGenerator keyGenerator) {
            mappedStatement.keyGenerator = keyGenerator;
            return this;
        }

        public Builder keyProperty(String keyProperty) {
            mappedStatement.keyProperties = delimitedStringToArray(keyProperty);
            return this;
        }

        public Builder cache(Cache cache) {
            mappedStatement.cache = cache;
            return this;
        }
        /** 分表 */
        public Builder dbRouter(DBRouter router) {
            mappedStatement.dBRouter = router;
            return this;
        }

        public Builder flushCacheRequired(boolean flushCacheRequired) {
            mappedStatement.flushCacheRequired = flushCacheRequired;
            return this;
        }

        public Builder useCache(boolean useCache) {
            mappedStatement.useCache = useCache;
            return this;
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getId() {
        return id;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public SqlSource getSqlSource() {
        return sqlSource;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    private static String[] delimitedStringToArray(String in) {
        if (in == null || in.trim().length() == 0) {
            return null;
        } else {
            return in.split(",");
        }
    }
}

