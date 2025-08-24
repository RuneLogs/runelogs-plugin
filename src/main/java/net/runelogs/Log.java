package net.runelogs;

import lombok.Value;

@Value
public class Log {
    long timestamp;
    String message;
    int tickCount;

    @Override
    public String toString() {
        return String.format("%-7s %s %s", tickCount, timestamp, message).toLowerCase();
    }
}
