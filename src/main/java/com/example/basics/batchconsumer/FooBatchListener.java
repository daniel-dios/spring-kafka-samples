package com.example.basics.batchconsumer;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;

public class FooBatchListener {

    private final FooBatchSolver batchFooSolver;
    private final Logger logger = LoggerFactory.getLogger(FooBatchListener.class);

    public FooBatchListener(
            final FooBatchSolver batchFooSolver
    ) {
        this.batchFooSolver = batchFooSolver;
    }

    @KafkaListener(
            topics = "my-app.foo.batch.v1",
            containerFactory = "kafkaFooBatchConsumerContainerFactory",
            clientIdPrefix = "my-foo-consumer-in-batch",
            groupId = "my-foo-consumer-in-batch"
    )
    public void consume(List<Message<String>> events) {
        batchFooSolver.execute(events);
        logger.info("List of events {}", events.size());
    }
}
