package net.runelogs.tracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelogs.StaticConstant;
import net.runelogs.event.pattern.HistoricEvent;
import net.runelogs.event.wrapper.*;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class AbstractTracker implements Tracker {

    protected final BoundedList<HistoricEvent<?>> eventHistory = new BoundedList<>(
            StaticConstant.EVENT_HISTORY_LIMIT
    );

    protected final AtomicInteger internal = new AtomicInteger(0);
    protected int currentTick, finishedTick;

    private void add(HistoricEvent<?> event) {
        this.eventHistory.add(event);
    }

    @Override
    public void onGameTickFinished(int finishedTick) {
        this.currentTick = finishedTick + 1;
        this.finishedTick = finishedTick;
    }

    @Override
    public void onMenuOptionClicked(int clientTick, MenuOptionClicked menuOptionClicked) {
        this.add(
                new HistoricEvent<>(
                        internal.getAndIncrement(),
                        clientTick,
                        new SafeMenuOptionClicked(
                                menuOptionClicked.getMenuOption(),
                                menuOptionClicked.getMenuTarget().replaceAll("(<.*?>)", ""),
                                menuOptionClicked.getMenuAction()
                        )
                )
        );
    }

    @Override
    public void onSafeInteractionChanged(int clientTick, InteractingChanged interactingChanged) {
        this.add(
                new HistoricEvent<>(
                        internal.getAndIncrement(),
                        clientTick,
                        new SafeInteractionChanged(interactingChanged.getTarget())
                )
        );
    }

    @Override
    public void onSelfAnimationChange(int clientTick, AnimationChanged animationChanged) {
        this.add(
                new HistoricEvent<>(
                        internal.getAndIncrement(),
                        clientTick,
                        new SafeAnimationChanged(animationChanged.getActor().getAnimation())
                )
        );
    }

    @Override
    public void onSelfHitsplat(int clientTick, HitsplatApplied hitsplatApplied) {
        this.add(
                new HistoricEvent<>(internal.getAndIncrement(), clientTick, new SafeHitsplatApplied())
        );
    }

    @Override
    public void onExperienceGain(int clientTick, Skill skill, int experience) {
        this.add(
                new HistoricEvent<>(
                        internal.getAndIncrement(),
                        clientTick,
                        new SafeStatChanged(skill, experience)
                )
        );
    }

    public abstract boolean isOnCycle();
}
