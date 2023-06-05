package com.zyh.mybatisframework.mapping.result;

import com.zyh.mybatisframework.session.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 在一条语句配置中需要有包括一个返回类型的配置，这个返回类型可以是
 * 通过 resultType 配置，也可以使用 resultMap 进行处理，
 * 而无论使用哪种方式其实最终都会被封装成统一的 ResultMap 结果映射类。
 *
 * @description: 结果映射  ———— ResultMap 的对象化，
 * @author：zhanyh
 * @date: 2023/5/29
 */
public class ResultMap {
    private String id;
    private Class<?> type;
    private List<ResultMapping> resultMappings;
    private Set<String> mappedColumns;

    private ResultMap() {
    }

    public List<ResultMapping> getPropertyResultMappings() {
        return resultMappings;
    }

    public static class Builder {
        private ResultMap resultMap = new ResultMap();

        public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings) {
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
        }

        public ResultMap build() {
            resultMap.mappedColumns = new HashSet<>();
            for (ResultMapping resultMapping : resultMap.resultMappings) {
                final String column = resultMapping.getColumn();
                if (column != null) {
                    resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
                }
            }
            return resultMap;
        }

    }


    /* 四个属性的getter方法 */
    public String getId() {
        return id;
    }

    public Set<String> getMappedColumns() {
        return mappedColumns;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }



}
