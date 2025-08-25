package net.runelogs.tracker;

import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;

public interface Tracker {
    void onSafeInteractionChanged(int clientTick, InteractingChanged interactingChanged);

    void onSelfAnimationChange(int clientTick, AnimationChanged animationChanged);

    void onSelfHitsplat(int clientTick, HitsplatApplied hitsplatApplied);

    void onExperienceGain(int clientTick, Skill skill, int experience);

    void onGameTickFinished(int finishedTick);
}
