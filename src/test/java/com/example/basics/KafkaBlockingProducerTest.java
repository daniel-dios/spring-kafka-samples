package com.example.basics.producer;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class KafkaBlockingProducerTest {

    @Test
    void shouldFailWhenTemplateThrowError() {
        final KafkaTemplate<String, String> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        final var producer = new KafkaBlockingProducer<>("topic", kafkaTemplate);
        when(kafkaTemplate.send("topic", "key", "event"))
                .thenThrow(new UnknownError());

        assertThatThrownBy(() -> producer.produce("key", "event"))
                .isInstanceOf(UnknownError.class);
    }
}
