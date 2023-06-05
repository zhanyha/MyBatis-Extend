package com.zyh.mybatisframework.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @description: 参数处理器
 * @author：zhanyh
 * @date: 2023/5/29
 */
public interface ParameterHandler {
    /**
     * 获取参数
     */
    Object getParameterObject();

    /**
     * 设置参数
     */
    void setParameters(PreparedStatement ps) throws SQLException;
}
