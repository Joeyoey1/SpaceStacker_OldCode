package com.joeyoey.spacestacker.listeners;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.events.SpawnerBreakEvent;
import com.joeyoey.spacestacker.objects.JoLocation;
import com.joeyoey.spacestacker.objects.StackedSpawner;
import com.joeyoey.spacestacker.util.MessageFactory;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Break implements Listener {
	
	
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		String debug = "7";

		if (e.isCancelled()) {
			debug += "1";
			if (SpaceStacker.instance.debug) {
				SpaceStacker.instance.getLogger().log(Level.SEVERE, debug);
			}
			return;
		}
		if (e.getBlock().getType().equals(Material.valueOf("MOB_SPAWNER"))) {
			debug += "2";

			JoLocation jLoc = new JoLocation(e.getBlock().getLocation());
			if (SpaceStacker.instance.getStackedSpawners().containsKey(jLoc)) {
				if (ASkyBlockAPI.getInstance().playerIsOnIsland(e.getPlayer())) {
					Island island = ASkyBlockAPI.getInstance().getIslandAt(e.getBlock().getLocation());
					if (!island.getMembers().contains(e.getPlayer().getUniqueId())) {
						if (!ASkyBlockAPI.getInstance().getCoopIslands(e.getPlayer()).contains(ASkyBlockAPI.getInstance().getIslandAt(e.getBlock().getLocation()).getCenter())) {
							e.setCancelled(true);
							return;
						}
					}
				}
				debug += "1";

				if ((e.getPlayer().hasPermission("spacestacker.silk") && e.getPlayer().getInventory().getItemInHand().getType().equals(Material.DIAMOND_PICKAXE) && e.getPlayer().getInventory().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) || e.getPlayer().isOp() || e.getPlayer().hasPermission("spacestacker.nosilk")) {
					debug += "1";

					
					
					StackedSpawner custom = SpaceStacker.instance.getStackedSpawners().get(jLoc);
					
					SpawnerBreakEvent event = new SpawnerBreakEvent(custom, e.getPlayer(), e.getBlock().getLocation());
					
					SpaceStacker.instance.getServer().getPluginManager().callEvent(event);

					if (event.isCancelled()) {
						return;
					}	
					
					Material mat = custom.getMat();
					EntityType eType = custom.getEntity();
					e.setCancelled(true);
					int amount = custom.getStackAmount();
					if ((amount == 1) || e.getPlayer().isSneaking()) {
						debug += "2";

						custom.getHolo().delete();
						
						jLoc.getBlock().getBlock().setType(Material.AIR);
						ItemStack is = new ItemStack(Material.valueOf("MOB_SPAWNER"));
						ItemMeta bMeta = is.getItemMeta();
//						CreatureSpawner cs = (CreatureSpawner) bMeta.getBlockState();
//						cs.setSpawnedType(eType);
//						bMeta.setBlockState(cs);
						bMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getConfig().getString("formats.tier-spawner.name").replaceAll("%entity%", StringUtils.capitaliseAllWords(eType.toString().toLowerCase().replaceAll("_", " ")))));
						List<String> lore = new ArrayList<String>();
						for (String s : SpaceStacker.instance.getConfig().getStringList("formats.tier-spawner.lore")) {
							if (mat.equals(Material.AIR)) {
								lore.add(ChatColor.translateAlternateColorCodes('&', s.replaceAll("%upgrade%", "DEFAULT").replaceAll("%amount%", amount + "").replaceAll("%entity%", StringUtils.capitaliseAllWords(eType.toString().toLowerCase().replaceAll("_", " ")))));
							} else {
								lore.add(ChatColor.translateAlternateColorCodes('&', s.replaceAll("%upgrade%", StringUtils.capitaliseAllWords(mat.toString().toLowerCase())).replaceAll("%amount%", amount + "").replaceAll("%entity%", StringUtils.capitaliseAllWords(eType.toString().toLowerCase().replaceAll("_", " ")))));
							}
						}
						bMeta.setLore(lore);
						is.setItemMeta(bMeta);
						e.getPlayer().getInventory().addItem(is);
						SpaceStacker.instance.getStackedSpawners().remove(jLoc);
						SpaceStacker.instance.getVisibility().remove(custom.getHolo());

						if (SpaceStacker.instance.debug) {
							SpaceStacker.instance.getLogger().log(Level.SEVERE, debug);
						}
					} else if (amount > 1) {
						debug += "3";

						
						
						
						custom.setStackAmount(amount - 1);
						custom.updateHolo();
						ItemStack is = new ItemStack(Material.valueOf("MOB_SPAWNER"));
						ItemMeta bMeta = is.getItemMeta();
//						CreatureSpawner cs = (CreatureSpawner) bMeta.getBlockState();
//						cs.setSpawnedType(eType);
//						bMeta.setBlockState(cs);
						bMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getConfig().getString("formats.tier-spawner.name").replaceAll("%entity%", StringUtils.capitaliseAllWords(eType.toString().toLowerCase().replaceAll("_", " ")))));
						List<String> lore = new ArrayList<String>();
						for (String s : SpaceStacker.instance.getConfig().getStringList("formats.tier-spawner.lore")) {
							if (mat.equals(Material.AIR)) {
								lore.add(ChatColor.translateAlternateColorCodes('&', s.replaceAll("%upgrade%", "DEFAULT").replaceAll("%amount%", 1 + "").replaceAll("%entity%", StringUtils.capitaliseAllWords(eType.toString().toLowerCase().replaceAll("_", " ")))));
							} else {
								lore.add(ChatColor.translateAlternateColorCodes('&', s.replaceAll("%upgrade%", StringUtils.capitaliseAllWords(mat.toString().toLowerCase())).replaceAll("%amount%", 1 + "").replaceAll("%entity%", StringUtils.capitaliseAllWords(eType.toString().toLowerCase().replaceAll("_", " ")))));
							}
						}
						bMeta.setLore(lore);
						is.setItemMeta(bMeta);
						e.getPlayer().getInventory().addItem(is);
						if (SpaceStacker.instance.debug) {
							SpaceStacker.instance.getLogger().log(Level.SEVERE, debug);
						}
					} else {
						debug += "4";
						if (SpaceStacker.instance.debug) {
							SpaceStacker.instance.getLogger().log(Level.SEVERE, debug);
						}
					}
				} else {
					debug += "2";
					e.setCancelled(true);
					if (SpaceStacker.instance.debug) {
						SpaceStacker.instance.getLogger().log(Level.SEVERE, debug);
					}
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', MessageFactory.test.get("no-silk").toString()));
				}
			} else {
				debug += "2";
				if ((e.getPlayer().hasPermission("spacestacker.silk") && e.getPlayer().getInventory().getItemInHand().getType().equals(Material.DIAMOND_PICKAXE) && e.getPlayer().getInventory().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) || e.getPlayer().isOp() || e.getPlayer().hasPermission("spacestacker.nosilk")) {
					e.setCancelled(true);
					CreatureSpawner cspawn = (CreatureSpawner) e.getBlock().getState();
					EntityType eType = cspawn.getSpawnedType();
					int amount = 1;

					ItemStack is = new ItemStack(Material.valueOf("MOB_SPAWNER"));
					ItemMeta bMeta = is.getItemMeta();
//				CreatureSpawner cs = (CreatureSpawner) bMeta.getBlockState();
//				cs.setSpawnedType(eType);
//				bMeta.setBlockState(cs);
					bMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getConfig().getString("formats.tier-spawner.name").replaceAll("%entity%", StringUtils.capitaliseAllWords(eType.toString().toLowerCase()))));
					List<String> lore = new ArrayList<String>();
					for (String s : SpaceStacker.instance.getConfig().getStringList("formats.tier-spawner.lore")) {
						lore.add(ChatColor.translateAlternateColorCodes('&', s.replaceAll("%upgrade%", "&c&l&oDEFAULT").replaceAll("%amount%", amount + "")));
					}
					bMeta.setLore(lore);
					is.setItemMeta(bMeta);
					e.getPlayer().getInventory().addItem(is);
					e.getBlock().setType(Material.AIR);
					if (SpaceStacker.instance.debug) {
						SpaceStacker.instance.getLogger().log(Level.SEVERE, debug);
					}
				}
			}
		} else {
			debug += "3";

			if (SpaceStacker.instance.debug) {
				SpaceStacker.instance.getLogger().log(Level.SEVERE, debug);
			}
		}
	}


	@EventHandler
	public void onCactusGrow(BlockGrowEvent event) {
		Location location = event.getBlock().getLocation().clone();
		location.subtract(0, 1,0);
		if (location.getBlock().getType().equals(Material.CACTUS)) {
			Block block = event.getBlock();
			if (!block.getRelative(BlockFace.EAST).getType().equals(Material.AIR) || !block.getRelative(BlockFace.WEST).getType().equals(Material.AIR) || !block.getRelative(BlockFace.NORTH).getType().equals(Material.AIR) || !block.getRelative(BlockFace.SOUTH).getType().equals(Material.AIR)) {
				event.setCancelled(true);
				location.subtract(.5,0,0);
				location.getWorld().dropItem(location, new ItemStack(Material.CACTUS));
			}
		}
	}

}
