package net.runelogs.event.wrapper;

import lombok.Value;
import net.runelite.api.MenuAction;

@Value
public class SafeMenuOptionClicked {
    String option, target;
    MenuAction action;
}
