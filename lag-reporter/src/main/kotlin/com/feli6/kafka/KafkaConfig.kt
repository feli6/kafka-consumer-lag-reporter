package com.feli6.kafka

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "kafka")
class KafkaConfig {
    lateinit var brokers: String
    lateinit var consumerGroups: List<String>

}
