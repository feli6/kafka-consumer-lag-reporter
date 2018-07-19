package com.feli6.kafka

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag.of
import io.micrometer.core.instrument.Tags
import kafka.admin.ConsumerGroupCommand
import kafka.admin.ConsumerGroupCommand.ConsumerGroupCommandOptions
import kafka.admin.ConsumerGroupCommand.KafkaConsumerGroupService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scala.Option
import scala.Tuple2
import scala.collection.Seq

@Component
class KafkaCommandHandler(@Autowired val kafkaConfig: KafkaConfig, @Autowired val meterRegistry: MeterRegistry) {

    private val CONSUMER_GROUP_STATUS = "consumer-group-state"
    private val GROUP = "group"
    private val PARTITION = "partition"
    private val TOPIC = "topic"
    private val CONSUMER_LAG = "consumer_lag"

    enum class ConsumerGroupStatus(ordinal: Int) {
        Empty(0),
        Stable(1),
        PreparingRebalance(2),
        Dead(3),
        Unknown(4)

    }

    fun updateConsumerLagMetrics() {
        kafkaConfig.consumerGroups.forEach { consumerGroup ->
            val consumerGroupService = getConsumerGroupService(consumerGroup)

            try {
                val groupStateResult = consumerGroupService.describeGroup()

                groupStateResult.groupState().reportGroupStatusMetric(consumerGroup)

                groupStateResult.parttionAssignments().forEach { it.reportConsumerLagMetric(consumerGroup) }

            } finally {
                consumerGroupService.close()
            }

        }
    }

    private fun ConsumerGroupCommand.PartitionAssignmentState.getTopic() = topic().getOrElse { "N/A" }

    private fun ConsumerGroupCommand.PartitionAssignmentState.getLag() = lag().getOrElse { 0 }

    private fun ConsumerGroupCommand.PartitionAssignmentState.getPartition() = partition().map { it.toString() }.getOrElse { "N/A" }

    private fun ConsumerGroupStatus.reportGroupStatusMetric(consumerGroup: String) {
        meterRegistry.gauge(CONSUMER_GROUP_STATUS, Tags.of(of(GROUP, consumerGroup)), ordinal)
    }

    private fun ConsumerGroupCommand.PartitionAssignmentState.reportConsumerLagMetric(consumerGroup: String) {
        val tags = Tags.of(
                of(GROUP, consumerGroup),
                of(PARTITION, getPartition()),
                of(TOPIC, getTopic())
        )
        meterRegistry.gauge(CONSUMER_LAG, tags, getLag())
    }

    private fun Tuple2<Option<String>, Option<Seq<ConsumerGroupCommand.PartitionAssignmentState>>>.groupState() = _1
            .map { ConsumerGroupStatus.valueOf(it.toString()) }
            .getOrElse { ConsumerGroupStatus.Unknown }

    private fun Tuple2<Option<String>, Option<Seq<ConsumerGroupCommand.PartitionAssignmentState>>>.parttionAssignments(): List<ConsumerGroupCommand.PartitionAssignmentState> = _2
            .map { it.toJavaList() }
            .getOrElse { emptyList() }


    private fun getConsumerGroupService(group: String): ConsumerGroupCommand.KafkaConsumerGroupService = KafkaConsumerGroupService(ConsumerGroupCommandOptions(arrayOf("--bootstrap-server", kafkaConfig.brokers, "--group", group)))

}

fun <T> scala.collection.Seq<T>.toJavaList(): List<T> {
    return scala.collection.JavaConverters.seqAsJavaList(this)
}
