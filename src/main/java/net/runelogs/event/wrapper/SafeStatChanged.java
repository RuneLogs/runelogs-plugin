package net.runelogs.event.wrapper;

import lombok.Value;
import net.runelite.api.Skill;

@Value
public class SafeStatChanged {
    Skill skill;
    int xp;
}
