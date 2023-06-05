package com.zyh.mybatisframework.builder.xml;

import com.zyh.mybatisframework.builder.BaseBuilder;
import com.zyh.mybatisframework.datasource.DataSourceFactory;
import com.zyh.mybatisframework.io.Resources;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.Environment;
import com.zyh.mybatisframework.mapping.MappedStatement;
import com.zyh.mybatisframework.mapping.SqlCommandType;
import com.zyh.mybatisframework.plugin.Interceptor;
import com.zyh.mybatisframework.session.Configuration;
import com.zyh.mybatisframework.session.LocalCacheScope;
import com.zyh.mybatisframework.transaction.TransactionFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: mybatis XML mapper XMl的解析类————XML配置构建器， 建造者模式
 * @author：zhanyh
 * @date: 2023/5/25
 */
public class XMLConfigBuilder extends BaseBuilder {
    private Element root;

    public XMLConfigBuilder(Reader reader) {
        // 1. 调用父类初始化Configuration
        super(new Configuration());
        // 2. dom4j 处理 xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(new InputSource(reader));
            root = document.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析配置；类型别名、插件、对象工厂、对象包装工厂、设置、环境、类型转换、映射器
     *
     * @return mybatis的环境——————Configuration
     */
    public Configuration parse() {
        try {
            // 1. 解析环境
            environmentsElement(root.element("environments"));

            // 2. 解析配置
            settingsElement(root.element("settings"));

            // 3. 解析插件
            pluginElement(root.element("plugins"));
            // 4. 解析映射器
            mapperElement(root.element("mappers"));

        } catch (Exception e) {
            throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
        return configuration;
    }

    private void settingsElement(Element context) {
        if (context == null) return;
        List<Element> elements = context.elements();
        Properties props = new Properties();
        for (Element element : elements) {
            props.setProperty(element.attributeValue("name"), element.attributeValue("value"));
        }
        // 一级缓存
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope")));

        // 二级缓存
        configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));

        // 慢sql日志
        configuration.setSlowSqlEnabled(booleanValueOf(props.getProperty("slowSqlEnabled"), false));
        configuration.setSlowSqlLogPath(props.getProperty("slowSqlLogPath"));
        configuration.setSlowSqlTimeLimit(intValueOf(Integer.valueOf(props.getProperty("timeLimit")), 2000));
    }



    /**
     * <plugins>
     * <plugin interceptor="cn.bugstack.mybatis.test.plugin.TestPlugin">
     * <property name="test00" value="100"/>
     * <property name="test01" value="100"/>
     * </plugin>
     * </plugins>
     */
    private void pluginElement(Element parent) throws Exception {
        // 1. 获取dom4j处理的<plugins>标签内容被包装为DefaultElement对象
        if (parent == null) return;
        List<Element> elements = parent.elements();
        for (Element element : elements) {
            String interceptor = element.attributeValue("interceptor");
            // 参数配置
            Properties properties = new Properties();
            List<Element> propertyElementList = element.elements("property");
            for (Element property : propertyElementList) {
                properties.setProperty(property.attributeValue("name"), property.attributeValue("value"));
            }
            // 获取插件实现类并实例化：cn.zyh.mybatis.test.plugin.TestPlugin
            Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
            interceptorInstance.setProperties(properties);
            configuration.addInterceptor(interceptorInstance);
        }
    }

    private void environmentsElement(Element context) throws Exception {
        /**
         * 解析<environments>
         *
         *     <environments default="development">
         *         <environment id="development">
         *             <transactionManager type="JDBC"/>
         *             <dataSource type="DRUID">
         *                 <property name="driver" value="com.mysql.jdbc.Driver"/>
         *                 <property name="url" value="jdbc:mysql://127.0.0.1:3306/mybatis?useUnicode=true"/>
         *                 <property name="username" value="root"/>
         *                 <property name="password" value="123456"/>
         *             </dataSource>
         *         </environment>
         *     </environments>
         */
        String environment = context.attributeValue("default");
        List<Element> environmentList = context.elements("environment");
        for (Element e : environmentList) {
            String id = e.attributeValue("id");
            if (environment.equals(id)) {
                // 1 事务管理器
                String transactionManagerType = e.element("transactionManager").attributeValue("type");
                Class<Object> transactionManagerClass = typeAliasRegistry.resolveAlias(transactionManagerType);
                TransactionFactory txFactory = (TransactionFactory) transactionManagerClass.newInstance();

                // 2 数据源
                //  2.1 数据源类型 c3p0 druid ...
                Element dataSourceElement = e.element("dataSource");
                String dataSourceType = dataSourceElement.attributeValue("type");
                Class<Object> dataSourceClass = typeAliasRegistry.resolveAlias(dataSourceType);
                DataSourceFactory dataSourceFactory = (DataSourceFactory) dataSourceClass.newInstance();

                List<Element> propertyList = dataSourceElement.elements("property");
                Properties props = new Properties();
                for (Element property : propertyList) {
                    props.setProperty(property.attributeValue("name"), property.attributeValue("value"));
                }
                dataSourceFactory.setProperties(props);
                DataSource dataSource = dataSourceFactory.getDataSource();

                // 3 构建环境
                Environment.Builder environmentBuilder = new Environment.Builder(id)
                        .transactionFactory(txFactory)
                        .dataSource(dataSource);
                configuration.setEnvironment(environmentBuilder.build());
            }
        }

    }

    private void mapperElement(Element mappers) throws Exception {
        List<Element> mapperList = mappers.elements("mapper");
        for (Element e : mapperList) {
            String resource = e.attributeValue("resource");
            String mapperClass = e.attributeValue("class");

            // XML 解析
            if (resource != null && mapperClass == null) {
                InputStream inputStream = Resources.getResourceAsStream(resource);

                // 在for循环里每个mapper都重新new一个XMLMapperBuilder，来解析
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource);
                mapperParser.parse();
            }
            // 注解解析
            else if (resource == null && mapperClass != null) {
                Class<?> mapperInterface = Resources.classForName(mapperClass);
                configuration.addMapper(mapperInterface);
            }
        }
    }


//   /*******************  把mapper的解析交给XMLMapperBuilder *************************/
//    private void mapperElement(Element mappers) throws Exception {
//        /**
//         * 解析<mapper>
//         * <mappers>
//         *     <mapper resource="mapper/User_Mapper.xml"/>
//         *     <mapper resource="mapper/Order_Mapper.xml"/>
//         *     <mapper resource="mapper/Goods_Mapper.xml"/>
//         * </mappers>
//         */
//        List<Element> mapperList = mappers.elements("mapper");
//        for (Element e : mapperList) {
//            // 1. mapper.xml的类路径
//            String resource = e.attributeValue("resource");
//            // 2. 转成字符流
//            Reader reader = Resources.getResourceAsReader(resource);
//            // 3. 解析XXX_Mapper.xml
//            SAXReader saxReader = new SAXReader();
//            Document document = saxReader.read(new InputSource(reader));
//            // 4. 获取mapper的Element对象
//            Element root = document.getRootElement();
//
//            // 5. 获取命名空间
//            String namespace = root.attributeValue("namespace");
//
//            // 6. 获取mapper下的所有SELECT SQL语句
//            List<Element> selectNodes = root.elements("select");
//
//            for (Element node : selectNodes) {
//                String id = node.attributeValue("id");
//                String parameterType = node.attributeValue("parameterType");
//                String resultType = node.attributeValue("resultType");
//                String sql = node.getText();
//
//                // 7. ? 匹配
//                Map<Integer, String> parameter = new HashMap<>();
//                Pattern pattern = Pattern.compile("(#\\{(.*?)})");
//                Matcher matcher = pattern.matcher(sql);
//                for (int i = 1; matcher.find(); i++) {
//                    String g1 = matcher.group(1);
//                    String g2 = matcher.group(2);
//                    parameter.put(i, g2);
//                    sql = sql.replace(g1, "?");
//                }
//
//                String msId = namespace + "." + id;
//                String nodeName = node.getName();
//                // 8. 获取SQL语句类型
//                SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
//                /**
//                 * <select id="queryUserInfoById" parameterType="java.lang.Long" resultType="cn.bugstack.mybatis.test.po.User">
//                 *         SELECT id, userId, userHead, createTime
//                 *         FROM user
//                 *         where id = #{id}
//                 * </select>
//                 * 上面所有的操作相当于解析了一个<select><update><insert><delete>
//                 */
//                // 9. [select|insert|update|delete]封装成一个MappedStatement对象
//                // 9.1  封装BoundSql
//                BoundSql boundSql = new BoundSql(sql, parameter, parameterType, resultType);
//                MappedStatement mappedStatement = new MappedStatement.Builder(
//                        configuration, msId, sqlCommandType, boundSql
//                ).build();
//                // 添加解析 SQL
//                configuration.addMappedStatement(mappedStatement);
//            }
//            // 注册Mapper映射器
//            configuration.addMapper(Resources.classForName(namespace));
//        }
//
//    }
}
