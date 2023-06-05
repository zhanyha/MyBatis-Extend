package com.zyh.mybatisframework.mapping;

import com.zyh.mybatisframework.session.Configuration;
import com.zyh.mybatisframework.type.JdbcType;
import com.zyh.mybatisframework.type.TypeHandler;
import com.zyh.mybatisframework.type.TypeHandlerRegistry;

/**
 * 把#{}里面所有可能的内容封装成一个对象
 *
 * @description: 参数映射 #{property,javaType=int,jdbcType=NUMERIC}
 * @author：zhanyh
 * @date: 2023/5/28
 */

public class ParameterMapping {

    private Configuration configuration;

    // property
    private String property;
    // javaType = int
    private Class<?> javaType = Object.class;
    // jdbcType=NUMERIC
    private JdbcType jdbcType;

    private TypeHandler<?> typeHandler;

    private ParameterMapping() {
        // disabled
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    public static class Builder {

        private ParameterMapping parameterMapping = new ParameterMapping();

        public Builder(Configuration configuration, String property, Class<?> javaType) {
            parameterMapping.configuration = configuration;
            parameterMapping.property = property;
            parameterMapping.javaType = javaType;
        }

        public Builder javaType(Class<?> javaType) {
            parameterMapping.javaType = javaType;
            return this;
        }

        public Builder jdbcType(JdbcType jdbcType) {
            parameterMapping.jdbcType = jdbcType;
            return this;
        }

        public ParameterMapping build() {
            if (parameterMapping.typeHandler == null && parameterMapping.javaType != null) {
                Configuration configuration = parameterMapping.configuration;
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                parameterMapping.typeHandler = typeHandlerRegistry.getTypeHandler(parameterMapping.javaType, parameterMapping.jdbcType);
            }

            return parameterMapping;
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }


}
