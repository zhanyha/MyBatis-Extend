package com.zyh.mybatisframework.scripting;

import com.zyh.mybatisframework.executor.parameter.ParameterHandler;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.MappedStatement;
import com.zyh.mybatisframework.mapping.SqlSource;
import com.zyh.mybatisframework.session.Configuration;
import org.dom4j.Element;

/**
 * @description: 脚本语言驱动
 * @author：zhanyh
 * @date: 2023/5/28
 */
public interface LanguageDriver {

    /**
     * 创建SQL源码(mapper xml方式)
     */
    SqlSource createSqlSource(Configuration configuration, Element element, Class<?> parameterType);

    SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

        /**
         * 创建参数处理器
         */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

}
