package com.zyh.mybatisframework.executor.statement;

import com.zyh.mybatisframework.executor.parameter.ParameterHandler;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.MappedStatement;
import com.zyh.mybatisframework.session.Configuration;
import com.zyh.mybatisframework.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @description: 语句处理器
 * @author：zhanyh
 * @date: 2023/5/26
 * Statement 是执行SQL 和 获取 SQL 执行结果的对象。
 */
public interface StatementHandler {

    /** 准备语句 */
    Statement prepare(Connection connection) throws SQLException;

    /** 处理SQL参数 */
    void parameterize(Statement statement) throws SQLException;

    /** 执行查询 */
    <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;

    int update(Statement stmt) throws SQLException;

    BoundSql getBoundSql();

    ParameterHandler getParameterHandler();
    Configuration getConfiguration();
    MappedStatement getMappedStatement();
}
