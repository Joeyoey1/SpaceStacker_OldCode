package com.joeyoey.spacestacker.listeners;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.objects.JoLocation;
import com.joeyoey.spacestacker.objects.StackedSpawner;
import com.joeyoey.spacestacker.objects.UpgradeContainer;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

public class Place implements Listener {

    private List<String> testLore;
    private String entity;
    private int a;

    public Place() {
        testLore = SpaceStacker.instance.getConfig().getStringList("formats.tier-spawner.lore");
        entity = ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getConfig().getString("formats.tier-spawner.name"));
        String[] strName = entity.split(" ");
        int i = 0;
        for (String b : strName) {
            if (ChatColor.stripColor(b).equalsIgnoreCase("%entity%")) {
                a = i;
                break;
            }
            i++;
        }
    }

    @SuppressWarnings({"deprecation", "unused"})
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent e) {
        String debug = "6";
        if (e.getBlock().getType().equals(Material.valueOf("MOB_SPAWNER"))) {
            JoLocation jLoc = new JoLocation(e.getBlock().getLocation());
            if (ASkyBlockAPI.getInstance().playerIsOnIsland(e.getPlayer())) {
                Island island = ASkyBlockAPI.getInstance().getIslandAt(e.getBlock().getLocation());
                if (!island.getMembers().contains(e.getPlayer().getUniqueId())) {
                    if (!ASkyBlockAPI.getInstance().getCoopIslands(e.getPlayer()).contains(ASkyBlockAPI.getInstance().getIslandAt(e.getBlock().getLocation()).getCenter())) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            //TODO add item protection on kill
            CreatureSpawner cs = (CreatureSpawner) e.getBlock().getState();
            if (e.getPlayer().getItemInHand().hasItemMeta()) {
                if (e.getPlayer().getItemInHand().getItemMeta().hasLore()) {
                    String pt1 = ChatColor.stripColor(e.getPlayer().getItemInHand().getItemMeta().getDisplayName().split(" ")[a].toUpperCase());
                    String itemDisplayName = e.getPlayer().getItemInHand().getItemMeta().getDisplayName();
                    try {
                        if (ChatColor
                                .stripColor(e.getPlayer().getItemInHand().getItemMeta().getDisplayName().split(" ")[a + 1])
                                .equalsIgnoreCase("golem")
                                || ChatColor.stripColor(itemDisplayName.split(" ")[a + 1]).equalsIgnoreCase("zombie")
                                || ChatColor.stripColor(itemDisplayName.split(" ")[a + 1]).equalsIgnoreCase("skeleton")
                                || ChatColor.stripColor(itemDisplayName.split(" ")[a + 1]).equalsIgnoreCase("spider")
                                || ChatColor.stripColor(itemDisplayName.split(" ")[a + 1]).equalsIgnoreCase("cow")
                                || ChatColor.stripColor(itemDisplayName.split(" ")[a + 1]).equalsIgnoreCase("cube")) {
                            pt1 += "_" + ChatColor.stripColor(e.getPlayer().getItemInHand().getItemMeta().getDisplayName().split(" ")[a + 1].toUpperCase());
                        }
                    } catch (IndexOutOfBoundsException extra) {
                    }
                    EntityType type = EntityType.valueOf(pt1.toUpperCase());

                    debug += "1";
                    Material mat = Material.AIR;
                    int[] amount = {1};
                    // try {
                    int line = 0;
                    for (String lore : testLore) {
                        if (lore.contains("%upgrade%")) {

                            String[] up = lore.split(" ");
                            for (int i = 0; i < up.length; i++) {
                                if (up[i].contains("%upgrade%")) {

                                    String loreLine = e.getPlayer().getItemInHand().getItemMeta().getLore().get(line);
                                    String[] upgradeSpace = loreLine.split(" ");
                                    if (upgradeSpace[i].contains("DEFAULT")) {
                                        mat = Material.AIR;
                                    } else {
                                        mat = Material.getMaterial(ChatColor
                                                .stripColor(upgradeSpace[i].replaceAll(" ", "_").toUpperCase()));
                                    }
                                }
                            }
                        }

                        if (lore.contains("%amount%")) {

                            String[] up = lore.split(" ");
                            for (int i = 0; i < up.length; i++) {
                                if (up[i].contains("%amount%")) {
                                    String loreLine = e.getPlayer().getItemInHand().getItemMeta().getLore().get(line);
                                    String[] upgradeSpace = loreLine.split(" ");
                                    amount[0] = Integer.parseInt(ChatColor.stripColor(upgradeSpace[i]));
                                }
                            }
                        }
                        line++;
                    }
                    EntityType[] eType = {cs.getSpawnedType()};

                    if (!type.equals(eType[0])) {
                        eType[0] = type;
                    }
                    ItemStack is = new ItemStack(mat);
                    if (SpaceStacker.instance.getEntityUpgrades().containsKey(eType[0])) {
                        for (UpgradeContainer uCont : SpaceStacker.instance.getEntityUpgrades().get(eType[0])) {
                            if (uCont.getMat().equals(mat)) {
                                is = uCont.getItemStack();
                                break;
                            }
                        }
                    } else {
                        is = new ItemStack(Material.AIR);
                    }

                    StackedSpawner spawner = new StackedSpawner(eType[0], is, jLoc);
                    spawner.setStackAmount(amount[0]);
                    SpaceStacker.instance.getStackedSpawners().put(jLoc, spawner);
                    spawner.updateHolo();
                    CreatureSpawner spawn = (CreatureSpawner) e.getBlock().getState();
                    spawn.setSpawnedType(eType[0]);
                    spawn.update();
                    spawner.updateHolo();
                    spawner.getjLoc().getBlock().getWorld().getNearbyEntities(spawner.getjLoc().getBlock(), 5, 5, 5).forEach(entity -> {
                        if (entity instanceof Player) {
                            Player play = (Player) entity;
                            play.closeInventory();
                        }
                    });
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            CreatureSpawner spawn = (CreatureSpawner) e.getBlock().getState();
                            spawn.setSpawnedType(eType[0]);
                            spawn.update();
                            spawner.updateHolo();
                            tryAll(spawner);
                        }

                    }.runTaskLater(SpaceStacker.instance, 1);
                } else {
                    new BukkitRunnable() {
                        public void run() {
                            StackedSpawner spawner = new StackedSpawner(e.getBlock().getChunk().getX(),
                                    e.getBlock().getChunk().getZ(), 1, cs.getSpawnedType(), Material.AIR, jLoc);
                            SpaceStacker.instance.getStackedSpawners().put(jLoc, spawner);
                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    spawner.updateHolo();
                                    tryAll(spawner);
                                }

                            }.runTaskLater(SpaceStacker.instance, 1);
                        }
                    }.runTaskLater(SpaceStacker.instance, 2);
                }
            } else {
                StackedSpawner spawner = new StackedSpawner(e.getBlock().getChunk().getX(),
                        e.getBlock().getChunk().getZ(), 1, cs.getSpawnedType(), Material.AIR, jLoc);
                SpaceStacker.instance.getStackedSpawners().put(jLoc, spawner);
                spawner.updateHolo();
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        spawner.updateHolo();
                        tryAll(spawner);
                    }

                }.runTaskLater(SpaceStacker.instance, 1);
            }
        } else if (e.getBlock().getType().equals(Material.WATER)) {
            ItemStack inHand = e.getItemInHand();
            if (inHand.getType().equals(Material.BUCKET) || inHand.getType().equals(Material.WATER_BUCKET)) {
                int amount = inHand.getAmount();
                if (amount > 1) {
                    ItemStack newBucket = new ItemStack(Material.WATER_BUCKET, amount - 1);
                    ItemStack emptyBucket = new ItemStack(Material.BUCKET, 1);
                    e.getPlayer().getInventory().setItemInMainHand(newBucket);
                    Map<Integer, ItemStack> extra = e.getPlayer().getInventory().addItem(emptyBucket);
                    if (!extra.isEmpty()) {
                        for (Map.Entry<Integer, ItemStack> ex : extra.entrySet()) {
                            e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), ex.getValue());
                        }
                    }
                }
            }
            return;
        }
    }

    public void tryAll(StackedSpawner ss) {
        for (StackedSpawner s : SpaceStacker.instance.getStackedSpawners().values()) {
            if (ss.canStackWith(s)) {
                ss.tryStack(s);
                tryAll(s);
                break;
            }
        }
    }

}
