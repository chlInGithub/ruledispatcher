package com.chl.ruledispatcher.handler;

import java.util.HashMap;
import java.util.Map;

import com.chl.ruledispatcher.anotations.AsAfterAction;
import com.chl.ruledispatcher.handler.param.RuleDispatcherContext;

/**
 * 后置行为
 * @author ccchhhlll1988@163.com
 */
public abstract class AfterAction<T extends RuleDispatcherContext> {
    AsAfterAction asAfterAction;

    /**
     * 可以将处理结果放在context中
     * @param ruleDispatcherContext
     */
    abstract protected void handle(T ruleDispatcherContext);

    @Override
    public String toString() {
        Map<String, String> map = new HashMap<>();
        map.put("className", this.getClass().getSimpleName());
        map.put("asAfterAction", asAfterAction.toString());
        return map.toString();
    }
}
