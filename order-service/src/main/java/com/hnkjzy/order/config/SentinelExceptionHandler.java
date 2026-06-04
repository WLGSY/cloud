//package com.hnkjzy.order.config;
//
//import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
////import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
//import com.alibaba.csp.sentinel.slots.block.BlockException;
//import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
//import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
//import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
//import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.stereotype.Component;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//public class SentinelExceptionHandler implements BlockExceptionHandler {
//
//    @Override
//    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws IOException {
//        response.setContentType("application/json;charset=utf-8");
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("code", 429);
//
//        if (e instanceof FlowException) {
//            result.put("msg", "系统繁忙，请稍后再试（限流）");
//        } else if (e instanceof DegradeException) {
//            result.put("msg", "服务暂时不可用，请稍后再试（熔断）");
//        } else if (e instanceof ParamFlowException) {
//            result.put("msg", "热点参数限流");
//        } else if (e instanceof AuthorityException) {
//            result.put("msg", "授权规则触发");
//        } else {
//            result.put("msg", "系统繁忙，请稍后再试");
//        }
//
//        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
//    }
//}
