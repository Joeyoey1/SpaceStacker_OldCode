package com.joeyoey.spacestacker.commands;

import com.joeyoey.spacestacker.SpaceStacker;
import com.joeyoey.spacestacker.objects.UpgradeContainer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StackerCommands implements CommandExecutor, TabCompleter {

	private SpaceStacker plugin;

	public StackerCommands(SpaceStacker instance) {
		this.plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("spacespawners")) {
			if (sender.hasPermission("spacespawners.admin")) {
				if (args.length >= 4) {
					if (args[0].equalsIgnoreCase("give")) {
						Player p = null;
						int amount = 1;
						Material mat = null;
						EntityType eType = null;
						try {
							p = Bukkit.getPlayer(args[1]);
						} catch (Exception e) {
							sender.sendMessage(ChatColor.RED + "That player doesnt exist...");
							return false;
						}

						if (args.length == 4) {
							try {
								eType = EntityType.valueOf(args[2].toUpperCase());
								amount = Integer.parseInt(args[3]);
							} catch (Exception e) {
								amount = 1;
							}
							mat = Material.AIR;
						} else {

							try {
								mat = Material.getMaterial(args[3].toUpperCase());
								eType = EntityType.valueOf(args[2].toUpperCase());
							} catch (Exception e) {
								sender.sendMessage(ChatColor.RED + "The material or entity is invalid.");
								return false;
							}

							try {
								amount = Integer.parseInt(args[4]);
							} catch (Exception e) {
								amount = 1;
							}
						}
						if (plugin.getEntityUpgrades().containsKey(eType)) {
							for (UpgradeContainer upContain : plugin.getEntityUpgrades().get(eType)) {
								if (upContain.getItemStack().getType() == mat || mat.equals(Material.AIR)) {
									ItemStack is = new ItemStack(Material.valueOf("SPAWNER"));
									ItemMeta im = is.getItemMeta();
									String name = plugin.getConfig().getString("formats.tier-spawner.name");
									List<String> lore = plugin.getConfig().getStringList("formats.tier-spawner.lore");

									im.setDisplayName(ChatColor.translateAlternateColorCodes('&',
											name.replaceAll("%entity%", StringUtils.capitaliseAllWords(
													eType.toString().replaceAll("_", " ").toLowerCase()))));
									List<String> lore2Add = new ArrayList<String>();
									for (String lorePiece : lore) {
										if (mat.equals(Material.AIR)) {
											lore2Add.add(ChatColor.translateAlternateColorCodes('&', lorePiece
													.replaceAll("%upgrade%", "DEFAULT")
													.replaceAll("%amount%", amount + "")
													.replaceAll("%entity%", StringUtils.capitaliseAllWords(
															eType.toString().toLowerCase().replaceAll("_", " ")))));
										} else {
											lore2Add.add(
													ChatColor
															.translateAlternateColorCodes('&',
																	lorePiece
																			.replaceAll("%upgrade%",
																					StringUtils.capitaliseAllWords(mat
																							.toString().toLowerCase()))
																			.replaceAll("%amount%", amount + "")
																			.replaceAll("%entity%",
																					StringUtils.capitaliseAllWords(eType
																							.toString().toLowerCase()
																							.replaceAll("_", " ")))));
										}
									}

									im.setLore(lore2Add);
									is.setItemMeta(im);
									Map<Integer, ItemStack> leftover = p.getInventory().addItem(is);
									if (!leftover.isEmpty()) {
										for (Map.Entry<Integer, ItemStack> entry : leftover.entrySet()) {
											p.getWorld().dropItemNaturally(p.getLocation(), entry.getValue());
										}
									}
									break;
								}
							}
						} else {
							ItemStack is = new ItemStack(Material.valueOf("SPAWNER"));
							ItemMeta im = is.getItemMeta();
						
							String name = plugin.getConfig().getString("formats.tier-spawner.name");
							List<String> lore = plugin.getConfig().getStringList("formats.tier-spawner.lore");

							im.setDisplayName(
									ChatColor.translateAlternateColorCodes('&', name.replaceAll("%entity%", StringUtils
											.capitaliseAllWords(eType.toString().replaceAll("_", " ").toLowerCase()))));
							List<String> lore2Add = new ArrayList<String>();
							for (String lorePiece : lore) {
								lore2Add.add(ChatColor.translateAlternateColorCodes('&',
										lorePiece.replaceAll("%upgrade%", "DEFAULT").replaceAll("%amount%", amount + "")
												.replaceAll("%entity%", StringUtils.capitaliseAllWords(
														eType.toString().toLowerCase().replaceAll("_", " ")))));

							}

							im.setLore(lore2Add);
							is.setItemMeta(im);
							p.getInventory().addItem(is);
							Map<Integer, ItemStack> leftover = p.getInventory().addItem(is);
							if (!leftover.isEmpty()) {
								for (Map.Entry<Integer, ItemStack> entry : leftover.entrySet()) {
									p.getWorld().dropItemNaturally(p.getLocation(), entry.getValue());
								}
							}
						}
						return false;
					} else {
						sender.sendMessage(ChatColor.RED
								+ "Use the command like this: /spacespawners give <player> <entitytype> <material> [amount]");
					}
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("reload")) {
						SpaceStacker.instance.loadCustomSpawnerTypes();
						SpaceStacker.instance.loadSettings();
						sender.sendMessage(ChatColor.BLUE + "Successfully reloaded SpaceStacker!");
						return false;
					}
				} else {
					if (sender instanceof Player) {
						sender.sendMessage(((Player) sender).getLocation().getChunk().getEntities().length + " entities, " + ((Player) sender).getLocation().getChunk().getTileEntities().length + " tileEntities");
					}
					sender.sendMessage(ChatColor.RED
							+ "Use the command like this: /spacespawners give <player> <entitytype> <material> [amount]");
				}
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("spacespawners")) {
			if (sender.hasPermission("spacespawners.give")) {
				if (args.length == 1) {
					List<String> out = new ArrayList<>();
					if (!args[0].equals("")) {
						out.add("give");
						out.add("reload");
					} else {
						out.add("give");
						out.add("reload");
					}
					Collections.sort(out);
					return out;
				} else if (args.length == 3) {
					if (args[0].equalsIgnoreCase("give")) {
						List<String> out = new ArrayList<String>();

						if (!args[2].equals("")) {
							plugin.getEntityUpgrades().keySet().forEach(s -> {
								if (s.toString().toLowerCase().startsWith(args[2].toLowerCase())) {
									out.add(s.toString().toLowerCase());
								}
							});
						} else {
							plugin.getEntityUpgrades().keySet().forEach(s -> {
								out.add(s.toString().toLowerCase());
							});
						}

						Collections.sort(out);

						return out;
					}
				} else if (args.length == 4) {
					if (args[0].equalsIgnoreCase("give")) {
						List<String> out = new ArrayList<String>();

						if (!args[3].equals("")) {
							plugin.getEntityUpgrades().get(EntityType.valueOf(args[2].toUpperCase())).forEach(s -> {
								if (s.getMat().toString().toLowerCase().startsWith(args[3].toLowerCase())) {
									out.add(s.getMat().toString().toLowerCase());
								}
							});
						} else {
							plugin.getEntityUpgrades().get(EntityType.valueOf(args[2].toUpperCase())).forEach(s -> {
								out.add(s.getMat().toString().toLowerCase());
							});
						}

						Collections.sort(out);

						return out;
					}
				}
			}
		}
		return null;
	}

}
