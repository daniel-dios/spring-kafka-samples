package com.example.simpleconsumer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;

import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY;

public class FooListener {

    private final MeterRegistry meterRegistry;
    private final Logger logger = LoggerFactory.getLogger(FooListener.class);
    private final String metricAfterException = "event-consumed-keys";
    private final String metricBeforeException = "event-consumed-event";

    public FooListener(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @KafkaListener(
            topics = "my-app.foo.v1",
            containerFactory = "kafkaFooConsumerContainerFactory",
            clientIdPrefix = "my-foo-consumer",
            groupId = "my-foo-consumer"
    )
    public void consume(Message<String> event) {
        meterRegistry.counter(metricBeforeException, Tags.of("event", event.getPayload())).increment();

        final var key = (String) event.getHeaders().get(RECEIVED_KEY);
        if (key == null) {
            throw new NonRetrievableException();
        }

        meterRegistry.counter(metricAfterException, Tags.of("key", key)).increment();
        logger.info("Event with key {} consumed", key);
    }

    public static class NonRetrievableException extends RuntimeException {
    }
}
