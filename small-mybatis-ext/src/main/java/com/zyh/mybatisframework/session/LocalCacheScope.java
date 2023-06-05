package com.zyh.mybatisframework.session;

/**
 * @description: 本地缓存机制；作用级别
 *               SESSION 默认值，缓存一个会话中执行的所有查询
 *               STATEMENT 本地会话仅用在语句执行上，对相同 SqlSession 的不同调用将不做数据共享
 * @author：zhanyh
 * @date: 2023/6/1
 */
public enum LocalCacheScope {

    SESSION,

    STATEMENT
}
