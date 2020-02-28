package com.joeyoey.spacestacker.events;

import com.joeyoey.spacestacker.objects.StackedItem;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StackedItemMergeEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;
	private StackedItem init;
	private StackedItem destination;
	
	
	public StackedItemMergeEvent(StackedItem init, StackedItem destinatin) {
		this.init = init;
		this.destination = destinatin;
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


	public StackedItem getInit() {
		return init;
	}


	public StackedItem getDestination() {
		return destination;
	}
	
	
	
	
}
