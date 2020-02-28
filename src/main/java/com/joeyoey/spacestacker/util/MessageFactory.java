package com.joeyoey.spacestacker.util;

import com.joeyoey.spacestacker.SpaceStacker;

import java.util.Map;

public class MessageFactory {

	
	public static Map<String, Object> test;
	private SpaceStacker plugin;
	
	
	public MessageFactory(SpaceStacker instance) {
		plugin = instance;
		
		test = plugin.getConfig().getConfigurationSection("messages").getValues(true);
	}
	
}
