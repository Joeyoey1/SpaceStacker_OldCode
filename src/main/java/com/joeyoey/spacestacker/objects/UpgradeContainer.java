package com.joeyoey.spacestacker.objects;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UpgradeContainer {

	
	private int slot;
	private Material mat;
	private double cost;
	private String name;
	private String guiName;
	private ItemStack item;
	private List<String> guiLore;
	
	
	
	@SuppressWarnings("deprecation")
	public UpgradeContainer(int slot, Material mat, double cost) {
		super();
		this.slot = slot;
		this.mat = mat;
		this.item = new ItemStack(mat);
		this.cost = cost;
		this.name = StringUtils.capitaliseAllWords(mat.toString().replaceAll("_", " ").toLowerCase());
		this.guiName = name;
		this.guiLore = new ArrayList<>();
	}

	public UpgradeContainer(int slot, Material mat, double cost, List<String> guiLore) {
		super();
		this.slot = slot;
		this.mat = mat;
		this.item = new ItemStack(mat);
		this.cost = cost;
		this.name = StringUtils.capitaliseAllWords(mat.toString().replaceAll("_", " ").toLowerCase());
		this.guiName = name;
		this.guiLore = guiLore;
	}

	@SuppressWarnings("deprecation")
	public UpgradeContainer(int slot, Material mat, double cost, String name) {
		super();
		this.slot = slot;
		this.mat = mat;
		this.item = new ItemStack(mat);
		this.cost = cost;
		this.name = name.replaceAll("%type%", StringUtils.capitaliseAllWords(mat.toString().replaceAll("_", " ").toLowerCase()));
		this.guiName = name;
		this.guiLore = new ArrayList<>();
	}

	public UpgradeContainer(int slot, Material mat, double cost, String name, List<String> guiLore) {
		super();
		this.slot = slot;
		this.mat = mat;
		this.item = new ItemStack(mat);
		this.cost = cost;
		this.name = name.replaceAll("%type%", StringUtils.capitaliseAllWords(mat.toString().replaceAll("_", " ").toLowerCase()));
		this.guiName = name;
		this.guiLore = guiLore;
	}

	
	@SuppressWarnings("deprecation")
	public UpgradeContainer(int slot, ItemStack item, double cost, String guiName, List<String> guiLore) {
		this.slot = slot;
		this.mat = item.getType();
		this.item = item;
		this.cost = cost;
		this.name = (!item.hasItemMeta()) ? StringUtils.capitaliseAllWords(item.getType().toString().replaceAll("_", " ").toLowerCase()) : (!item.getItemMeta().hasDisplayName()) ? StringUtils.capitaliseAllWords(item.getType().toString().replaceAll("_", " ").toLowerCase()) : item.getItemMeta().getDisplayName();
		this.guiName = (!guiName.equals(" ")) ? guiName : StringUtils.capitaliseAllWords(item.getType().toString().replaceAll("_", " ").toLowerCase());
		this.guiLore = guiLore;
	}
	

	public int getSlot() {
		return slot;
	}

	public ItemStack getItemStack() {
		return this.item;
	}

	public Material getMat() {
		return mat;
	}

	public double getCost() {
		return cost;
	}
	
	
	public String getName() {
		return name;
	}
	
	public String getGuiName() {
		return guiName;
	}
	
	public List<String> getGuiLore() {
		return this.guiLore;
	}
	
	
	
	
	
	
}
