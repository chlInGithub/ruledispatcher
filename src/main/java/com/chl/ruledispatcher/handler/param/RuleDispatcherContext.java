package com.chl.ruledispatcher.handler.param;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则调度参数与过程上下文，需要根据场景+应用提供具体的实现类。
 * @author ccchhhlll1988@163.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleDispatcherContext implements Serializable {

    private static final long serialVersionUID = -5135038735923083684L;

    /**
     * 场景
     */
    String scene;

    /**
     * 应用
     */
    String app;

    /**
     * 特定规则
     */
    String rule;
}
