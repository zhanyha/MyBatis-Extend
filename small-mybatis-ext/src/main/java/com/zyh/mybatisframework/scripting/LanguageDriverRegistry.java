package com.zyh.mybatisframework.scripting;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 脚本语言注册器
 * @author：zhanyh
 * @date: 2023/5/28
 */
public class LanguageDriverRegistry {

    private final Map<Class<?>, LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<Class<?>, LanguageDriver>();

    private Class<?> defaultDriverClass = null;

    public Class<?> getDefaultDriverClass() {
        return defaultDriverClass;
    }

    public LanguageDriver getDriver(Class<?> cls) {
        return LANGUAGE_DRIVER_MAP.get(cls);
    }

    public void setDefaultDriverClass(Class<?> defaultDriverClass) {
        register(defaultDriverClass);
        this.defaultDriverClass = defaultDriverClass;
    }

    public void register(Class<?> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        if (!LanguageDriver.class.isAssignableFrom(cls)) {
            throw new RuntimeException(cls.getName() + " does not implements " + LanguageDriver.class.getName());
        }
        // 如果没注册过，再去注册
        LanguageDriver driver = LANGUAGE_DRIVER_MAP.get(cls);
        if (driver == null) {
            try {
                //单例模式，即一个Class只有一个对应的LanguageDriver
                driver = (LanguageDriver) cls.newInstance();
                LANGUAGE_DRIVER_MAP.put(cls, driver);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load language driver for " + cls.getName(), ex);
            }
        }
    }

    public LanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }
}
