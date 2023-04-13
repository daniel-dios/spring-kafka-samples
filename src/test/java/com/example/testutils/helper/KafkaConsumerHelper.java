package com.example.testutils.helper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

public class KafkaConsumerHelper {

    private final KafkaConsumer<String, String> consumer;
    private final List<String> topics;

    public KafkaConsumerHelper(
            KafkaProperties kafkaProperties,
            List<String> bootstrapServers,
            List<String> topics
    ) {
        final var consumerProperties = kafkaProperties.getConsumer();
        final var groupId = UUID.randomUUID().toString();
        consumerProperties.setClientId(groupId);
        consumerProperties.setGroupId(groupId);
        consumerProperties.setAutoOffsetReset("earliest");
        consumerProperties.setBootstrapServers(bootstrapServers);
        this.consumer = new KafkaConsumer<>(
                consumerProperties.buildProperties(),
                new StringDeserializer(),
                new StringDeserializer()
        );
        consumer.subscribe(topics);
        this.topics = topics;
    }

    public List<ConsumerRecord<String, String>> findRecords(
            final Duration atMost,
            final Integer atLeast,
            final String key,
            final Duration pollEvery,
            final String topic
    ) {
        if (!topics.contains(topic)) {
            throw new IllegalArgumentException("Consumer is not subscribed to the topic");
        }
        final var timer = new Timer(atMost);
        final var records = new ArrayList<ConsumerRecord<String, String>>();
        while (!(timer.finished() || (atLeast != null && records.size() > atLeast))) {
            consumer.poll(pollEvery)
                    .records(topic).forEach(it -> {
                        if (key == null || key.equals(it.key())) records.add(it);
                    });
            timer.subs(pollEvery);
        }
        return records;
    }

    public List<ConsumerRecord<String, String>> findRecords(
            final Duration maxTime,
            final Integer atLeast,
            final String key,
            final String topic
    ) {
        return findRecords(maxTime, atLeast, key, Duration.ofMillis(500), topic);
    }

    public List<ConsumerRecord<String, String>> findRecords(
            final Duration maxTime,
            final Integer atLeast,
            final String topic
    ) {
        return findRecords(maxTime, atLeast, null, Duration.ofMillis(500), topic);
    }

    public List<ConsumerRecord<String, String>> findRecords(
            final Duration maxTime,
            final String key,
            final String topic
    ) {
        return findRecords(maxTime, null, key, Duration.ofMillis(500), topic);
    }

    public List<ConsumerRecord<String, String>> findRecords(
            final Duration maxTime,
            final String topic
    ) {
        return findRecords(maxTime, null, null, Duration.ofMillis(500), topic);
    }

    private static class Timer {
        private Duration maxTime;

        public Timer(final Duration maxTime) {
            this.maxTime = maxTime;
        }

        public boolean finished() {
            return maxTime.isNegative();
        }

        public void subs(final Duration pollEvery) {
            this.maxTime = maxTime.minus(pollEvery);
        }
    }
}
