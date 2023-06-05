package com.zyh.mybatisframework.executor.statement;

import com.zyh.mybatisframework.executor.Executor;
import com.zyh.mybatisframework.executor.parameter.ParameterHandler;
import com.zyh.mybatisframework.executor.resultset.ResultSetHandler;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.MappedStatement;
import com.zyh.mybatisframework.session.Configuration;
import com.zyh.mybatisframework.session.ResultHandler;
import com.zyh.mybatisframework.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @description: 语句处理器抽象基类
 * @author：zhanyh
 * @date: 2023/5/26
 */
public abstract class BaseStatementHandler implements StatementHandler {
    protected final Configuration configuration;
    protected final Executor executor;
    protected final MappedStatement mappedStatement;

    protected final Object parameterObject;
    protected final ResultSetHandler resultSetHandler;

    protected BoundSql boundSql;

    protected final ParameterHandler parameterHandler;


    public BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        // 新增判断，因为 update 不会传入 boundSql 参数，所以这里要做初始化处理
        if (boundSql == null) {
            boundSql = mappedStatement.getBoundSql(parameterObject);
        }
        this.boundSql = boundSql;

        this.parameterObject = parameterObject;
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, resultHandler, boundSql);
        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    @Override
    public Statement prepare(Connection connection) throws SQLException {
        Statement statement;
        try {
            // 实例化 Statement
            statement = instantiateStatement(connection);
            // 参数设置，
            // 查询超时时间 350s
            statement.setQueryTimeout(350);
            // 一次最多返回的数据 10000条
            statement.setFetchSize(10000);
            return statement;
        } catch (Exception e) {
            throw new RuntimeException("Error preparing statement.  Cause: " + e, e);
        }
    }

    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

    @Override
    public BoundSql getBoundSql() {
        return boundSql;
    }

    @Override
    public MappedStatement getMappedStatement() {
        return mappedStatement;
    }
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public ParameterHandler getParameterHandler() {
        return parameterHandler;
    }
}
