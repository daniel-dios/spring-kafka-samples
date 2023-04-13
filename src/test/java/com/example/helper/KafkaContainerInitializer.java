package com.example.helper;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(final @NotNull ConfigurableApplicationContext applicationContext) {
        var kafkaContainer = new KafkaContainer(
                DockerImageName
                        .parse("artifactory.cd-tech26.de/docker/confluentinc/cp-kafka:6.0.2")
                        .asCompatibleSubstituteFor("confluentinc/cp-kafka")
        );
        kafkaContainer.addEnv("TOPIC_AUTO_CREATE", "true");
        kafkaContainer.start();
        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
    }
}
