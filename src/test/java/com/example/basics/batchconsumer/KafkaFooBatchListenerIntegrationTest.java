package com.example.basics.batchconsumer;


import com.example.testutils.KafkaContainerInitializer;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(profiles = {"test", "kafka"})
@ContextConfiguration(initializers = KafkaContainerInitializer.class)
class KafkaFooBatchListenerIntegrationTest {

    @Autowired
    private FooBatchSolver solver;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Test
    void shouldConsumeAndIncrementMetric() throws ExecutionException, InterruptedException {
        final var kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                new StringSerializer()
        ));
        for (int i = 0; i < 100; i++) {
            kafkaTemplate.send(new ProducerRecord<>("my-app.foo.batch.v1", UUID.randomUUID().toString(), String.valueOf(i))).get();
        }

        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(solver.allTheMessagesConsumed()).isNotEmpty().contains(100));
        // we verify the messages were not collected one by one
        assertThat(solver.getEventsCollected().size()).isLessThan(100);
    }
}
