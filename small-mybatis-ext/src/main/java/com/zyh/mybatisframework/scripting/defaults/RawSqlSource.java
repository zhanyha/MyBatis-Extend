package com.zyh.mybatisframework.scripting.defaults;

import com.zyh.mybatisframework.builder.SqlSourceBuilder;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.SqlSource;
import com.zyh.mybatisframework.scripting.xmltags.DynamicContext;
import com.zyh.mybatisframework.scripting.xmltags.SqlNode;
import com.zyh.mybatisframework.session.Configuration;

import java.util.HashMap;

/**
 * @description: Static SqlSource => 带#{} ${}。
 *              比 DynamicSqlSource 动态SQL处理快
 * @author：zhanyh
 * @date: 2023/5/28
 */
public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    /**
     *
     * @param configuration     mybatis环境
     * @param rootSqlNode       sql的根节点，一般是MixSqlNode
     * @param parameterType     xml中配置的参数类型的class，一般都是null
     */
    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    /**
     * 解析原生sql —————————— TODO: 目前是最简单的静态sql文本，后期需要再这里先去掉<where> <if>动态标签
     * @param configuration mybatis环境
     * @param rootSqlNode mixSqlNode ————————目前是最简单的静态sql文本（带#{} ${}的sql语句，但不含<where> <if>动态标签）
     *                    ,mixSqlNode中仅包含一个staticSqlNode节点
     * @return 只带#{} ${}的sql语句
     */
    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        // 1 动态sql 上下文（databaseId, sql——包含不断以递归方式拼凑出来的只带#{} ${}的sql语句)
        DynamicContext context = new DynamicContext(configuration, null);
        // 2 递归的拼凑DynamicContext的sql属性
        rootSqlNode.apply(context);
        return context.getSql();
    }

    /**
     * 给成员变量sqlSource初始化
     * @param configuration     mybatis环境
     * @param sql               只带#{} ${}的sql语句
     * @param parameterType     xml中配置的参数类型的class，一般都是null
     */
    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        // XML中的select标签的parameterType属性一般为null
        Class<?> parameterTypeClass = parameterType == null ? Object.class : parameterType;

        sqlSource = sqlSourceParser.parse(sql, parameterTypeClass, new HashMap<>());
    }



    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }
}
