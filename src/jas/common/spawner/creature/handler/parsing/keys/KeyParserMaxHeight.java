package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.spawner.creature.handler.parsing.OptionalParser;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.world.World;

public class KeyParserMaxHeight extends KeyParserBase {

    public KeyParserMaxHeight(Key key) {
        super(key, false, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        String[] pieces = parseable.split(",");

        Operand operand = getOperand(pieces);

        TypeValuePair typeValue = new TypeValuePair(key, OptionalParser.parseSingleInteger(pieces, -1, key.key));

        if (typeValue.getValue() != null) {
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
            return true;
        }
        return false;
    }

    @Override
    public boolean parseValue(String parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord, TypeValuePair typeValuePair,
            HashMap<String, Object> valueCache) {
        Integer maxSpawnHeight = (Integer) typeValuePair.getValue();
        return yCoord > maxSpawnHeight ? true : false;
    }
}