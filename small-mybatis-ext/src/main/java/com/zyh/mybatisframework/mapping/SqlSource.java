package com.zyh.mybatisframework.mapping;

/**
 * @description: SQL源码 SqlSource 主要是 返回BoundSql 带?的sql语句，参数(name), 值("zhangsan")
 * @author：zhanyh
 * @date: 2023/5/28
 */
public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);
}
