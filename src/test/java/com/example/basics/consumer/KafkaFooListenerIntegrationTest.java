package com.example.basics.consumer;


import com.example.basics.producer.KafkaBlockingProducer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.example.testutils.KafkaContainerInitializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(profiles = {"test", "kafka"})
@ContextConfiguration(initializers = KafkaContainerInitializer.class)
class KafkaFooListenerIntegrationTest {

    private final String counterAfterException = "event-consumed-keys";
    private final String counterBeforeException = "event-consumed-event";
    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private KafkaBlockingProducer<String, String> kafkaFooPublisher;

    @Test
    void shouldConsumeAndIncrementMetric() throws ExecutionException, InterruptedException {
        final var key = UUID.randomUUID().toString();
        final var payload = "payload";

        kafkaFooPublisher.produce(key, payload);

        assertEventWasConsumed(key, payload);
    }

    private void assertEventWasConsumed(
            final String key,
            final String payload
    ) {
        await()
                .pollInterval(Duration.ofMillis(50))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> assertThat(meterRegistry.counter(counterAfterException, Tags.of("key", key)))
                        .extracting(Counter::count)
                        .isEqualTo(1.0));
        assertThat(meterRegistry.counter(counterBeforeException, Tags.of("event", payload)))
                .extracting(Counter::count)
                .isEqualTo(1.0);
    }

    @Test
    void shouldNotRetryEventsWithNullKey() throws ExecutionException, InterruptedException {
        kafkaFooPublisher.produce(null, "payload-with-null-key");
        final var key = UUID.randomUUID().toString();
        final var eventWithKey = "payload-with-key-random-uuid";
        kafkaFooPublisher.produce(key, eventWithKey);

        assertEventWasConsumed(key, eventWithKey);

        assertThat(meterRegistry.counter(counterBeforeException, Tags.of("event", "payload-with-null-key")))
                .extracting(Counter::count)
                .isEqualTo(1.0);
    }
}
