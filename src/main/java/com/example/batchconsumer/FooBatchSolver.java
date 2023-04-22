package com.example.batchconsumer;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.messaging.Message;

public class FooBatchSolver {

    private final Set<List<Message<String>>> eventsCollected = new HashSet<>();

    public void execute(List<Message<String>> events) {
        if (!events.isEmpty()) {
            eventsCollected.add(events);
        }
    }

    public Set<List<Message<String>>> getEventsCollected() {
        return eventsCollected;
    }

    public Optional<Integer> allTheMessagesConsumed() {
        return eventsCollected.stream().map(List::size).reduce(Integer::sum);
    }
}
