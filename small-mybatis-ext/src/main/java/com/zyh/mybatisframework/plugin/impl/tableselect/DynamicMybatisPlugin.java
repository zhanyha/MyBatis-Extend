package com.zyh.mybatisframework.plugin.impl.tableselect;

import com.alibaba.druid.util.StringUtils;
import com.zyh.mybatisframework.executor.statement.StatementHandler;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.DBRouter;
import com.zyh.mybatisframework.mapping.MappedStatement;
import com.zyh.mybatisframework.plugin.Interceptor;
import com.zyh.mybatisframework.plugin.Intercepts;
import com.zyh.mybatisframework.plugin.Invocation;
import com.zyh.mybatisframework.plugin.Signature;
import com.zyh.mybatisframework.reflection.MetaObject;
import com.zyh.mybatisframework.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: 分表插件
 * @author：zhanyh
 * @date: 2023/6/4
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class DynamicMybatisPlugin implements Interceptor {

    private final Logger logger = LoggerFactory.getLogger(DynamicMybatisPlugin.class);
    private final Pattern pattern = Pattern.compile("(from|into|update)[\\s]{1,}(\\w{1,})", Pattern.CASE_INSENSITIVE);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 1 获取StatementHandler
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
        // 2 获取自定义注解判断是否进行分表操作
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        String methodName = id.substring(id.lastIndexOf(".") + 1);
        Class<?> clazz = Class.forName(className);
        DBRouterStrategy dbRouterStrategy = clazz.getAnnotation(DBRouterStrategy.class);
        if (null == dbRouterStrategy || !dbRouterStrategy.splitTable()) {
            return invocation.proceed();
        }
        // 3 根据key得到要路由到的哪张表
        Method method = getMethod(clazz, methodName);
        if (method == null) {
            throw new RuntimeException("no such method called" + methodName + " ！");
        }
        TableSelect dbRouter = method.getAnnotation(TableSelect.class);
        if (dbRouter == null) {
            return invocation.proceed();
        }
        if (mappedStatement.getDBRouter() == null) {
            throw new RuntimeException("In XML TableSelect label config is null！");
        }

        // 3.1 获取路由属性
        String dbKey = dbRouter.key();
        if (StringUtils.isEmpty(dbKey)) {
            throw new RuntimeException("annotation TableSelect key is null！");
        }
        BoundSql boundSql = statementHandler.getBoundSql();
        Object parameterObject = boundSql.getParameterObject();
        // 3.2 获取路由属性值
        Object value = getAttrValue(parameterObject, dbKey);
        // 3.3
        DBRouter xmlConfig = mappedStatement.getDBRouter();
        int tbIdx = (xmlConfig.getSize() - 1) & (value.hashCode() ^ (value.hashCode() >>> 16));
        // 获取SQL
        String sql = boundSql.getSql();

        // 替换SQL表名 USER 为 USER_03
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        if (matcher.find()) {
            tableName = matcher.group().trim();
        }
        assert null != tableName;
        String replaceSql = matcher.replaceAll(tableName + xmlConfig.getJoin() +
                String.format(xmlConfig.getFormat(), tbIdx));

        logger.info("分表: " + replaceSql);
        // 通过反射修改SQL语句
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, replaceSql);
        field.setAccessible(false);

        return invocation.proceed();
    }

    private Method getMethod(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private Object getAttrValue(Object parameterObject, String attr) {
        if (parameterObject instanceof String) {
            return parameterObject.toString();
        }
        if (parameterObject instanceof Long) {
            return parameterObject;
        }
        Object filedValue;
        try {
            MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
            filedValue = metaObject.getValue(attr);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取路由属性值失败 attr：{"+attr+"}");
        }
        return filedValue;
    }


    public boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

}

