package com.zyh.mybatisframework.mapping;

/**
 * @description: Sql语句类型的枚举类
 * @author：zhanyh
 * @date: 2023/5/25
 */
public enum SqlCommandType {
    /**
     * 未知
     */
    UNKNOWN,
    /**
     * 插入
     */
    INSERT,
    /**
     * 更新
     */
    UPDATE,
    /**
     * 删除
     */
    DELETE,
    /**
     * 查找
     */
    SELECT;
}
