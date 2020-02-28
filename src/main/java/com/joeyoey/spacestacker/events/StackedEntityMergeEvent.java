package com.joeyoey.spacestacker.events;

import com.joeyoey.spacestacker.objects.StackedEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StackedEntityMergeEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;
	private StackedEntity stackedEnt;
	private StackedEntity stackingInto;
	
	public StackedEntityMergeEvent(StackedEntity init, StackedEntity destination) {
		this.stackedEnt = init;
		this.stackingInto = destination;
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


	public StackedEntity getStackedEnt() {
		return stackedEnt;
	}


	public StackedEntity getStackingInto() {
		return stackingInto;
	}
	
	
	
}
