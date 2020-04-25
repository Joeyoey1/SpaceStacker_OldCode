package com.joeyoey.spacestacker.listeners;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.events.SpawnerBreakEvent;
import com.joeyoey.spacestacker.events.SpawnerPlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EventTest implements Listener {


    @EventHandler
    public void onSpawnerPlace(SpawnerPlaceEvent event) {
        SpaceStacker.instance.getLogger().info(event.getLoc().toString());
        SpaceStacker.instance.getLogger().info(event.getPlacedSpawner().toString());
        event.setCancelled(true);
    }


    @EventHandler
    public void onSpawnerBreak(SpawnerBreakEvent event) {
        SpaceStacker.instance.getLogger().info(event.getLoc().toString());
        SpaceStacker.instance.getLogger().info(event.getBrokenSpawner().toString());
        event.setCancelled(true);
    }


}
