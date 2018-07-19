[![Build Status](https://travis-ci.org/feli6/kafka-consumer-lag-reporter.svg?branch=master)](https://travis-ci.org/feli6/kafka-consumer-lag-reporter)

# Kafka consumer lag reporting using prometheus

A standalone spring boot app for reporting kafka consumer group lag metrics to prometheus.
Tested with Kafka 1.0

Prometheus scrape endpoint: http://host:port/actuator/prometheus-kafka

Following properties are required:
kafka.brokers= broker urls
kafka.consumerGroups= list of consumer groups separated by comma

The properties can be configured frm the web/src/main/resources/application.properties file.
If you are running using docker, these properties can be overridden using environment variables.

