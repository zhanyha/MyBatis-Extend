package com.zyh.mybatisframework.builder;

import com.zyh.mybatisframework.mapping.result.ResultMap;
import com.zyh.mybatisframework.mapping.result.ResultMapping;

import java.util.List;

/**
 * @description: 结果映射解析器
 * @author：zhanyh
 * @date: 2023/5/31
 */
public class ResultMapResolver {
    private final MapperBuilderAssistant assistant;
    private String id;
    private Class<?> type;
    private List<ResultMapping> resultMappings;

    public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, List<ResultMapping> resultMappings) {
        this.assistant = assistant;
        this.id = id;
        this.type = type;
        this.resultMappings = resultMappings;
    }

    public ResultMap resolve() {
        return assistant.addResultMap(this.id, this.type, this.resultMappings);
    }
}
