//package com.hnkjzy.order.config;
//
//import com.alibaba.csp.sentinel.slots.block.RuleConstant;
//import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
//import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
//import org.springframework.stereotype.Component;
//
//import jakarta.annotation.PostConstruct;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class SentinelFlowRuleConfig {
//
//    @PostConstruct
//    public void initFlowRules() {
//        List<FlowRule> rules = new ArrayList<>();
//
//        FlowRule rule1 = new FlowRule();
//        rule1.setResource("/order/create");
//        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        rule1.setCount(10);
//        rules.add(rule1);
//
//        FlowRule rule2 = new FlowRule();
//        rule2.setResource("/order/list");
//        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        rule2.setCount(20);
//        rules.add(rule2);
//
//        FlowRule rule3 = new FlowRule();
//        rule3.setResource("/order/list");
//        rule3.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        rule3.setCount(5);
//        rule3.setStrategy(RuleConstant.STRATEGY_RELATE);
//        rule3.setRefResource("/order/create");
//        rules.add(rule3);
//
//        FlowRuleManager.loadRules(rules);
//        System.out.println("[Sentinel] 限流规则已加载");
//    }
//}
