package com.zyh.mybatisframework.session;

import com.zyh.mybatisframework.binding.MapperRegistry;
import com.zyh.mybatisframework.cache.Cache;
import com.zyh.mybatisframework.cache.decorators.FifoCache;
import com.zyh.mybatisframework.cache.impl.PerpetualCache;
import com.zyh.mybatisframework.datasource.druid.DruidDataSourceFactory;
import com.zyh.mybatisframework.datasource.pooled.PooledDataSourceFactory;
import com.zyh.mybatisframework.datasource.unpooled.UnpooledDataSourceFactory;
import com.zyh.mybatisframework.executor.CachingExecutor;
import com.zyh.mybatisframework.executor.Executor;
import com.zyh.mybatisframework.executor.SimpleExecutor;
import com.zyh.mybatisframework.executor.keygen.KeyGenerator;
import com.zyh.mybatisframework.executor.keygen.SelectKeyGenerator;
import com.zyh.mybatisframework.executor.parameter.ParameterHandler;
import com.zyh.mybatisframework.executor.resultset.DefaultResultSetHandler;
import com.zyh.mybatisframework.executor.resultset.ResultSetHandler;
import com.zyh.mybatisframework.executor.statement.PreparedStatementHandler;
import com.zyh.mybatisframework.executor.statement.StatementHandler;
import com.zyh.mybatisframework.mapping.BoundSql;
import com.zyh.mybatisframework.mapping.Environment;
import com.zyh.mybatisframework.mapping.MappedStatement;
import com.zyh.mybatisframework.mapping.result.ResultMap;
import com.zyh.mybatisframework.plugin.Interceptor;
import com.zyh.mybatisframework.plugin.InterceptorChain;
import com.zyh.mybatisframework.plugin.impl.slowsql.SlowSqlLoggerPlugin;
import com.zyh.mybatisframework.reflection.MetaObject;
import com.zyh.mybatisframework.reflection.factory.DefaultObjectFactory;
import com.zyh.mybatisframework.reflection.factory.ObjectFactory;
import com.zyh.mybatisframework.reflection.wrapper.DefaultObjectWrapperFactory;
import com.zyh.mybatisframework.reflection.wrapper.ObjectWrapperFactory;
import com.zyh.mybatisframework.scripting.LanguageDriver;
import com.zyh.mybatisframework.scripting.LanguageDriverRegistry;
import com.zyh.mybatisframework.scripting.xmltags.XMLLanguageDriver;
import com.zyh.mybatisframework.transaction.Transaction;
import com.zyh.mybatisframework.transaction.jdbc.JdbcTransactionFactory;
import com.zyh.mybatisframework.type.TypeAliasRegistry;
import com.zyh.mybatisframework.type.TypeHandlerRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @description: mybatis环境
 * @author：zhanyh
 * @date: 2023/5/25
 */
public class Configuration {
    /**
     * 1 映射注册机
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 2 映射的语句，存在Map里
     * MappedStatement: [select|update|insert|delete]语句的对象化
     * mappedStatements：
     * {"com.zyh.dao.IUserDao.findById" : MappedStatement}
     */
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    /**
     * 3 别名注册机
     */
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    /**
     * 数据源环境
     */
    protected Environment environment;

    /**
     * 保存已经加载过的资源，避免用户冗余配置，造成重复加载
     */
    protected final Set<String> loadedResources = new HashSet<>();

    /**
     * 脚本语言驱动注册机
     */
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    /**
     * 对象工厂和对象包装器工厂
     */
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    // 类型处理器注册机
    protected String databaseId;

    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();


    // 结果映射，存在Map里
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();

    // ste14 获取自增主键
    protected final Map<String, KeyGenerator> keyGenerators = new HashMap<>();

    protected boolean useGeneratedKeys = false;

    // 插件拦截器链
    protected final InterceptorChain interceptorChain = new InterceptorChain();

    // 一级缓存机制，默认不配置的情况是 SESSION
    protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;

    // 二级缓存机制，默认开关打开
    protected boolean cacheEnabled = true;

    // 缓存,存在Map里
    protected final Map<String, Cache> caches = new HashMap<>();


    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);

        typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
        typeAliasRegistry.registerAlias("FIFO", FifoCache.class);


        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    }

    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 创建结果集处理器——————后期插件拓展的位置之一
     * 创建结果集处理器, 暂时只用到了BoundSql中的返回值类型
     */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor, mappedStatement, resultHandler, rowBounds, boundSql);
    }

    /**
     * step16拓展
     * 创建语句处理器——————插件拓展的位置之一
     * 默认使用 PreparedStatement
     * SQL语句处理器， 包括sql的执行 和 sql的占位符的参数值替换 和 获取sql的结果
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // 创建语句处理器，Mybatis 这里加了路由 STATEMENT、PREPARED、CALLABLE 我们默认只根据预处理进行实例化
        StatementHandler statementHandler = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        // 嵌入插件，代理对象
        statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
        return statementHandler;
    }

    /**
     * 生产执行器——————后期插件拓展的位置之一
     *
     * @param transaction 事务
     * @return 执行器
     */
    public Executor newExecutor(Transaction transaction) {
        Executor executor = new SimpleExecutor(this, transaction);
        // 配置开启缓存，创建 CachingExecutor(默认就是有缓存)装饰者模式
        if (cacheEnabled) {
            executor = new CachingExecutor(executor);
        }
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
    }

    /**
     * 参数处理器——————后期插件拓展的位置之一
     *
     * @param mappedStatement
     * @param parameterObject
     * @param boundSql
     * @return
     */
    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        /** TODO: 理解 为什么不直接 new DefaultParameterHandler(ms, parameterObject, boundSql) */
        ParameterHandler parameterHandler = mappedStatement.getLanguageDriver().createParameterHandler(mappedStatement, parameterObject, boundSql);
        // 插件的一些参数，也是在这里处理，暂时不添加这部分内容 interceptorChain.pluginAll(parameterHandler);
        return parameterHandler;
    }

    /**
     * 加载某个mapper资源
     *
     * @param resource mapper资源
     */
    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    /**
     * 检查是否加载过某个mapper资源
     *
     * @param resource mapper资源
     * @return true加载过， false没加载过
     */
    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }


    /**
     * 使用默认的对象创建工厂和包装工厂 创建元对象
     *
     * @param object 源对象
     * @return 元对象
     */
    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory);
    }

    /**
     * @return 返回SQL语句的所属数据库类型
     */
    public Object getDatabaseId() {
        return databaseId;
    }


    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }


    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public void addResultMap(ResultMap resultMap) {
        resultMaps.put(resultMap.getId(), resultMap);
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public boolean hasKeyGenerator(String keyStatementId) {
        return keyGenerators.containsKey(keyStatementId);
    }

    public KeyGenerator getKeyGenerator(String keyStatementId) {
        return keyGenerators.get(keyStatementId);
    }

    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public void addKeyGenerator(String id, SelectKeyGenerator selectKeyGenerator) {

        keyGenerators.put(id, selectKeyGenerator);
    }

    /**
     * 往拦截器链中添加拦截器
     *
     * @param interceptorInstance 用户自定义拦截器实例
     */
    public void addInterceptor(Interceptor interceptorInstance) {
        interceptorChain.addInterceptor(interceptorInstance);
    }

    public void setLocalCacheScope(LocalCacheScope localCacheScope) {
        this.localCacheScope = localCacheScope;
    }

    public LocalCacheScope getLocalCacheScope() {
        return localCacheScope;
    }

    public void setCacheEnabled(Boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public void addCache(Cache cache) {
        caches.put(cache.getId(), cache);
    }


    /* slow sql logger code start */
    private boolean slowSqlEnabled = false;

    public boolean getSlowSqlEnabled() {
        return slowSqlEnabled;
    }

    public String getSlowSqlLogPath() {
        return slowSqlLogPath;
    }

    public int getSlowSqlTimeLimit() {
        return slowSqlTimeLimit;
    }

    private String slowSqlLogPath;
    private int slowSqlTimeLimit = 2000;

    public void setSlowSqlEnabled(Boolean enabled) {
        slowSqlEnabled = enabled;
        if(slowSqlEnabled){
            this.addInterceptor(new SlowSqlLoggerPlugin());
        }
    }

    public void setSlowSqlLogPath(String sqlLogPath) {
        slowSqlLogPath = sqlLogPath;
    }

    public void setSlowSqlTimeLimit(int timeLimit) {
        slowSqlTimeLimit = timeLimit;
    }
    /* slow sql logger code end */
}
