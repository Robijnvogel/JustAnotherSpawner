package jas.common.spawner.creature.handler;

import jas.common.JASLog;

import java.util.ArrayList;

import net.minecraft.world.World;

public class OptionalSettingsSpawning extends OptionalSettings {
    public OptionalSettingsSpawning(String parseableString) {
        super(parseableString.replace("}", ""));
    }

    @Override
    protected final void parseString() {
        if (stringParsed) {
            return;
        }
        stringParsed = true;

        /* Set default Paramters that are assumed to be Present */
        valueCache.put(Key.minLightLevel.key, 16); // Light < Min means Spawn
        valueCache.put(Key.maxLightLevel.key, -1); // Light > Max means Spawn
        valueCache.put(Key.blockList.key, new ArrayList<Integer>());
        valueCache.put(Key.metaList.key, new ArrayList<Integer>());

        if (parseableString.equals("")) {
            return;
        }

        String[] masterParts = parseableString.split(":");
        for (int i = 0; i < masterParts.length; i++) {
            if (i == 0) {
                if (masterParts[i].equalsIgnoreCase(Key.spawn.key)) {
                    valueCache.put(Key.enabled.key, Boolean.TRUE);
                } else {
                    JASLog.severe("Optional Settings Error expected spawn from %s", masterParts[i]);
                    break;
                }
            } else {
                String[] childParts = masterParts[i].split(",");

                switch (Key.getKeybyString(childParts[0])) {
                case light:
                    OptionalParser.parseLight(childParts, valueCache);
                    break;
                case block:
                    OptionalParser.parseBlock(childParts, valueCache);
                    break;
                case spawnRange:
                    OptionalParser.parseSpawnRange(childParts, valueCache);
                    break;
                case sky:
                case noSky:
                    OptionalParser.parseSky(childParts, valueCache);
                    break;
                case material:
                    // TODO: Add Material Tag ? Air or Water? What is the point?
                    JASLog.info("Material Tag is not implemented yet. Have some %s", Math.PI);
                    break;
                default:
                    JASLog.severe("Could Not Recognize any valid Spawn properties from %s", masterParts[i]);
                    break;
                }
            }
        }
    }

    @Override
    public boolean isOptionalEnabled() {
        parseString();
        return valueCache.get(Key.enabled.key) != null;
    }

    public boolean isValidLightLevel(int lightLevel) {
        parseString();
        return lightLevel > (Integer) valueCache.get(Key.maxLightLevel.key)
                || lightLevel < (Integer) valueCache.get(Key.minLightLevel.key);
    }

    @SuppressWarnings("unchecked")
    public boolean isValidBlock(int blockID, int meta) {
        parseString();
        ArrayList<Integer> blockIDlist = (ArrayList<Integer>) valueCache.get(Key.blockList.key);
        ArrayList<Integer> metaIDList = (ArrayList<Integer>) valueCache.get(Key.metaList.key);

        for (int i = 0; i < blockIDlist.size(); i++) {
            if (blockID == blockIDlist.get(i) && meta == metaIDList.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the Distance to Player is valid
     * 
     * @param playerDistance Distance Squared to Nearest Player
     * @return True to Continue as Normal, False to Interrupt, Null Use Global Check
     */
    public Boolean isValidDistance(int playerDistance) {
        parseString();
        Integer distanceToPlayer = (Integer) valueCache.get(Key.spawnRange);
        return distanceToPlayer != null ? playerDistance > distanceToPlayer * distanceToPlayer : null;
    }

    public Boolean isValidSky(World world, int xCoord, int yCoord, int zCoord) {
        if (valueCache.get(Key.sky.key) == null) {
            return true;
        } else if ((Boolean) valueCache.get(Key.sky.key)) {
            return !world.canBlockSeeTheSky(xCoord, yCoord, zCoord);
        } else {
            return world.canBlockSeeTheSky(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}