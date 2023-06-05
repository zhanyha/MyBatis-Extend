package com.zyh.mybatisframework.executor.keygen;

import com.zyh.mybatisframework.executor.Executor;
import com.zyh.mybatisframework.mapping.MappedStatement;

import java.sql.Statement;

/**
 * @description: 不用键值生成器
 * @author：zhanyh
 * @date: 2023/5/31
 */
public class NoKeyGenerator implements KeyGenerator{
    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }
}
