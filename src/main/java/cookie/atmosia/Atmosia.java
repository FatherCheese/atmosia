package cookie.atmosia;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.sound.SoundRepository;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.Biomes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ClientStartEntrypoint;

import java.util.ArrayList;
import java.util.List;


public class Atmosia implements ModInitializer, ClientStartEntrypoint {
    public static final String MOD_ID = "atmosia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static List<Biome> forestBiomes = new ArrayList<>();
	public static List<Biome> hellBiomes = new ArrayList<>();
	public static List<Biome> plainsBiomes = new ArrayList<>();
	public static List<Biome> swampBiomes = new ArrayList<>();

    @Override
    public void onInitialize() {
		forestBiomes.add(Biomes.OVERWORLD_BIRCH_FOREST);
		forestBiomes.add(Biomes.OVERWORLD_BOREAL_FOREST);
		forestBiomes.add(Biomes.OVERWORLD_CAATINGA);
		forestBiomes.add(Biomes.OVERWORLD_FOREST);
		forestBiomes.add(Biomes.OVERWORLD_RAINFOREST);
		forestBiomes.add(Biomes.OVERWORLD_SEASONAL_FOREST);

		hellBiomes.add(Biomes.NETHER_NETHER);
		hellBiomes.add(Biomes.OVERWORLD_HELL);

		plainsBiomes.add(Biomes.OVERWORLD_CAATINGA_PLAINS);
		plainsBiomes.add(Biomes.OVERWORLD_DESERT);
		plainsBiomes.add(Biomes.OVERWORLD_GLACIER);
		plainsBiomes.add(Biomes.OVERWORLD_GRASSLANDS);
		plainsBiomes.add(Biomes.OVERWORLD_MEADOW);
		plainsBiomes.add(Biomes.OVERWORLD_OUTBACK);
		plainsBiomes.add(Biomes.OVERWORLD_OUTBACK_GRASSY);
		plainsBiomes.add(Biomes.OVERWORLD_PLAINS);
		plainsBiomes.add(Biomes.OVERWORLD_SHRUBLAND);
		plainsBiomes.add(Biomes.OVERWORLD_TUNDRA);

		swampBiomes.add(Biomes.OVERWORLD_SWAMPLAND);
		swampBiomes.add(Biomes.OVERWORLD_SWAMPLAND_MUDDY);

		LOGGER.info("Atmosia initialized.");
    }

	@Override
	public void beforeClientStart() {
	}

	@Override
	public void afterClientStart() {
		SoundRepository.registerNamespace(MOD_ID);
	}
}
