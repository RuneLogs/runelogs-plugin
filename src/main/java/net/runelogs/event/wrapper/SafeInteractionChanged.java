package net.runelogs.event.wrapper;

import lombok.Value;
import net.runelite.api.Actor;

@Value
public class SafeInteractionChanged {
    Actor target;
}
