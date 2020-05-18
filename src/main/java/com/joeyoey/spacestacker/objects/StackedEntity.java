package com.joeyoey.spacestacker.objects;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.events.StackedEntityMergeEvent;
import com.joeyoey.spacestacker.util.JaroAlg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class StackedEntity {

	private Entity baseEnt;
	private Material matToDrop;
	private int stackAmount;
	private UUID id;
	private ItemStack item;
	private long creationTime;

	public StackedEntity(Entity baseEnt, Material matToDrop, UUID id) {
		this.baseEnt = baseEnt;
		this.matToDrop = matToDrop;
		this.item = new ItemStack(matToDrop);
		this.stackAmount = 1;
		this.id = id;
		this.creationTime = System.currentTimeMillis();
	}

	public StackedEntity(Entity baseEnt, Material matToDrop, int stackAmount, UUID id) {
		this.baseEnt = baseEnt;
		this.matToDrop = matToDrop;
		this.item = new ItemStack(matToDrop);
		this.stackAmount = stackAmount;
		this.id = id;
		this.creationTime = System.currentTimeMillis();
	}

	public StackedEntity(Entity baseEnt, ItemStack matToDrop, int stackAmount, UUID id) {
		this.baseEnt = baseEnt;
		this.matToDrop = matToDrop.getType();
		this.item = matToDrop;
		this.stackAmount = stackAmount;
		this.id = id;
		this.creationTime = System.currentTimeMillis();
	}
	
	public Entity getBaseEnt() {
		return baseEnt;
	}

	public Material getMatToDrop() {
		return matToDrop;
	}

	public void setMatToDrop(Material matToDrop) {
		this.matToDrop = matToDrop;
	}

	public ItemStack getItemStack() {
		return this.item;
	}
	
	public int getStackAmount() {
		return stackAmount;
	}

	public void setStackAmount(int stackAmount) {
		this.stackAmount = stackAmount;
	}

	public UUID getId() {
		return id;
	}

	@SuppressWarnings("deprecation")
	public void updateName() {
		if (matToDrop.equals(Material.AIR)) {
			this.getBaseEnt()
					.setCustomName(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getEntityFormat()
							.replaceAll("%amt%", SpaceStacker.instance.numberDecFormatter(stackAmount))
							.replaceAll("%mobType%",
									StringUtils.capitaliseAllWords(
											baseEnt.getType().toString().replaceAll("_", " ").toLowerCase()))
							.replaceAll("%drop%", "")));
		} else {
			this.getBaseEnt()
			.setCustomName(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getEntityFormat()
					.replaceAll("%amt%", SpaceStacker.instance.numberDecFormatter(stackAmount))
					.replaceAll("%mobType%",
							StringUtils.capitaliseAllWords(
									baseEnt.getType().toString().replaceAll("_", " ").toLowerCase()))
					.replaceAll("%drop%", StringUtils
							.capitaliseAllWords(matToDrop.toString().replaceAll("_", " ").toLowerCase()))));
		}

		
		// this.getBaseEnt().setCustomName(ChatColor.translateAlternateColorCodes('&',
		// "&b&l"
		// + SpaceStacker.instance.numberDecFormatter(stackAmount) + "x "
		// + StringUtils.capitaliseAllWords(baseEnt.getType().toString().replaceAll("_",
		// " ").toLowerCase())));
		this.getBaseEnt().setCustomNameVisible(true);
	}

	public boolean canStackWith(StackedEntity a) {
		if (this.getStackAmount() == SpaceStacker.instance.getMaxEntityStack()
				|| a.getStackAmount() == SpaceStacker.instance.getMaxEntityStack() || a.getStackAmount() >= 10000 || this.getStackAmount() >= 10000) {
			return false;
		}
		if (System.currentTimeMillis() - this.creationTime > 600000) {
			SpaceStacker.instance.getTimedOutMobs().add(this);
			return false;
		}
		if (System.currentTimeMillis() - a.creationTime > 600000) {
			SpaceStacker.instance.getTimedOutMobs().add(a);
			return false;
		}
		if (this.baseEnt.getLocation().getWorld().equals(a.getBaseEnt().getLocation().getWorld())) {
			if (this.baseEnt.getLocation().distance(a.getBaseEnt().getLocation()) < SpaceStacker.instance.getMergeDist()
					&& !this.baseEnt.getLocation().equals(a.getBaseEnt().getLocation())) {
				if (this.baseEnt.getType().equals(a.getBaseEnt().getType())) {
					if (JaroAlg.getJaroWinkler(this.getBaseEnt().getCustomName(),
							a.getBaseEnt().getCustomName()) > .7) {
                        return this.matToDrop.equals(a.getMatToDrop());
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

	public boolean tryStack(StackedEntity a) {
		if (this.canStackWith(a)) {
			int total = this.getStackAmount();
			total += a.getStackAmount();
			StackedEntityMergeEvent event = new StackedEntityMergeEvent(this, a);

			SpaceStacker.instance.getServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return false;
			}

			if (total > SpaceStacker.instance.getMaxEntityStack()) {
				a.setStackAmount(SpaceStacker.instance.getMaxEntityStack());
				this.setStackAmount(total - SpaceStacker.instance.getMaxEntityStack());
				a.updateName();
				this.updateName();
				return false;
			} else {
				this.baseEnt.remove();
				a.setStackAmount(total);
				a.updateName();
				a.baseEnt.setMetadata("STACKED", new FixedMetadataValue(SpaceStacker.instance, true));
				SpaceStacker.instance.getListOfEnt().remove(this.id);
				SpaceStacker.instance.getListOfEnt().put(a.getId(), a);
				return true;
			}
		}
		return false;
	}

}
