package net.runelogs.event.pattern;

import net.runelogs.event.wrapper.SafeAnimationChanged;
import net.runelogs.event.wrapper.SafeHitsplatApplied;
import net.runelogs.event.wrapper.SafeInteractionChanged;

import java.util.Set;

public class Predicates {
    public static EventPredicate hitsplat() {
        return e -> e.getEvent() instanceof SafeHitsplatApplied;
    }

    public static EventPredicate interactingWithTarget() {
        return e -> e.getEvent() instanceof SafeInteractionChanged &&
                ((SafeInteractionChanged) e.getEvent()).getTarget() != null;
    }

    public static EventPredicate interactingWithNothing() {
        return e -> e.getEvent() instanceof SafeInteractionChanged &&
                ((SafeInteractionChanged) e.getEvent()).getTarget() == null;
    }

    public static EventPredicate animationEquals(int animationId) {
        return e -> e.getEvent() instanceof SafeAnimationChanged &&
                ((SafeAnimationChanged) e.getEvent()).getAnimationId() == animationId;
    }

    public static EventPredicate animationEquals(Set<Integer> animationIds) {
        return e -> {
            if (!(e.getEvent() instanceof SafeAnimationChanged)) return false;
            SafeAnimationChanged wrapper = (SafeAnimationChanged) e.getEvent();
            for (int animationId : animationIds) {
                if (wrapper.getAnimationId() != animationId) continue;
                return true;
            }
            return false;
        };
    }
}
