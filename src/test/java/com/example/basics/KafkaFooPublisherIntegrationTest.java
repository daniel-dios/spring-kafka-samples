package com.example.basics.producer;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.example.testutils.KafkaContainerInitializer;
import com.example.testutils.helper.KafkaConsumerHelper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(profiles = {"test", "kafka"})
@ContextConfiguration(initializers = KafkaContainerInitializer.class)
class KafkaFooPublisherIntegrationTest {

    @Autowired
    private KafkaBlockingProducer<String, String> kafkaBlockingProducer;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Test
    void shouldProduceMessages() throws ExecutionException, InterruptedException {
        final var key = UUID.randomUUID().toString();
        final var consumer = new KafkaConsumerHelper(
                kafkaProperties,
                List.of(System.getProperty("spring.kafka.bootstrap-servers")),
                List.of("my-app.foo.v1")
        );

        kafkaBlockingProducer.produce(key, "my-value");

        assertThat(consumer.findRecords(Duration.ofSeconds(10), 1, key, "my-app.foo.v1"))
                .isNotEmpty();
    }
}
