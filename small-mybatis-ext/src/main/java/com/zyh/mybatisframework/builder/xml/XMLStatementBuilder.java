package com.zyh.mybatisframework.builder.xml;

import com.zyh.mybatisframework.builder.BaseBuilder;
import com.zyh.mybatisframework.builder.MapperBuilderAssistant;
import com.zyh.mybatisframework.executor.keygen.Jdbc3KeyGenerator;
import com.zyh.mybatisframework.executor.keygen.KeyGenerator;
import com.zyh.mybatisframework.executor.keygen.NoKeyGenerator;
import com.zyh.mybatisframework.executor.keygen.SelectKeyGenerator;
import com.zyh.mybatisframework.mapping.MappedStatement;
import com.zyh.mybatisframework.mapping.SqlCommandType;
import com.zyh.mybatisframework.mapping.SqlSource;
import com.zyh.mybatisframework.scripting.LanguageDriver;
import com.zyh.mybatisframework.session.Configuration;
import org.dom4j.Element;

import java.util.List;
import java.util.Locale;

/**
 * @description: select | update 标签的解析,
 *               该类解析sql语句的类型，sqlId,参数值，返回值类型，具体的sql内容交给LanguageDriver解析
 *               不同的sql配置方法（注解，xml）用不同的LanguageDriver解析
 *               把每个select标签 转成 MappedStatement对象
 * @author：zhanyh
 * @date: 2023/5/28
 */
public class XMLStatementBuilder extends BaseBuilder {

    private MapperBuilderAssistant builderAssistant;
    private Element element;

    public XMLStatementBuilder(Configuration configuration,  MapperBuilderAssistant builderAssistant, Element element) {
        super(configuration);
        this.element = element;
        this.builderAssistant = builderAssistant;
    }


    /**
     *  解析语句(select|insert|update|delete)
     *     <select
     *       id="selectPerson"
     *       parameterType="int"
     *       parameterMap="deprecated"
     *       resultType="hashmap"
     *       resultMap="personResultMap"
     *       flushCache="false"
     *       useCache="true"
     *       timeout="10000"
     *       fetchSize="256"
     *       statementType="PREPARED"
     *       resultSetType="FORWARD_ONLY">
     *       SELECT * FROM PERSON WHERE ID = #{id}
     *     </select>
     */
    public void parseStatementNode() {
        String id = element.attributeValue("id");
        // sql语句中 ？ 的参数类型
        String parameterType = element.attributeValue("parameterType");
        // xml中配置的参数类型的class，一般都是null
        Class<?> parameterTypeClass = resolveAlias(parameterType);
        // 结果类型
        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);

        // 外部应用 resultMap
        String resultMap = element.attributeValue("resultMap");

        // 获取命令类型(select|insert|update|delete)
        String nodeName = element.getName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        boolean flushCache = Boolean.parseBoolean(element.attributeValue("flushCache", String.valueOf(!isSelect)));
        boolean useCache = Boolean.parseBoolean(element.attributeValue("useCache", String.valueOf(isSelect)));

        // 获取默认语言驱动器 XMLLanguageDriver
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver langDriver = configuration.getLanguageRegistry().getDriver(langClass);

        // insert的时候 解析<selectKey> step-14 新增
        processSelectKeyNodes(id, parameterTypeClass, langDriver);

        // LanguageDriver去解析<select>标签里面的最原始的sql（带<if>,带#{}的sql）
        SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);

        // 属性标记【仅对 insert 有用】, MyBatis 会通过 getGeneratedKeys 或者通过 insert 语句的 selectKey 子元素设置它的值 step-14 新增
        String keyProperty = element.attributeValue("keyProperty");

        KeyGenerator keyGenerator;
        String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);

        if (configuration.hasKeyGenerator(keyStatementId)) {
            keyGenerator = configuration.getKeyGenerator(keyStatementId);
        } else {
            keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
        }

        /*
            MappedStatement mappedStatement = new MappedStatement.Builder(configuration, currentNamespace + "." + id, sqlCommandType, sqlSource, resultTypeClass).build();
             // 添加解析 SQL
            configuration.addMappedStatement(mappedStatement);
            后期的ResultMap封装都写在这里不利用维护。
        */

        // 调用助手类
        builderAssistant.addMappedStatement(id,
                sqlSource,
                sqlCommandType,
                parameterTypeClass,
                resultMap,
                resultTypeClass,
                flushCache,
                useCache,
                keyGenerator,
                keyProperty,
                langDriver);
    }

    private void processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        List<Element> selectKeyNodes = element.elements("selectKey");
        parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver);
    }

    private void parseSelectKeyNodes(String parentId, List<Element> list, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        for (Element nodeToHandle : list) {
            String id = parentId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
            parseSelectKeyNode(id, nodeToHandle, parameterTypeClass, languageDriver);
        }
    }

    /**
     * <selectKey keyProperty="id" order="AFTER" resultType="long">
     * SELECT LAST_INSERT_ID()
     * </selectKey>
     */
    private void parseSelectKeyNode(String id, Element nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        String resultType = nodeToHandle.attributeValue("resultType");
        Class<?> resultTypeClass = resolveClass(resultType);
        boolean executeBefore = "BEFORE".equals(nodeToHandle.attributeValue("order", "AFTER"));
        String keyProperty = nodeToHandle.attributeValue("keyProperty");

        // default
        String resultMap = null;
        boolean flushCache = false;
        boolean useCache = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();

        // 解析成SqlSource，DynamicSqlSource/RawSqlSource
        SqlSource sqlSource = langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        // 调用助手类
        builderAssistant.addMappedStatement(id,
                sqlSource,
                sqlCommandType,
                parameterTypeClass,
                resultMap,
                resultTypeClass,
                flushCache,
                useCache,
                keyGenerator,
                keyProperty,
                langDriver);

        // 给id加上namespace前缀
        id = builderAssistant.applyCurrentNamespace(id, false);

        // 存放键值生成器配置
        MappedStatement keyStatement = configuration.getMappedStatement(id);
        configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement, executeBefore));
    }

}
