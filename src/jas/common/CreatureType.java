package jas.common;

import static net.minecraftforge.common.ForgeDirection.UP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStep;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;

public class CreatureType {
    public final int spawnRate;
    public final int maxNumberOfCreature;
    public final boolean needSky;
    public final Material spawnMedium;

    public CreatureType(int maxNumberOfCreature, Material spawnMedium, int spawnRate, boolean needSky) {
        this.maxNumberOfCreature = maxNumberOfCreature;
        this.spawnMedium = spawnMedium;
        this.needSky = needSky;
        this.spawnRate = spawnRate;
    }
    
    /**
     * Called by CustomSpawner to get a creature dependent on the world type
     * @param xCoord Random xCoordinate nearby to Where Creature will spawn
     * @param yCoord Random yCoordinate nearby to Where Creature will spawn
     * @param zCoord Random zCoordinate nearby to Where Creature will spawn
     * @return Creature to Spawn
     */
    //TODO: Incomplete
    public SpawnListEntry getSpawnListEntry(int xCoord, int yCoord, int zCoord) {
        return null;
    }
    
    /**
     * Called by CustomSpawner to get the base coordinate to spawn an Entity
     * @param world 
     * @param xCoord
     * @param zCoord
     * @return
     */
    public ChunkPosition getRandomSpawningPointInChunk(World world, int chunkX, int chunkZ) {
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int xCoord = chunkX * 16 + world.rand.nextInt(16);
        int zCoord = chunkZ * 16 + world.rand.nextInt(16);
        int yCoord = world.rand.nextInt(chunk == null ? world.getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
        return new ChunkPosition(xCoord, yCoord, zCoord);
    }
    
    /**
     * 
     * @param entity Entity that is being Checked
     * @return
     */
    //TODO: Incomplete
    public boolean isEntityOfType(Entity entity){
        return true;
    }
    
    /**
     * Called by CustomSpawner to determine if the Chunk Postion to be spawned at is a valid Type
     * @param worldServer
     * @param xCoord
     * @param yCoord
     * @param zCoord
     * @return
     */
    public boolean isValidMedium(WorldServer worldServer, int xCoord, int yCoord, int zCoord){
        return !worldServer.isBlockNormalCube(xCoord, yCoord, zCoord) && worldServer.getBlockMaterial(xCoord, yCoord, zCoord) == spawnMedium;
    }
    
    /**
     * Called by CustomSpawner the location is valid for determining if the Chunk Postion is a valid location to spawn
     * @param worldServer
     * @param xCoord
     * @param yCoord
     * @param zCoord
     * @return
     */
    public boolean canSpawnAtLocation(WorldServer worldServer, int xCoord, int yCoord, int zCoord) {
        if (spawnMedium == Material.water) {
            return worldServer.getBlockMaterial(xCoord, yCoord, zCoord).isLiquid()
                    && worldServer.getBlockMaterial(xCoord, yCoord - 1, zCoord).isLiquid()
                    && !worldServer.isBlockNormalCube(xCoord, yCoord + 1, zCoord);
        } else if (!worldServer.doesBlockHaveSolidTopSurface(xCoord, yCoord - 1, zCoord)) {
            return false;
        } else {
            int l = worldServer.getBlockId(xCoord, yCoord - 1, zCoord);
            boolean spawnBlock = (Block.blocksList[l] != null && canCreatureSpawn(Block.blocksList[l], worldServer,
                    xCoord, yCoord - 1, zCoord));
            return spawnBlock && l != Block.bedrock.blockID && !worldServer.isBlockNormalCube(xCoord, yCoord, zCoord)
                    && !worldServer.getBlockMaterial(xCoord, yCoord, zCoord).isLiquid()
                    && !worldServer.isBlockNormalCube(xCoord, yCoord + 1, zCoord);
        }
    }

    /*
     * TODO: Does not Belong Here. Possible Block Helper. Mods should be able to Register a Block. Similar to Proposed
     * Entity Registry. How will end-users fix issue?
     */
    /**
     * Custom Implementation of canCreatureSpawnMethod which Required EnumCreatureType. Cannot be Overrident.
     * @param block
     * @param world
     * @param xCoord
     * @param yCoord
     * @param zCoord
     * @return
     */
    private boolean canCreatureSpawn(Block block, World world, int xCoord, int yCoord, int zCoord) {
        int meta = world.getBlockMetadata(xCoord, yCoord, zCoord);
        if (block instanceof BlockStep) {
            return (((meta & 8) == 8) || block.isOpaqueCube());
        } else if (block instanceof BlockStairs) {
            return ((meta & 4) != 0);
        }
        return block.isBlockSolidOnSide(world, xCoord, yCoord, zCoord, UP);
    }
}
