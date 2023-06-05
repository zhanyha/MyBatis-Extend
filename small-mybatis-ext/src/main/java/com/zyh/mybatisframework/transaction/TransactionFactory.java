package com.zyh.mybatisframework.transaction;

import com.zyh.mybatisframework.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @description: 事务工厂
 * @author：zhanyh
 * @date: 2023/5/25
 */
public interface TransactionFactory {

    /**
     * 根据 Connection 创建 Transaction， 自动提交和隔离级别走默认。
     * @param conn 数据库连接Connection
     * @return 事务Transaction
     */
    Transaction newTransaction(Connection conn);

    /**
     * 设置 自动提交 和 隔离级别 创建 Transaction
     * @param dataSource  数据源
     * @param level 事务隔离级别
     * @param autoCommit 是否自动提交
     * @return 事务Transaction
     */
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
