package com.joeyoey.spacestacker.listeners;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.objects.JoLocation;
import com.joeyoey.spacestacker.objects.StackedSpawner;
import com.joeyoey.spacestacker.objects.UpgradeContainer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;


public class InvClick implements Listener {

	private String purSucc;
	private String purFail;

	public InvClick() {
		this.purSucc = SpaceStacker.instance.getConfig().getString("messages.purchase-success");
		this.purFail = SpaceStacker.instance.getConfig().getString("messages.purchase-fail");

	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getInventory() != null) {

			Player p = (Player) e.getWhoClicked();
			if (e.getInventory().equals(SpaceStacker.instance.getOpenInv().get(p.getUniqueId()))) {
				int raw = e.getRawSlot();
				e.setCancelled(true);
				JoLocation jLoc = SpaceStacker.instance.getpBound().get(p).getjLoc();
				for (UpgradeContainer uContainer : SpaceStacker.instance.getEntityUpgrades()
						.get(SpaceStacker.instance.getpBound().get(p).geteType())) {
					if (uContainer.getSlot() == raw) {

						if (uContainer.getMat() == SpaceStacker.instance.getStackedSpawners().get(jLoc).getMat()) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									SpaceStacker.instance.getConfig().getString("messages.already-upgraded")));
							return;
						} else {
							double price = uContainer.getCost() * SpaceStacker.instance.getStackedSpawners().get(jLoc).getStackAmount();
							// EconomyResponse er = SpaceStacker.instance.getEconomy().withdrawPlayer(p, price);
							if (price <= SpaceStacker.instance.getEconomy().getBalance(p)) {
								SpaceStacker.instance.getEconomy().withdrawPlayer(p, price);
								StackedSpawner ss = SpaceStacker.instance.getStackedSpawners().get(jLoc);//.put(jLoc, new StackedSpawner(SpaceStacker.instance.getpBound().get(p).geteType(), uContainer.getMat(), jLoc));
								ss.setItemStack(uContainer.getItemStack());
								SpaceStacker.instance.getStackedSpawners().put(jLoc, ss);
								ss.updateHolo();
								ss.updateHolo();
								p.sendMessage(ChatColor.translateAlternateColorCodes('&',
										purSucc.replaceAll("%upgrade%", StringUtils.capitaliseAllWords(
												uContainer.getMat().toString().toLowerCase().replaceAll("_", " ")))));
								tryAll(SpaceStacker.instance.getStackedSpawners().get(jLoc));
								p.closeInventory();
								SpaceStacker.instance.getOpenInv().remove(e.getWhoClicked().getUniqueId());
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', purFail));
								p.closeInventory();
								SpaceStacker.instance.getOpenInv().remove(e.getWhoClicked().getUniqueId());
							}
						}
						// if (er.transactionSuccess()) {
						// plugin.getSpawnerLocation().put(jLoc, new CustomSpawner(jLoc,
						// plugin.getpBound().get(p).geteType(), uContainer.getMat()));
						// p.sendMessage(ChatColor.translateAlternateColorCodes('&',
						// purSucc.replaceAll("%upgrade%",
						// StringUtils.capitaliseAllWords(uContainer.getMat().toString().toLowerCase().replaceAll("_",
						// " ")))));
						// WildStackerAPI.getStackedSpawner((CreatureSpawner)
						// jLoc.getBlock().getState()).tryStack();
						// p.closeInventory();
						// plugin.getOpenInv().remove(e.getWhoClicked().getUniqueId());
						// } else {
						// p.sendMessage(ChatColor.translateAlternateColorCodes('&', purFail));
						// p.closeInventory();
						// plugin.getOpenInv().remove(e.getWhoClicked().getUniqueId());
						// }
						break;
					}
				}
			}
		}
	}

	@EventHandler
	public void invClose(InventoryCloseEvent e) {
		if (SpaceStacker.instance.getOpenInv().containsKey(e.getPlayer().getUniqueId())) {
			if (SpaceStacker.instance.getOpenInv().containsKey(e.getPlayer().getUniqueId())) {
				if (e.getInventory().equals(SpaceStacker.instance.getOpenInv().get(e.getPlayer().getUniqueId()))) {
					SpaceStacker.instance.getOpenInv().remove(e.getPlayer().getUniqueId());
				}
			}
		} else {
			return;
		}
	}
	
	public void tryAll(StackedSpawner ss) {
		for (StackedSpawner s : SpaceStacker.instance.getStackedSpawners().values()) {
			if (ss.canStackWith(s)) {
				ss.tryStack(s);
				break;
			}
		}
	}
	
}
