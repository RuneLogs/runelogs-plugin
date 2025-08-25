package net.runelogs;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelogs.tracker.AnimationReference;
import net.runelogs.tracker.SkillingTracker;

import javax.inject.Inject;
import java.awt.*;

@Getter(AccessLevel.NONE)
public class RuneLogsOverlay extends OverlayPanel {

    private final RuneLogsPlugin plugin;

    @Inject
    public RuneLogsOverlay(RuneLogsPlugin plugin) {
        this.plugin = plugin;
        this.setPreferredPosition(OverlayPosition.TOP_LEFT);
        this.getMenuEntries().add(
                new OverlayMenuEntry(
                        MenuAction.RUNELITE_OVERLAY_CONFIG,
                        OverlayManager.OPTION_CONFIGURE,
                        "RuneLogs Overlay"
                )
        );
    }

    @Override
    public Dimension render(Graphics2D graphics2D) {
        if (!plugin.getConfig().isRuneLogsOverlayEnabled()) return super.render(graphics2D);
        for (SkillingTracker skillingTracker : plugin.getSkillingTrackerSet()) {
            if (skillingTracker.getAnimationReference() == null) continue;
            this.panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left(String.valueOf(skillingTracker.getAnimationReference().getSkill()))
                            .build()
            );
            this.panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("Cycle:")
                            .right(String.valueOf(skillingTracker.getPresumedCycleDuration()))
                            .build()
            );
            this.panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("Rolls:")
                            .right(String.valueOf(skillingTracker.getRolls()))
                            .build()
            );
            this.panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("Missed:")
                            .right(String.valueOf(skillingTracker.getMissed()))
                            .build()
            );
        }
        return super.render(graphics2D);
    }
}
