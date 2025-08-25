package net.runelogs.tracker;

import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuOptionClicked;

public interface Tracker {
    void onSafeInteractionChanged(int clientTick, InteractingChanged interactingChanged);

    void onMenuOptionClicked(int clientTick, MenuOptionClicked menuOptionClicked);

    void onSelfAnimationChange(int clientTick, AnimationChanged animationChanged);

    void onSelfHitsplat(int clientTick, HitsplatApplied hitsplatApplied);

    void onExperienceGain(int clientTick, Skill skill, int experience);

    void onGameTickFinished(int finishedTick);
}
