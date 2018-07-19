package com.feli6.kafka.web

import com.feli6.kafka.KafkaCommandHandler
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint
import org.springframework.stereotype.Component
import java.io.StringWriter


/**
 * [Endpoint] that outputs metrics in a format that can be scraped by the Prometheus
 * server. An additional all to update kafka metrics is plugged in to the original implementation.
 *
 * Adapted from [org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint]
 *
 */
@WebEndpoint(id = "prometheus-kafka")
@Component
class PrometheusScrapeEndpoint(@Autowired private val collectorRegistry: CollectorRegistry,
                               @Autowired val kafkaConsumerGroupCommandHandler: KafkaCommandHandler) {

    @ReadOperation(produces = [(TextFormat.CONTENT_TYPE_004)])
    fun scrape(): String {
        try {
            //we override default prometheus endpoint so that kafka consumer lag metrics can be updated
            kafkaConsumerGroupCommandHandler.updateConsumerLagMetrics()
            val writer = StringWriter()
            TextFormat.write004(writer, this.collectorRegistry.metricFamilySamples())
            return writer.toString()
        } catch (ex: Throwable) {
            // This actually never happens since StringWriter::write() doesn't throw any
            // IOException
            throw RuntimeException("Writing metrics failed", ex)
        }

    }

}