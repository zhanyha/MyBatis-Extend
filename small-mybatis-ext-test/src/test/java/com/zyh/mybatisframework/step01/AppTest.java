package com.zyh.mybatisframework.step01;

import com.zyh.mybatisframework.binding.MapperProxy;
import com.zyh.mybatisframework.step01.mapper.IUserDao;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author：zhanyh
 * @date: 2023/6/6
 */
public class AppTest {

    private Logger logger = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void test_MapperProxy() {
        Map<String, String> sqlSession = new HashMap<>();
        sqlSession.put("com.zyh.mybatisframework.test.dao.IUserDao.queryUserName", "模拟执行 Mapper.xml 中 SQL 语句的操作：查询用户姓名");
        sqlSession.put("com.zyh.mybatisframework.test.dao.IUserDao.queryUserAge", "模拟执行 Mapper.xml 中 SQL 语句的操作：查询用户年龄");

        MapperProxy<IUserDao> mapperProxy = new MapperProxy<>(sqlSession, IUserDao.class);
        IUserDao userDao = (IUserDao) Proxy.newProxyInstance(IUserDao.class.getClassLoader(), new Class[]{IUserDao.class}, mapperProxy);
        String res = userDao.queryUserName("10001");
        logger.info("测试结果：{}", res);
    }

}
