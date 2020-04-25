package com.joeyoey.spacestacker;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.joeyoey.spacestacker.commands.StackerCommands;
import com.joeyoey.spacestacker.listeners.*;
import com.joeyoey.spacestacker.objects.*;
import com.joeyoey.spacestacker.util.MessageFactory;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SpaceStacker extends JavaPlugin {

    public static SpaceStacker instance;
    public static int mergeDist;
    public static boolean itemStack;
    public boolean debug;
    private Map<JoLocation, StackedSpawner> stackedSpawners = new HashMap<>();
    private Map<UUID, StackedEntity> listOfEnt = new HashMap<>();
    private Map<Player, InventoryCheck> pBound = new HashMap<>();
    private Map<EntityType, List<UpgradeContainer>> entityUpgrades = new HashMap<>();
    private Map<UUID, Material> mobDrops = new HashMap<>();
    private Economy economy = null;
    private Map<UUID, Inventory> openInv = new HashMap<>();
    private List<Hologram> visibility = new ArrayList<>();
    private Map<UUID, StackedItem> listOfItems = new ConcurrentHashMap<>();
    private File playerBalanceConfigFile;
    private FileConfiguration playerBalanceConfig;
    private int saveFrequency;
    private int holoViewDist;
    private List<DamageCause> instantAllKill = new ArrayList<>();
    private int maxSpawnerStack;
    private int maxItemStack;
    private int maxEntityStack;
    private String entityFormat;
    private String spawnerFormat;
    private String itemFormat;
    private boolean hoverUpgrade;
    private Set<SpawnReason> reasons = new HashSet<>();
    private BukkitTask itemTask;
    private int times;
    private Set<StackedEntity> timedOutMobs = new HashSet<>();


    public void onEnable() {
        debug = false;
        instance = this;
        times = 0;
        saveDefaultConfig();
        itemStack = getConfig().getBoolean("settings.itemstack-mode");
        debug = getConfig().getBoolean("settings.debug");
        createPBConfig();
        loadSettings();
        loadData();

        setupEconomy();

        getServer().getPluginManager().registerEvents(new Interact(), this);
        getServer().getPluginManager().registerEvents(new InvClick(), this);
        getServer().getPluginManager().registerEvents(new Place(), this);
        getServer().getPluginManager().registerEvents(new Break(), this);
        getServer().getPluginManager().registerEvents(new SpawnerSpawn(), this);
        getServer().getPluginManager().registerEvents(new ItemDrop(), this);
        getServer().getPluginManager().registerEvents(new ItemMerge(this), this);
        //getServer().getPluginManager().registerEvents(new EventTest(), this);

        getCommand("spacespawners").setExecutor(new StackerCommands(this));
        getCommand("spacespawners").setTabCompleter(new StackerCommands(this));

        loadCustomSpawnerTypes();

        new BukkitRunnable() {

            @Override
            public void run() {
                saveTask();
            }

        }.runTaskLater(this, 500);


        @SuppressWarnings("unused")
        MessageFactory mF = new MessageFactory(this);
        // setupSpawners();

    }

    public void saveTask() {
        new BukkitRunnable() {

            @Override
            public void run() {
                getLogger().log(Level.INFO, ChatColor.DARK_PURPLE + "Saving Spawner data");
                saveData();
                getLogger().log(Level.INFO, ChatColor.LIGHT_PURPLE + "Spawner data saved!");
            }

        }.runTaskTimerAsynchronously(this, 0, saveFrequency);
        new BukkitRunnable() {

            @Override
            public void run() {
                for (Hologram h : visibility) {
                    h.getVisibilityManager().setVisibleByDefault(false);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (h.getLocation().getWorld().equals(p.getLocation().getWorld())) {
                            if (h.getLocation().distance(p.getLocation()) < holoViewDist) {
                                h.getVisibilityManager().showTo(p);
                            } else {
                                h.getVisibilityManager().hideTo(p);
                            }
                        }
                    }
                }
            }

        }.runTaskTimer(this, 0, 40);
        itemTask = new BukkitRunnable() {
            @Override
            public void run() {
//                listOfItems.putAll(toADD);
//                toADD.clear();
                try {
                    for (Iterator<StackedItem> item = listOfItems.values().iterator(); item.hasNext(); ) {
                        StackedItem stackedItem = item.next();
                        if ((System.currentTimeMillis() - stackedItem.getCreation()) > 60000 || stackedItem.getItem().getLocation().getY() < 0) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    stackedItem.getItem().remove();
                                }
                            }.runTask(SpaceStacker.instance);
                            item.remove();
                            continue;
                        }
                        if (! stackedItem.getItem().isValid() || tryAll(stackedItem)) {
                            item.remove();
                        }
                    }
                    //listOfItems.values().removeIf(aa -> !aa.getItem().isValid() || tryAll(aa));
                } catch (NullPointerException ignored) {
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 20);
    }

    public void onDisable() {
        instance = null;
        getLogger().log(Level.INFO, "Stopping Item Task...");
        itemTask.cancel();
        while (! itemTask.isCancelled()) {
            itemTask.cancel();
        }
        for (StackedEntity e : listOfEnt.values()) {
            e.getBaseEnt().remove();
        }
        for (StackedItem items : listOfItems.values()) {
            items.getItem().remove();
        }
        getLogger().log(Level.WARNING, "Saving data!");
        saveData();
        getLogger().log(Level.WARNING, "Data saved!");
    }

    public void loadData() {
        new BukkitRunnable() {

            @Override
            public void run() {
                try {
//					if (itemStack) {
                    for (String s : getPBConfig().getConfigurationSection("Spawners").getKeys(false)) {
                        String[] jLocationSplit = s.split(",");
                        String world = jLocationSplit[0];
                        int x = Integer.parseInt(jLocationSplit[1]);
                        int y = Integer.parseInt(jLocationSplit[2]);
                        int z = Integer.parseInt(jLocationSplit[3]);
                        JoLocation jLoc = new JoLocation(world, x, y, z);
//							int chunkX = getPBConfig().getInt("Spawners." + s + ".chunkx");
//							int chunkZ = getPBConfig().getInt("Spawners." + s + ".chunkz");
                        int stackAmt = getPBConfig().getInt("Spawners." + s + ".stackAmount");
                        Material mat = Material.getMaterial(getPBConfig().getString("Spawners." + s + ".material"));
                        String name = getPBConfig().getString("Spawners." + s + ".name", " ");
                        List<String> lore = new ArrayList<>();
                        if (getPBConfig().isList("Spawners." + s + ".lore")) {
                            for (String piece : getPBConfig().getStringList("Spawners." + s + ".lore")) {
                                lore.add(piece);
                            }
                        }
                        ItemStack is = new ItemStack(mat);
                        ItemMeta im = is.getItemMeta();
                        if (! name.equals(" ")) {
                            im.setDisplayName(name);
                        }
                        if (! lore.isEmpty()) {
                            im.setLore(lore);
                        }
                        is.setItemMeta(im);
                        EntityType ent = EntityType.valueOf(getPBConfig().getString("Spawners." + s + ".entity"));
                        StackedSpawner ss = new StackedSpawner(ent, is, jLoc);
                        ss.setStackAmount(stackAmt);
                        stackedSpawners.put(jLoc, ss);
                    }
                } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                    getLogger().log(Level.INFO, "Looks like you havent placed any spawners yet! Enjoy the plugin.");
                }
                stackedSpawners.values().forEach(StackedSpawner::updateHolo);
            }

        }.runTaskLater(this, 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                check();
            }
        }.runTaskTimer(this, 0, 10000);
    }

    public void loadSettings() {
        maxSpawnerStack = getConfig().getInt("settings.max-spawner-stack");
        maxItemStack = getConfig().getInt("settings.max-item-stack");
        maxEntityStack = getConfig().getInt("settings.max-entity-stack");
        saveFrequency = getConfig().getInt("settings.savefrequency");
        holoViewDist = getConfig().getInt("settings.holo-view-dist");
        mergeDist = getConfig().getInt("settings.merge-distance");
        entityFormat = getConfig().getString("formats.entity-name-format");
        spawnerFormat = getConfig().getString("formats.spawner-name-format");
        itemFormat = getConfig().getString("formats.item-name-format");
        hoverUpgrade = getConfig().getBoolean("formats.hover-upgrade-drop");

        instantAllKill.clear();
        for (String s : getConfig().getStringList("settings.stack-kill")) {
            try {
                instantAllKill.add(DamageCause.valueOf(s.toUpperCase()));
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, s + " was not recognised as a proper damage cause.");
            }
        }
        reasons.clear();
        for (String s : getConfig().getStringList("settings.stack-reasons")) {
            try {
                reasons.add(SpawnReason.valueOf(s.toUpperCase()));
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, s + " was not recognised as a proper damage cause.");
            }
        }
    }

    public void saveData() {
        try {
            for (String s : getPBConfig().getConfigurationSection("Spawners").getKeys(false)) {
                getPBConfig().set("Spawners." + s, null);
                savePBConfig();
            }
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            this.getLogger().log(Level.INFO, "Looks like you havent placed any spawners yet! Enjoy the plugin.");
        }
        for (StackedSpawner spawner : stackedSpawners.values()) {

            getPBConfig().set("Spawners." + spawner.getjLoc().toString() + ".stackAmount", spawner.getStackAmount());
            savePBConfig();

            getPBConfig().set("Spawners." + spawner.getjLoc().toString() + ".material", spawner.getMat().toString());
            savePBConfig();

            getPBConfig().set("Spawners." + spawner.getjLoc().toString() + ".entity", spawner.getEntity().toString());
            savePBConfig();

            if (spawner.getItemStack().hasItemMeta()) {
                if (spawner.getItemStack().getItemMeta().hasDisplayName()) {
                    getPBConfig().set("Spawners." + spawner.getjLoc().toString() + ".name",
                            spawner.getItemStack().getItemMeta().getDisplayName());
                    savePBConfig();
                }
                if (spawner.getItemStack().getItemMeta().hasLore()) {
                    getPBConfig().set("Spawners." + spawner.getjLoc().toString() + ".lore",
                            spawner.getItemStack().getItemMeta().getLore());
                    savePBConfig();
                }
            }

        }
    }

    public Set<StackedEntity> getTimedOutMobs() {
        return timedOutMobs;
    }

    public Map<JoLocation, StackedSpawner> getStackedSpawners() {
        return stackedSpawners;
    }

    public Map<Player, InventoryCheck> getpBound() {
        return pBound;
    }

    public Map<EntityType, List<UpgradeContainer>> getEntityUpgrades() {
        return entityUpgrades;
    }

    public Map<UUID, Material> getMobDrops() {
        return mobDrops;
    }

    public Map<UUID, Inventory> getOpenInv() {
        return openInv;
    }

    public List<Hologram> getVisibility() {
        return visibility;
    }

    public Map<UUID, StackedEntity> getListOfEnt() {
        return listOfEnt;
    }

    public Map<UUID, StackedItem> getListOfItems() {
        return listOfItems;
    }

    public List<DamageCause> getInstantAllKill() {
        return instantAllKill;
    }

    public int getMaxSpawnerStack() {
        return maxSpawnerStack;
    }

//    public Map<UUID, StackedItem> getToADD() {
//        return toADD;
//    }

    public int getMaxItemStack() {
        return maxItemStack;
    }

    public int getMaxEntityStack() {
        return maxEntityStack;
    }

    public String getEntityFormat() {
        return entityFormat;
    }

    public String getSpawnerFormat() {
        return spawnerFormat;
    }

    public String getItemFormat() {
        return itemFormat;
    }

    public boolean isHoverUpgrade() {
        return hoverUpgrade;
    }

    public Set<SpawnReason> getReasons() {
        return reasons;
    }

    public void loadCustomSpawnerTypes() {
        entityUpgrades.clear();
        if (! itemStack) {
            getConfig().getConfigurationSection("entities").getKeys(false).forEach(entity -> {
                String eType = entity.toUpperCase();
                EntityType ent = EntityType.valueOf(eType);
                List<UpgradeContainer> entry = new ArrayList<UpgradeContainer>();
                getConfig().getConfigurationSection("entities." + entity).getKeys(false).forEach(matString -> {
                    try {
                        String matValid = matString.toUpperCase();
                        Material mat = Material.getMaterial(matValid);
                        int slot = getConfig().getInt("entities." + entity + "." + matString + ".slot");
                        double cost = getConfig().getDouble("entities." + entity + "." + matString + ".cost");
                        String name = getConfig().getString("entities." + entity + "." + matString + ".name");

                        List<String> initGLore = new ArrayList<>();
                        if (getConfig().isList("entities." + entity + "." + matString + ".gui-lore")) {
                            initGLore = getConfig().getStringList("entities." + entity + "." + matString + ".gui-lore");
                        }

                        List<String> guiLore = new ArrayList<>();


                        if (! initGLore.isEmpty()) {
                            initGLore.forEach(string -> guiLore.add(ChatColor.translateAlternateColorCodes('&', string)));
                        }
                        entry.add(new UpgradeContainer(slot, mat, cost, name, guiLore));
                    } catch (Exception e) {
                        String matValid = matString.toUpperCase();
                        Material mat = Material.getMaterial(matValid);
                        int slot = getConfig().getInt("entities." + entity + "." + matString + ".slot");
                        double cost = getConfig().getDouble("entities." + entity + "." + matString + ".cost");
                        List<String> initGLore = new ArrayList<>();
                        if (getConfig().isList("entities." + entity + "." + matString + ".gui-lore")) {
                            initGLore = getConfig().getStringList("entities." + entity + "." + matString + ".gui-lore");
                        }

                        List<String> guiLore = new ArrayList<>();


                        if (! initGLore.isEmpty()) {
                            initGLore.forEach(string -> guiLore.add(ChatColor.translateAlternateColorCodes('&', string)));
                        }
                        entry.add(new UpgradeContainer(slot, mat, cost, guiLore));
                    }
                });
                entityUpgrades.put(ent, entry);
                if (debug) getLogger().info("Loaded : " + entry.size() + " for type: " + ent.name());
            });
        } else {
            getConfig().getConfigurationSection("entities").getKeys(false).forEach(entity -> {
                String eType = entity.toUpperCase();
                EntityType ent = EntityType.valueOf(eType);
                List<UpgradeContainer> entry = new ArrayList<UpgradeContainer>();
                getConfig().getConfigurationSection("entities." + entity).getKeys(false).forEach(matString -> {
                    try {
                        String matValid = matString.toUpperCase();
                        Material mat = Material.getMaterial(matValid);
                        int slot = getConfig().getInt("entities." + entity + "." + matString + ".slot");
                        double cost = getConfig().getDouble("entities." + entity + "." + matString + ".cost");
                        String guiName = getConfig().getString("entities." + entity + "." + matString + ".name", " ");
                        String name = getConfig().getString("entities." + entity + "." + matString + ".item-name", " ");
                        Byte data = (byte) getConfig().getInt("entities." + entity + "." + matString + ".data", 0);
                        List<String> initLore = new ArrayList<>();
                        if (getConfig().isList("entities." + entity + "." + matString + ".lore")) {
                            initLore = getConfig().getStringList("entities." + entity + "." + matString + ".lore");
                        }

                        List<String> initGLore = new ArrayList<>();
                        if (getConfig().isList("entities." + entity + "." + matString + ".gui-lore")) {
                            initGLore = getConfig().getStringList("entities." + entity + "." + matString + ".gui-lore");
                        }

                        List<String> guiLore = new ArrayList<>();


                        if (! initGLore.isEmpty()) {
                            initGLore.forEach(string -> {
                                guiLore.add(ChatColor.translateAlternateColorCodes('&', string));
                            });
                        }

                        List<String> lore = new ArrayList<>();
                        if (! initLore.isEmpty()) {
                            initLore.forEach(string -> {
                                lore.add(ChatColor.translateAlternateColorCodes('&', string));
                            });
                        }
                        ItemStack item = new ItemStack(mat, 1, data);
                        ItemMeta iMeta = item.getItemMeta();
                        if (! name.equals(" ")) {
                            iMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                        }
                        iMeta.setLore(lore);
                        item.setItemMeta(iMeta);
                        entry.add(new UpgradeContainer(slot, item, cost, guiName, guiLore));
                    } catch (Exception e) {
                        //e.printStackTrace();
                        getLogger().severe("ERROR while loading spawner types check your config.yml");
                        this.setEnabled(false);
                    }
                });
                entityUpgrades.put(ent, entry);
                if (debug) getLogger().info("Loaded : " + entry.size() + " for type: " + ent.name());
            });
        }

    }

    public String numberDecFormatter(double l) {
        DecimalFormat mFormatter = new DecimalFormat("###,###,###,###,###,###");
        String output = mFormatter.format(l);
        return output;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
                .getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public FileConfiguration getPBConfig() {
        return this.playerBalanceConfig;
    }

    private void createPBConfig() {
        playerBalanceConfigFile = new File(getDataFolder(), "StackedSpawners.yml");
        if (! playerBalanceConfigFile.exists()) {
            playerBalanceConfigFile.getParentFile().mkdirs();
            saveResource("StackedSpawners.yml", false);
        }

        playerBalanceConfig = new YamlConfiguration();
        try {
            playerBalanceConfig.load(playerBalanceConfigFile);
        } catch (IOException | InvalidConfigurationException ignored) {
        }
    }

    public void savePBConfig() {
        try {
            playerBalanceConfig.save(playerBalanceConfigFile);
        } catch (IOException ignored) {
        }
    }

    public void check() {
        Set<JoLocation> removal = new HashSet<>();
        this.getStackedSpawners().forEach((k, v) -> {
            if (k.getBlock().getBlock().getType() != Material.valueOf("MOB_SPAWNER")) {
                visibility.remove(v.getHolo());

                v.getHolo().delete();
                removal.add(k);
            }
            k.getBlock().getChunk().unload();
        });
        removal.forEach(jLoc -> getStackedSpawners().remove(jLoc));
    }

    public boolean tryAll(StackedItem aa) {
        for (StackedItem next : SpaceStacker.instance.getListOfItems().values()) {
            if (aa.tryStack(next)) {
                return true;
            }
        }
        return false;
    }

}
