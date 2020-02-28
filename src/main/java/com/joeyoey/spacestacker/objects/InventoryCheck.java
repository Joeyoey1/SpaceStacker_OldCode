package com.joeyoey.spacestacker.objects;

import org.bukkit.entity.EntityType;

public class InventoryCheck {

	
	private EntityType eType;
	private JoLocation jLoc;
	
	
	
	public InventoryCheck(EntityType eType, JoLocation jLoc) {
		super();
		this.eType = eType;
		this.jLoc = jLoc;
	}



	public EntityType geteType() {
		return eType;
	}



	public JoLocation getjLoc() {
		return jLoc;
	}
	
	
	
	
	
	
	
	
}
