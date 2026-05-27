package com.beanpattern.service;

import com.beanpattern.config.AiServiceProperties;
import com.beanpattern.model.AiGenerateMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AiTaskPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AiServiceProperties properties;

    public AiTaskPublisher(RabbitTemplate rabbitTemplate, AiServiceProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publish(AiGenerateMessage message) {
        rabbitTemplate.convertAndSend(
                properties.getExchangeName(),
                properties.getRoutingKey(),
                message
        );
    }
}
