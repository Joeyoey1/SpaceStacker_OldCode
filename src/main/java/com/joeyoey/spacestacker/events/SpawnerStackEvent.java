package com.joeyoey.spacestacker.events;

import com.joeyoey.spacestacker.objects.StackedSpawner;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpawnerStackEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;
	private StackedSpawner stackedSpawner;
	private StackedSpawner stackingInto;
	
	public SpawnerStackEvent(StackedSpawner stackedSpawner, StackedSpawner stackingInto) {
		this.stackedSpawner = stackedSpawner;
		this.stackingInto = stackingInto;
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


	public StackedSpawner getStackedSpawner() {
		return stackedSpawner;
	}


	public StackedSpawner getStackingInto() {
		return stackingInto;
	}
	
	
}
