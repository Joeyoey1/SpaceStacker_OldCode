package com.joeyoey.spacestacker.listeners;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.events.StackedEntityDeathEvent;
import com.joeyoey.spacestacker.objects.JoLocation;
import com.joeyoey.spacestacker.objects.StackedEntity;
import com.joeyoey.spacestacker.objects.StackedItem;
import com.joeyoey.spacestacker.objects.StackedSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnerSpawn implements Listener {

	public static Map<EntityType, Set<ItemStack>> defaultDrops = new HashMap<>();

	@EventHandler
	public void onSpawn(SpawnerSpawnEvent event) {
		try {
			JoLocation jLoc = new JoLocation(event.getSpawner().getLocation());
			if (SpaceStacker.instance.getStackedSpawners().containsKey(jLoc)) {
				StackedSpawner ss = SpaceStacker.instance.getStackedSpawners().get(jLoc);
				if (!ss.isSpawnable()) {
                    event.setCancelled(true);
                    return;
                }
					ItemStack item = ss.getItemStack();
					UUID id = event.getEntity().getUniqueId();
					int stackAmt = ss.getStackAmount();
					int[] amt = { 0 };
					CompletableFuture<Void> compFuture = CompletableFuture.runAsync(() -> amt[0] = sumEntity(stackAmt));
					compFuture.thenRun(() -> new BukkitRunnable() {

						@Override
						public void run() {
							if (amt[0] > SpaceStacker.instance.getMaxEntityStack()) {
								for (int i = amt[0]; i > 0; i -= SpaceStacker.instance.getMaxEntityStack()) {
									if (i > SpaceStacker.instance.getMaxEntityStack()) {
										Entity entity = event.getLocation().getWorld().spawnEntity(event.getLocation(),
												event.getEntityType());
										LivingEntity livingEntity = (LivingEntity) entity;
										entity.setMetadata("STACKED", new FixedMetadataValue(SpaceStacker.instance, true));
										SpaceStacker.instance.getListOfEnt().put(entity.getUniqueId(),
												new StackedEntity(entity, item,
														SpaceStacker.instance.getMaxEntityStack(),
														entity.getUniqueId()));
										SpaceStacker.instance.getListOfEnt().get(entity.getUniqueId()).updateName();
										tryAll(SpaceStacker.instance.getListOfEnt().get(entity.getUniqueId()));
									} else {
										SpaceStacker.instance.getListOfEnt().put(id,
												new StackedEntity(event.getEntity(), item, i, id));
										event.getEntity().setMetadata("STACKED", new FixedMetadataValue(SpaceStacker.instance, true));
										SpaceStacker.instance.getListOfEnt().get(id).updateName();
										tryAll(SpaceStacker.instance.getListOfEnt().get(id));
									}
								}
							} else {
								SpaceStacker.instance.getListOfEnt().put(id,
										new StackedEntity(event.getEntity(), item, amt[0], id));
								event.getEntity().setMetadata("STACKED", new FixedMetadataValue(SpaceStacker.instance, true));
								SpaceStacker.instance.getListOfEnt().get(id).updateName();
								tryAll(SpaceStacker.instance.getListOfEnt().get(id));
							}
						}

					}.runTask(SpaceStacker.instance));
			}
		} catch (NullPointerException ex) {
			UUID id = event.getEntity().getUniqueId();
			SpaceStacker.instance.getListOfEnt().put(event.getEntity().getUniqueId(),
					new StackedEntity(event.getEntity(), Material.STONE, 1, id));
			event.getEntity().setMetadata("STACKED", new FixedMetadataValue(SpaceStacker.instance, true));
			SpaceStacker.instance.getListOfEnt().get(id).updateName();
			tryAll(SpaceStacker.instance.getListOfEnt().get(id));
		}
	}

	public void tryAll(StackedEntity aa) {
		for (StackedEntity a : SpaceStacker.instance.getListOfEnt().values()) {
			if (aa.canStackWith(a)) {
				if (a.getBaseEnt().isValid() && aa.getBaseEnt().isValid()) {
					aa.tryStack(a);
					tryAll(a);
					break;
				} else if (!aa.getBaseEnt().isValid()) {
					SpaceStacker.instance.getListOfEnt().remove(aa.getId());
					try {
						aa.getBaseEnt().setCustomNameVisible(false);
					} catch (NullPointerException ignored) {
					}
					break;
				} else if (!a.getBaseEnt().isValid()) {
					SpaceStacker.instance.getListOfEnt().remove(a.getId());
					try {
						a.getBaseEnt().setCustomNameVisible(false);
					} catch (NullPointerException ignored) {
					}
					break;
				}
			}
		}
		for (StackedEntity stackedEntity : SpaceStacker.instance.getTimedOutMobs()) {
			if (stackedEntity.getBaseEnt().isValid() && !stackedEntity.getBaseEnt().isDead()) {
				SpaceStacker.instance.getListOfEnt().remove(stackedEntity.getId());
				stackedEntity.getBaseEnt().remove();
			}
		}
		SpaceStacker.instance.getTimedOutMobs().clear();
	} //

	@EventHandler
	public void onUnLoad(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		for (Entity entity : chunk.getEntities()) {
			if (entity.hasMetadata("STACKED")) {
				SpaceStacker.instance.getListOfEnt().remove(entity.getUniqueId());
				entity.setCustomNameVisible(false);
				entity.remove();
			}
		}
	}

	@EventHandler
	public void targetEvent(EntityTargetEvent e) {
		if (e.getEntity().hasMetadata("STACKED") && SpaceStacker.instance.isNerfMobs()) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void creatureSpawn(CreatureSpawnEvent e) {
		if (SpaceStacker.instance.getReasons().contains(e.getSpawnReason())) {
			SpaceStacker.instance.getListOfEnt().put(e.getEntity().getUniqueId(),
					new StackedEntity(e.getEntity(), Material.AIR, 1, e.getEntity().getUniqueId()));
			e.getEntity().setMetadata("STACKED", new FixedMetadataValue(SpaceStacker.instance, true));
			SpaceStacker.instance.getListOfEnt().get(e.getEntity().getUniqueId()).updateName();
			tryAll(SpaceStacker.instance.getListOfEnt().get(e.getEntity().getUniqueId()));
		}
	}

	private final boolean isNonStackable(final ItemStack itemStack) {
		if (itemStack == null)
			return false;
		final String typeNameString = itemStack.getType().name();

		return typeNameString.endsWith("_HELMET")
				|| typeNameString.endsWith("_CHESTPLATE")
				|| typeNameString.endsWith("_LEGGINGS")
				|| typeNameString.endsWith("_BOOTS")
				|| typeNameString.endsWith("SWORD")
				|| typeNameString.contains("BOW")
				|| typeNameString.endsWith("PICKAXE")
				|| typeNameString.endsWith("SHOVEL")
				|| typeNameString.contains("POTION")
				|| typeNameString.contains("SKULL")
				|| typeNameString.endsWith("HOE");
	}
	
	@SuppressWarnings({ "deprecation" })
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeath(EntityDeathEvent e) {
		UUID id = e.getEntity().getUniqueId();
		Player p = null;
		if (SpaceStacker.instance.getListOfEnt().containsKey(id)) {
			StackedEntity stackEnt = SpaceStacker.instance.getListOfEnt().get(id);
			int amountEnt = stackEnt.getStackAmount();

			if (e.getEntity().getKiller() != null) {
				p = e.getEntity().getKiller();
			}

			ItemStack is;
			int amount = 0;
			int multi = 0;

			int xp = e.getDroppedExp();

			if (p != null) {
				is = p.getInventory().getItemInHand();
				if (is.containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
					multi = is.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
				}
			}

			boolean all = false;
			boolean nerfDrops = false;
			if (e.getEntity().getLastDamageCause().getCause() != null && SpaceStacker.instance.getInstantAllKill()
					.contains(e.getEntity().getLastDamageCause().getCause())) {
				all = true;

				if (!e.getEntity().getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
					nerfDrops = true;
				}
			}
			if (all) {
				xp *= (amountEnt * .7);
				amount = sumItems(amountEnt, multi);
				if (nerfDrops) {
					amount *= .6;
				}
				if (amount < 1) {
					amount = 1;
				}
				ItemStack mat = stackEnt.getItemStack();
				if (mat.getType().equals(Material.AIR) || mat.getType().equals(Material.STONE)) {

					List<ItemStack> isList = e.getDrops();
					Material material = Material.STONE;

					if (!isList.isEmpty()) {

						addDefaultDrop(e.getEntityType(), isList.get(0));
						for (ItemStack itemStack : isList) {
							StackedEntityDeathEvent event = new StackedEntityDeathEvent(stackEnt, p, isNonStackable(itemStack) ? 1 : amount, itemStack, xp);
							SpaceStacker.instance.getServer().getPluginManager().callEvent(event);

							e.setDroppedExp(event.getExpDropped());
							Item item = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), event.getItemDropped());
							UUID itemId = item.getUniqueId();
							int stackAmt = event.getAmountDropped();
							StackedItem sI = new StackedItem(itemStack.getType(), stackAmt, item, itemId, System.currentTimeMillis());
							SpaceStacker.instance.getListOfItems().put(itemId, sI);
						}
						e.getDrops().clear();
					} else if (defaultDrops.containsKey(e.getEntityType())) {

						StackedEntityDeathEvent event = new StackedEntityDeathEvent(stackEnt, p, amount, getDefaultDrop(e.getEntityType()), xp);
						SpaceStacker.instance.getServer().getPluginManager().callEvent(event);

						e.setDroppedExp(event.getExpDropped());
						e.getDrops().clear();

						Item item = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), event.getItemDropped());
						UUID itemId = item.getUniqueId();
						int stackAmt = event.getAmountDropped();
						StackedItem sI = new StackedItem(material, stackAmt, item, itemId, System.currentTimeMillis());
						SpaceStacker.instance.getListOfItems().put(itemId, sI);
					}

					SpaceStacker.instance.getListOfEnt().remove(id);
					return;
				} else {

					SpaceStacker.instance.getListOfEnt().remove(id);
					StackedEntityDeathEvent event = new StackedEntityDeathEvent(stackEnt, p, amount, stackEnt.getItemStack(), xp);
					die(e, stackEnt, event);

				}
			} else {

				ItemStack mat = stackEnt.getItemStack();
				if (mat.getType().equals(Material.AIR) || mat.getType().equals(Material.STONE)) {

					List<ItemStack> isList = e.getDrops();
					Material material = Material.STONE;
					int amountItem = ThreadLocalRandom.current().nextInt(multi == 0 ? 1 : multi);
					ItemStack drop;
					StackedEntity sE = SpaceStacker.instance.getListOfEnt().get(id);
					sE.setStackAmount(sE.getStackAmount() - 1);
					if (sE.getStackAmount() > 0) {
						Entity ent = e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(),
								sE.getBaseEnt().getType());
						ent.setMetadata("STACKED", new FixedMetadataValue(SpaceStacker.instance, true));
						SpaceStacker.instance.getListOfEnt().put(ent.getUniqueId(),
								new StackedEntity(ent, mat, sE.getStackAmount(), ent.getUniqueId()));
						SpaceStacker.instance.getListOfEnt().get(ent.getUniqueId()).updateName();
					}
					SpaceStacker.instance.getListOfEnt().remove(id);


					if (!isList.isEmpty()) {

						addDefaultDrop(e.getEntityType(), isList.get(0));
						int i = 0;
						for (ItemStack itemStack : isList) {
							itemStack.setAmount(itemStack.getAmount() + ThreadLocalRandom.current().nextInt(multi == 0 ? 1 : multi));
							StackedEntityDeathEvent event = new StackedEntityDeathEvent(sE, p, itemStack.getAmount(), itemStack,
									e.getDroppedExp());
							SpaceStacker.instance.getServer().getPluginManager().callEvent(event);

							e.setDroppedExp(event.getExpDropped());
							// e.getDrops().add(new ItemStack(material, amount));
							ItemStack isa = event.getItemDropped();
							isa.setAmount((event.getAmountDropped() == 0) ? 1 : event.getAmountDropped());

							Item item = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), event.getItemDropped());
							UUID itemId = item.getUniqueId();
							Material material1 = isa.getType();
							int stackAmt = event.getAmountDropped();
							StackedItem sI = new StackedItem(material1, stackAmt, item, itemId, System.currentTimeMillis());
							SpaceStacker.instance.getListOfItems().put(itemId, sI);
						}
						e.getDrops().clear();
					}
					else {
						if (defaultDrops.containsKey(e.getEntityType())) {
							drop = getDefaultDrop(e.getEntityType());
							StackedEntityDeathEvent event = new StackedEntityDeathEvent(sE, p, amountItem, drop,
									e.getDroppedExp());
							SpaceStacker.instance.getServer().getPluginManager().callEvent(event);

							e.setDroppedExp(event.getExpDropped());
							// e.getDrops().add(new ItemStack(material, amount));
							ItemStack isa = event.getItemDropped();
							isa.setAmount((event.getAmountDropped() == 0) ? 1 : event.getAmountDropped());

							Item item = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), event.getItemDropped());
							UUID itemId = item.getUniqueId();
							Material material1 = isa.getType();
							int stackAmt = event.getAmountDropped();
							StackedItem sI = new StackedItem(material1, stackAmt, item, itemId, System.currentTimeMillis());
							SpaceStacker.instance.getListOfItems().put(itemId, sI);
						}
					}


//					e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), isa);
//					for (int i = event.getAmountDropped(); i > 0; i -= 64) {
//						if (i > 64) {
//							e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(),
//									new ItemStack(event.getMatDropped(), 64));
//						} else {
//							e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(),
//									new ItemStack(event.getMatDropped(), i));
//							break;
//						}
//					}
				} else {
					List<ItemStack> isList = e.getDrops();
					int amountItem = isList.isEmpty() ? 1 : isList.get(0).getAmount();
					StackedEntity sE = SpaceStacker.instance.getListOfEnt().get(id);
					sE.setStackAmount(sE.getStackAmount() - 1);
					if (sE.getStackAmount() > 0) {
						Entity ent = e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(),
								sE.getBaseEnt().getType());
						ent.setMetadata("STACKED", new FixedMetadataValue(SpaceStacker.instance, true));
						SpaceStacker.instance.getListOfEnt().put(ent.getUniqueId(),
								new StackedEntity(ent, mat, sE.getStackAmount(), ent.getUniqueId()));
						SpaceStacker.instance.getListOfEnt().get(ent.getUniqueId()).updateName();
					}
					SpaceStacker.instance.getListOfEnt().remove(id);

					StackedEntityDeathEvent event = new StackedEntityDeathEvent(sE, p, amountItem, sE.getItemStack(),
							e.getDroppedExp());
					die(e, sE, event);
				}
			}
		} else {
			for (ItemStack itemStack : e.getDrops()) {
				Item item = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), itemStack);
				UUID itemId = item.getUniqueId();
				Material material = itemStack.getType();
				int stackAmt = itemStack.getAmount();
				StackedItem sI = new StackedItem(material, stackAmt, item, itemId, System.currentTimeMillis());
				SpaceStacker.instance.getListOfItems().put(itemId, sI);
			}
			e.getDrops().clear();
		}
	}

	private void die(EntityDeathEvent e, StackedEntity stackEnt, StackedEntityDeathEvent event) {
		SpaceStacker.instance.getServer().getPluginManager().callEvent(event);

		e.getDrops().clear();
		e.setDroppedExp(event.getExpDropped());

		Item item = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), stackEnt.getItemStack());
		UUID itemId = item.getUniqueId();
		int stackAmt = event.getAmountDropped();
		StackedItem sI = new StackedItem(stackEnt.getItemStack().getType(), stackAmt, item, itemId, System.currentTimeMillis());
		SpaceStacker.instance.getListOfItems().put(itemId, sI);
	}

	public int sumEntity(int stackAMT) {
		int sum = 0;
		for (int i = 0; i < stackAMT; i++) {
			sum += ThreadLocalRandom.current().nextInt(1) + 1;
		}
		return sum;
	}

	public int sumItems(int stackAMT, int multi) {
		int sum = 0;
		for (int i = 0; i < stackAMT; i++) {
			sum += ThreadLocalRandom.current().nextInt(1) + 1 + multi;
		}
		return sum;
	}


	public void addDefaultDrop(EntityType entityType, ItemStack itemStack) {
		if (defaultDrops.containsKey(entityType)) {
			defaultDrops.get(entityType).add(itemStack);
		} else {
			Set<ItemStack> drops = new HashSet<>();
			drops.add(itemStack);
			defaultDrops.put(entityType, drops);
		}
	}


	public ItemStack getDefaultDrop(EntityType entityType) {
		if (defaultDrops.containsKey(entityType)) {
			int amt = defaultDrops.get(entityType).size();
			int rand = ThreadLocalRandom.current().nextInt(amt);
			int i = 0;
			for (ItemStack itemStack : defaultDrops.get(entityType)) {
				if (i == rand) {
					return itemStack;
				}
				i++;
			}

		}

		return null;
	}

}
