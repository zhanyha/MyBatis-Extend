package com.zyh.mybatisframework.executor.statement;

import com.zyh.mybatisframework.executor.Executor;
import com.zyh.mybatisframework.executor.keygen.KeyGenerator;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.MappedStatement;
import com.zyh.mybatisframework.session.ResultHandler;
import com.zyh.mybatisframework.session.RowBounds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @description: 执行 带？占位符的SQL语句 和 获取返回结果
 * @author：zhanyh
 * @date: 2023/5/26
 */
public class PreparedStatementHandler extends BaseStatementHandler {


    public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
//        super(executor, mappedStatement, parameterObject, resultHandler, boundSql);
        super(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    }

    /**
     * SQL 带占位符的
     *
     * @param connection
     * @return
     * @throws SQLException
     */
    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();
        return connection.prepareStatement(sql);
    }

    /**
     * 处理SQL占位符, 目前直接写死 ？转成 Long类型，后期处理
     *
     * @param statement jdbc的statement
     */
    @Override
    public void parameterize(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        /*
            写死，后期拓展
            ps.setLong(1, Long.parseLong(((Object[]) parameterObject)[0].toString()));
         */
        parameterHandler.setParameters(ps);
    }

    /**
     * 这里ResultHandler 的作用？
     *
     * @param statement     sql, sql参数， sql待处理结果
     * @param resultHandler 未知
     * @return 处理后的SQL结果List
     */
    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        /*String sql = boundSql.getSql();
        statement.executeQuery(sql);
        return resultSetHandler.handleResultSets(statement);*/
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return resultSetHandler.handleResultSets(ps);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        int rows = ps.getUpdateCount();


        Object parameterObject = boundSql.getParameterObject();
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
        return rows;
    }

}
