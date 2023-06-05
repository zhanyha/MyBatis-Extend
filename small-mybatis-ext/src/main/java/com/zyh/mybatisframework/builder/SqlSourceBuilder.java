package com.zyh.mybatisframework.builder;

import com.zyh.mybatisframework.mapping.ParameterMapping;
import com.zyh.mybatisframework.mapping.SqlSource;
import com.zyh.mybatisframework.parsing.GenericTokenParser;
import com.zyh.mybatisframework.parsing.TokenHandler;
import com.zyh.mybatisframework.reflection.MetaClass;
import com.zyh.mybatisframework.reflection.MetaObject;
import com.zyh.mybatisframework.session.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @description: SQL源码（可以返回SqlBound） 构建器
 * @author：zhanyh
 * @date: 2023/5/28
 */
public class SqlSourceBuilder  extends BaseBuilder {

    private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
    }

    /**
     * 最底层解析最原始的sql的方法
     * @param originalSql 只含#{}的sql SELECT * FROM user where id = #{id}
     * @param parameterTypeClass  参数类型 id -> Long, 但大部分为Object,因为xml中一般不会配置parameterType属性
     * @param additionalParameters 额外的参数
     * @return  Sql源
     */
    public SqlSource parse(String originalSql, Class<?> parameterTypeClass, HashMap<String, Object> additionalParameters) {
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterTypeClass, additionalParameters);
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        /* #{} -> ? 代替 */
        String sql = parser.parse(originalSql);
        // 返回静态 SQL
        return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
    }

    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {


        private List<ParameterMapping> parameterMappings = new ArrayList<>();
        private Class<?> parameterType;

        private MetaObject metaParameters;

        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }
        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        /**
         * 构建参数映射, 并根据参数类型完成对应参数处理器的赋值工作
         * @param content content =#{}里面的内容，比如 "id,javaType=Integer"
         * @return 把#{}里面的可能的内容封装成一个对象——————ParameterMapping
         */
        private ParameterMapping buildParameterMapping(String content) {
            // 先解析参数映射,就是转化成一个 HashMap，{"property":"id", javaType:"Integer"}
            Map<String, String> propertiesMap = new ParameterExpression(content);
            String property = propertiesMap.get("property");
            Class<?> propertyType;
            if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
                propertyType = parameterType;
            } else if (property != null) {
                MetaClass metaClass = MetaClass.forClass(parameterType);
                if (metaClass.hasGetter(property)) {
                    propertyType = metaClass.getGetterType(property);
                } else {
                    propertyType = Object.class;
                }
            } else {
                propertyType = Object.class;
            }

            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
            return builder.build();
        }
    }
}
