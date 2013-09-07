package jas.common.spawner.creature.entry;

import jas.common.ImportedSpawnList;
import jas.common.JASLog;
import jas.common.WorldProperties;
import jas.common.config.LivingConfiguration;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;
import jas.common.spawner.biome.group.BiomeHelper;
import jas.common.spawner.biome.structure.BiomeHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.MobSpecificConfigCache;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

public class BiomeSpawnListRegistry {

    /* Contains Mapping between BiomeGroupID, LivingType to valid SpawnListEntry */
    private final Table<String, String, Set<SpawnListEntry>> validSpawnListEntries = HashBasedTable.create();
    /* Contains Mapping Between BiomeGroupID, LivingType to invalid SpawnListEntry i.e. spawnWeight <=0 etc. */
    private final Table<String, String, Set<SpawnListEntry>> invalidSpawnListEntries = HashBasedTable.create();

    public boolean addSpawn(SpawnListEntry spawnListEntry) {
        LivingHandler handler = livingHandlerRegistry.getLivingHandler(spawnListEntry.livingClass);
        if (spawnListEntry.itemWeight > 0
                && livingHandlerRegistry.getLivingHandler(spawnListEntry.livingClass).shouldSpawn) {
            logSpawning(spawnListEntry, handler, true);
            Set<SpawnListEntry> spawnList = validSpawnListEntries.get(spawnListEntry.pckgName, handler.creatureTypeID);
            if (spawnList == null) {
                spawnList = new HashSet<SpawnListEntry>();
                validSpawnListEntries.put(spawnListEntry.pckgName, handler.creatureTypeID, spawnList);
            }
            return spawnList.add(spawnListEntry);
        } else {
            logSpawning(spawnListEntry, handler, false);
            Set<SpawnListEntry> spawnList = invalidSpawnListEntries
                    .get(spawnListEntry.pckgName, handler.creatureTypeID);
            if (spawnList == null) {
                spawnList = new HashSet<SpawnListEntry>();
                invalidSpawnListEntries.put(spawnListEntry.pckgName, handler.creatureTypeID, spawnList);
            }
            return spawnList.add(spawnListEntry);
        }
    }

    private void logSpawning(SpawnListEntry spawnListEntry, LivingHandler handler, boolean success) {
        String entityName = (String) EntityList.classToStringMapping.get(spawnListEntry.livingClass);
        if (success) {
            JASLog.info("Adding SpawnListEntry %s of type %s to BiomeGroup %s", entityName, handler.creatureTypeID,
                    spawnListEntry.pckgName);
        } else {
            JASLog.debug(Level.INFO,
                    "Not adding Generated SpawnListEntry of %s due to Weight %s or ShouldSpawn %s, BiomeGroup: %s",
                    entityName, spawnListEntry.itemWeight, handler, spawnListEntry.pckgName);
        }
    }

    /**
     * Returns an Immutable copy of every BiomeGroup SpawnList applicable to the provided creature type and biome.
     * 
     * @param creatureType the entity category ID i.e. MONSTER, AMBIENT See {@link CreatureTypeRegistry}
     * @param biomePackageName Package name of the applicable Biome. See
     *            {@link BiomeHelper#getPackageName(BiomeGenBase)}
     * @return Immutable copy of Collection of SpawnListEntries
     */
    public ImmutableCollection<SpawnListEntry> getSpawnListFor(String creatureType, String biomePackageName) {
        ImmutableCollection<String> groupIDList = biomeGroupRegistry.getPackgNameToGroupIDList().get(biomePackageName);
        ArrayList<SpawnListEntry> biomeSpawnList = new ArrayList<SpawnListEntry>(30);
        for (String groupID : groupIDList) {
            Collection<SpawnListEntry> spawnList = validSpawnListEntries.get(groupID, creatureType);
            if (spawnList != null) {
                biomeSpawnList.addAll(spawnList);
            }
        }
        return ImmutableList.copyOf(biomeSpawnList);
    }

    /**
     * Returns an Immutable copy of every BiomeGroup SpawnList applicable to the provided biome.
     * 
     * @param biomePackageName Package name of the applicable Biome. See
     *            {@link BiomeHelper#getPackageName(BiomeGenBase)}
     * @return Immutable copy of Collection of SpawnListEntries
     */
    public ImmutableCollection<SpawnListEntry> getSpawnListFor(String biomePackageName) {
        ImmutableCollection<String> groupIDList = biomeGroupRegistry.getPackgNameToGroupIDList().get(biomePackageName);
        ArrayList<SpawnListEntry> biomeSpawnList = new ArrayList<SpawnListEntry>(30);
        for (String groupID : groupIDList) {
            for (Set<SpawnListEntry> biomeIDtoSpawnList : validSpawnListEntries.row(groupID).values()) {
                biomeSpawnList.addAll(biomeIDtoSpawnList);
            }
        }
        return ImmutableList.copyOf(biomeSpawnList);
    }

    private WorldProperties worldProperties;
    private BiomeGroupRegistry biomeGroupRegistry;
    private LivingHandlerRegistry livingHandlerRegistry;
    private BiomeHandlerRegistry biomeHandlerRegistry;

    public BiomeSpawnListRegistry(WorldProperties worldProperties, BiomeGroupRegistry biomeGroupRegistry,
            CreatureTypeRegistry creatureTypeRegistry, LivingHandlerRegistry livingHandlerRegistry,
            BiomeHandlerRegistry biomeHandlerRegistry) {
        this.worldProperties = worldProperties;
        this.livingHandlerRegistry = livingHandlerRegistry;
        this.biomeGroupRegistry = biomeGroupRegistry;
        this.biomeHandlerRegistry = biomeHandlerRegistry;
    }

    /**
     * Called by customSpawner to get a random spawnListEntry entity
     * 
     * @param world
     * @param xCoord, yCoord, zCoord Random Coordinates nearby to Where Creature will spawn
     * @return Creature to Spawn
     */
    public SpawnListEntry getSpawnListEntryToSpawn(World world, CreatureType creatureType, int xCoord, int yCoord,
            int zCoord) {
        Collection<SpawnListEntry> structureSpawnList = biomeHandlerRegistry.getSpawnListAt(world, xCoord, yCoord,
                zCoord);
        if (!structureSpawnList.isEmpty()) {
            JASLog.debug(Level.INFO, "Structure SpawnListEntry found for ChunkSpawning at %s, %s, %s", xCoord, yCoord,
                    zCoord);
            SpawnListEntry spawnListEntry = (SpawnListEntry) WeightedRandom.getRandomItem(world.rand,
                    structureSpawnList);
            return creatureType.isEntityOfType(livingHandlerRegistry, spawnListEntry.livingClass) ? spawnListEntry
                    : null;
        }
        ImmutableCollection<String> groupIDList = biomeGroupRegistry.getPackgNameToGroupIDList().get(
                BiomeHelper.getPackageName(world.getBiomeGenForCoords(xCoord, zCoord)));
        return getRandomValidEntry(world.rand, groupIDList, creatureType.typeID);
    }

    private SpawnListEntry getRandomValidEntry(Random random, ImmutableCollection<String> groupIDList,
            String creatureType) {
        int totalWeight = 0;

        for (String groupID : groupIDList) {
            Collection<SpawnListEntry> spawnList = validSpawnListEntries.get(groupID, creatureType);
            if (spawnList == null) {
                continue;
            }

            for (SpawnListEntry spawnListEntry : spawnList) {
                totalWeight += spawnListEntry.itemWeight;
            }
        }

        if (totalWeight <= 0) {
            return null;
        } else {
            int selectedWeight = random.nextInt(totalWeight);
            SpawnListEntry resultEntry = null;

            for (String groupID : groupIDList) {
                Collection<SpawnListEntry> spawnList = validSpawnListEntries.get(groupID, creatureType);
                if (spawnList == null) {
                    continue;
                }

                for (SpawnListEntry spawnListEntry : spawnList) {
                    resultEntry = spawnListEntry;
                    selectedWeight -= spawnListEntry.itemWeight;
                    if (selectedWeight <= 0) {
                        return resultEntry;
                    }
                }
            }
            return resultEntry;
        }
    }

    public void loadFromConfig(File configDirectory, ImportedSpawnList spawnList) {
        validSpawnListEntries.clear();
        invalidSpawnListEntries.clear();

        MobSpecificConfigCache confgiCache = new MobSpecificConfigCache(worldProperties);

        Collection<LivingHandler> livingHandlers = livingHandlerRegistry.getLivingHandlers();
        for (LivingHandler handler : livingHandlers) {
            Class<? extends EntityLiving> livingClass = handler.entityClass;
            if (handler.creatureTypeID.equalsIgnoreCase(CreatureTypeRegistry.NONE)) {
                JASLog.debug(Level.INFO,
                        "Not Generating SpawnList entries for %s as it does not have CreatureType. CreatureTypeID: %s",
                        EntityList.classToStringMapping.get(livingClass), handler.creatureTypeID);
                continue;
            }

            for (BiomeGroup group : biomeGroupRegistry.getBiomeGroups()) {
                LivingConfiguration worldConfig = confgiCache.getLivingEntityConfig(configDirectory, livingClass);
                SpawnListEntry spawnListEntry = findVanillaSpawnListEntry(group, livingClass, spawnList)
                        .createFromConfig(worldConfig, worldProperties);
                addSpawn(spawnListEntry);
            }
        }
    }

    /**
     * Searches For a Vanilla SpawnListEntry of the provided entity class in one of the biomes in the BiomeGroup.
     * 
     * Generates using defaults values (i.e. spawn rate == 0) if one doesn't exist.
     */
    private SpawnListEntry findVanillaSpawnListEntry(BiomeGroup group, Class<? extends EntityLiving> livingClass,
            ImportedSpawnList importedSpawnList) {
        for (String pckgNames : group.getBiomeNames()) {
            for (Integer biomeID : biomeGroupRegistry.pckgNameToBiomeID.get(pckgNames)) {
                Collection<net.minecraft.world.biome.SpawnListEntry> spawnListEntries = importedSpawnList
                        .getSpawnableCreatureList(biomeID);
                for (net.minecraft.world.biome.SpawnListEntry spawnListEntry : spawnListEntries) {
                    if (spawnListEntry.entityClass.equals(livingClass)) {
                        return new SpawnListEntry(livingClass, group.groupID, spawnListEntry.itemWeight, 4,
                                spawnListEntry.minGroupCount, spawnListEntry.maxGroupCount, "");
                    }
                }
            }
        }
        return new SpawnListEntry(livingClass, group.groupID, 0, 4, 0, 4, "");
    }

    public void saveToConfig(File configDirectory) {
        MobSpecificConfigCache configCache = new MobSpecificConfigCache(worldProperties);
        for (Collection<SpawnListEntry> spawnlist : validSpawnListEntries.values()) {
            for (SpawnListEntry spawnEntry : spawnlist) {
                LivingConfiguration config = configCache.getLivingEntityConfig(configDirectory, spawnEntry.livingClass);
                spawnEntry.saveToConfig(config, worldProperties);
            }
        }

        for (Collection<SpawnListEntry> spawnlist : invalidSpawnListEntries.values()) {
            for (SpawnListEntry spawnEntry : spawnlist) {
                LivingConfiguration config = configCache.getLivingEntityConfig(configDirectory, spawnEntry.livingClass);
                spawnEntry.saveToConfig(config, worldProperties);
            }
        }
        configCache.saveAndCloseConfigs();
    }
}