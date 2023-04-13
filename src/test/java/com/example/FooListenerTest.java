package com.example;


import com.example.helper.KafkaContainerInitializer;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(profiles = {"test", "kafka"})
@ContextConfiguration(initializers = KafkaContainerInitializer.class)
class FooListenerTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private KafkaBlockingProducer<String, String> kafkaBlockingProducer;

    @Test
    void shouldConsumeAndIncrementMetric() throws ExecutionException, InterruptedException {
        final var key = UUID.randomUUID().toString();
        final var payload = "payload";
        Thread.sleep(10000);  // TODO fix this, I don't like this but I'm blocked

        kafkaBlockingProducer.produce(key, payload);

        await()
                .pollInterval(Duration.ofMillis(50))
                .atMost(Duration.ofSeconds(10))
                .until(() -> meterRegistry.get("event-consumed-keys").tags("key", key).counter().count() >= 0.0);
    }

    @Test
    void shouldNotRetryNullKey() throws ExecutionException, InterruptedException {
        final var key = UUID.randomUUID().toString();
        Thread.sleep(10000);  // TODO fix this, I don't like this but I'm blocked

        kafkaBlockingProducer.produce(null, "payload-with-null-key");
        kafkaBlockingProducer.produce(key, "payload-with-key-random-uuid");

        await()
                .pollInterval(Duration.ofMillis(50))
                .atMost(Duration.ofSeconds(10))
                .until(() -> meterRegistry.get("event-consumed-event").tags("event", "payload-with-null-key").counter().count() >= 0.0);

        await()
                .pollInterval(Duration.ofMillis(50))
                .atMost(Duration.ofSeconds(10))
                .until(() -> meterRegistry.get("event-consumed-keys").tags("key", key).counter().count() >= 0.0);

        assertThat(meterRegistry.get("event-consumed-event").tag("event", "payload-with-null-key").counter().count())
                .isCloseTo(1.0, Percentage.withPercentage(0.2));
    }
}
