package com.zyh.mybatisframework.session;

import java.util.List;

/**
 * @author zhanyh
 * @description SqlSession 用来执行SQL，获取映射器，管理事务。
 * PS：通常情况下，我们在应用程序中使用的Mybatis的API就是这个接口定义的方法。
 * @date 2022/04/01
 */
public interface SqlSession {

    /**
     * 根据指定的SqlID获取一条记录的封装对象
     *
     * @param <T>       the returned object type 封装之后的对象类型
     * @param statement sqlID
     * @return Mapped object 封装之后的对象
     */
    <T> T selectOne(String statement);

    /**
     * 根据指定的SqlID获取一条记录的封装对象，只不过这个方法容许我们可以给sql传递一些参数
     * 一般在实际使用中，这个参数传递的是pojo，或者Map或者ImmutableMap
     *
     * @param <T>       the returned object type
     * @param statement Unique identifier matching the statement to use.
     * @param parameter A parameter object to pass to the statement.
     * @return Mapped object
     */
    <T> T selectOne(String statement, Object parameter);

    /**
     * 获取多条记录，这个方法容许我们可以传递一些参数
     * @param statementId sql语句的唯一标识
     * @param parameter   参数
     * @return sql执行结果list
     */

    <E> List<E> selectList(String statementId, Object parameter);

    int insert(String statement, Object parameter);

    int update(String statement, Object parameter);
    Object delete(String statement, Object parameter);

    void commit();

    void rollback();
    /**
     * 得到映射器，这个巧妙的使用了泛型，使得类型安全
     *
     * @param <T>  the mapper type
     * @param type Mapper interface class
     * @return a mapper bound to this SqlSession
     */
    <T> T getMapper(Class<T> type);

    /**
     * Retrieves current configuration
     * 得到配置
     * @return Configuration
     */
    Configuration getConfiguration();

    void close();

    /**
     * 清理 Session 缓存
     */
    void clearCache();
}
