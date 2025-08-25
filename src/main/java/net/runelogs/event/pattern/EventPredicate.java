package net.runelogs.event.pattern;

public interface EventPredicate {
    boolean matches(HistoricEvent<?> event);
}
