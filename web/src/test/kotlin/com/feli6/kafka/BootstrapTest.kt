package com.feli6.kafka

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(BootstrapTest.TestConfig::class)
class BootstrapTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `Should be able to startup the app and access prometheus endpoint`() {
        println(">> Assert blog page title, content and status code")
        val entity = restTemplate.getForEntity<String>("/actuator/prometheus-kafka")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).contains("system_cpu_count")
    }

    @TestConfiguration
    public open class TestConfig {

        @Bean
        @Primary
        fun getMockKafkaHandler(): KafkaCommandHandler = mock()
    }

}