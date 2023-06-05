package com.zyh.mybatisframework.scripting.xmltags;

import com.zyh.mybatisframework.builder.SqlSourceBuilder;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.SqlSource;
import com.zyh.mybatisframework.session.Configuration;

import java.util.Map;

/**
 * @description: 动态类型sql
 * @author：zhanyh
 * @date: 2023/5/31
 */
public class DynamicSqlSource implements SqlSource {

    private Configuration configuration;

    private SqlNode rootSqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 生成一个 DynamicContext 动态上下文
        DynamicContext context = new DynamicContext(configuration, parameterObject);

        // SqlNode.apply 将 动态节点对象解析递归 拼接成只含 #{}的sql 字符串，不替换 #{} 这种参数
        rootSqlNode.apply(context);

        // 调用 SqlSourceBuilder
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);

        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();

        // SqlSourceBuilder.parse 这里返回的是 StaticSqlSource，解析过程就把那些参数都替换成?了，也就是最基本的JDBC的SQL语句。
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());

        // SqlSource.getBoundSql，非递归调用，而是调用 StaticSqlSource 实现类
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return boundSql;
    }
}
