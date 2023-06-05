package com.zyh.mybatisframework.session.defaults;

import com.zyh.mybatisframework.binding.MapperRegistry;
import com.zyh.mybatisframework.executor.Executor;
import com.zyh.mybatisframework.mapping.Environment;
import com.zyh.mybatisframework.session.Configuration;
import com.zyh.mybatisframework.session.SqlSession;
import com.zyh.mybatisframework.session.SqlSessionFactory;
import com.zyh.mybatisframework.session.TransactionIsolationLevel;
import com.zyh.mybatisframework.transaction.Transaction;
import com.zyh.mybatisframework.transaction.TransactionFactory;

/**
 * @description: 默认的 DefaultSqlSessionFactory
 * @author：zhanyh
 * @date: 2023/5/23
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    /*
    以前通过手动注册的方式
    private final MapperRegistry mapperRegistry;

    public DefaultSqlSessionFactory(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(mapperRegistry);
    }
     */
    /**
     * 现在通过解析<mappers></mappers>配置的方式自动注册
     */
    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        final Environment environment = configuration.getEnvironment();
        /* 从xml读取的，并反射实例化后赋值给Environment */
        TransactionFactory transactionFactory = environment.getTransactionFactory();
        /* 设置事务的隔离级别读已提交 和 事务自动提交 */
        Transaction tx = transactionFactory.newTransaction(configuration.getEnvironment().getDataSource(), TransactionIsolationLevel.READ_COMMITTED, false);
        // 创建执行器
        final Executor executor = configuration.newExecutor(tx);
        return new DefaultSqlSession(configuration, executor);
    }

}
