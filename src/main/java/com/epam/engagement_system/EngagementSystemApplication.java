package com.epam.engagement_system;

import com.epam.engagement_system.configuration.FileStorageConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(FileStorageConfiguration.class)
public class EngagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EngagementSystemApplication.class, args);
	}

}
