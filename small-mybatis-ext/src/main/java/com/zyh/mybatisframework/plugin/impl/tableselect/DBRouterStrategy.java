package com.zyh.mybatisframework.plugin.impl.tableselect;

import java.lang.annotation.*;

/**
 * @description: 路由策略，分表标记
 * @author：zhanyh
 * @date: 2023/6/4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterStrategy {

    boolean splitTable() default true;

}
