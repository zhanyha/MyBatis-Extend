package com.zyh.mybatisframework.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @description: 通过类加载器获得resource的辅助类,
 *               类加载器将 classPath下的资源转换成字节流 再换成字符流
 * @author：zhanyh
 * @date: 2023/5/25
 */
public class Resources {
    /**
     * InputStreamReader 字节流转字符流
     * @param resource  资源路径
     * @return          字符流
     * @throws IOException
     */
    public static Reader getResourceAsReader(String resource) throws IOException {
        return new InputStreamReader(getResourceAsStream(resource));
    }

    /**
     * 类加载器将 classPath下的资源转换成字节流
     * @param resource 资源路径
     * @return  字节流
     * @throws IOException IOException
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        ClassLoader[] classLoaders = getClassLoaders();
        for (ClassLoader classLoader : classLoaders) {
            InputStream inputStream = classLoader.getResourceAsStream(resource);
            if (null != inputStream) {
                return inputStream;
            }
        }
        throw new IOException("Could not find resource " + resource);
    }

    private static ClassLoader[] getClassLoaders() {
        return new ClassLoader[]{
                ClassLoader.getSystemClassLoader(),
                Thread.currentThread().getContextClassLoader()};
    }

    /**
     * 加载一个 Class
     * @param className      字符串， 类路径 com.zyh.mybatisframework.dao.IUserDao
     * @return Class类        IUserDao.class
     * @throws ClassNotFoundException ClassNotFoundException
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
