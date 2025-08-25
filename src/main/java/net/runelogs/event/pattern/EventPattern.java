package net.runelogs.event.pattern;

import java.util.*;
import java.util.stream.Collectors;

public class EventPattern {
    private final List<TickSpecification> sequence;
    private final EventPredicate anywhere;

    private EventPattern(List<TickSpecification> sequence, EventPredicate anywhere) {
        this.sequence = sequence;
        this.anywhere = anywhere;
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

        int min = events.stream().mapToInt(HistoricEvent::getClientTick).min().orElse(0);
        int max = events.stream().mapToInt(HistoricEvent::getClientTick).max().orElse(0);

        boolean isGlobalConditionMet = anywhere == null;

        int sequenceIndex = 0;
        for (int i = min; i <= max && sequenceIndex < sequence.size(); i++) {
            List<HistoricEvent<?>> eventsInTick = grouped.getOrDefault(i, Collections.emptyList());
            TickSpecification specification = sequence.get(sequenceIndex);
            if (specification.matches(eventsInTick)) {
                sequenceIndex++;
            }

            if (anywhere == null) continue;
            long count = eventsInTick.stream().filter(anywhere::matches).count();
            if (count == 0) continue;
            if (isGlobalConditionMet) return false;
            isGlobalConditionMet = true;
        }

        return sequenceIndex == sequence.size() && isGlobalConditionMet;
    }


    private static class TickSpecification {
        private final List<EventPredicate> ordered;
        private final List<EventPredicate> unordered;

        TickSpecification(List<EventPredicate> ordered, List<EventPredicate> unordered) {
            this.unordered = unordered;
            this.ordered = ordered;
        }

        boolean matches(List<HistoricEvent<?>> eventsInTick) {
            if (ordered.isEmpty() && unordered.isEmpty()) {
                return eventsInTick.isEmpty();
            }

            for (EventPredicate predicate : unordered) {
                boolean matched = eventsInTick.stream().anyMatch(predicate::matches);
                if (!matched) return false;
            }

            int patternIndex = 0;
            for (HistoricEvent<?> event : eventsInTick) {
                if (patternIndex < ordered.size() && ordered.get(patternIndex).matches(event)) {
                    patternIndex++;
                    if (patternIndex == ordered.size()) return true;
                }
            }

            return ordered.isEmpty();
        }
    }

    public static class Builder {
        private final List<TickSpecification> sequence = new ArrayList<>();
        private EventPredicate anywhere;

        public Builder tick(EventPredicate... ordered) {
            this.sequence.add(new TickSpecification(Arrays.asList(ordered), Collections.emptyList()));
            return this;
        }

        public Builder tick(List<EventPredicate> unordered, EventPredicate... ordered) {
            this.sequence.add(new TickSpecification(new ArrayList<>(Arrays.asList(ordered)), unordered));
            return this;
        }

        public Builder tick() {
            this.sequence.add(new TickSpecification(Collections.emptyList(), Collections.emptyList()));
            return this;
        }

        public Builder anywhere(EventPredicate anywhere) {
            this.anywhere = anywhere;
            return this;
        }

        public EventPattern build() {
            return new EventPattern(sequence, anywhere);
        }
    }
}
