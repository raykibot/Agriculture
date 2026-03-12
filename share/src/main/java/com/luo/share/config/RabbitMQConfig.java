package com.luo.share.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // ================== 1. 定义名称常量 ==================
    // 延迟队列（陷阱，没人消费，设了 TTL 存活时间）
    public static final String DELAY_QUEUE = "order.delay.queue";
    public static final String DELAY_EXCHANGE = "order.delay.exchange";
    public static final String DELAY_ROUTING_KEY = "order.delay";

    // 死信队列（收尸房，监听器专门盯着这里）
    public static final String DEAD_QUEUE = "order.dead.queue";
    public static final String DEAD_EXCHANGE = "order.dead.exchange";
    public static final String DEAD_ROUTING_KEY = "order.dead";

    // ================== 2. 死信队列配置 (消费者监听的地方) ==================
    @Bean
    public DirectExchange deadExchange() {
        return new DirectExchange(DEAD_EXCHANGE);
    }

    @Bean
    public Queue deadQueue() {
        return new Queue(DEAD_QUEUE);
    }

    @Bean
    public Binding deadBinding(Queue deadQueue, DirectExchange deadExchange) {
        return BindingBuilder.bind(deadQueue).to(deadExchange).with(DEAD_ROUTING_KEY);
    }

    // ================== 3. 延迟队列(陷阱)配置 ==================
    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(DELAY_EXCHANGE);
    }

    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 核心1：消息死后（过期），交接给哪个交换机？(交接给死信交换机)
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE);

        // 核心2：死信消息的路由键是什么？
        args.put("x-dead-letter-routing-key", DEAD_ROUTING_KEY);

        // 核心3：消息存活时间 TTL。为了方便你测试，先写 60000 毫秒（60秒）。
        // 实际投入使用时，请改成 15 * 60 * 1000（即15分钟）
        args.put("x-message-ttl", 60000);

        return new Queue(DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding delayBinding(Queue delayQueue, DirectExchange delayExchange) {
        return BindingBuilder.bind(delayQueue).to(delayExchange).with(DELAY_ROUTING_KEY);
    }
}