package com.zyh.mybatisframework.datasource.unpooled;

import com.zyh.mybatisframework.datasource.DataSourceFactory;
import com.zyh.mybatisframework.reflection.MetaObject;
import com.zyh.mybatisframework.reflection.SystemMetaObject;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @description:
 * @author：zhanyh
 * @date: 2023/5/26
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {
    protected DataSource dataSource;

    public UnpooledDataSourceFactory() {
        this.dataSource = new UnpooledDataSource();
    }

    /**
     * 拓展了配置，以后不管配置多少和dataSource相关的配置（如超时时间，核心链接数， 最大链接数），
     * 都不用再修改以下代码。
     * @param props
     */
    @Override
    public void setProperties(Properties props) {
        MetaObject metaObject = SystemMetaObject.forObject(dataSource);
        for (Object key : props.keySet()) {
            String propertyName = (String) key;
            if (metaObject.hasSetter(propertyName)) {
                String value = (String) props.get(propertyName);
                Object convertedValue = convertValue(metaObject, propertyName, value);
                metaObject.setValue(propertyName, convertedValue);
            }
        }
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 根据setter的类型,将配置文件中的值强转成相应的类型
     */
    private Object convertValue(MetaObject metaObject, String propertyName, String value) {
        Object convertedValue = value;
        Class<?> targetType = metaObject.getSetterType(propertyName);
        if (targetType == Integer.class || targetType == int.class) {
            convertedValue = Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            convertedValue = Long.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            convertedValue = Boolean.valueOf(value);
        }
        return convertedValue;
    }
}
