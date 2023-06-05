package com.zyh.mybatisframework.mapping.result;

import com.zyh.mybatisframework.session.Configuration;
import com.zyh.mybatisframework.type.JdbcType;
import com.zyh.mybatisframework.type.TypeHandler;
import com.zyh.mybatisframework.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 结果映射 ，包括：colum、javaType、jdbcType 等
 * @author：zhanyh
 * @date: 2023/5/29
 */
public class ResultMapping {

    private Configuration configuration;
    private String property;
    private String column;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private TypeHandler<?> typeHandler;
    private List<ResultFlag> flags;

    ResultMapping() {
    }

    public String getColumn() {
        return column;
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();

        public Builder(Configuration configuration, String property, String column, Class<?> javaType) {
            resultMapping.configuration = configuration;
            resultMapping.property = property;
            resultMapping.column = column;
            resultMapping.javaType = javaType;
            resultMapping.flags = new ArrayList<>();
        }

        public Builder typeHandler(TypeHandler<?> typeHandler) {
            resultMapping.typeHandler = typeHandler;
            return this;
        }

        public Builder flags(List<ResultFlag> flags) {
            resultMapping.flags = flags;
            return this;
        }


        public ResultMapping build() {
            resolveTypeHandler();
            return resultMapping;
        }

        private void resolveTypeHandler() {
            if (resultMapping.typeHandler == null && resultMapping.javaType != null) {
                Configuration configuration = resultMapping.configuration;
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, null);
            }
        }
    }

}
