package com.joeyoey.spacestacker.events;

import com.joeyoey.spacestacker.objects.StackedEntity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class StackedEntityDeathEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;
	private StackedEntity dead;
	private Player killer;
	private int amountDropped;
	private ItemStack itemDropped;
	private int expDropped;

	public StackedEntityDeathEvent(StackedEntity dead, Player killer, int amountDropped, ItemStack itemDropped, int expDropped) {
		this.dead = dead;
		this.killer = killer;
		this.amountDropped = amountDropped;
		this.itemDropped = itemDropped;
		this.expDropped = expDropped;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public int getAmountDropped() {
		return amountDropped;
	}

	public void setAmountDropped(int amountDropped) {
		this.amountDropped = amountDropped;
	}

	public int getExpDropped() {
		return expDropped;
	}

	public void setExpDropped(int expDropped) {
		this.expDropped = expDropped;
	}

	public StackedEntity getDead() {
		return dead;
	}

	public Player getKiller() {
		return killer;
	}

	public ItemStack getItemDropped() {
		return itemDropped;
	}

	public void setItemDropped(ItemStack itemDropped) {
		this.itemDropped = itemDropped;
	}
}
