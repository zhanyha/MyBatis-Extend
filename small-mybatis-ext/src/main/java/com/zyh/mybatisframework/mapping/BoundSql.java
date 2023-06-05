package com.zyh.mybatisframework.mapping;

import com.zyh.mybatisframework.reflection.MetaObject;
import com.zyh.mybatisframework.session.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 绑定的SQL,是从SqlSource而来，将动态内容都处理完成得到的SQL语句字符串，其中包括?,还有绑定的参数
 * @author：zhanyh
 * @date: 2023/5/25
 */
public class BoundSql {

    /*
        private String parameterType;
        private String resultType;
        private String sql;
        private Map<Integer, String> parameterMappings;
    */

    /**
     * 只含 ? 的sql
     */
    private String sql;

    /** 参数信息:(参数名称Id， 数据库参数int(4)， 对应Java类型的参数 Long) */
    private List<ParameterMapping> parameterMappings;

    /** 参数值： id=1 */
    private Object parameterObject;

    /** 额外的参数 */
    private Map<String, Object> additionalParameters;

    private MetaObject metaParameters;

    public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterObject = parameterObject;
        this.additionalParameters = new HashMap<>();
        this.metaParameters = configuration.newMetaObject(additionalParameters);
    }

    public String getSql() {
        return sql;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

    public Object getParameterObject() {
        return parameterObject;
    }

    public boolean hasAdditionalParameter(String name) {
        return metaParameters.hasGetter(name);
    }

    public void setAdditionalParameter(String name, Object value) {
        metaParameters.setValue(name, value);
    }

    public Object getAdditionalParameter(String name) {
        return metaParameters.getValue(name);
    }

    public void setSql(String replaceSql) {
        this.sql = replaceSql;
    }
}
