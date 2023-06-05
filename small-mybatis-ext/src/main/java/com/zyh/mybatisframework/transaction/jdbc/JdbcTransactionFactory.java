package com.zyh.mybatisframework.transaction.jdbc;

import com.zyh.mybatisframework.session.TransactionIsolationLevel;
import com.zyh.mybatisframework.transaction.Transaction;
import com.zyh.mybatisframework.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @description: Jdbc事务工厂
 * @author：zhanyh
 * @date: 2023/5/25
 */
public class JdbcTransactionFactory implements TransactionFactory {

    @Override
    public Transaction newTransaction(Connection conn) {
        return new JdbcTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(dataSource, level, autoCommit);
    }
}
