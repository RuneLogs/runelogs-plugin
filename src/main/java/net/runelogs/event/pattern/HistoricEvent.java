package net.runelogs.event.pattern;

import lombok.Getter;
import lombok.Value;

@Value
@Getter
public class HistoricEvent<T> {
    int internalOrder, clientTick;
    T event;
}
