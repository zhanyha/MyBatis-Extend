package com.zyh.mybatisframework.builder;

import com.zyh.mybatisframework.session.Configuration;
import com.zyh.mybatisframework.type.TypeAliasRegistry;
import com.zyh.mybatisframework.type.TypeHandler;
import com.zyh.mybatisframework.type.TypeHandlerRegistry;

/**
 * @description: 各中配置读取的顶层抽象类 建造者模式
 * @author：zhanyh
 * @date: 2023/5/25
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;

    /**
     * 提供别名解析功能
     */
    protected final TypeAliasRegistry typeAliasRegistry;

    /**
     * 参数处理器
     */
    protected final TypeHandlerRegistry typeHandlerRegistry;
    protected BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 根据别名找到Class
     * @param alias 别名
     * @return Class
     */
    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }

    /**
     * 根据别名解析 Class 类型别名注册/事务管理器别名
     */
    protected Class<?> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new RuntimeException("Error resolving class. Cause: " + e, e);
        }
    }

    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
        if (typeHandlerType == null){
            return null;
        }
        return typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
    }

    protected Boolean booleanValueOf(String value, Boolean defaultValue) {
        return value == null ? defaultValue : Boolean.valueOf(value);
    }

    protected int intValueOf(Integer timeLimit, int defaultValue) {
        return timeLimit == null ? defaultValue : timeLimit;
    }


}
