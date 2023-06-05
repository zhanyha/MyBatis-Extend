package com.zyh.mybatisframework.binding;

import com.zyh.mybatisframework.session.SqlSession;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @description: 映射器(xxxMapper)代理类
 * @author：zhanyh
 * @date: 2023/5/23
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {
    private static final long serialVersionUID = -6424540398559729838L;
    /**
     * sqlSession 后期用于执行sql
     */
    private SqlSession sqlSession;
    /**
     * 被代理的mapper接口
     */
    private final Class<T> mapperInterface;
    /**
     *  缓存
     */
    private final Map<Method, MapperMethod> methodCache;


    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    /**
     * mapper被代理后的对象
     * 如我们执行IUserDao接口的selectByUserName的方法，会被代理进来
     *
     * @param method selectByUserName的方法对象
     * @param args selectByUserName的 参数如（username，id）
     * @return selectByUserName的执行结果
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(proxy, args);
        } else {
            final MapperMethod mapperMethod = cachedMapperMethod(method);
            //return sqlSession.selectOne(method.getName(), args);
            return mapperMethod.execute(sqlSession, args);
        }
    }

    /**
     * 系统和数据打交道是很频繁的，避免重复创建太多的SQL执行对象
     * 去缓存中找MapperMethod
     */
    private MapperMethod cachedMapperMethod(Method method) {
        MapperMethod mapperMethod = methodCache.get(method);
        if (mapperMethod == null) {
            //找不到才去new
            mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
            methodCache.put(method, mapperMethod);
        }
        return mapperMethod;
    }
}
