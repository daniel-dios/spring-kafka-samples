package com.example;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.apache.kafka.clients.CommonClientConfigs.CLIENT_ID_CONFIG;

@EnableKafka
@Configuration
public class MyKafkaConfig {

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

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaFooConsumerContainerFactory(
            final KafkaProperties kafkaProperties,
            final MeterRegistry meterRegistry
    ) {
        final var defaultKafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                new StringDeserializer()
        );
        defaultKafkaConsumerFactory.addListener(new MicrometerConsumerListener<>(meterRegistry));
        final var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(defaultKafkaConsumerFactory);
        final var errorHandler = new DefaultErrorHandler();
        final var classifications = new HashMap<Class<? extends Throwable>, Boolean>();
        classifications.put(FooListener.NonRetrievableException.class, false);
        errorHandler.setClassifications(classifications, true);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
