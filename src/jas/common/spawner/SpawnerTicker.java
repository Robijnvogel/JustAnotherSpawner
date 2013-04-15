package jas.common.spawner;

import jas.common.Properties;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.EnumSet;
import java.util.Iterator;

import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

public class SpawnerTicker implements IScheduledTickHandler {

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.WORLD);
    }

    @Override
    public String getLabel() {
        return "jasSpawner";
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        WorldServer world = (WorldServer) tickData[0];
        // TODO: Add Check such that Chunks are only populated if at least one CreatureType can be spawning
        CustomSpawner.determineChunksForSpawnering(world);
        if (world.getWorldInfo().getWorldTime() % 60 == 0) {
            CustomSpawner.countCreatureInChunks(world);
        }
        if (world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {
            Iterator<CreatureType> typeIterator = CreatureTypeRegistry.INSTANCE.getCreatureTypes();
            while (typeIterator.hasNext()) {
                CreatureType creatureType = typeIterator.next();
                if (world.getWorldInfo().getWorldTotalTime() % creatureType.spawnRate == 0L) {
                    CustomSpawner.spawnCreaturesInChunks(world, creatureType);
                }
            }
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {

    }

    @Override
    public int nextTickSpacing() {
        return Properties.spawnerTickSpacing;
    }
}
