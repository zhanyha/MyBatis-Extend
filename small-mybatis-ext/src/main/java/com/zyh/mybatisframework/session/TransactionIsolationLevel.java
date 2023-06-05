package com.zyh.mybatisframework.session;

import java.sql.Connection;

/**
 * @description:  事务的隔离级别
 * @author：zhanyh
 * @date: 2023/5/25
 */
public enum TransactionIsolationLevel {
    /**
     * 无事务
     */
    NONE(Connection.TRANSACTION_NONE),
    /**
     * 读已提交
     */
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    /**
     * 读未提交
     */
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    /**
     * 可重复读
     */
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    /**
     * 串行化
     */
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);


    private final int level;
    TransactionIsolationLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
