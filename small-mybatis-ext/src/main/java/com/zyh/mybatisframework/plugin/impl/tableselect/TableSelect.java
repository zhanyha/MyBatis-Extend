package com.zyh.mybatisframework.plugin.impl.tableselect;

import java.lang.annotation.*;

/**
 * @description: 根据key计算要路由的表号
 * @author：zhanyh
 * @date: 2023/6/4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TableSelect {

    /** 分库分表字段 */
    String key() default "";

}
