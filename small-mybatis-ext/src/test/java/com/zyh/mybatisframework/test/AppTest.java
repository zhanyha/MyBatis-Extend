package com.zyh.mybatisframework.test;


import com.alibaba.fastjson.JSON;
import com.zyh.mybatisframework.io.Resources;
import com.zyh.mybatisframework.plugin.impl.page.PageSeger;
import com.zyh.mybatisframework.session.SqlSession;
import com.zyh.mybatisframework.session.SqlSessionFactory;
import com.zyh.mybatisframework.session.SqlSessionFactoryBuilder;
import com.zyh.mybatisframework.test.dao.IActivityDao;
import com.zyh.mybatisframework.test.po.Activity;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Unit test for simple App.
 */

public class AppTest {
    private final Logger logger = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void test_queryActivity() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);

        // 2. 请求对象
        Activity req = new Activity();
        req.setActivityId(100001L);

        // 3. 第一组：SqlSession
        // 3.1 开启 Session
        SqlSession sqlSession01 = sqlSessionFactory.openSession();
        // 3.2 获取映射器对象
        IActivityDao dao01 = sqlSession01.getMapper(IActivityDao.class);
        PageSeger.startPage(2, 5);
        List<Activity> activities = dao01.queryActivity();
        System.out.println(JSON.toJSONString(activities));
        System.out.println(activities.size());
    }


    @Test
    public void test_TableSelect() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);

        // 2. 请求对象
        Activity req = new Activity();
        req.setActivityId(100001L);

        // 3. 第一组：SqlSession
        // 3.1 开启 Session
        SqlSession sqlSession01 = sqlSessionFactory.openSession();
        // 3.2 获取映射器对象
        IActivityDao dao01 = sqlSession01.getMapper(IActivityDao.class);

        Activity activity = dao01.queryActivityById(req);
        System.out.println(JSON.toJSONString(activity));
    }

}