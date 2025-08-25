package net.runelogs.tracker.method.hitsplat;

import lombok.Getter;
import net.runelite.api.Skill;
import net.runelite.api.events.HitsplatApplied;
import net.runelogs.event.pattern.HistoricEvent;
import net.runelogs.event.wrapper.SafeHitsplatApplied;
import net.runelogs.tracker.SkillingTracker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class HitsplatTracker extends SkillingTracker {
    private static final int hitsplatQueueLimit = 2;

    protected double hitsplatDelayInTicks, previousHitsplatDelayInTicks;
    protected boolean isOnHitsplatCycle;

    public int getLastHitsplatTick() {
        for (int i = eventHistory.size() - 1; i >= 0; i--) {
            HistoricEvent<?> event = eventHistory.get(i);
            if (event.getClientTick() > finishedTick) continue;
            if (event.getEvent() instanceof SafeHitsplatApplied) {
                return event.getClientTick();
            }
        }
        return -1;
    }

    public int getMatchingCycleDuration(Skill skill) {
        Map<Integer, Integer> map = getCycleDurations(skill);
        if (isOnHitsplatCycle) return (int) hitsplatDelayInTicks;
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(getPresumedCycleDuration(skill));
    }

    @Override
    public void onGameTickFinished(int finishedTick) {
        super.onGameTickFinished(finishedTick);
        long historicHitsplatCount = eventHistory.stream()
                .filter(event -> event.getEvent() instanceof SafeHitsplatApplied)
                .filter(event -> Math.abs(event.getClientTick() - this.finishedTick) < 14)
                .count();
        if (historicHitsplatCount < hitsplatQueueLimit) {
            this.isOnHitsplatCycle = false;
            this.hitsplatDelayInTicks = 0;
        }
    }

    @Override
    public void onSelfHitsplat(int clientTick, HitsplatApplied hitsplatApplied) {
        super.onSelfHitsplat(clientTick, hitsplatApplied);
        List<HistoricEvent<?>> hitsplatQueue = eventHistory.stream()
                .filter(event -> event.getEvent() instanceof SafeHitsplatApplied)
                .filter(event -> (finishedTick - event.getClientTick()) < 14).collect(Collectors.toMap(
                        HistoricEvent::getClientTick,
                        event -> event,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .limit(4)
                .collect(Collectors.toList());
        if (hitsplatQueue.size() >= hitsplatQueueLimit) {
            HistoricEvent<?>[] events = hitsplatQueue.toArray(HistoricEvent[]::new);
            double totalTickDuration = 0;
            for (int i = events.length - 1; i >= 1; i--) {
                int delay = Math.abs(events[i].getClientTick() - events[i - 1].getClientTick());
                totalTickDuration += delay;
            }
            this.previousHitsplatDelayInTicks = hitsplatDelayInTicks;
            this.hitsplatDelayInTicks = totalTickDuration / (events.length - 1);
            boolean isCleanCycle = (hitsplatDelayInTicks - ((int) hitsplatDelayInTicks)) == 0;
            this.isOnHitsplatCycle = isCleanCycle && (previousHitsplatDelayInTicks == hitsplatDelayInTicks);
        } else {
            this.isOnHitsplatCycle = false;
            this.hitsplatDelayInTicks = 0;
            this.previousHitsplatDelayInTicks = 0;
        }
    }

    @Override
    public boolean isOnCycle() {
        return isOnHitsplatCycle;
    }
}
