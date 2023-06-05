package com.zyh.mybatisframework.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @description: 类型处理器 ps.setParameter的抽象方法
 * @author：zhanyh
 * @date: 2023/5/29
 */
public interface TypeHandler<T> {

    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    /**
     * 根据列名获取结果
     */
    T getResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * 根据列的索引获取结果
     */
    T getResult(ResultSet rs, int columIndex) throws SQLException;

}
