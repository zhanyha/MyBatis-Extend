package com.zyh.mybatisframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务接口
 * 定义标准的事务接口，链接、提交、回滚、关闭，具体可以由不同的事务方式进行实现
 */
public interface Transaction {

    Connection getConnection() throws SQLException;

    void commit() throws SQLException;

    void rollback() throws SQLException;

    void close() throws SQLException;

}
