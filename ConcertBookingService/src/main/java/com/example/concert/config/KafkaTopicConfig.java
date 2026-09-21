package com.example.concert.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String CONCERT_EVENTS_TOPIC = "concert-events";

    @Bean
    public NewTopic concertEventsTopic() {
        return TopicBuilder.name(CONCERT_EVENTS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
