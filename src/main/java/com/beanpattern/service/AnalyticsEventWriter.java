package com.beanpattern.service;

import com.beanpattern.entity.AnalyticsEvent;
import com.beanpattern.mapper.AnalyticsEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsEventWriter {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventWriter.class);

    private final AnalyticsEventMapper analyticsEventMapper;

    public AnalyticsEventWriter(AnalyticsEventMapper analyticsEventMapper) {
        this.analyticsEventMapper = analyticsEventMapper;
    }

    @Async("analyticsTaskExecutor")
    public void saveEventsAsync(List<AnalyticsEvent> events) {
        for (AnalyticsEvent event : events) {
            try {
                analyticsEventMapper.insertIgnore(event);
            } catch (Exception e) {
                log.warn("Failed to save analytics event: {}", event.getEventName(), e);
            }
        }
    }
}
