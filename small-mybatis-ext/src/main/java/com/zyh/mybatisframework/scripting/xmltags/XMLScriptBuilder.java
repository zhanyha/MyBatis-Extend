package com.zyh.mybatisframework.scripting.xmltags;

import com.zyh.mybatisframework.builder.BaseBuilder;
import com.zyh.mybatisframework.mapping.SqlSource;
import com.zyh.mybatisframework.scripting.defaults.RawSqlSource;
import com.zyh.mybatisframework.session.Configuration;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: XML脚本构建器
 * @author：zhanyh
 * @date: 2023/5/28
 */
public class XMLScriptBuilder extends BaseBuilder {

    private Element element;

    private boolean isDynamic;
    /**
     * xml中配置的参数类型的class，一般都是null
     */
    private Class<?> parameterType;

    private final Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();

    public XMLScriptBuilder(Configuration configuration, Element element, Class<?> parameterType) {
        super(configuration);
        this.element = element;
        this.parameterType = parameterType;

        // 9种，实现其中2种 trim/where/set/foreach/if/choose/when/otherwise/bind
        nodeHandlerMap.put("trim", new TrimHandler());
        nodeHandlerMap.put("if", new IfHandler());
    }

    public SqlSource parseScriptNode() {
        // 1. 递归的 获取sql语句的每个sqlNode节点信息, 并在解析的时候判断是否是动态sql
        List<SqlNode> contents = parseDynamicTags(element);
        // 2. 所有sqlNode封装成一个MixedSqlNode
        MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
        // 3. 创建不同的sql类型对象
        SqlSource sqlSource;
        if (isDynamic) {
            sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
        } else {
            sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
        }
        return sqlSource;
    }

    /**
     * @param element select的dom
     * @return  把原生sql转换成SqlNode List
     */
    List<SqlNode> parseDynamicTags(Element element) {
        List<SqlNode> contents = new ArrayList<>();
        List<Node> children = element.content();
        for (Node child : children) {
            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                String data = child.getText();
                TextSqlNode textSqlNode = new TextSqlNode(data);
                if (textSqlNode.isDynamic()) {
                    contents.add(textSqlNode);
                    isDynamic = true;
                } else {
                    contents.add(new StaticTextSqlNode(data));
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = child.getName();
                NodeHandler handler = nodeHandlerMap.get(nodeName);
                if (handler == null) {
                    throw new RuntimeException("Unknown element <" + nodeName + "> in SQL statement.");
                }
                handler.handleNode(element.element(child.getName()), contents);
                isDynamic = true;
            }
        }
        return contents;
    }


    private interface NodeHandler {
        void handleNode(Element nodeToHandle, List<SqlNode> targetContents);
    }

    private class TrimHandler implements NodeHandler {
        @Override
        public void handleNode(Element nodeToHandle, List<SqlNode> targetContents) {
            List<SqlNode> contents = parseDynamicTags(nodeToHandle);
            MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
            String prefix = nodeToHandle.attributeValue("prefix");
            String prefixOverrides = nodeToHandle.attributeValue("prefixOverrides");
            String suffix = nodeToHandle.attributeValue("suffix");
            String suffixOverrides = nodeToHandle.attributeValue("suffixOverrides");
            TrimSqlNode trim = new TrimSqlNode(configuration, mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides);
            targetContents.add(trim);
        }
    }

    private class IfHandler implements NodeHandler {
        @Override
        public void handleNode(Element nodeToHandle, List<SqlNode> targetContents) {
            List<SqlNode> contents = parseDynamicTags(nodeToHandle);
            MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
            String test = nodeToHandle.attributeValue("test");
            IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, test);
            targetContents.add(ifSqlNode);
        }
    }
}
