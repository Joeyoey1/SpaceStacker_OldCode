package com.joeyoey.spacestacker.listeners;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.objects.StackedItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class ItemDrop implements Listener {


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickUp(PlayerPickupItemEvent e) {
//		if (e.getEntity() instanceof Player) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public void onPickUp(PlayerPickupItemEvent e) a");
        Item item = e.getItem();
        UUID id = item.getUniqueId();
        if (SpaceStacker.instance.getListOfItems().containsKey(id)) {
            stringBuilder.append("b");
            e.setCancelled(true);
            item.getItemStack().setAmount(SpaceStacker.instance.getListOfItems().get(id).getStackAmount());
            boolean pickedUp = false;

            HashMap<Integer, ItemStack> extras = e.getPlayer().getInventory().addItem(item.getItemStack());
            if (extras.isEmpty()) {
                stringBuilder.append("c");
                SpaceStacker.instance.getListOfItems().remove(id);
                item.remove();
                pickedUp = true;
            } else {
                stringBuilder.append("d");
                int amount = 0;
                for (Map.Entry<Integer, ItemStack> entry : extras.entrySet()) {
                    amount += entry.getValue().getAmount();
                }
                if (amount <= 0) {
                    SpaceStacker.instance.getListOfItems().remove(id);
                    item.remove();
                } else {
                    SpaceStacker.instance.getListOfItems().get(id).setStackAmount(amount);
                }
            }

            if (pickedUp) {
                for (Sound sound : Sound.values()) {
                    if (sound.name().contains("PICKUP")) {
                        e.getPlayer().playSound(e.getPlayer().getLocation(), sound, 1, 0);
                        break;
                    }
                }
            }
        }
    }

//    @EventHandler
//    public void onEntityPickup(EntityPickupItemEvent event) {
//        if (!(event.getEntity() instanceof Player)) {
//            event.setCancelled(true);
//        }
//    }


    @EventHandler
    public void onDespawn(ItemDespawnEvent e) {
        UUID id = e.getEntity().getUniqueId();
        SpaceStacker.instance.getListOfItems().remove(id);
    }


    @EventHandler
    public void onInvPickup(InventoryPickupItemEvent e) {
        Item item = e.getItem();
        Inventory inv = e.getInventory();
        UUID id = item.getUniqueId();
        if (SpaceStacker.instance.getListOfItems().containsKey(id)) {
            e.setCancelled(true);
            StackedItem sI = SpaceStacker.instance.getListOfItems().get(id);
            item.getItemStack().setAmount(SpaceStacker.instance.getListOfItems().get(id).getStackAmount());

            HashMap<Integer, ItemStack> itemsa = inv.addItem(item.getItemStack());
            if (itemsa.isEmpty()) {
                SpaceStacker.instance.getListOfItems().get(id).getItem().remove();
                SpaceStacker.instance.getListOfItems().remove(id);
            } else {
                SpaceStacker.instance.getListOfItems().get(id).setStackAmount(0);
                for (ItemStack is : itemsa.values()) {
                    SpaceStacker.instance.getListOfItems().get(id).setStackAmount(SpaceStacker.instance.getListOfItems().get(id).getStackAmount() + is.getAmount());
                }
                if (SpaceStacker.instance.getListOfItems().get(id).getStackAmount() <= 0) {
                    SpaceStacker.instance.getListOfItems().get(id).getItem().remove();
                    SpaceStacker.instance.getListOfItems().remove(id);
                } else {
                    SpaceStacker.instance.getListOfItems().get(id).updateName();
                }
            }


//            for (int i = sI.getStackAmount(); i > 0; i -= 64) {
//                int x;
//                if (i > 64) {
//                    item.getItemStack().setAmount(64);
//                    x = 64;
//                } else {
//                    item.getItemStack().setAmount(i);
//                    x = i;
//                }
//            HashMap<Integer, ItemStack> itemsa = inv.addItem(item.getItemStack());
//            if (!itemsa.isEmpty()) {
//                SpaceStacker.instance.getListOfItems().get(id).setStackAmount(i - (x - itemsa.get(0).getAmount()));
//                if (SpaceStacker.instance.getListOfItems().get(id).getStackAmount() <= 0) {
//                    SpaceStacker.instance.getListOfItems().get(id).getItem().remove();
//                    SpaceStacker.instance.getListOfItems().remove(id);
//                } else {
//                    SpaceStacker.instance.getListOfItems().get(id).updateName();
//                }
//                return;
//            } else {
//                SpaceStacker.instance.getListOfItems().get(id).setStackAmount(SpaceStacker.instance.getListOfItems().get(id).getStackAmount() - i);
//                if (SpaceStacker.instance.getListOfItems().get(id).getStackAmount() <= 0) {
//                    SpaceStacker.instance.getListOfItems().get(id).getItem().remove();
//                    SpaceStacker.instance.getListOfItems().remove(id);
//                    e.getItem().remove();
//                } else {
//                    SpaceStacker.instance.getListOfItems().get(id).updateName();
//                }
//            }
//        }
            //SpaceStacker.instance.getListOfItems().get(id).getItem().remove();
            //SpaceStacker.instance.getListOfItems().remove(id);
        }
    }

    @EventHandler
    public void onDrop(ItemSpawnEvent event) {
        Item item = event.getEntity();
        UUID id = item.getUniqueId();
        ItemStack itemStack = item.getItemStack();
        Material material = itemStack.getType();
        int stackAmount = itemStack.getAmount();

        if (SpaceStacker.instance.getListOfItems().size() > 5000) {
            if (SpaceStacker.instance.getListOfItems().size() > 5000) {
                SpaceStacker.instance.getListOfItems().values().removeIf(next -> !next.getItem().isValid());
            }
            event.setCancelled(true);
        } else {
            StackedItem stackedItem = new StackedItem(material, stackAmount, item, id, System.currentTimeMillis());
            SpaceStacker.instance.getListOfItems().put(id, stackedItem);
        }
        //tryAll(sI);
    }
}
