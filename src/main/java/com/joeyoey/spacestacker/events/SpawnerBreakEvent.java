package com.joeyoey.spacestacker.events;

import com.joeyoey.spacestacker.objects.StackedSpawner;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpawnerBreakEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;
	private StackedSpawner brokenSpawner;
	private Player player;
	private Location loc;
	
	
	public SpawnerBreakEvent(StackedSpawner broken, Player player, Location loc) {
		this.brokenSpawner = broken;
		this.player = player;
		this.loc = loc;
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



	public StackedSpawner getBrokenSpawner() {
		return brokenSpawner;
	}



	public Player getPlayer() {
		return player;
	}



	public Location getLoc() {
		return loc;
	}

	
	
}
