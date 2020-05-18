package com.joeyoey.spacestacker;

import com.joeyoey.spacestacker.objects.JoLocation;
import com.joeyoey.spacestacker.objects.StackedEntity;
import com.joeyoey.spacestacker.objects.StackedItem;
import com.joeyoey.spacestacker.objects.StackedSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

public class SpaceStackerApi {

    private SpaceStacker plugin;


    SpaceStackerApi(SpaceStacker instance) {
        this.plugin = instance;
    }


    public Optional<StackedItem> getStackedItem(UUID itemUUID) {
        return Optional.ofNullable(this.plugin.getListOfItems().getOrDefault(itemUUID, null));
    }

    public Optional<StackedEntity> getStackedEntity(UUID entityUUID) {
        return Optional.ofNullable(this.plugin.getListOfEnt().getOrDefault(entityUUID, null));
    }

    public Optional<StackedSpawner> getStackedSpawner(Location locationOfSpawner) {
        return Optional.ofNullable(this.plugin.getStackedSpawners().getOrDefault(JoLocation.getLocationFromLocation(locationOfSpawner), null));
    }

    public boolean removeItem(UUID itemUUID) {
        if (this.plugin.getListOfItems().containsKey(itemUUID)) {
            StackedItem stackedItem = this.plugin.getListOfItems().getOrDefault(itemUUID, null);
            if (stackedItem == null) return false;
            Bukkit.getScheduler().callSyncMethod(this.plugin, () -> {
                stackedItem.getItem().remove();
                return null;
            });
            return null != this.plugin.getListOfItems().remove(itemUUID);
        }
        return false;
    }


    public boolean removeEntity(UUID entityUUID) {
        if (this.plugin.getListOfEnt().containsKey(entityUUID)) {
            StackedEntity stackedEntity = this.plugin.getListOfEnt().getOrDefault(entityUUID, null);
            if (stackedEntity == null) return false;
            Bukkit.getScheduler().callSyncMethod(this.plugin, () -> {
                stackedEntity.getBaseEnt().remove();
                return null;
            });
            return null != this.plugin.getListOfEnt().remove(entityUUID);
        }
        return false;
    }


}
