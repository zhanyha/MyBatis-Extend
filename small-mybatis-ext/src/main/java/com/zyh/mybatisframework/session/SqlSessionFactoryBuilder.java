package com.zyh.mybatisframework.session;

import com.zyh.mybatisframework.builder.xml.XMLConfigBuilder;
import com.zyh.mybatisframework.session.defaults.DefaultSqlSessionFactory;

import java.io.Reader;

/**
 * @description: 整个mybatis的启动入口
 * @author：zhanyh
 * @date: 2023/5/25
 */
public class SqlSessionFactoryBuilder {

    /**
     * 通过传入配置文件流的方式启动mybatis环境
     * @param reader 配置文件字符流
     * @return SqlSession工厂
     */
    public SqlSessionFactory build(Reader reader) {
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
        return build(xmlConfigBuilder.parse());
    }

    /**
     * 通过传入配置类的方式启动mybatis环境
     * @param config 配置类
     * @return SqlSession工厂
     */
    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }
}
