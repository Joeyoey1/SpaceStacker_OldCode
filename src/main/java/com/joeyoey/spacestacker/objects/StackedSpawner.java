package com.joeyoey.spacestacker.objects;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.events.SpawnerStackEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

public class StackedSpawner implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int chunkX;
	private int chunkZ;
	private int stackAmount;
	private EntityType entity;
	private Material mat;
	private JoLocation jLoc;
	private Hologram holo;
	private ItemStack item;
	private boolean spawnable;

	/**
	 * This is used to create a stacked spawner
	 * 
	 * This cannot be used ASYNC
	 * 
	 * @param stackAmount
	 * @param jLoc
	 */
	@SuppressWarnings("deprecation")
	public StackedSpawner(int stackAmount, EntityType ent, Material mat, JoLocation jLoc) {
		this.stackAmount = stackAmount;
		this.entity = ent;
		this.mat = mat;
		this.item = new ItemStack(mat);
		this.jLoc = jLoc;
		this.chunkX = jLoc.getBlock().getChunk().getX();
		this.chunkZ = jLoc.getBlock().getChunk().getZ();
		this.spawnable = false;

		holo = HologramsAPI.createHologram(SpaceStacker.instance, jLoc.getBlock().add(0.5, 2.0, 0.5));
		holo.appendTextLine(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getSpawnerFormat()
				.replaceAll("%amt%", SpaceStacker.instance.numberDecFormatter(stackAmount))
				.replaceAll("%mobType%",
						StringUtils.capitaliseAllWords(entity.toString().replaceAll("_", " ").toLowerCase()))
				.replaceAll("%drop%",
						StringUtils.capitaliseAllWords(this.getMat().toString().replaceAll("_", " ").toLowerCase()))));

	}

	/**
	 * This is used to create a stacked spawner
	 * 
	 * This cannot be used ASYNC
	 * 
	 * @param jLoc
	 */
	@SuppressWarnings("deprecation")
	public StackedSpawner(EntityType ent, Material mat, JoLocation jLoc) {
		this.stackAmount = 1;
		this.entity = ent;
		this.mat = mat;
		this.item = new ItemStack(mat);
		this.jLoc = jLoc;
		this.chunkX = jLoc.getBlock().getChunk().getX();
		this.chunkZ = jLoc.getBlock().getChunk().getZ();
		this.spawnable = false;


		holo = HologramsAPI.createHologram(SpaceStacker.instance, jLoc.getBlock().add(0.5, 2.0, 0.5));
		holo.appendTextLine(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getSpawnerFormat()
				.replaceAll("%amt%", SpaceStacker.instance.numberDecFormatter(stackAmount))
				.replaceAll("%mobType%",
						StringUtils.capitaliseAllWords(entity.toString().replaceAll("_", " ").toLowerCase()))
				.replaceAll("%drop%",
						StringUtils.capitaliseAllWords(this.getMat().toString().replaceAll("_", " ").toLowerCase()))));

	}
	
	
	/**
	 * This is used to create a stacked spawner
	 * 
	 * This cannot be used ASYNC
	 * 
	 * @param jLoc
	 */
	@SuppressWarnings("deprecation")
	public StackedSpawner(EntityType ent, ItemStack mat, JoLocation jLoc) {
		this.stackAmount = 1;
		this.entity = ent;
		this.mat = mat.getType();
		this.item = mat;
		this.jLoc = jLoc;
		this.chunkX = jLoc.getBlock().getChunk().getX();
		this.chunkZ = jLoc.getBlock().getChunk().getZ();
		this.spawnable = false;


		holo = HologramsAPI.createHologram(SpaceStacker.instance, jLoc.getBlock().add(0.5, 2.0, 0.5));
		holo.appendTextLine(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getSpawnerFormat()
				.replaceAll("%amt%", SpaceStacker.instance.numberDecFormatter(stackAmount))
				.replaceAll("%mobType%",
						StringUtils.capitaliseAllWords(entity.toString().replaceAll("_", " ").toLowerCase()))
				.replaceAll("%drop%",
						StringUtils.capitaliseAllWords(this.getMat().toString().replaceAll("_", " ").toLowerCase()))));

	}
	

	/**
	 * This is used to create a stacked spawner
	 * 
	 * This can be used ASYNC
	 * 
	 * @param chunkX
	 * @param stackAmount
	 * @param jLoc
	 */
	@SuppressWarnings("deprecation")
	public StackedSpawner(int chunkX, int chunkZ, int stackAmount, EntityType entityType, Material mat,
			JoLocation jLoc) {
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.stackAmount = stackAmount;
		this.entity = entityType;
		this.mat = mat;
		this.item = new ItemStack(mat);
		this.jLoc = jLoc;
		this.spawnable = false;


		holo = HologramsAPI.createHologram(SpaceStacker.instance, jLoc.getBlock().add(0.5, 2.0, 0.5));
		holo.appendTextLine(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getSpawnerFormat()
				.replaceAll("%amt%", SpaceStacker.instance.numberDecFormatter(stackAmount))
				.replaceAll("%mobType%",
						StringUtils.capitaliseAllWords(entity.toString().replaceAll("_", " ").toLowerCase()))
				.replaceAll("%drop%",
						StringUtils.capitaliseAllWords(this.getMat().toString().replaceAll("_", " ").toLowerCase()))));
		SpaceStacker.instance.getVisibility().add(holo);
	}

	public int getStackAmount() {
		return stackAmount;
	}

	public void setStackAmount(int stackAmount) {
		this.stackAmount = stackAmount;
	}

	public Material getMat() {
		return mat;
	}

	public void setMat(Material mat) {
		this.mat = mat;
	}

	public ItemStack getItemStack() {
		return this.item;
	}
	
	public void setItemStack(ItemStack item) {
		this.mat = item.getType();
		this.item = item;
	}
	
	public EntityType getEntity() {
		return entity;
	}

	public int getChunkX() {
		return chunkX;
	}

	public int getChunkZ() {
		return chunkZ;
	}

	public JoLocation getjLoc() {
		return jLoc;
	}

	public Hologram getHolo() {
		return holo;
	}

	public boolean isSpawnable() {
		this.spawnable = !this.spawnable;
		return this.spawnable;
	}

	public void createHolo() {
		holo = HologramsAPI.createHologram(SpaceStacker.instance, jLoc.getBlock().add(0.5, 2.0, 0.5));
		SpaceStacker.instance.getVisibility().add(holo);
	}

	@SuppressWarnings("deprecation")
	public boolean updateHolo() {

		holo.clearLines();
		for (int i = 0; i < holo.size(); i++) {
			holo.removeLine(0);
		}

		if (!(this.jLoc.getBlock().getBlock().getState() instanceof CreatureSpawner)) return false;

		CreatureSpawner cs = (CreatureSpawner) this.jLoc.getBlock().getBlock().getState();
		
		if (cs.getSpawnedType() != this.entity) {
			this.entity = cs.getSpawnedType();
		}
		// holo.insertTextLine(0, ChatColor.translateAlternateColorCodes('&', "&b&l" +
		// stackAmount + "x " +
		// StringUtils.capitaliseAllWords(entity.toString().replaceAll("_", "
		// ").toLowerCase()) + " &f&lSpawner"));
		holo.appendTextLine(ChatColor.translateAlternateColorCodes('&', SpaceStacker.instance.getSpawnerFormat()
				.replaceAll("%amt%", SpaceStacker.instance.numberDecFormatter(stackAmount))
				.replaceAll("%mobType%",
						StringUtils.capitaliseAllWords(entity.toString().replaceAll("_", " ").toLowerCase()))
				.replaceAll("%drop%",
						StringUtils.capitaliseAllWords(this.getMat().toString().replaceAll("_", " ").toLowerCase()))));

		//holo.appendTextLine(ChatColor.translateAlternateColorCodes('&',
		//		"&b&l" + SpaceStacker.instance.numberDecFormatter(stackAmount) + "x "
		//				+ StringUtils.capitaliseAllWords(entity.toString().replaceAll("_", " ").toLowerCase())
		//				+ " &f&lSpawner"));
		if (mat != Material.AIR && SpaceStacker.instance.isHoverUpgrade()) {
			ItemLine itemLine = holo.appendItemLine(new ItemStack(Material.BEDROCK));
			ItemStack is = new ItemStack(mat);
			is.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
			itemLine.setItemStack(is);
		}

		return true;
	}

	public boolean canStackWith(StackedSpawner a) {
		if (this.getStackAmount() == SpaceStacker.instance.getMaxSpawnerStack()
				|| a.getStackAmount() == SpaceStacker.instance.getMaxSpawnerStack()) {
			return false;
		}
		if (this.getChunkX() == a.getChunkX()) {
			if (this.getChunkZ() == a.getChunkZ()) {
				if (this.getEntity().equals(a.getEntity())) {
					if (this.getMat().equals(a.getMat())) {
						return ! this.jLoc.equals(a.getjLoc());
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

	public boolean tryStack(StackedSpawner a) {
		if (this.canStackWith(a)) {
			int stackAmt = a.getStackAmount();
			int totalAmt = stackAmt + stackAmount;
			JoLocation jl = a.getjLoc();
			SpawnerStackEvent event = new SpawnerStackEvent(this, a);

			SpaceStacker.instance.getServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return false;
			}

			if (totalAmt > SpaceStacker.instance.getMaxSpawnerStack()) {
				a.setStackAmount(SpaceStacker.instance.getMaxSpawnerStack());
				this.setStackAmount(totalAmt - SpaceStacker.instance.getMaxSpawnerStack());
				a.getHolo().clearLines();
				this.getHolo().clearLines();
				a.updateHolo();
				this.updateHolo();
				return false;
			} else {
				this.getHolo().delete();
				SpaceStacker.instance.getStackedSpawners().remove(this.getjLoc());
				a.setStackAmount(totalAmt);
				SpaceStacker.instance.getStackedSpawners().put(jl, a);
				SpaceStacker.instance.getVisibility().remove(holo);
				a.getHolo().clearLines();

				a.updateHolo();
				this.getjLoc().getBlock().getBlock().breakNaturally();
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "StackedSpawner [chunkX=" + chunkX + ", chunkZ=" + chunkZ + ", stackAmount=" + stackAmount + ", entity="
				+ entity + ", mat=" + mat + ", jLoc=" + jLoc + "]";
	}

}
