package com.zyh.mybatisframework.builder;

import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.ParameterMapping;
import com.zyh.mybatisframework.mapping.SqlSource;
import com.zyh.mybatisframework.session.Configuration;

import java.util.List;

/**
 * @description:
 * @author：zhanyh
 * @date: 2023/5/28
 */
public class StaticSqlSource implements SqlSource {

    private String sql;
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }


    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }
}
