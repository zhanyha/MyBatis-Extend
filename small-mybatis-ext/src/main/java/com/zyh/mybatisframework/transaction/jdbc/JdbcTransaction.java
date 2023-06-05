package com.zyh.mybatisframework.transaction.jdbc;

import com.zyh.mybatisframework.session.TransactionIsolationLevel;
import com.zyh.mybatisframework.transaction.Transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @description: 事务方式进行可以又不同的实现，包括：JDBC和托管事务，托管事务是交给 Spring 这样的容器来管理。
 * @author：zhanyh
 * @date: 2023/5/25
 */
public class JdbcTransaction implements Transaction {
    protected Connection connection;
    protected DataSource dataSource;

    /* 默认的事务隔离级别 REPEATABLE_READ 可重复读 */
    protected TransactionIsolationLevel level = TransactionIsolationLevel.REPEATABLE_READ;

    /* 默认自动提交事务 */
    protected boolean autoCommit = true;

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    public JdbcTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        this.dataSource = dataSource;
        this.level = level;
        this.autoCommit = autoCommit;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection != null) {
            return connection;
        }
        connection = dataSource.getConnection();
        connection.setTransactionIsolation(level.getLevel());
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    /**
     * 已经建立了连接，并且事务不是自动提交的，才需要手动提交
     */
    @Override
    public void commit() throws SQLException {

        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
        }
    }

    /**
     * 已经建立了连接，并且事务不是自动提交的，才需要手动回滚
     */
    @Override
    public void rollback() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.rollback();
        }
    }

    /**
     * 已经建立了连接，并且事务不是自动提交的，才需要手动关闭连接
     */
    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.close();
        }
    }
}
