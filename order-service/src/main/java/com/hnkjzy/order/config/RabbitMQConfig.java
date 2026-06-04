package com.hnkjzy.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RabbitMQConfig {

    // 队列名称
    public static final String ORDER_CREATE_QUEUE = "order.create.queue";
    public static final String ORDER_PAY_QUEUE = "order.pay.queue";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";

    // 交换机
    public static final String ORDER_EXCHANGE = "order.exchange";

    // 路由键
    public static final String ROUTING_KEY_CREATE = "order.create";
    public static final String ROUTING_KEY_PAY = "order.pay";
    public static final String ROUTING_KEY_CANCEL = "order.cancel";

    // 死信配置
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
    public MessageConverter jsonMessageConverter() {
        log.info("配置JSON消息转换器");
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter converter) {
        log.info("配置RabbitTemplate - 启用发送确认机制");
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        
        // 启用发送确认（Publisher Confirm）
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("[订单服务] 消息发送成功，消息ID: {}", 
                        correlationData != null ? correlationData.getId() : "unknown");
            } else {
                log.error("[订单服务] 消息发送失败，消息ID: {}, 原因: {}", 
                        correlationData != null ? correlationData.getId() : "unknown", cause);
            }
        });
        
        // 启用消息返回（Publisher Returns）
        template.setReturnsCallback(returnedMessage -> {
            log.error("[订单服务] 消息被退回 - 交换机: {}, 路由键: {}, 原因: {} - {}", 
                    returnedMessage.getExchange(), 
                    returnedMessage.getRoutingKey(), 
                    returnedMessage.getReplyCode(), 
                    returnedMessage.getReplyText());
            log.error("[订单服务] 消息内容: {}", new String(returnedMessage.getMessage().getBody()));
        });
        
        // 强制启用消息返回模式
        template.setMandatory(true);
        
        return template;
    }
}