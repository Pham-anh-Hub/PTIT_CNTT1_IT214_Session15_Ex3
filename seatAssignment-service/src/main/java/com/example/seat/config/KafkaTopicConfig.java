package com.example.seat.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String CONCERT_EVENTS_TOPIC = "concert-events";
    public static final String SEAT_EVENTS_TOPIC = "seat-events";

    @Bean
    public NewTopic seatEventsTopic() {
        return TopicBuilder.name(SEAT_EVENTS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
