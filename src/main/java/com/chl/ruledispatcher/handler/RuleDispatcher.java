package com.chl.ruledispatcher.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.chl.ruledispatcher.anotations.AsAfterAction;
import com.chl.ruledispatcher.anotations.AsPreAction;
import com.chl.ruledispatcher.anotations.AsRuleHandler;
import com.chl.ruledispatcher.handler.param.RuleDispatcherContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.CollectionUtils;

/**
 * 规则调度器
 * <br/>
 * <b>组装，具有特定注解的bean，组装场景维度的控制器组。</b>
 * <br/>
 * <b>处理流程：</b>
 * <ul>
 *     <li>场景+app维度，执行前置处理</li>
 *     <li>规则处理</li>
 *     <li>场景+app维度，执行后置处理</li>
 * </ul>
 *
 * @author ccchhhlll1988@163.com
 */
@Slf4j
public class RuleDispatcher {

    /**
     * 获取当前配置的控制器关系
     */
    public static String getNodes(){
        return sceneHandlerMap.toString();
    }

    /**
     * 调度
     */
    public static void dispatch(RuleDispatcherContext ruleDispatcherContext) {
        if (log.isInfoEnabled()) {
            log.info("RuleDispatcher dispatch {}", ruleDispatcherContext);
        }

        if (isEmpty(ruleDispatcherContext.getScene()) || isEmpty(ruleDispatcherContext.getApp())) {
            throw new RuntimeException("must set scene and app");
        }

        boolean isReturn = doPreAction(ruleDispatcherContext);
        if (log.isInfoEnabled()) {
            log.info("RuleDispatcher dispatch after doPreAction {} {}", ruleDispatcherContext, isReturn);
        }

        if (isReturn) {
            return;
        }

        doRuleHandler(ruleDispatcherContext);
        if (log.isInfoEnabled()) {
            log.info("RuleDispatcher dispatch after doRuleHandler {}", ruleDispatcherContext);
        }

        doAfterAction(ruleDispatcherContext);
        if (log.isInfoEnabled()) {
            log.info("RuleDispatcher dispatch after doAfterAction {}", ruleDispatcherContext);
        }
    }

    /**
     * 执行规则链
     * @param ruleDispatcherContext
     */
    private static void doRuleHandler(RuleDispatcherContext ruleDispatcherContext) {
        String scene = ruleDispatcherContext.getScene();
        String app = ruleDispatcherContext.getApp();
        String rule = ruleDispatcherContext.getRule();

        SceneAppHandlerNode sceneAppHandlerNode = getSceneAppHandlerNode(scene, app);
        if (null == sceneAppHandlerNode){
            return;
        }

        Map<String, RuleHandler> ruleHandlerMap = sceneAppHandlerNode.ruleHandlerMap;
        List<RuleHandler> ruleHandlers = sceneAppHandlerNode.ruleHandlers;
        if (isEmpty(ruleHandlerMap) || isEmpty(ruleHandlers)) {
            return;
        }

        List<RuleHandler> ruleHandlersTemp = ruleHandlers;

        // 执行精确rule
        if (!isEmpty(rule)) {
            RuleHandler ruleHandler = ruleHandlerMap.get(rule);
            ruleHandlersTemp = new ArrayList<>();
            ruleHandlersTemp.add(ruleHandler);
        }

        for (RuleHandler ruleHandler : ruleHandlersTemp) {
            boolean matched = ruleHandler.handle(ruleDispatcherContext);
            if (matched) {
                if (log.isInfoEnabled()) {
                    log.info("doRuleHandler hitRule {} {} {} {}", scene, app, ruleHandler.asRuleHandler.rule(), ruleDispatcherContext);
                }
                // 排他性规则
                boolean exclusive = ruleHandler.asRuleHandler.exclusive();
                if (exclusive) {
                    break;
                }
            }
        }
    }

    static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    static boolean isEmpty(String str) {
        return (str == null || "".equals(str));
    }

    /**
     * 执行 前置行为
     * @param ruleDispatcherContext
     * @return 是否return
     */
    private static boolean doPreAction(RuleDispatcherContext ruleDispatcherContext) {
        String scene = ruleDispatcherContext.getScene();
        String app = ruleDispatcherContext.getApp();
        SceneAppHandlerNode sceneAppHandlerNode = getSceneAppHandlerNode(scene, app);
        if (null == sceneAppHandlerNode){
            return false;
        }

        if (CollectionUtils.isEmpty(sceneAppHandlerNode.preActions)) {
            return false;
        }

        for (PreAction preAction : sceneAppHandlerNode.preActions) {
            boolean isReturn = preAction.handle(ruleDispatcherContext);
            if (isReturn) {
                return true;
            }
        }

        return false;
    }

    /**
     * 执行 后置行为
     * @param ruleDispatcherContext
     */
    private static void doAfterAction(RuleDispatcherContext ruleDispatcherContext) {
        String scene = ruleDispatcherContext.getScene();
        String app = ruleDispatcherContext.getApp();
        SceneAppHandlerNode sceneAppHandlerNode = getSceneAppHandlerNode(scene, app);
        if (null == sceneAppHandlerNode){
            return;
        }

        if (CollectionUtils.isEmpty(sceneAppHandlerNode.afterActions)) {
            return;
        }

        for (AfterAction afterAction : sceneAppHandlerNode.afterActions) {
            afterAction.handle(ruleDispatcherContext);
        }
    }

    private static SceneAppHandlerNode getSceneAppHandlerNode(String scene, String appType) {
        SceneHandlerNode sceneHandlerNode = sceneHandlerMap.get(scene);

        SceneAppHandlerNode sceneAppHandlerNode = sceneHandlerNode.appHandlerMap.get(appType);
        if (null == sceneAppHandlerNode) {
            log.error("RuleDispatcher noSceneAppHandlerNode {} {} {}", scene, appType, sceneHandlerNode.appHandlerMap.toString());
            throw new RuntimeException("RuleDispatcher noSceneAppHandlerNode " + scene + " " + appType);
        }

        if (CollectionUtils.isEmpty(sceneAppHandlerNode.preActions)) {
            if (log.isInfoEnabled()) {
                log.info("RuleDispatcher noPreAction {} {} {}", scene, appType, sceneAppHandlerNode);
            }
        }

        if (CollectionUtils.isEmpty(sceneAppHandlerNode.afterActions)) {
            if (log.isInfoEnabled()) {
                log.info("RuleDispatcher noAfterAction {} {} {}", scene, appType, sceneAppHandlerNode);
            }
        }

        if (CollectionUtils.isEmpty(sceneAppHandlerNode.ruleHandlers)) {
            if (log.isInfoEnabled()) {
                log.info("RuleDispatcher noRuleHandlers {} {} {}", scene, appType, sceneAppHandlerNode);
            }
        }

        return sceneAppHandlerNode;
    }

    /**
     * 一次性整理控制器关系
     * @param beanFactory
     */
    public static synchronized void arrange(ConfigurableListableBeanFactory beanFactory){
        if (arranged){
            return;
        }

        Map<String, Object> beansWithAsPreActionAnnotation = beanFactory.getBeansWithAnnotation(AsPreAction.class);
        for (Object beanObject : beansWithAsPreActionAnnotation.values()) {
            if (beanObject instanceof PreAction){
                PreAction preAction = (PreAction) beanObject;
                AsPreAction annotation = preAction.getClass().getAnnotation(AsPreAction.class);
                preAction.asPreAction = annotation;
                addPreAction(preAction);
            }
        }

        Map<String, Object> beansWithAsRuleHanlerAnnotation = beanFactory.getBeansWithAnnotation(AsRuleHandler.class);
        for (Object beanObject : beansWithAsRuleHanlerAnnotation.values()) {
            if (beanObject instanceof RuleHandler){
                RuleHandler handler = (RuleHandler) beanObject;
                AsRuleHandler annotation = beanObject.getClass().getAnnotation(AsRuleHandler.class);
                handler.asRuleHandler = annotation;
                addRuleHandler(handler);
            }
        }

        Map<String, Object> beansWithAsAfterActionAnnotation = beanFactory
                .getBeansWithAnnotation(AsAfterAction.class);
        for (Object beanObject : beansWithAsAfterActionAnnotation.values()) {
            if (beanObject instanceof AfterAction){
                AfterAction afterAction = (AfterAction) beanObject;
                AsAfterAction annotation = beanObject.getClass().getAnnotation(AsAfterAction.class);
                afterAction.asAfterAction = annotation;
                addAfterAction(afterAction);
            }
        }

        arranged = true;
    }

    private static Boolean arranged = false;
    /**
     * 不需要考虑并发
     */
    private static Map<String, SceneHandlerNode> sceneHandlerMap = new HashMap<>();

    /**
     * 添加 前置处理；若已存在前置处理，则忽略本次配置
     * @param preAction
     */
    private static void addPreAction(PreAction preAction) {
        String app = preAction.asPreAction.app();
        String scene = preAction.asPreAction.scene();
        String rule = preAction.asPreAction.rule();

        initNode(app, scene);

        SceneAppHandlerNode sceneAppHandlerNode = sceneHandlerMap.get(scene).appHandlerMap.get(app);
        if (!sceneAppHandlerNode.preActionMap.containsKey(rule)) {
            sceneAppHandlerNode.preActionMap.put(rule, preAction);
            sceneAppHandlerNode.preActions.add(preAction);
            sceneAppHandlerNode.preActions.sort(PreAction.comparator);
            log.info("RuleDispatcher add preAction for {} {} {} ", scene, app, rule);
        }
    }

    /**
     * 添加 后置处理；若已存在后置处理，则忽略本次配置
     * @param afterAction
     */
    private static void addAfterAction(AfterAction afterAction) {
        String app = afterAction.asAfterAction.app();
        String scene = afterAction.asAfterAction.scene();
        String rule = afterAction.asAfterAction.rule();

        initNode(app, scene);

        SceneAppHandlerNode sceneAppHandlerNode = sceneHandlerMap.get(scene).appHandlerMap.get(app);
        if (!sceneAppHandlerNode.afterActionMap.containsKey(rule)) {
            sceneAppHandlerNode.afterActionMap.put(rule, afterAction);
            sceneAppHandlerNode.afterActions.add(afterAction);
            sceneAppHandlerNode.afterActions.sort(AfterAction.comparator);
            log.info("RuleDispatcher add afterAction for {} {} {} ", scene, app, rule);
        }
    }

    /**
     * 添加 规则控制器 根据优先级排序
     * @param handler
     */
    private static void addRuleHandler(RuleHandler handler) {
        String app = handler.asRuleHandler.app();
        String scene = handler.asRuleHandler.scene();
        String rule = handler.asRuleHandler.rule();

        initNode(app, scene);

        SceneAppHandlerNode sceneAppHandlerNode = sceneHandlerMap.get(scene).appHandlerMap.get(app);
        if (!sceneAppHandlerNode.ruleHandlerMap.containsKey(rule)) {
            sceneAppHandlerNode.ruleHandlerMap.put(rule, handler);
            sceneAppHandlerNode.ruleHandlers.add(handler);
            sceneAppHandlerNode.ruleHandlers.sort(RuleHandler.comparator);
            log.info("RuleDispatcher add ruleHandler for {} {} {} ", scene, app, rule);
        }
    }

    private static void initNode(String app, String scene) {
        if (!sceneHandlerMap.containsKey(scene)) {
            SceneHandlerNode sceneHandlerNode = new SceneHandlerNode();
            sceneHandlerNode.scene = scene;
            sceneHandlerMap.put(scene, sceneHandlerNode);
            log.info("RuleDispatcher add SceneHandlerNode for {} ", scene);
        }

        SceneHandlerNode sceneHandlerNode = sceneHandlerMap.get(scene);
        if (!sceneHandlerNode.appHandlerMap.containsKey(app)) {
            SceneAppHandlerNode sceneAppHandlerNode = new SceneAppHandlerNode();
            sceneAppHandlerNode.scene = scene;
            sceneAppHandlerNode.app = app;
            sceneHandlerNode.appHandlerMap.put(app, sceneAppHandlerNode);
            log.info("RuleDispatcher add SceneAppHandlerNode for {} {} ", scene, app);
        }
    }

    /**
     * scene维度，封装app控制器集合
     */
    static class SceneHandlerNode{
        String scene;

        Map<String, SceneAppHandlerNode> appHandlerMap = new HashMap<>();

        @Override
        public String toString() {
            Map<String, String> map = new HashMap<>();
            map.put("scene", scene);
            map.put("appHandlerMap", appHandlerMap.toString());
            return map.toString();
        }
    }

    /**
     * scene + app 维度，封装控制器集合
     */
    static class SceneAppHandlerNode{
        String scene;
        String app;

        /**
         * 前置行为
         */
        //PreAction preAction;
        Map<String, PreAction> preActionMap = new HashMap<>();
        List<PreAction> preActions = new LinkedList<>();

        /**
         * 规则维度，规则控制器
         */
        Map<String, RuleHandler> ruleHandlerMap = new HashMap<>();

        /**
         * 按照优先级正序，即优先级数字越小越靠前
         */
        List<RuleHandler> ruleHandlers = new LinkedList<>();

        /**
         * 后置行为
         */
        //AfterAction afterAction;
        Map<String, AfterAction> afterActionMap = new HashMap<>();
        List<AfterAction> afterActions = new LinkedList<>();

        @Override
        public String toString() {
            Map<String, String> map = new HashMap<>();
            map.put("scene", scene);
            map.put("app", app);

            Map<String, String> preActionMap = new HashMap<>();
            for (PreAction ruleHandler : preActions) {
                preActionMap.put(ruleHandler.asPreAction.rule(), ruleHandler.toString());
            }
            map.put("preAction", preActionMap.toString());

            Map<String, String> ruleMap = new HashMap<>();
            for (RuleHandler ruleHandler : ruleHandlers) {
                ruleMap.put(ruleHandler.asRuleHandler.rule(), ruleHandler.toString());
            }
            map.put("rules", ruleMap.toString());

            Map<String, String> afterActionMap = new HashMap<>();
            for (AfterAction ruleHandler : afterActions) {
                afterActionMap.put(ruleHandler.asAfterAction.rule(), ruleHandler.toString());
            }
            map.put("afterAction", afterActionMap.toString());

            return map.toString();
        }
    }
}
