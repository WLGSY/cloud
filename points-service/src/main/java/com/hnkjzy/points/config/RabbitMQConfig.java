package com.hnkjzy.points.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
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

    @Bean
    public Queue orderCreateQueue() {
        log.info("声明订单创建队列: {}", ORDER_CREATE_QUEUE);
        return QueueBuilder.durable(ORDER_CREATE_QUEUE).build();
    }

    @Bean
    public Queue orderPayQueue() {
        log.info("声明订单支付队列: {}", ORDER_PAY_QUEUE);
        return QueueBuilder.durable(ORDER_PAY_QUEUE).build();
    }

    @Bean
    public Queue orderCancelQueue() {
        log.info("声明订单取消队列: {}", ORDER_CANCEL_QUEUE);
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE).build();
    }

    @Bean
    public TopicExchange orderExchange() {
        log.info("声明订单交换机: {}", ORDER_EXCHANGE);
        return ExchangeBuilder.topicExchange(ORDER_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding orderCreateBinding() {
        log.info("绑定订单创建队列: {} -> {} -> {}", ORDER_EXCHANGE, ROUTING_KEY_CREATE, ORDER_CREATE_QUEUE);
        return BindingBuilder.bind(orderCreateQueue()).to(orderExchange()).with(ROUTING_KEY_CREATE);
    }

    @Bean
    public Binding orderPayBinding() {
        log.info("绑定订单支付队列: {} -> {} -> {}", ORDER_EXCHANGE, ROUTING_KEY_PAY, ORDER_PAY_QUEUE);
        return BindingBuilder.bind(orderPayQueue()).to(orderExchange()).with(ROUTING_KEY_PAY);
    }

    @Bean
    public Binding orderCancelBinding() {
        log.info("绑定订单取消队列: {} -> {} -> {}", ORDER_EXCHANGE, ROUTING_KEY_CANCEL, ORDER_CANCEL_QUEUE);
        return BindingBuilder.bind(orderCancelQueue()).to(orderExchange()).with(ROUTING_KEY_CANCEL);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        log.info("配置JSON消息转换器（支持信任所有包）");
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setCreateMessageIds(true);
        
        // 配置信任包，解决反序列化问题
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.hnkjzy.*", "java.util", "java.lang", "java.math", "*");
        converter.setJavaTypeMapper(typeMapper);
        
        return converter;
    }

    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        log.info("配置RabbitListenerContainerFactory");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(3);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.AUTO);
        return factory;
    }
}