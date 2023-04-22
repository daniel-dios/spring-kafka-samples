package com.example.simpleproducer;

import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaBlockingProducer<K, V> {
    private final String topic;
    private final KafkaTemplate<K, V> kafkaTemplate;

    private final Logger logger = LoggerFactory.getLogger(KafkaBlockingProducer.class);

    public KafkaBlockingProducer(
            final String topic,
            final KafkaTemplate<K, V> kafkaTemplate
    ) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produce(
            final K key,
            final V message
    ) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(topic, key, message).get();
        logger.info("Message with key {} produced", key);
    }
}
