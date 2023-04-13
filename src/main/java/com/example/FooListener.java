package com.example;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY;

@Component
public class FooListener {

    private final MeterRegistry meterRegistry;
    private final Logger logger = LoggerFactory.getLogger(FooListener.class);

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
        meterRegistry.counter("event-consumed-event", Tags.of("event", event.getPayload())).increment();

        final var key = (String) event.getHeaders().get(RECEIVED_KEY);
        if (key == null) {
            throw new NonRetrievableException();
        }

        meterRegistry.counter("event-consumed-keys", Tags.of("key", key)).increment();
        logger.info("Event with key {} consumed", key);
    }

    public static class NonRetrievableException extends RuntimeException {
    }
}
