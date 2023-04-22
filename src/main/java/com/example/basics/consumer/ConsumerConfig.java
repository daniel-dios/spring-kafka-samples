package com.example.basics.consumer;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.listener.DefaultErrorHandler;

@EnableKafka
@Configuration
public class ConsumerConfig {

    @Bean
    public DefaultErrorHandler myCustomErrorHandler() {
        final var classifications = new HashMap<Class<? extends Throwable>, Boolean>();
        // it won't retry NonRetrievableExceptions
        classifications.put(FooListener.NonRetrievableException.class, false);

        // it will retry forever all the exceptions by default
        final var errorHandler = new DefaultErrorHandler();
        errorHandler.setClassifications(classifications, true);
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaFooConsumerContainerFactory(
            final KafkaProperties kafkaProperties,
            final MeterRegistry meterRegistry,
            @Qualifier("myCustomErrorHandler") final DefaultErrorHandler errorHandler
    ) {
        final var defaultKafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                new StringDeserializer()
        );
        defaultKafkaConsumerFactory.addListener(new MicrometerConsumerListener<>(meterRegistry));
        final var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(defaultKafkaConsumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public FooListener fooListener(
            final MeterRegistry meterRegistry
    ) {
        return new FooListener(meterRegistry);
    }
}
