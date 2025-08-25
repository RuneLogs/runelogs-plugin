package net.runelogs.tracker;

import net.runelite.api.Skill;
import net.runelogs.event.pattern.HistoricEvent;
import net.runelogs.event.wrapper.SafeStatChanged;

import java.util.HashMap;
import java.util.Map;

public abstract class SkillingTracker extends AbstractTracker {

    public int getPresumedCycleDuration(Skill skill) {
        return getCycleDurations(skill).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
    }

    public Map<Integer, Integer> getCycleDurations(Skill skill) {
        Map<Integer, Integer> map = new HashMap<>();
        Integer lastTickForSkill = null;

        for (int i = eventHistory.size() - 1; i >= 0; i--) {
            HistoricEvent<?> event = eventHistory.get(i);
            if (!(event.getEvent() instanceof SafeStatChanged)) continue;

            SafeStatChanged statChange = (SafeStatChanged) event.getEvent();
            if (statChange.getSkill() != skill) continue;

            int clientTick = event.getClientTick();
            if (this.finishedTick - clientTick >= 200) break;

            if (lastTickForSkill != null) {
                int delay = lastTickForSkill - clientTick;
                map.put(delay, map.getOrDefault(delay, 0) + 1);
            }

            lastTickForSkill = clientTick;
        }

        return map;
    }

    public abstract AnimationReference getAnimationReference();

    public abstract double getPresumedCycleDuration();

    public abstract int getMissed();

    public abstract int getRolls();
}
