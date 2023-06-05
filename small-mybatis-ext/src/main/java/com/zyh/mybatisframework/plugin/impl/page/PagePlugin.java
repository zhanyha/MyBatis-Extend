package com.zyh.mybatisframework.plugin.impl.page;

import com.zyh.mybatisframework.executor.parameter.ParameterHandler;
import com.zyh.mybatisframework.executor.statement.StatementHandler;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.plugin.*;
import com.zyh.mybatisframework.reflection.MetaObject;
import com.zyh.mybatisframework.reflection.SystemMetaObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

/**
 * 分页SQL插件
 *
 * @author zhanyh
 * @date 2023-6-4
 */
@Intercepts(
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})
)
public class PagePlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Page page = PageSeger.getLocalPage();
        useMetaObject(invocation, page);
        try {
            return invocation.proceed();
        }finally {
            PageSeger.clearPage();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }


    private void useMetaObject(Invocation invocation, Page page) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        if (Objects.nonNull(page)) {
            int total = getTotalSize(statementHandler, (Connection) invocation.getArgs()[0]);
            MetaObject metaObject = SystemMetaObject.forObject(boundSql);
            if (total <= 0) {
                // 返回数量小于零，查询一个简单的sql,不去执行明细查询 【基于反射，重新设置boundSql】
                String sql = "select * from (select 0 as id) as temp where  id>0";
                metaObject.setValue("sql", sql);
                metaObject.setValue("parameterMappings", Collections.emptyList());
                metaObject.setValue("parameterObject", null);
            } else {
                page.calculate(total);
                boolean limitExist = boundSql.getSql().trim().toLowerCase().contains("limit");
                if (!limitExist) {
                    String sql = boundSql.getSql() + " limit " + (page.getCurPage() - 1) * page.getPageSize() + ", " + page.getPageSize();
                    metaObject.setValue("sql", sql);
                }
            }
        }
    }

    /**
     * 查询总记录数
     *
     * @param statementHandler mybatis sql 对象
     * @param conn             链接信息
     */
    private int getTotalSize(StatementHandler statementHandler, Connection conn) {
        ParameterHandler parameterHandler = statementHandler.getParameterHandler();
        String countSql = getCountSql(statementHandler.getBoundSql().getSql());
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(countSql);
            parameterHandler.setParameters(pstmt);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                // 设置总记录数
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /***
     * 获取统计sql
     * @param originalSql 原始sql
     * @return 返回统计加工的sql
     */
    private String getCountSql(String originalSql) {
        originalSql = originalSql.trim().toLowerCase();
        // 判断是否存在 limit 标识
        boolean limitExist = originalSql.contains("limit");
        if (limitExist) {
            originalSql = originalSql.substring(0, originalSql.indexOf("limit"));
        }
        boolean distinctExist = originalSql.contains("distinct");
        boolean groupExist = originalSql.contains("group by");
        if (distinctExist || groupExist) {
            return "select count(1) from (" + originalSql + ") temp_count";
        }
        // 去掉 order by
        boolean orderExist = originalSql.contains("order by");
        if (orderExist) {
            originalSql = originalSql.substring(0, originalSql.indexOf("order by"));
        }
        int indexFrom = originalSql.indexOf("from");
        return "select count(*)  " + originalSql.substring(indexFrom);
    }
}