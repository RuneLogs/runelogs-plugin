package net.runelogs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("runelogs")
public interface RuneLogsConfig extends Config {
    @ConfigItem(
            keyName = "runeLogsOverlay",
            name = "Overlay",
            description = "Whether to draw the RuneLogs overlay or not",
            position = 0
    )
    default boolean isRuneLogsOverlayEnabled() {
        return true;
    }
}
