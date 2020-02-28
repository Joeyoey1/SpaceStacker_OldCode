package com.joeyoey.spacestacker.listeners;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.objects.InventoryCheck;
import com.joeyoey.spacestacker.objects.JoLocation;
import com.joeyoey.spacestacker.objects.StackedSpawner;
import com.joeyoey.spacestacker.objects.UpgradeContainer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public class Interact implements Listener {

	private String invName;
	private int invSize;
	private Material mat;
	private short data;
	private String fillName;
	private String color;
	private List<String> upgradeLore;
	private List<String> already;
	
	public Interact() {
		invName = SpaceStacker.instance.getConfig().getString("settings.inv-name");
		invSize = SpaceStacker.instance.getConfig().getInt("settings.inv-size");
		mat = Material.getMaterial(SpaceStacker.instance.getConfig().getString("settings.filler-material"));
		data = (short) SpaceStacker.instance.getConfig().getInt("settings.filler-data");
		fillName = SpaceStacker.instance.getConfig().getString("settings.filler-name");
		color = SpaceStacker.instance.getConfig().getString("settings.color-for-upgrade-text");
		upgradeLore = SpaceStacker.instance.getConfig().getStringList("settings.upgrade-lore");
		already = SpaceStacker.instance.getConfig().getStringList("settings.already-upgraded");
	}
	
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getPlayer().isSneaking()) {
			if (e.getClickedBlock().getType().equals(Material.valueOf("MOB_SPAWNER"))) {
				JoLocation jLoc = new JoLocation(e.getClickedBlock().getLocation());
				CreatureSpawner cs = (CreatureSpawner) e.getClickedBlock().getState();
				if (SpaceStacker.instance.getStackedSpawners().containsKey(jLoc)) {
					buildInvOpen(e.getPlayer(), cs.getSpawnedType(), SpaceStacker.instance.getStackedSpawners().get(jLoc));
					SpaceStacker.instance.getpBound().put(e.getPlayer(), new InventoryCheck(cs.getSpawnedType(), jLoc));
				} else if (SpaceStacker.instance.getEntityUpgrades().containsKey(cs.getSpawnedType())) {
					SpaceStacker.instance.getStackedSpawners().put(jLoc, new StackedSpawner(cs.getSpawnedType(), Material.AIR, jLoc));
					buildInvOpen(e.getPlayer(), cs.getSpawnedType(), SpaceStacker.instance.getStackedSpawners().get(jLoc));
					SpaceStacker.instance.getpBound().put(e.getPlayer(), new InventoryCheck(cs.getSpawnedType(), jLoc));
				} else {
					return;
				}
			}
		} else {
			return;
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public void buildInvOpen(Player p, EntityType eType, StackedSpawner cs) {
		if (SpaceStacker.instance.getEntityUpgrades().containsKey(eType)) {
			Inventory inv = Bukkit.createInventory(null, invSize, ChatColor.translateAlternateColorCodes('&', invName.replaceAll("%entity%", StringUtils.capitaliseAllWords(eType.toString().replaceAll("_", " ").toLowerCase()))));
			ItemStack is = new ItemStack(mat, 1, data);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(ChatColor.translateAlternateColorCodes('&', fillName));
			im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			is.setItemMeta(im);
			for (int i = 0; i < invSize; i++) {
				inv.setItem(i, is);
			}
			
			Material matToGlow = Material.AIR;
			
			try {
				matToGlow = cs.getMat();
			} catch (NullPointerException e) {
				
			}
			
			for (UpgradeContainer container : SpaceStacker.instance.getEntityUpgrades().get(eType)) {
				Material mat = container.getMat();
				int slot = container.getSlot();
				double cost = container.getCost();
				
				int stackAmount = cs.getStackAmount();
				
				ItemStack item = new ItemStack(mat);
				ItemMeta itemMeta = item.getItemMeta();
				itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', color + container.getGuiName()));
				List<String> lore = new ArrayList<String>();
				if (matToGlow.equals(mat)) {
					itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
					itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					for (String s : already) {
						lore.add(ChatColor.translateAlternateColorCodes('&', s));
					}
				} else {
					if (container.getGuiLore().isEmpty()) {
						for (String s : upgradeLore) {
							lore.add(ChatColor.translateAlternateColorCodes('&', s.replaceAll("%cost%", SpaceStacker.instance.numberDecFormatter(cost * stackAmount)).replaceAll("%costP%", SpaceStacker.instance.numberDecFormatter(cost)).replaceAll("%upgrade%", StringUtils.capitaliseAllWords(mat.toString().toLowerCase().replaceAll("_", " ")))));
						}
					} else {
						for (String s : container.getGuiLore()) {
							lore.add(ChatColor.translateAlternateColorCodes('&', s.replaceAll("%cost%", SpaceStacker.instance.numberDecFormatter(cost * stackAmount)).replaceAll("%costP%", SpaceStacker.instance.numberDecFormatter(cost)).replaceAll("%upgrade%", StringUtils.capitaliseAllWords(mat.toString().toLowerCase().replaceAll("_", " ")))));
						}
					}
				}
				itemMeta.setLore(lore);
				item.setItemMeta(itemMeta);
				inv.setItem(slot, item);
			}
			SpaceStacker.instance.getOpenInv().put(p.getUniqueId(), inv);
			p.openInventory(inv);
		}
	}
	
	
}
