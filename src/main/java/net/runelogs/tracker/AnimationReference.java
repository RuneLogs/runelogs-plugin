package net.runelogs.tracker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Skill;
import net.runelogs.StaticConstant;
import net.runelogs.event.pattern.EventPattern;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum AnimationReference {
    WOODCUTTING(
            Skill.WOODCUTTING,
            StaticConstant.WOODCUTTING_ANIMATION_IDS,
            StaticConstant.WOODCUTTING_PATTERN
    ),
    FISHING(
            Skill.FISHING,
            StaticConstant.FISHING_ANIMATION_IDS,
            StaticConstant.FISHING_PATTERN
    );

    private final Skill skill;
    private final Set<Integer> animationIds;
    private final Set<EventPattern> patterns;
}
