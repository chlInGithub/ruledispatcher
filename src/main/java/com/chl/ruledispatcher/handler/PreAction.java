package com.chl.ruledispatcher.handler;

import java.util.HashMap;
import java.util.Map;

import com.chl.ruledispatcher.anotations.AsPreAction;
import com.chl.ruledispatcher.handler.param.RuleDispatcherContext;

/**
 * 前置行为
 * @author ccchhhlll1988@163.com
 */
public abstract class PreAction<T extends RuleDispatcherContext> {
    AsPreAction asPreAction;

    /**
     * 可将结果放在context
     * @param ruleDispatcherContext
     * @return true 立即返回
     */
    abstract protected boolean handle(T ruleDispatcherContext);

    @Override
    public String toString() {
        Map<String, String> map = new HashMap<>();
        map.put("className", this.getClass().getSimpleName());
        map.put("asPreAction", asPreAction.toString());
        return map.toString();
    }
}
