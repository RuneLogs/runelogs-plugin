package net.runelogs;

import com.google.common.collect.ImmutableSet;
import net.runelite.api.gameval.AnimationID;
import net.runelogs.event.pattern.EventPattern;
import net.runelogs.event.pattern.Predicates;

import java.util.Set;

public class StaticConstant {

    public static final int EVENT_HISTORY_LIMIT = 1000;

    public static final Set<Integer> WOODCUTTING_ANIMATION_IDS = ImmutableSet.of(
            AnimationID.HUMAN_WOODCUTTING_BRONZE_AXE,
            AnimationID.HUMAN_WOODCUTTING_IRON_AXE,
            AnimationID.HUMAN_WOODCUTTING_STEEL_AXE,
            AnimationID.HUMAN_WOODCUTTING_BLACK_AXE,
            AnimationID.HUMAN_WOODCUTTING_MITHRIL_AXE,
            AnimationID.HUMAN_WOODCUTTING_ADAMANT_AXE,
            AnimationID.HUMAN_WOODCUTTING_RUNE_AXE,
            AnimationID.HUMAN_WOODCUTTING_GILDED_AXE,
            AnimationID.HUMAN_WOODCUTTING_DRAGON_AXE,
            AnimationID.HUMAN_WOODCUTTING_TRAILBLAZER_AXE_NO_INFERNAL,
            AnimationID.HUMAN_WOODCUTTING_INFERNAL_AXE,
            AnimationID.HUMAN_WOODCUTTING_3A_AXE,
            AnimationID.HUMAN_WOODCUTTING_CRYSTAL_AXE,
            AnimationID.HUMAN_WOODCUTTING_TRAILBLAZER_RELOADED_AXE_NO_INFERNAL,
            AnimationID.HUMAN_WOODCUTTING_TRAILBLAZER_AXE,
            AnimationID.HUMAN_WOODCUTTING_TRAILBLAZER_RELOADED_AXE,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_BRONZE,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_IRON,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_STEEL,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_BLACK,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_MITHRIL,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_ADAMANT,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_RUNE,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_DRAGON,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_CRYSTAL,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_CRYSTAL_INACTIVE,
            AnimationID.FORESTRY_2H_AXE_CHOPPING_3A
    );

    public static final Set<EventPattern> WOODCUTTING_PATTERN = ImmutableSet.of(
            EventPattern.builder()
                    .tick(
                            Predicates.hitsplat(),
                            Predicates.interactingWithTarget()
                    )
                    .tick(
                            Predicates.animationEquals(-1),
                            Predicates.interactingWithNothing(),
                            Predicates.animationEquals(WOODCUTTING_ANIMATION_IDS)
                    )
                    .build(),
            EventPattern.builder()
                    .tick(
                            Predicates.hitsplat(),
                            Predicates.interactingWithTarget(),
                            Predicates.animationEquals(424)
                    )
                    .tick(
                            Predicates.interactingWithNothing()
                    )
                    .build()

    );

    public static final Set<Integer> FISHING_ANIMATION_IDS = ImmutableSet.of(
            AnimationID.HUMAN_HARPOON,
            AnimationID.HUMAN_HARPOON_BARBED,
            AnimationID.HUMAN_HARPOON_DRAGON,
            AnimationID.HUMAN_HARPOON_TRAILBLAZER_NO_INFERNAL,
            AnimationID.HUMAN_HARPOON_INFERNAL,
            AnimationID.HUMAN_HARPOON_CRYSTAL,
            AnimationID.HUMAN_HARPOON_LEAGUE_TRAILBLAZER,
            AnimationID.HUMAN_HARPOON_TRAILBLAZER_RELOADED_NO_INFERNAL,
            AnimationID.HUMAN_HARPOON_TRAILBLAZER,
            AnimationID.HUMAN_HARPOON_TRAILBLAZER_RELOADED
    );

    public static final Set<EventPattern> FISHING_PATTERN = ImmutableSet.of(
            EventPattern.builder()
                    .tick(
                            Predicates.hitsplat(),
                            Predicates.interactingWithTarget()
                    )
                    .tick(
                            Predicates.animationEquals(-1),
                            Predicates.interactingWithTarget(),
                            Predicates.animationEquals(FISHING_ANIMATION_IDS)
                    )
                    .build(),
            EventPattern.builder()
                    .tick(
                            Predicates.hitsplat(),
                            Predicates.interactingWithTarget(),
                            Predicates.animationEquals(424)
                    )
                    .tick(
                            Predicates.interactingWithTarget(),
                            Predicates.animationEquals(FISHING_ANIMATION_IDS)
                    )
                    .build()

    );
}
