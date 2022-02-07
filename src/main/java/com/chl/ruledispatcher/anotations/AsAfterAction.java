package com.chl.ruledispatcher.anotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作为 后置行为
 * @author ccchhhlll1988@163.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsAfterAction {

    /**
     * @return 场景
     */
    String scene();

    /**
     * @return 应用
     */
    String app();
}
