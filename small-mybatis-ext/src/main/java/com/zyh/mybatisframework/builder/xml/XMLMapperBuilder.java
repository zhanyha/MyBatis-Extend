package com.zyh.mybatisframework.builder.xml;

import com.zyh.mybatisframework.builder.BaseBuilder;
import com.zyh.mybatisframework.builder.MapperBuilderAssistant;
import com.zyh.mybatisframework.builder.ResultMapResolver;
import com.zyh.mybatisframework.cache.Cache;
import com.zyh.mybatisframework.io.Resources;
import com.zyh.mybatisframework.mapping.DBRouter;
import com.zyh.mybatisframework.mapping.result.ResultFlag;
import com.zyh.mybatisframework.mapping.result.ResultMap;
import com.zyh.mybatisframework.mapping.result.ResultMapping;
import com.zyh.mybatisframework.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @description: mapper解析
 *              这里只负责解析最外层的<mapper>
 *              <mapper>里面的<select> <update> 交给XMLStatementBuilder解析
 * @author：zhanyh
 * @date: 2023/5/28
 */
public class XMLMapperBuilder extends BaseBuilder {
    private Element element;

    // 映射器构建助手
    private MapperBuilderAssistant builderAssistant;


    private String resource;

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource) throws DocumentException {
        this(new SAXReader().read(inputStream), configuration, resource);
    }

    private XMLMapperBuilder(Document document, Configuration configuration, String resource) {
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
    }

    /**
     * 解析
     */
    public void parse() throws Exception {
        // 如果当前资源没有加载过再加载，防止重复加载
        if (!configuration.isResourceLoaded(resource)) {
            configurationElement(element);
            // 标记一下，已经加载过了
            configuration.addLoadedResource(resource);
            // 绑定映射器到namespace
            configuration.addMapper(Resources.classForName(builderAssistant.getCurrentNamespace()));
        }
    }

    // 配置mapper元素
    // <mapper namespace="org.mybatis.example.BlogMapper">
    //   <select id="selectBlog" parameterType="int" resultType="Blog">
    //      select * from Blog where id = #{id}
    //   </select>
    // </mapper>
    /* 这里只负责解析最外层的<mapper namespace="org.mybatis.example.BlogMapper">
    * <select id="selectBlog" parameterType="int" resultType="Blog">
    //      select * from Blog where id = #{id}
    //   </select>
    * 的解析交给XMLStatementBuilder来完成
    *  */
    private void configurationElement(Element element) {
        // 1. 解析namespace
        String namespace = element.attributeValue("namespace");
        if (namespace == null || namespace.equals("")) {
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }
        builderAssistant.setCurrentNamespace(namespace);
        // 2. 配置分表
        dbRouterElement(element.element("dbRouter"));
        // 3. 配置cache
        cacheElement(element.element("cache"));

        // 4. 解析resultMap
        resultMapElements(element.elements("resultMap"));


        // 5. 解析配置select|insert|update|delete
        buildStatementFromContext(
                element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete")
        );
    }

    /**
     * <TableSelect size="4" format="%3d" join="_"/>
     */
    private void dbRouterElement(Element context) {
        if (context == null) return;
        Integer size = Integer.valueOf(context.attributeValue("size"));
        String format = context.attributeValue("format");
        String join = context.attributeValue("join");
        DBRouter dbRouter = new DBRouter(size, format, join);
        builderAssistant.setDBRouter(dbRouter);
    }

    /**
     * <cache eviction="FIFO" flushInterval="600000" size="512" readOnly="true"/>
     */
    private void cacheElement(Element context) {
        if (context == null) return;
        // 基础配置信息
        String type = context.attributeValue("type", "PERPETUAL");
        Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
        // 缓存队列 FIFO
        String eviction = context.attributeValue("eviction", "FIFO");
        Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
        Long flushInterval = Long.valueOf(context.attributeValue("flushInterval"));
        Integer size = Integer.valueOf(context.attributeValue("size"));
        boolean readWrite = !Boolean.parseBoolean(context.attributeValue("readOnly", "false"));
        boolean blocking = !Boolean.parseBoolean(context.attributeValue("blocking", "false"));

        // 解析额外属性信息；<property name="cacheFile" value="/tmp/xxx-cache.tmp"/>
        List<Element> elements = context.elements();
        Properties props = new Properties();
        for (Element element : elements) {
            props.setProperty(element.attributeValue("name"), element.attributeValue("value"));
        }
        // 构建缓存
        builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
    }

    @SafeVarargs
    private final void buildStatementFromContext(List<Element>... lists) {
        for (List<Element> list : lists) {
            for (Element element : list) {
                final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant , element);
                statementParser.parseStatementNode();
            }
        }
    }

    private void resultMapElements(List<Element> resultMapLabels) {
        for (Element element : resultMapLabels) {
            try {
                resultMapElement(element, Collections.emptyList());
            } catch (Exception ignore) {
            }
        }
    }

    private ResultMap resultMapElement(Element resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
        String id = resultMapNode.attributeValue("id");
        String type = resultMapNode.attributeValue("type");
        Class<?> typeClass = resolveClass(type);

        List<ResultMapping> resultMappings = new ArrayList<>();
        resultMappings.addAll(additionalResultMappings);

        List<Element> resultChildren = resultMapNode.elements();
        for (Element resultChild : resultChildren) {
            List<ResultFlag> flags = new ArrayList<>();
            if ("id".equals(resultChild.getName())) {
                flags.add(ResultFlag.ID);
            }
            // 构建 ResultMapping
            resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
        }

        // 创建结果映射解析器, 注入Configuration中
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, resultMappings);
        return resultMapResolver.resolve();
    }

    /**
     * 解析一行一行的resultMapping
     * <id column="id" property="id"/>
     * <result column="activity_id" property="activityId"/>
     */
    private ResultMapping buildResultMappingFromContext(Element context, Class<?> resultType, List<ResultFlag> flags) throws Exception {
        String property = context.attributeValue("property");
        String column = context.attributeValue("column");
        return builderAssistant.buildResultMapping(resultType, property, column, flags);
    }


}
