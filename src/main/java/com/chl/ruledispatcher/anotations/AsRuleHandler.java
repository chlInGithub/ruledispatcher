package com.chl.ruledispatcher.anotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作为 规则处理器
 * @author ccchhhlll1988@163.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsRuleHandler {
    /**
     * @return 场景
     */
    String scene();

    /**
     * @return 应用
     */
    String app();

    /**
     * @return 规则
     */
    String rule() default "default";

    /**
     * @return 优先级 越小优先级越高,默认50.
     */
    int priority() default 50;

    /**
     * @return 排他性，命中后不再匹配后续规则处理器，默认false，即继续后续规则匹配
     */
    boolean exclusive() default false;
}
