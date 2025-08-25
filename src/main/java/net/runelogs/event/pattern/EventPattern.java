package net.runelogs.event.pattern;

import java.util.*;
import java.util.stream.Collectors;

public class EventPattern {
    private final List<List<EventPredicate>> sequence;

    private EventPattern(final List<List<EventPredicate>> sequence) {
        this.sequence = sequence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean matches(List<HistoricEvent<?>> events) {
        Map<Integer, List<HistoricEvent<?>>> grouped =
                events.stream()
                        .collect(Collectors.groupingBy(
                                HistoricEvent::getClientTick,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            list.sort(Comparator.comparingInt(HistoricEvent::getInternalOrder));
                                            return list;
                                        }
                                )
                        ));

        List<Integer> ticks = new ArrayList<>(grouped.keySet());
        Collections.sort(ticks);

        int sequenceIndex = 0;
        for (int i = 0; i < ticks.size() && sequenceIndex < sequence.size(); i++) {
            List<HistoricEvent<?>> eventsInTick = grouped.get(ticks.get(i));
            List<EventPredicate> predicates = sequence.get(sequenceIndex);

            if (matchesInOrder(eventsInTick, predicates)) {
                sequenceIndex++;
            }
        }
        return sequenceIndex == sequence.size();
    }

    private boolean matchesInOrder(List<HistoricEvent<?>> eventsInTick, List<EventPredicate> predicates) {
        int patternIndex = 0;
        for (HistoricEvent<?> event : eventsInTick) {
            if (predicates.get(patternIndex).matches(event)) {
                patternIndex++;
                if (patternIndex == predicates.size()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class Builder {
        private final List<List<EventPredicate>> sequence = new ArrayList<>();

        public Builder tick(EventPredicate... predicates) {
            this.sequence.add(Arrays.asList(predicates));
            return this;
        }

        public EventPattern build() {
            return new EventPattern(sequence);
        }
    }
}
