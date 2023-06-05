package com.zyh.mybatisframework.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @description: 拦截器链
 * @author：zhanyh
 * @date: 2023/6/1
 */
public class InterceptorChain {

    private final List<Interceptor> interceptors = new ArrayList<>();


    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public List<Interceptor> getInterceptors(){
        return Collections.unmodifiableList(interceptors);
    }

    public Object pluginAll(Object target) {
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }
}

