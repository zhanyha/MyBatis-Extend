package com.zyh.mybatisframework.datasource.pooled;

import com.zyh.mybatisframework.datasource.unpooled.UnpooledDataSourceFactory;

import javax.sql.DataSource;

/**
 * @author zhanyh
 * @description 有连接池的数据源工厂
 * @date 2023/05/26
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

    public PooledDataSourceFactory() {
        this.dataSource = new PooledDataSource();
    }

}
