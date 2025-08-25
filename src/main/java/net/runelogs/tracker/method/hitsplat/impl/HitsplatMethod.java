package net.runelogs.tracker.method.hitsplat.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelogs.event.pattern.EventPattern;
import net.runelogs.event.pattern.HistoricEvent;
import net.runelogs.event.wrapper.SafeStatChanged;
import net.runelogs.tracker.AnimationReference;
import net.runelogs.tracker.method.hitsplat.HitsplatTracker;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class HitsplatMethod extends HitsplatTracker {

    private AnimationReference animationReference;
    private double presumedCycleDuration;
    private boolean isOnActionCycle;

    private int rolls, missed;

    @Override
    public void onGameTickFinished(int finishedTick) {
        super.onGameTickFinished(finishedTick);

        log.info("TICK: {}", this.finishedTick);

        AnimationReference animationReference = getPresumableMethod();

        if (this.animationReference != animationReference) {
            log.info("Activity has changed, reset roll count");
            this.missed = 0;
            this.rolls = 0;
        }

        this.animationReference = animationReference;

        if (animationReference == null) {
            log.info("Unable to find animation reference");
            this.isOnActionCycle = false;
            return;
        }

        this.presumedCycleDuration = getMatchingCycleDuration(animationReference.getSkill());

        int lastHitsplatTick = getLastHitsplatTick();
        if (finishedTick != lastHitsplatTick + 1) {
            log.info("Current Cycle skipped due to hitsplat history");
            return;
        }

        List<HistoricEvent<?>> list = createEventHistory();
        if (list == null) {
            log.info("Unable to create history");
            this.isOnActionCycle = false;
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            log.info("{}: >  {}", i, list.get(i));
        }

        boolean isOnActionCycle = false;
        for (EventPattern pattern : animationReference.getPatterns()) {
            if (!pattern.matches(list)) continue;
            isOnActionCycle = true;
        }

        log.info("isOnActionCycle: {}", isOnActionCycle);

        this.isOnActionCycle = isOnActionCycle;
        if (!isOnActionCycle) this.missed++;
        else this.rolls++;
    }

    private AnimationReference getPresumableMethod() {
        for (int i = eventHistory.size() - 1; i >= 0; i--) {
            HistoricEvent<?> event = eventHistory.get(i);
            if (event.getEvent() instanceof SafeStatChanged) {
                SafeStatChanged safeStatChanged = (SafeStatChanged) event.getEvent();
                for (AnimationReference skill : AnimationReference.values()) {
                    if (skill.getSkill() == safeStatChanged.getSkill()) {
                        return skill;
                    }
                }
            }
        }
        return null;
    }

    private List<HistoricEvent<?>> createEventHistory() {
        List<HistoricEvent<?>> list = eventHistory.stream()
                .filter(event -> finishedTick - event.getClientTick() < presumedCycleDuration)
                .collect(Collectors.toList());
        if (list.isEmpty() || list.size() < 2) return null;
        return list;
    }

    @Override
    public boolean isOnCycle() {
        return isOnHitsplatCycle && isOnActionCycle;
    }
}
