package com.zyh.mybatisframework.binding;


import cn.hutool.core.lang.ClassScanner;
import com.zyh.mybatisframework.builder.annotation.MapperAnnotationBuilder;
import com.zyh.mybatisframework.session.Configuration;
import com.zyh.mybatisframework.session.SqlSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 管理用户的Mapper.class和代理后的Mapper工厂（生成代理Mapper类）之间的映射
 * @description: Mapper注册器
 * @author：zhanyh
 * @date: 2023/5/23
 */
public class MapperRegistry {
    private Configuration config;

    public MapperRegistry(Configuration config) {
        this.config = config;
    }

    /**
     * 将已添加的映射器代理加入到 HashMap
     */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    /**
     * 从缓存中获取代理对象实例
     * @param type 被代理对象
     * @param sqlSession 被代理对象的SQL
     * @return  代理对象实例
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new RuntimeException("Type " + type + " is not known to the MapperRegistry.");
        }
        return mapperProxyFactory.newInstance(sqlSession);
    }

    /**
     * 注册映射器mapper代理
     * @param type
     * @param <T>
     */
    public <T> void addMapper(Class<T> type){
        /* Mapper 必须是接口才会注册 */
        if(type.isInterface()){
            if (hasMapper(type)) {
                throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
            }

            // 注册映射器代理工厂
            knownMappers.put(type, new MapperProxyFactory<>(type));

            // 解析注解类语句配置
            MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
            parser.parse();
        }
    }

    /**
     * 扫描包下的Mapper 并注册映射器mapper代理
     * @param packageName
     */
    public void addMappers(String packageName){
        Set<Class<?>> mapperSet = ClassScanner.scanPackage(packageName);
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }


}
