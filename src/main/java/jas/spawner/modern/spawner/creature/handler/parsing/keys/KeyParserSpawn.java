package jas.spawner.modern.spawner.creature.handler.parsing.keys;

import jas.spawner.modern.spawner.creature.handler.parsing.TypeValuePair;
import jas.spawner.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserSpawn extends KeyParserBase {

    public KeyParserSpawn(Key key) {
        super(key, true, KeyType.PARENT);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean parseValue(String parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord,
            TypeValuePair typeValuePair, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

	@Override
	public String toExpression(String parseable) {
        throw new UnsupportedOperationException();
	}
}
