package com.hnkjzy.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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

    // 死信配置（必须与 order-service 完全一致）
    public static final String DEAD_EXCHANGE = "dead.exchange";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";
    public static final String DEAD_ROUTING_KEY = "dead.letter";

    // ========== 死信交换机与队列 ==========

    @Bean
    public DirectExchange deadExchange() {
        return ExchangeBuilder.directExchange(DEAD_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadExchange()).with(DEAD_ROUTING_KEY);
    }

    // ========== 业务队列（与 order-service 配置完全一致，带死信参数） ==========

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
        return BindingBuilder.bind(orderCreateQueue()).to(orderExchange()).with(ROUTING_KEY_CREATE);
    }

    @Bean
    public Binding orderPayBinding() {
        return BindingBuilder.bind(orderPayQueue()).to(orderExchange()).with(ROUTING_KEY_PAY);
    }

    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue()).to(orderExchange()).with(ROUTING_KEY_CANCEL);
    }

    // ========== 消息转换器（使用 Map 反序列化，避免类型匹配问题） ==========

    @Bean
    public MessageConverter messageConverter() {
        log.info("配置JSON消息转换器");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setCreateMessageIds(true);

        // 信任所有包，消费者使用 Map<String, Object> 接收，避免 __TypeId__ 类型匹配失败
        org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper typeMapper =
                new org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        log.info("配置 RabbitListenerContainerFactory");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(3);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }
}
