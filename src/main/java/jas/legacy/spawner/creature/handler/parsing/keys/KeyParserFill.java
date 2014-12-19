package jas.legacy.spawner.creature.handler.parsing.keys;

import jas.legacy.spawner.creature.handler.parsing.TypeValuePair;
import jas.legacy.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.modern.JASLog;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserFill extends KeyParserBase {

    public KeyParserFill(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        String[] pieces = parseable.split(",");
        Operand operand = parseOperand(pieces);
        if (pieces.length == 1) {
            parsedChainable.add(new TypeValuePair(key, isInverted(parseable)));
            operandvalue.add(operand);
            return true;
        } else {
            JASLog.log().severe("Error Parsing Needs %s parameter. Invalid Argument Length.", key.key);
            return false;
        }
    }

    @Override
    public boolean parseValue(String parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord,
            TypeValuePair typeValuePair, HashMap<String, Object> valueCache) {
        boolean isInverted = (Boolean) typeValuePair.getValue();
        boolean isFillerBlock = world.getBiomeGenForCoords(xCoord, zCoord).fillerBlock == world.getBlock(xCoord,
                yCoord - 1, zCoord);
        return isInverted ? isFillerBlock : !isFillerBlock;
    }
}