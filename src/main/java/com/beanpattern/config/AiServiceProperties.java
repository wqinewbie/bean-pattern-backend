package com.beanpattern.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai-service")
public class AiServiceProperties {

    private boolean enabled = false;
    private String defaultModelKey = "seedream-5-lite";
    private String callbackToken = "dev-ai-callback-token";
    private String queueName = "ai.generate.request";
    private String exchangeName = "ai.generate.exchange";
    private String routingKey = "ai.generate";
    private long taskTimeoutMinutes = 15;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultModelKey() {
        return defaultModelKey;
    }

    public void setDefaultModelKey(String defaultModelKey) {
        this.defaultModelKey = defaultModelKey;
    }

    public String getCallbackToken() {
        return callbackToken;
    }

    public void setCallbackToken(String callbackToken) {
        this.callbackToken = callbackToken;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public long getTaskTimeoutMinutes() {
        return taskTimeoutMinutes;
    }

    public void setTaskTimeoutMinutes(long taskTimeoutMinutes) {
        this.taskTimeoutMinutes = taskTimeoutMinutes;
    }
}
