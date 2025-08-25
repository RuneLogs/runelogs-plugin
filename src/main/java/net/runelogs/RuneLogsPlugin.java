package net.runelogs;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelogs.tracker.SkillingTracker;
import net.runelogs.tracker.method.hitsplat.impl.HitsplatMethod;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@PluginDescriptor(
        name = "RuneLogs",
        description = "tick tracking done right",
        tags = {"ehp", "skill", "minmax", "wise", "wom", "temple", "rune", "logs"}
)
public class RuneLogsPlugin extends Plugin {
    @Getter(AccessLevel.NONE)
    private Map<Skill, Integer> previousSkillExpTable;

    @Getter(AccessLevel.NONE)
    private FileWriter fileWriter;

    @Getter(AccessLevel.NONE)
    private Queue<Log> queue;

    @Getter(AccessLevel.NONE)
    private File directory;

    @Getter(AccessLevel.NONE)
    private Path latest;

    @Getter(AccessLevel.NONE)
    private int clientTick, regionId, worldX, worldY, worldZ;

    @Getter(AccessLevel.PUBLIC)
    private final Set<SkillingTracker> skillingTrackerSet = ImmutableSet.of(
            new HitsplatMethod()
    );

    @Inject
    @Getter(AccessLevel.PUBLIC)
    private RuneLogsConfig config;

    @Inject
    private Client client;

    @Inject
    private RuneLogsOverlay basicOverlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ScheduledExecutorService executor;

    @Override
    protected void startUp() throws Exception {
        this.overlayManager.add(basicOverlay);
        this.previousSkillExpTable = new EnumMap<>(Skill.class);

        this.directory = new File(RuneLite.RUNELITE_DIR, "runelogs");
        log.info(directory.getAbsolutePath());
        this.directory.mkdirs();
        this.rotate();

        this.queue = new LinkedList<>();
        this.fileWriter = new FileWriter(latest.toFile(), true);
        this.enqueue("version", "1.0.0");
        this.flushQueue();
    }

    @Override
    protected void shutDown() throws Exception {
        this.overlayManager.remove(basicOverlay);
        this.flushQueue();
        this.previousSkillExpTable = null;
        this.fileWriter = null;
        this.directory = null;
        this.queue = null;
    }

    private void enqueue(String message) {
        this.queue.offer(new Log(System.currentTimeMillis(), message, clientTick));
    }

    private void enqueue(String... arguments) {
        this.queue.offer(new Log(System.currentTimeMillis(), String.join(" ", arguments), clientTick));
    }

    private void flushQueue() {
        try {
            while (!queue.isEmpty()) {
                fileWriter.write(queue.poll().toString() + System.lineSeparator());
            }
            fileWriter.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void rotate() {
        this.latest = directory.toPath().resolve("latest.log");
        if (!Files.exists(latest)) return;
        String timestamp;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(latest.toFile(), "r")) {
            timestamp = randomAccessFile.readLine().split("\\s+")[1];
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (timestamp == null) timestamp = UUID.randomUUID().toString();
        try {
            Path target = directory.toPath().resolve(
                    String.join(
                            ".",
                            timestamp,
                            "log"
                    )
            );
            Files.move(latest, target);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        this.clientTick = client.getTickCount();

        WorldPoint worldPoint = client.getLocalPlayer().getWorldLocation();
        LocalPoint localPoint = client.getLocalPlayer().getLocalLocation();

        if (worldX != worldPoint.getX() || worldY != worldPoint.getY() || worldZ != worldPoint.getPlane()) {
            this.enqueue(Transformer.WORLD_POINT.apply(worldPoint));
            this.worldZ = worldPoint.getPlane();
            this.worldX = worldPoint.getX();
            this.worldY = worldPoint.getY();
        }

        int currentRegionId = localPoint == null ? -1 : worldPoint.getRegionID();
        if (regionId != currentRegionId) {
            this.enqueue("region", String.valueOf(regionId));
            this.regionId = currentRegionId;
        }

        for (SkillingTracker tracker : skillingTrackerSet) {
            tracker.onGameTickFinished(clientTick - 1);
        }

        this.flushQueue();
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        this.enqueue(Transformer.VARBIT_CHANGED.apply(varbitChanged));
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
        if (hitsplatApplied.getActor() != client.getLocalPlayer()) return;
        this.enqueue(Transformer.HITSPLAT_APPLIED.apply(hitsplatApplied));
        for (SkillingTracker tracker : skillingTrackerSet) {
            tracker.onSelfHitsplat(clientTick, hitsplatApplied);
        }
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged interactingChanged) {
        if (interactingChanged.getSource() != client.getLocalPlayer()) return;
        this.enqueue(Transformer.INTERACTING_CHANGED.apply(interactingChanged));
        for (SkillingTracker tracker : skillingTrackerSet) {
            tracker.onSafeInteractionChanged(clientTick, interactingChanged);
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
        int containerId = itemContainerChanged.getContainerId();
        if (containerId != InventoryID.INV && containerId != InventoryID.WORN) return;
        this.enqueue(Transformer.ITEM_CONTAINER_CHANGED.apply(itemContainerChanged));
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged animationChanged) {
        if (animationChanged.getActor() != client.getLocalPlayer()) return;
        this.enqueue(Transformer.ANIMATION_CHANGED.apply(animationChanged));
        for (SkillingTracker tracker : skillingTrackerSet) {
            tracker.onSelfAnimationChange(clientTick, animationChanged);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        this.enqueue(Transformer.GAME_STATE_CHANGED.apply(gameStateChanged));
        if (gameStateChanged.getGameState() != GameState.LOGGED_IN) return;
        this.executor.schedule(() -> {
            this.enqueue("username", client.getLocalPlayer().getName());
        }, 600, TimeUnit.MILLISECONDS);
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        final Skill skill = statChanged.getSkill();
        final int xp = statChanged.getXp();

        Integer previous = previousSkillExpTable.put(skill, xp);

        this.enqueue(Transformer.STAT_CHANGED.apply(Pair.of(statChanged, previous)));

        if (previous == null) return;

        final int gained = xp - previous;
        if (gained == 0) return;

        for (SkillingTracker tracker : skillingTrackerSet) {
            tracker.onExperienceGain(clientTick, skill, gained);
        }
    }

    @Provides
    RuneLogsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(RuneLogsConfig.class);
    }
}
