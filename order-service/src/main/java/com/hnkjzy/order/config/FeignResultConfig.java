package com.hnkjzy.order.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Feign 客户端配置 — 自动解包 Result<T> 响应
 * dish-service 返回 {code, message, data:{...}}，Feign 需要提取 data 字段
 */
public class FeignResultConfig {

    @Bean
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new ResultUnwrapDecoder(new SpringDecoder(messageConverters));
    }

    /**
     * 自定义解码器：如果响应体是 Result 包装格式 {code, data}，自动提取 data
     */
    static class ResultUnwrapDecoder implements Decoder {

        private final Decoder delegate;
        private final ObjectMapper objectMapper = new ObjectMapper();

        ResultUnwrapDecoder(Decoder delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object decode(Response response, Type type) throws IOException, FeignException {
            // 读取响应体
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));

            try {
                JsonNode root = objectMapper.readTree(body);

                // 判断是否为 Result 包装格式 {code, data}
                if (root.has("code") && root.has("data") && root.get("data") != null && !root.get("data").isNull()) {
                    // 提取 data 字段，构造新响应传给 SpringDecoder
                    String dataJson = objectMapper.writeValueAsString(root.get("data"));
                    Response unwrappedResponse = response.toBuilder()
                            .body(dataJson, StandardCharsets.UTF_8)
                            .build();
                    return delegate.decode(unwrappedResponse, type);
                }
            } catch (Exception e) {
                // 解析失败，回退到原始处理
            }

            // 非包装格式，直接使用原始解码器
            Response reBuffered = response.toBuilder()
                    .body(body, StandardCharsets.UTF_8)
                    .build();
            return delegate.decode(reBuffered, type);
        }
    }
}
