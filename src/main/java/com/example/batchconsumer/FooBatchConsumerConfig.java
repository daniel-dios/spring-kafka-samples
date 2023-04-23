package com.example.batchconsumer;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
        final Map<String, Object> configs = kafkaProperties.buildConsumerProperties();

        // Here we can control the max batch
        configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        final var defaultKafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(
                configs,
                new StringDeserializer(),
                new StringDeserializer()
        );
        defaultKafkaConsumerFactory.addListener(new MicrometerConsumerListener<>(meterRegistry));
        final var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(defaultKafkaConsumerFactory);

        // mandatory
        factory.setBatchListener(true);

        // The bigger idle between poll the more chances to the max poll
        factory.getContainerProperties().setIdleBetweenPolls(1000);
        return factory;
    }

    @Bean
    public FooBatchListener fooBatchListener(
            final FooBatchSolver batchFooSolver
    ) {
        return new FooBatchListener(batchFooSolver);
    }

    @Bean
    public FooBatchSolver batchFooSolver() {
        return new FooBatchSolver();
    }
}
