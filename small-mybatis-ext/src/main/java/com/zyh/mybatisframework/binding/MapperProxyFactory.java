package com.zyh.mybatisframework.binding;

import com.zyh.mybatisframework.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 工厂操作相当于把代理的创建给封装起来了，如果不做这层封装，
 * 那么每一个创建代理类的操作，都需要自己使用 Proxy.newProxyInstance
 * 进行处理，那么这样的操作方式就显得比较麻烦了。
 *
 * @author：zhanyh
 * @date: 2023/5/23
 */
public class MapperProxyFactory<T> {
    private final Class<T> mapperInterface;
    private Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * 利用MapperProxy创建代理后的XXXMapper对象
     *
     * @param sqlSession
     * @return T 被代理后的XXXMapper
     */
    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession) {

        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);

        return (T) Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class[]{mapperInterface},
                mapperProxy
        );
    }
}
