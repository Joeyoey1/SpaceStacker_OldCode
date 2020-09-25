package com.joeyoey.spacestacker;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.joeyoey.spacestacker.commands.StackerCommands;
import com.joeyoey.spacestacker.listeners.*;
import com.joeyoey.spacestacker.objects.*;
import com.joeyoey.spacestacker.storage.EntityTypeAdapter;
import com.joeyoey.spacestacker.storage.ItemStackAdapter;
import com.joeyoey.spacestacker.storage.MaterialAdapter;
import com.joeyoey.spacestacker.util.MessageFactory;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class SpaceStacker extends JavaPlugin {

    /*
    Statics
     */
    public static SpaceStacker instance;
    public static Gson gson;
    public static SpaceStackerApi spaceStackerApi;

    /*
    Settings
     */
    public boolean debug;
    private boolean adaptive;
    private boolean hoverUpgrade;
    private boolean itemStack;
    private boolean nerfMobs;
    private boolean entMerger;

    private int saveFrequency;
    private int holoViewDist;
    private int maxSpawnerStack;
    private int maxItemStack;
    private int maxEntityStack;
    private int mergeDist;

    private String entityFormat;
    private String spawnerFormat;
    private String itemFormat;

    private List<DamageCause> instantAllKill = new ArrayList<>();

    private Set<SpawnReason> reasons = new HashSet<>();

    private Map<EntityType, List<UpgradeContainer>> entityUpgrades = new HashMap<>();
    private Map<UUID, List<Material>> mobDrops = new HashMap<>();

    /*
    Runtime variables
     */

    private Map<JoLocation, StackedSpawner> stackedSpawners = new ConcurrentHashMap<>();
    private Map<UUID, StackedEntity> listOfEnt = new ConcurrentHashMap<>();
    private Map<Player, InventoryCheck> pBound = new HashMap<>();
    private Map<UUID, Inventory> openInv = new HashMap<>();
    private Map<UUID, StackedItem> listOfItems = new ConcurrentHashMap<>();

    private List<Hologram> visibility = new ArrayList<>();

    private Set<StackedEntity> timedOutMobs = new HashSet<>();

    private Economy economy = null;

    private File playerBalanceConfigFile;
    private FileConfiguration playerBalanceConfig;

    private BukkitTask itemTask;
    private BukkitTask entityTask;

    private int times;


    public void onEnable() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(EntityType.class, new EntityTypeAdapter())
                .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
                .registerTypeAdapter(Material.class, new MaterialAdapter())
                .enableComplexMapKeySerialization()
                .create();

        spaceStackerApi = new SpaceStackerApi(this);

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

        if (entMerger) {
            entityTask = new BukkitRunnable() {
                @Override
                public void run() {
//                listOfItems.putAll(toADD);
//                toADD.clear();
                    try {
                        for (Iterator<StackedEntity> entity = listOfEnt.values().iterator(); entity.hasNext(); ) {
                            StackedEntity stackedEntity = entity.next();
                            if (stackedEntity.getBaseEnt().getLocation().getY() < 0) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        stackedEntity.getBaseEnt().remove();
                                    }
                                }.runTask(SpaceStacker.instance);
                                entity.remove();
                                continue;
                            }
                            if (!stackedEntity.getBaseEnt().isValid() || tryAll(stackedEntity)) {
                                entity.remove();
                            }
                        }
                        //listOfItems.values().removeIf(aa -> !aa.getItem().isValid() || tryAll(aa));
                    } catch (NullPointerException ignored) {
                    }
                }
            }.runTaskTimerAsynchronously(this, 0, 100);
        }

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
        try {
            FileWriter fileWriter = new FileWriter(this.getDataFolder().getAbsolutePath() + "/loottables.json");
            gson.toJson(SpawnerSpawn.defaultDrops, new TypeToken<Map<EntityType, Set<ItemStack>>>(){}.getType(), fileWriter);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().log(Level.INFO, "Looks like there was an issue saving loot tables");
        }
        getLogger().log(Level.INFO, ChatColor.DARK_PURPLE + "Saving Spawner data");
        saveData();
        getLogger().log(Level.INFO, ChatColor.LIGHT_PURPLE + "Spawner data saved!");
    }

    public void loadData() {

        if (this.adaptive) {
            try {
                File loottable = new File(this.getDataFolder().getAbsolutePath() + "/loottables.json");
                loottable.createNewFile();


                SpawnerSpawn.defaultDrops = gson.fromJson(new FileReader(loottable), new TypeToken<Map<EntityType, Set<ItemStack>>>() {
                }.getType());
                if (SpawnerSpawn.defaultDrops == null) {
                    SpawnerSpawn.defaultDrops = new HashMap<>();
                }
            } catch (Exception e) {
                getLogger().log(Level.INFO, "No basic loot table data yet!");
                SpawnerSpawn.defaultDrops = new HashMap<>();
            }
        }


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
        adaptive = getConfig().getBoolean("settings.adaptive");
        nerfMobs = getConfig().getBoolean("settings.nerf-mobs");
        entMerger = getConfig().getBoolean("settings.entity-merger-task");
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

    public Map<UUID, List<Material>> getMobDrops() {
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

    public boolean isItemStackMode() {
        return itemStack;
    }

    public int getMergeDist() {
        return mergeDist;
    }

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

    public boolean isNerfMobs() {
        return nerfMobs;
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
        int i = 0;
        int j = 1;
        for (Map.Entry<JoLocation, StackedSpawner> entry : this.getStackedSpawners().entrySet()) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (entry.getKey().getBlock().getChunk().isLoaded()) {
                    if (entry.getKey().getBlock().getBlock().getType() != Material.valueOf("MOB_SPAWNER")) {
                        visibility.remove(entry.getValue().getHolo());

                        entry.getValue().getHolo().delete();
                        removal.add(entry.getKey());
                    }
                }
            }, j);
            i++;
            if (i > 20) {
                i = 0;
                j += 5;
                if (j > 2000000000) {
                    j = 1;
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(this, () -> getStackedSpawners().entrySet().removeIf(entry -> removal.contains(entry.getKey())), j + 5);
    }

    public boolean tryAll(StackedItem aa) {
        if (Bukkit.getBukkitVersion().contains("1.15")) {
            try {
                return Bukkit.getScheduler().callSyncMethod(this, () -> {
                    for (StackedItem next : SpaceStacker.instance.getListOfItems().values()) {
                        if (aa.tryStack(next)) {
                            return true;
                        }
                    } return false;
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            for (StackedItem next : SpaceStacker.instance.getListOfItems().values()) {
                if (aa.tryStack(next)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean tryAll(StackedEntity aa) {
        if (Bukkit.getBukkitVersion().contains("1.15")) {
            try {
                return Bukkit.getScheduler().callSyncMethod(this, () -> {
                   for (StackedEntity next : SpaceStacker.instance.getListOfEnt().values()) {
                       if (aa.tryStack(next)) {
                           return true;
                       }
                   }
                   return false;
               }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            for (StackedEntity next : SpaceStacker.instance.getListOfEnt().values()) {
                if (aa.tryStack(next)) {
                    return true;
                }
            }
        }
        return false;
    }

}
