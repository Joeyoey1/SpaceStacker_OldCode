package com.joeyoey.spacestacker.listeners;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.objects.StackedItem;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;

public class ItemMerge implements Listener {

    private SpaceStacker plugin;

    public ItemMerge(SpaceStacker instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onMerge(ItemMergeEvent event) {
        Item subject = event.getEntity();
        Item target = event.getTarget();

        if (plugin.getListOfItems().containsKey(subject.getUniqueId()) && plugin.getListOfItems().containsKey(target.getUniqueId())) {
            event.setCancelled(true);
        }
    }

}
