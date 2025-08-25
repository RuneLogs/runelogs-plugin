package net.runelogs;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.function.Function;

public class Transformer {

    public static final Function<AnimationChanged, String> ANIMATION_CHANGED = event -> {
        return transform(event, event.getActor().getAnimation());
    };

    public static final Function<GameStateChanged, String> GAME_STATE_CHANGED = event -> {
        return transform(event, event.getGameState());
    };

    public static final Function<ItemContainerChanged, String> ITEM_CONTAINER_CHANGED = event -> {
        int containerId = event.getContainerId();
        ItemContainer itemContainer = event.getItemContainer();
        String[] items = new String[itemContainer.size()];
        Item[] itemContainerItems = itemContainer.getItems();
        for (int i = 0; i < itemContainerItems.length; i++) {
            Item item = itemContainerItems[i];
            items[i] = String.join(
                    ":",
                    String.valueOf(item.getId()),
                    String.valueOf(item.getQuantity())
            );
        }
        return transform(
                event,
                String.join(
                        " ",
                        String.valueOf(containerId),
                        Arrays.toString(items)
                )
        );
    };

    public static final Function<MenuOptionClicked, String> MENU_OPTION_CLICKED = event -> {
        return transform(
                event,
                String.join(
                        " ",
                        event.getMenuOption(),
                        event.getMenuTarget().replaceAll("(<.*?>)", ""),
                        event.getMenuAction().name()
                )
        );
    };

    public static final Function<VarbitChanged, String> VARBIT_CHANGED = event -> {
        return transform(
                event,
                String.join(
                        " ",
                        String.valueOf(event.getVarbitId()),
                        String.valueOf(event.getVarpId()),
                        String.valueOf(event.getValue())
                )
        );
    };

    public static final Function<HitsplatApplied, String> HITSPLAT_APPLIED = event -> {
        Hitsplat hitsplat = event.getHitsplat();
        return transform(
                event,
                String.join(
                        " ",
                        String.valueOf(hitsplat.getHitsplatType()),
                        String.valueOf(hitsplat.getAmount())
                )
        );
    };

    public static final Function<WorldPoint, String> WORLD_POINT = object -> {
        return transform(
                object,
                String.join(
                        " ",
                        String.valueOf(object.getX()),
                        String.valueOf(object.getY()),
                        String.valueOf(object.getPlane())
                )
        );
    };

    public static final Function<Pair<StatChanged, Integer>, String> STAT_CHANGED = pair -> {
        StatChanged statChanged = pair.getLeft();
        Integer previousXP = pair.getRight();
        if (previousXP == null) previousXP = 0;
        return transform(
                statChanged,
                String.join(
                        " ",
                        String.valueOf(statChanged.getSkill()),
                        String.valueOf(statChanged.getBoostedLevel()),
                        String.valueOf(statChanged.getLevel()),
                        String.valueOf(statChanged.getXp()),
                        String.valueOf(previousXP)
                )
        );
    };

    public static final Function<InteractingChanged, String> INTERACTING_CHANGED = event -> {
        Actor target = event.getTarget();
        if (target == null) {
            return transform(event, "null:null");
        } else if (target instanceof NPC) {
            return transform(event, String.join(":", "npc", String.valueOf(((NPC) target).getId())));
        } else if (target instanceof Player) {
            return transform(event, String.join(":", "player", target.getName()));
        } else {
            return transform(event, String.join(":", "unknown", target.getName()));
        }
    };

    private static String transform(Object klass, Object message) {
        return String.join(" ", klass.getClass().getSimpleName(), message.toString());
    }
}
