package com.joeyoey.spacestacker.objects;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.events.StackedItemMergeEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class StackedItem {

	private Material mat;
	private int stackAmount;
	private Item item;
	private UUID id;
	private long creation;


	public StackedItem(Material mat, int stackAmount, Item item, UUID id, long creation) {
		super();
		this.mat = mat;
		this.stackAmount = stackAmount;
		this.item = item;
		this.id = id;
		this.creation = creation;
		updateName();
	}

	public Material getMat() {
		return mat;
	}

	public int getStackAmount() {
		return stackAmount;
	}

	public void setStackAmount(int a) {
		stackAmount = a;
	}

	public Item getItem() {
		return item;
	}

	public UUID getId() {
		return id;
	}

	public long getCreation() {
		return creation;
	}

	@SuppressWarnings("deprecation")
	public void updateName() {
		if (item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta().hasDisplayName()) {
			item.setCustomName(ChatColor.translateAlternateColorCodes('&',
					SpaceStacker.instance.getItemFormat()
							.replaceAll("%amt%", SpaceStacker.instance.numberDecFormatter(stackAmount))
							.replaceAll("%matType%", item.getItemStack().getItemMeta().getDisplayName())));
		} else {
			item.setCustomName(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getItemFormat()
					.replaceAll("%amt%", SpaceStacker.instance.numberDecFormatter(stackAmount)).replaceAll("%matType%",
							StringUtils.capitaliseAllWords(mat.toString().replaceAll("_", " ").toLowerCase()))));
		}
		// item.setCustomName(ChatColor.translateAlternateColorCodes('&', "&b&l" +
		// SpaceStacker.instance.numberDecFormatter(stackAmount) + "x "
		// + StringUtils.capitaliseAllWords(mat.toString().replaceAll("_", "
		// ").toLowerCase())));
		item.setCustomNameVisible(true);
	}

	public boolean canStackWith(StackedItem a) {
		if (this.getStackAmount() == SpaceStacker.instance.getMaxItemStack()
				|| a.getStackAmount() == SpaceStacker.instance.getMaxItemStack()) {
			return false;
		}
		if (!this.getId().equals(a.getId())) {
			if (this.getItem().getItemStack().isSimilar(a.getItem().getItemStack()) && !isNonStacable(this.getItem().getItemStack())) {
				if (this.getItem().getLocation().getWorld().equals(a.getItem().getLocation().getWorld())) {
					if (this.getItem().getLocation().distanceSquared(a.getItem().getLocation()) < SpaceStacker.instance.getMergeDist()) {
						return a.getItem().isValid() && this.getItem().isValid();
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private final boolean isNonStacable(final ItemStack itemStack) {
		if (itemStack == null)
			return false;
		final String typeNameString = itemStack.getType().name();
		return typeNameString.endsWith("_HELMET")
				|| typeNameString.endsWith("_CHESTPLATE")
				|| typeNameString.endsWith("_LEGGINGS")
				|| typeNameString.endsWith("_BOOTS")
				|| typeNameString.endsWith("SWORD")
				|| typeNameString.endsWith("PICKAXE")
				|| typeNameString.endsWith("SHOVEL")
				|| typeNameString.endsWith("HOE");
	}

	public boolean tryStack(StackedItem a) {
		if (this.canStackWith(a)) {
			int total = this.getStackAmount();
			total += a.getStackAmount();

			StackedItemMergeEvent event = new StackedItemMergeEvent(this, a);

			SpaceStacker.instance.getServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return false;
			}

			if (total > SpaceStacker.instance.getMaxItemStack()) {
				a.setStackAmount(SpaceStacker.instance.getMaxItemStack());
				this.setStackAmount(total - SpaceStacker.instance.getMaxItemStack());
				a.updateName();
				this.updateName();
				return false;
			} else {
				new BukkitRunnable() {
					@Override
					public void run() {
						getItem().remove();
					}
				}.runTask(SpaceStacker.instance);
				a.setStackAmount(getStackAmount() + a.getStackAmount());
				a.updateName();
				SpaceStacker.instance.getListOfItems().replace(a.getId(), a);
				return true;
			}
		}
		return false;
	}

}
