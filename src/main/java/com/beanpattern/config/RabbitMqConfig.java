package com.beanpattern.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.ai-service", name = "enabled", havingValue = "true")
public class RabbitMqConfig {

    @Bean
    public DirectExchange aiGenerateExchange(AiServiceProperties properties) {
        return new DirectExchange(properties.getExchangeName(), true, false);
    }

    @Bean
    public Queue aiGenerateQueue(AiServiceProperties properties) {
        return new Queue(properties.getQueueName(), true);
    }

    @Bean
    public Binding aiGenerateBinding(Queue aiGenerateQueue,
                                     DirectExchange aiGenerateExchange,
                                     AiServiceProperties properties) {
        return BindingBuilder.bind(aiGenerateQueue)
                .to(aiGenerateExchange)
                .with(properties.getRoutingKey());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
