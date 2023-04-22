package com.example.basics.batchconsumer;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;

@EnableKafka
@Configuration
public class FooBatchConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaFooBatchConsumerContainerFactory(
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
        factory.setBatchListener(true);
        factory.getContainerProperties().setIdleBetweenPolls(5000);
        return factory;
    }

    @Bean
    public FooBatchListener stoppableFooListener(
            final FooBatchSolver batchFooSolver
    ) {
        return new FooBatchListener(batchFooSolver);
    }

    @Bean
    public FooBatchSolver batchFooSolver() {
        return new FooBatchSolver();
    }
}
