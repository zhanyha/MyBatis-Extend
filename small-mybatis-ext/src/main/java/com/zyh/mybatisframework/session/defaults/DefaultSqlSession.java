package com.zyh.mybatisframework.session.defaults;

import com.alibaba.fastjson.JSON;
import com.zyh.mybatisframework.executor.Executor;
import com.zyh.mybatisframework.mapping.MappedStatement;
import com.zyh.mybatisframework.session.Configuration;
import com.zyh.mybatisframework.session.RowBounds;
import com.zyh.mybatisframework.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * @description: 默认的SqlSession实现
 * @author：zhanyh
 * @date: 2023/5/23
 */
public class DefaultSqlSession implements SqlSession {

    private final Logger logger = LoggerFactory.getLogger(DefaultSqlSession.class);

    private Configuration configuration;
    private Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T selectOne(String statementId) {
        return this.selectOne(statementId, null);
    }

    /**
     * statementId: com.zyh.mybatisframework.test.dao.IUserDao.queryUserInfoById
     **/
    @Override
    public <T> T selectOne(String statementId, Object parameter) {
        List<T> list = this.<T>selectList(statementId, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new RuntimeException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }

    @Override
    public <E> List<E> selectList(String statementId, Object parameter) {
        logger.info("执行查询 statement：{} parameter：{}", statementId, JSON.toJSONString(parameter));
        MappedStatement ms = configuration.getMappedStatement(statementId);
        try {
            return executor.query(ms, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
        } catch (SQLException e) {
            throw new RuntimeException("Error querying database.  Cause: " + e);
        }
    }

    @Override
    public int insert(String statementId, Object parameter) {
        // 在 Mybatis 中 insert 调用的是 update
        return update(statementId, parameter);
    }

    @Override
    public int update(String statementId, Object parameter) {
        MappedStatement ms = configuration.getMappedStatement(statementId);
        try {
            return executor.update(ms, parameter);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating database.  Cause: " + e);
        }
    }

    @Override
    public Object delete(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public void commit() {
        try {
            executor.commit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error committing transaction.  Cause: " + e);
        }
    }

    @Override
    public void rollback() {
        try {
            executor.rollback(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearCache() {
        executor.clearLocalCache();
    }

    @Override
    public void close() {
        executor.close(false);
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }


// step 06之前
//    @Override
//    public <T> T selectOne(String statement, Object parameter) {
//        try {
//            MappedStatement mappedStatement = configuration.getMappedStatement(statement);
//            Environment environment = configuration.getEnvironment();
//
//            Connection connection = environment.getDataSource().getConnection();
//            BoundSql boundSql = mappedStatement.getBoundSql();
//            PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSql());
//            preparedStatement.setLong(1, Long.parseLong(((Object[]) parameter)[0].toString()));
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            List<T> objList = resultSet2Obj(resultSet, Class.forName(boundSql.getResultType()));
//            return objList.get(0);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//    /*
//    * 结果处理，转换成被ResultSetHandler处理
//    */
//   @SuppressWarnings("unchecked")
//    private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) {
//        List<T> list = new ArrayList<>();
//        try {
//            ResultSetMetaData metaData = resultSet.getMetaData();
//            int columnCount = metaData.getColumnCount();
//            // 每次遍历行值
//            while (resultSet.next()) {
//                T obj = (T) clazz.newInstance();
//                for (int i = 1; i <= columnCount; i++) {
//                    Object value = resultSet.getObject(i);
//                    String columnName = metaData.getColumnName(i);
//                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
//                    Method method;
//                    if (value instanceof Timestamp) {
//                        method = clazz.getMethod(setMethod, Date.class);
//                    } else {
//                        method = clazz.getMethod(setMethod, value.getClass());
//                    }
//                    method.invoke(obj, value);
//                }
//                list.add(obj);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }


// step 04之前
//     /**
//     * 映射器注册机
//     */
//    private final MapperRegistry mapperRegistry;
//
//    public DefaultSqlSession(MapperRegistry mapperRegistry) {
//        this.mapperRegistry = mapperRegistry;
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public <T> T selectOne(String statement) {
//        return (T) ("你被代理了！" + "方法：" + statement + " 无参");
//    }
//
//    /**
//     * 目前还没有与数据库进行关联
//     */
//    @Override
//    @SuppressWarnings("unchecked")
//    public <T> T selectOne(String statement, Object parameter) {
//        return (T) ("你被代理了！" + "方法：" + statement + " 入参：" + parameter);
//    }
//
//    /**
//     * 获取被代理后的mapper
//     *
//     * 后续这部分会被配置类进行替换
//     */
//    @Override
//    public <T> T getMapper(Class<T> type) {
//        return mapperRegistry.getMapper(type, this);
//    }


}
