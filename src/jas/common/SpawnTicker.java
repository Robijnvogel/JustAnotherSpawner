package jas.common;

import java.util.EnumSet;
import java.util.Iterator;

import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class SpawnTicker implements ITickHandler { //TODO: Implement IScheduledTickHandler and Expose NextTickRate to Config

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.WORLD);
    }

    @Override
    public String getLabel() {
        return "spawner";
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        WorldServer world = (WorldServer) tickData[0];
        //TODO: Add Check such that Chunks are only populated if at least one CreatureType can be spawning
        CustomSpawner.determineChunksForSpawnering(world, true, true,
                world.getWorldInfo().getWorldTotalTime() % 400L == 0L);
        if (world.getGameRules().getGameRuleBooleanValue("doMobSpawning")) {
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
}