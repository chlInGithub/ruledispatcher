package com.chl.ruledispatcher.handler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.chl.ruledispatcher.anotations.AsRuleHandler;
import com.chl.ruledispatcher.handler.param.RuleDispatcherContext;

/**
 * 规则控制器
 * @author ccchhhlll1988@163.com
 */
public abstract class RuleHandler<T extends RuleDispatcherContext> {
    AsRuleHandler asRuleHandler;

    /**
     * 命中规则与执行
     * @return true 规则命中
     */
    boolean handle(T param){
        boolean match = match(param);
        if (match) {
            action(param);
        }
        return match;
    }

    /**
     * 命中规则
     * @param param
     * @return
     */
    abstract protected boolean match(T param);

    /**
     * 规则命中后的行为
     * @param param
     */
    abstract protected void action(T param);


    static Comparator<RuleHandler> comparator = new Comparator<RuleHandler>() {

        @Override
        public int compare(RuleHandler o1, RuleHandler o2) {
            return o1.asRuleHandler.priority() < o2.asRuleHandler.priority() ? -1 : 1;
        }
    };

    @Override
    public String toString() {
        Map<String, String> map = new HashMap<>();
        map.put("className", this.getClass().getSimpleName());
        map.put("asRuleHandler", asRuleHandler.toString());
        return map.toString();
    }
}
