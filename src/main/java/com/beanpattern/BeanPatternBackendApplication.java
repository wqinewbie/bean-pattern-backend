package com.beanpattern;

import com.beanpattern.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 启动入口。
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableScheduling
public class BeanPatternBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeanPatternBackendApplication.class, args);
	}

}
