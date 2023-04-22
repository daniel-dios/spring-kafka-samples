package com.example.simpleproducer;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.MicrometerProducerListener;

import static org.apache.kafka.clients.CommonClientConfigs.CLIENT_ID_CONFIG;

@EnableKafka
@Configuration
public class ProducerConfig {

    @Bean
    public KafkaBlockingProducer<String, String> kafkaFooPublisher(
            final KafkaProperties kafkaProperties,
            final MeterRegistry meterRegistry
    ) {
        final var properties = kafkaProperties.buildProducerProperties();
        properties.put(CLIENT_ID_CONFIG, "foo-producer");
        final var producerFactory = new DefaultKafkaProducerFactory<>(
                properties,
                new StringSerializer(),
                new StringSerializer()
        );

        producerFactory.addListener(new MicrometerProducerListener<>(meterRegistry));

        return new KafkaBlockingProducer<>(
                "my-app.foo.v1",
                new KafkaTemplate<>(producerFactory)
        );
    }
}
