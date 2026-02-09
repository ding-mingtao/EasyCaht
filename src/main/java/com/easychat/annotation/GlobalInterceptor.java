package com.easychat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: GlobalInterceptor
 * @Description: 全局拦截器注解
 * @Author: 丁铭涛
 * @DateTime: 2025/4/12 13:45
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInterceptor {
    boolean checkLogin() default true;
    boolean checkAdmin() default false;
}
