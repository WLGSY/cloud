package com.hnkjzy.delivery.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String ORDER_PAY_QUEUE = "order.pay.queue";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_PAY_ROUTING_KEY = "order.pay";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";
    
    // 死信配置（必须与其他服务保持一致）
    public static final String DEAD_EXCHANGE = "dead.exchange";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";
    public static final String DEAD_ROUTING_KEY = "dead.letter";

    private final ConnectionFactory connectionFactory;

    public RabbitMQConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void cleanupOldQueues() {
        try {
            com.rabbitmq.client.Connection conn = connectionFactory.createConnection().getDelegate();
            com.rabbitmq.client.Channel channel = conn.createChannel();
            
            log.warn("正在删除旧队列以确保配置一致...");
            channel.queueDelete(ORDER_PAY_QUEUE);
            channel.queueDelete(ORDER_CANCEL_QUEUE);
            
            channel.close();
            conn.close();
            log.info("旧队列已删除，将使用新配置重新创建");
        } catch (Exception e) {
            log.warn("清理旧队列时出错（可能队列不存在）: {}", e.getMessage());
        }
    }

    // ========== 死信交换机与队列 ==========

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

    // ========== 业务队列 ==========

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
    public Binding orderPayBinding() {
        log.info("绑定队列到交换机: {} -> {} -> {}", ORDER_EXCHANGE, ORDER_PAY_ROUTING_KEY, ORDER_PAY_QUEUE);
        return BindingBuilder
                .bind(orderPayQueue())
                .to(orderExchange())
                .with(ORDER_PAY_ROUTING_KEY);
    }

    @Bean
    public Binding orderCancelBinding() {
        log.info("绑定队列到交换机: {} -> {} -> {}", ORDER_EXCHANGE, ORDER_CANCEL_ROUTING_KEY, ORDER_CANCEL_QUEUE);
        return BindingBuilder
                .bind(orderCancelQueue())
                .to(orderExchange())
                .with(ORDER_CANCEL_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        log.info("配置 JSON 消息转换器");
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        log.info("配置 RabbitListener 容器工厂");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        // 设置并发消费者数量
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        log.info("配置 RabbitTemplate 使用 JSON 转换器");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
