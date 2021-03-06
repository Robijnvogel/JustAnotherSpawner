package jas.spawner.modern.modification;

import java.util.HashMap;

import jas.spawner.modern.spawner.creature.type.CreatureTypeBuilder;
import jas.spawner.modern.spawner.creature.type.CreatureTypeRegistry;

public class ModUpdateCreatureType extends BaseModification {

	private CreatureTypeBuilder builder;

	public ModUpdateCreatureType(String typeID) {
		this(typeID, 1, 10);
	}

	public ModUpdateCreatureType(String typeID, int spawnRate, int maxNumberOfCreature) {
		builder = new CreatureTypeBuilder(typeID, spawnRate, maxNumberOfCreature);
	}

	public ModUpdateCreatureType(String typeID, int spawnRate, int maxNumberOfCreature, float chunkSpawnChance,
			String spawnMedium, String tags) {
		this(typeID, spawnRate, maxNumberOfCreature);
		builder.withChanceToChunkSpawn(chunkSpawnChance);
		builder.setRawMedium(spawnMedium);
		builder.withSpawnExpression(tags);
	}

	@Override
	public void applyModification(CreatureTypeRegistry registry) {
		registry.updateCreatureType(builder);
	}
}