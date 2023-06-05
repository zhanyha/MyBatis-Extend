package com.zyh.mybatisframework.builder;

import com.zyh.mybatisframework.cache.Cache;
import com.zyh.mybatisframework.cache.decorators.FifoCache;
import com.zyh.mybatisframework.cache.impl.PerpetualCache;
import com.zyh.mybatisframework.executor.keygen.KeyGenerator;
import com.zyh.mybatisframework.mapping.*;
import com.zyh.mybatisframework.mapping.result.ResultFlag;
import com.zyh.mybatisframework.mapping.result.ResultMap;
import com.zyh.mybatisframework.mapping.result.ResultMapping;
import com.zyh.mybatisframework.reflection.MetaClass;
import com.zyh.mybatisframework.scripting.LanguageDriver;
import com.zyh.mybatisframework.session.Configuration;
import com.zyh.mybatisframework.type.TypeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @description: 映射构建器助手，建造者
 * @author：zhanyh
 * @date: 2023/5/29
 */
public class MapperBuilderAssistant extends BaseBuilder{
    private String currentNamespace;
    private String resource;

    private Cache currentCache;

    private DBRouter dbRouter;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        this.currentNamespace = currentNamespace;
    }

    public void setDBRouter(DBRouter dbRouter) {
        this.dbRouter = dbRouter;
    }

    /**
     * 处理ResultMap的引用，返回ResultMap完整的类路径全限定类名。
     *
     * @param base              方法名如： queryUserById
     * @param isReference       是否是完整的全限定类名？
     * @return                  完整的类路径全限定类名。
     */
    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }

        if (isReference) {
            if (base.contains(".")) return base;
        } else {
            if (base.startsWith(currentNamespace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new RuntimeException("Dots are not allowed in element names, please remove it from " + base);
            }
        }

        return currentNamespace + "." + base;
    }

    /**
     * 添加映射器语句
     */
    public MappedStatement addMappedStatement(
            String id,
            SqlSource sqlSource,
            SqlCommandType sqlCommandType,
            Class<?> parameterType,
            String resultMap,
            Class<?> resultType,
            boolean flushCache,
            boolean useCache,
            KeyGenerator keyGenerator,
            String keyProperty,
            LanguageDriver lang
    ) {
        // 给id加上namespace前缀：cn.zyh.mybatis.test.dao.IUserDao.queryUserInfoById
        id = applyCurrentNamespace(id, false);
        
        MappedStatement.Builder mappedStatementBuilder = new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType);
        mappedStatementBuilder.resource(resource);
        mappedStatementBuilder.keyGenerator(keyGenerator);
        mappedStatementBuilder.keyProperty(keyProperty);

        // 结果映射，给 MappedStatement#resultMaps
        setStatementResultMap(resultMap, resultType, mappedStatementBuilder);
        //是否是select语句
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        setStatementCache(isSelect, flushCache, useCache, currentCache, dbRouter, mappedStatementBuilder);

        MappedStatement statement = mappedStatementBuilder.build();
        // 映射语句信息，建造完存放到配置项中
        configuration.addMappedStatement(statement);

        return statement;
    }

    private void setStatementCache(
            boolean isSelect,
            boolean flushCache,
            boolean useCache,
            Cache cache,
            DBRouter router,
            MappedStatement.Builder mappedStatementBuilder) {
        flushCache = valueOrDefault(flushCache, !isSelect);
        useCache = valueOrDefault(useCache, isSelect);
        mappedStatementBuilder.flushCacheRequired(flushCache);
        mappedStatementBuilder.useCache(useCache);
        mappedStatementBuilder.cache(cache);
        mappedStatementBuilder.dbRouter(router);
    }

    private void setStatementResultMap(
            String resultMap,
            Class<?> resultType,
            MappedStatement.Builder mappedStatementBuilder) {
        // 如果resultMap不为null
        resultMap = applyCurrentNamespace(resultMap, true);

        List<ResultMap> resultMaps = new ArrayList<>();

        if (resultMap != null) {
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                resultMaps.add(configuration.getResultMap(resultMapName.trim()));
            }
        }
        /*
         * 通常使用 resultType 即可满足大部分场景
         * <select id="queryUserInfoById" resultType="cn.zyh.mybatis.test.po.User">
         * 使用 resultType 的情况下，Mybatis 会自动创建一个 ResultMap，基于属性名称映射列到 JavaBean 的属性上。
         */
        // 如果resultMap为null
        else if (resultType != null) {
            ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                    configuration,
                    mappedStatementBuilder.id() + "-Inline",
                    resultType,
                    new ArrayList<>());
            resultMaps.add(inlineResultMapBuilder.build());
        }
        mappedStatementBuilder.resultMaps(resultMaps);
    }

    public ResultMap addResultMap(String resultMapId, Class<?> returnType, List<ResultMapping> resultMappings) {
        // 补全ID全路径，如：cn.zyh.mybatis.test.dao.IActivityDao + activityMap
        resultMapId = applyCurrentNamespace(resultMapId, false);
        ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                configuration,
                resultMapId,
                returnType,
                resultMappings);

        ResultMap resultMap = inlineResultMapBuilder.build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }

    public ResultMapping buildResultMapping(
            Class<?> resultType,
            String property,
            String column,
            List<ResultFlag> flags) {
        Class<?> javaTypeClass = resolveResultJavaType(resultType, property, null);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, null);

        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
        builder.typeHandler(typeHandlerInstance);
        builder.flags(flags);

        return builder.build();
    }

    private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
        if (javaType == null && property != null) {
            try {
                MetaClass metaResultType = MetaClass.forClass(resultType);
                javaType = metaResultType.getSetterType(property);
            } catch (Exception ignore) {
            }
        }
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    public Cache useNewCache(Class<? extends Cache> typeClass,
                            Class<? extends Cache> evictionClass,
                            Long flushInterval,
                            Integer size,
                            boolean readWrite,
                            boolean blocking,
                            Properties props) {
        // 判断为null，则用默认值
        typeClass = valueOrDefault(typeClass, PerpetualCache.class);
        evictionClass = valueOrDefault(evictionClass, FifoCache.class);

        // 建造者模式构建 Cache [currentNamespace=cn.zyh.mybatis.test.dao.IActivityDao]
        Cache cache = new CacheBuilder(currentNamespace)
                .implementation(typeClass)
                .addDecorator(evictionClass)
                .clearInterval(flushInterval)
                .size(size)
                .readWrite(readWrite)
                .blocking(blocking)
                .properties(props)
                .build();

        // 添加缓存
        configuration.addCache(cache);
        currentCache = cache;
        return cache;
    }

    private <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }


}
