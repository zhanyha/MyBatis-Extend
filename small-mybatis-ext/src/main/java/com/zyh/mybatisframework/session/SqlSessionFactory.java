package com.zyh.mybatisframework.session;

/**
 * @description: 工厂模式接口，构建SqlSession的工厂
 * @author：zhanyh
 * @date: 2023/5/23
 */
public interface SqlSessionFactory {
    /**
     * 打开一个 session
     * 作用：
     * @return SqlSession
     */
    SqlSession openSession();

}
