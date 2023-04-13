package com.example;

import com.example.helper.KafkaContainerInitializer;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(profiles = {"test", "kafka"})
@ContextConfiguration(initializers = KafkaContainerInitializer.class)
class kafkaFooPublisherTest {

    @Autowired
    private KafkaBlockingProducer<String, String> kafkaBlockingProducer;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Test
    void shouldProduceMessages() throws ExecutionException, InterruptedException {
        final var key = UUID.randomUUID().toString();
        final KafkaProperties.Consumer consumerProperties = kafkaProperties.getConsumer();
        final var groupId = UUID.randomUUID().toString();
        consumerProperties.setClientId(groupId);
        consumerProperties.setGroupId(groupId);
        consumerProperties.setAutoOffsetReset("earliest");
        consumerProperties.setBootstrapServers(Collections.singletonList(System.getProperty("spring.kafka.bootstrap-servers")));
        final var consumer = new KafkaConsumer<>(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                new StringDeserializer()
        );
        consumer.subscribe(List.of("my-app.foo.v1"));
        Thread.sleep(10000); // TODO fix this, I don't like this but I'm blocked

        kafkaBlockingProducer.produce(key, "my-value");

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> {
                    final var objects = new HashSet<String>();
                    final var records = consumer.poll(Duration.ofMillis(50)).records("my-app.foo.v1");
                    records.forEach(i -> objects.add(i.key()));
                    return objects.contains(key);
                });
    }
}
