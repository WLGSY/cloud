package com.hnkjzy.delivery.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String ORDER_PAY_QUEUE = "delivery.order.pay.queue";
    public static final String ORDER_CANCEL_QUEUE = "delivery.order.cancel.queue";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_PAY_ROUTING_KEY = "order.pay";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

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
        return BindingBuilder.bind(orderPayQueue()).to(orderExchange()).with(ORDER_PAY_ROUTING_KEY);
    }

    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue()).to(orderExchange()).with(ORDER_CANCEL_ROUTING_KEY);
    }

    // ========== 消息转换器 ==========

    @Bean
    public MessageConverter jsonMessageConverter() {
        log.info("配置 JSON 消息转换器");
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);

        // 核心修改：强制推断类型，解决转 Map 失败的问题
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        log.info("配置 RabbitListener 容器工厂");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(3);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        log.info("配置 RabbitTemplate");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
