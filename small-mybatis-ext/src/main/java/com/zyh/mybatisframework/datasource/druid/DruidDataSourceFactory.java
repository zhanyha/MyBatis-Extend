package com.zyh.mybatisframework.datasource.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.zyh.mybatisframework.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @description Druid 数据源工厂
 * @author zhanyh
 * @date 2022/04/13
 */
public class DruidDataSourceFactory implements DataSourceFactory {

    private Properties props;

    @Override
    public void setProperties(Properties props) {
        this.props = props;
    }

    @Override
    public DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(props.getProperty("driver"));
        dataSource.setUrl(props.getProperty("url"));
        dataSource.setUsername(props.getProperty("username"));
        dataSource.setPassword(props.getProperty("password"));
        return dataSource;
    }

}
