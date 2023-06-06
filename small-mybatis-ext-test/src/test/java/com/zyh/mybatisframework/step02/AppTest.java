package com.zyh.mybatisframework.step02;

import com.zyh.mybatisframework.binding.MapperRegistry;
import com.zyh.mybatisframework.session.SqlSession;
import com.zyh.mybatisframework.session.SqlSessionFactory;
import com.zyh.mybatisframework.session.defaults.DefaultSqlSessionFactory;
import com.zyh.mybatisframework.step02.mapper.IUserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

/**
 * @description:
 * @author：zhanyh
 * @date: 2023/6/6
 */
public class AppTest {

    private Logger logger = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void test_MapperProxy() {
        // 1. 注册 Mapper
        MapperRegistry mapperRegistry = new MapperRegistry();
        mapperRegistry.addMapper(IUserDao.class);

        // 2. 从 SqlSession 工厂获取 Session
        SqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(mapperRegistry);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 3. 获取映射器对象
        IUserDao mapper = sqlSession.getMapper(IUserDao.class);

        // 4. 测试验证
        String resultSet = mapper.queryUserName("zhangsan");
        logger.info(resultSet);
    }

}
