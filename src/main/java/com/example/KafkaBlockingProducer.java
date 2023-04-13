package com.example;

import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaBlockingProducer<K, V> {
    private final String topic;
    private final KafkaTemplate<K, V> stringStringKafkaTemplate;

    private final Logger logger = LoggerFactory.getLogger(KafkaBlockingProducer.class);

    public KafkaBlockingProducer(final String topic, final KafkaTemplate<K, V> stringStringKafkaTemplate) {
        this.topic = topic;
        this.stringStringKafkaTemplate = stringStringKafkaTemplate;
    }

    public void produce(K key, V message) throws ExecutionException, InterruptedException {
        stringStringKafkaTemplate.send(topic, key, message).get();
        logger.info("Message with key {} produced", key);
    }
}
