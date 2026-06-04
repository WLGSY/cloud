package com.hnkjzy.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String ORDER_CREATE_QUEUE = "order.create.queue";
    public static final String ORDER_PAY_QUEUE = "order.pay.queue";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ROUTING_KEY_CREATE = "order.create";
    public static final String ROUTING_KEY_PAY = "order.pay";
    public static final String ROUTING_KEY_CANCEL = "order.cancel";
    public static final String DEAD_EXCHANGE = "dead.exchange";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";
    public static final String DEAD_ROUTING_KEY = "dead.letter";

    @Bean
    public DirectExchange deadExchange() {
        log.info("创建死信交换机: {}", DEAD_EXCHANGE);
        return ExchangeBuilder.directExchange(DEAD_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        log.info("创建死信队列: {}", DEAD_LETTER_QUEUE);
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadExchange()).with(DEAD_ROUTING_KEY);
    }

    @Bean
    public Queue orderCreateQueue() {
        log.info("声明订单创建队列: {}", ORDER_CREATE_QUEUE);
        return QueueBuilder.durable(ORDER_CREATE_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderPayQueue() {
        log.info("声明订单支付队列: {}", ORDER_PAY_QUEUE);
        return QueueBuilder.durable(ORDER_PAY_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderCancelQueue() {
        log.info("声明订单取消队列: {}", ORDER_CANCEL_QUEUE);
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange orderExchange() {
        log.info("声明订单交换机: {}", ORDER_EXCHANGE);
        return ExchangeBuilder.topicExchange(ORDER_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding orderCreateBinding() {
        log.info("绑定队列到交换机: {} -> {}", ORDER_EXCHANGE, ORDER_CREATE_QUEUE);
        return BindingBuilder.bind(orderCreateQueue()).to(orderExchange()).with(ROUTING_KEY_CREATE);
    }

    @Bean
    public Binding orderPayBinding() {
        log.info("绑定队列到交换机: {} -> {}", ORDER_EXCHANGE, ORDER_PAY_QUEUE);
        return BindingBuilder.bind(orderPayQueue()).to(orderExchange()).with(ROUTING_KEY_PAY);
    }

    @Bean
    public Binding orderCancelBinding() {
        log.info("绑定队列到交换机: {} -> {}", ORDER_EXCHANGE, ORDER_CANCEL_QUEUE);
        return BindingBuilder.bind(orderCancelQueue()).to(orderExchange()).with(ROUTING_KEY_CANCEL);
    }

    @Bean
    public MessageConverter messageConverter() {
        log.info("配置JSON消息转换器");
        
        // 创建支持LocalDateTime的ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 使用带ObjectMapper的构造函数
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setCreateMessageIds(true);

        // 设置信任所有包的反序列化（解决__TypeId__类型匹配问题）
        org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper typeMapper =
                new org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.hnkjzy.*", "*");
        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }
}
