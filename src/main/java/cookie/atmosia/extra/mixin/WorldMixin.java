package cookie.atmosia.extra.mixin;

import cookie.atmosia.Atmosia;
import cookie.atmosia.client.SoundSettings;
import cookie.atmosia.extra.IWorldAtmospheric;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.provider.BiomeProvider;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkCoordinate;
import net.minecraft.core.world.season.SeasonManager;
import net.minecraft.core.world.season.SeasonWinter;
import net.minecraft.core.world.type.WorldType;
import net.minecraft.core.world.type.WorldTypes;
import net.minecraft.core.world.type.nether.WorldTypeNether;
import net.minecraft.core.world.type.overworld.WorldTypeOverworld;
import net.minecraft.core.world.type.overworld.WorldTypeOverworldHell;
import net.minecraft.core.world.weather.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Mixin(value = World.class, remap = false)
public abstract class WorldMixin implements WorldSource, IWorldAtmospheric {

	@Shadow
	public Random rand;

	@Shadow
	public abstract boolean isChunkLoaded(int x, int z);

	@Shadow
	public abstract Chunk getChunkFromChunkCoords(int x, int z);

	@Shadow
	protected int updateLCG;

	@Shadow
	public abstract Biome getBlockBiome(int x, int y, int z);

	@Shadow
	public abstract Player getClosestPlayer(double x, double y, double z, double radius);

	@Shadow
	public abstract Weather getCurrentWeather();

	@Shadow
	public abstract boolean canBlockSeeTheSky(int x, int y, int z);

	@Shadow
	public abstract BiomeProvider getBiomeProvider();

	@Shadow
	public abstract int getHeightBlocks();

	@Shadow
	public abstract boolean isDaytime();

	@Shadow
	public WorldType worldType;

	@Shadow
	public BiomeProvider biomeProvider;

	@Shadow
	public SeasonManager seasonManager;

	@Shadow
	public List<Player> players;
	@Shadow
	public WeatherManager weatherManager;

	@Shadow
	public abstract void playSoundAtEntity(@Nullable Entity player, @NotNull Entity entity, String soundPath, float volume, float pitch);

	@Unique
	private int atmosia_ambientSoundCounter;

	@Unique
	private final Set<ChunkCoordinate> atmosia_positionsToUpdate = new HashSet<>();

	@Inject(method = "<init>()V", at = @At("TAIL"))
	private void atmosia_initCounter(CallbackInfo ci) {
		atmosia_ambientSoundCounter = rand.nextInt(150);
	}

	@Override
	public void atmosia_playAmbientSounds() {
		atmosia_positionsToUpdate.clear();

		for (Player player : players) {
			int playerChunkX = MathHelper.floor(player.x / (double) 16.0F);
			int playerChunkZ = MathHelper.floor(player.z / (double) 16.0F);
			byte radius = 7;

			for (int x = -radius; x <= radius; ++x) {
				for (int z = -radius; z <= radius; ++z) {
					atmosia_positionsToUpdate.add(new ChunkCoordinate(x + playerChunkX, z + playerChunkZ));
				}
			}
		}

		if (atmosia_ambientSoundCounter > 0) {
			--atmosia_ambientSoundCounter;
		}

		for (ChunkCoordinate coordinate : atmosia_positionsToUpdate) {
			int chunkBlockX = coordinate.x * 16;
			int chunkBlockZ = coordinate.z * 16;
			if (isChunkLoaded(coordinate.x, coordinate.z)) {
				Chunk chunk = getChunkFromChunkCoords(coordinate.x, coordinate.z);
				updateLCG = updateLCG * 3 + 1013904223;
				int randVal = updateLCG >> 2;
				int blockX = randVal & 15;
				int blockZ = randVal / 256 & 15;
				if (atmosia_ambientSoundCounter == 0) {
					int blockY = randVal / 65536 & 255;
					int id = chunk.getBlockID(blockX, blockY, blockZ);
					blockX += chunkBlockX;
					blockZ += chunkBlockZ;
					String s = "";
					float vol = 1;
					int halfHeightBlocks = worldType == WorldTypes.OVERWORLD_EXTENDED ? (int) (getHeightBlocks() * 0.7) : getHeightBlocks() / 2;

					boolean validSkylight = canBlockSeeTheSky(blockX, blockY, blockZ);
					boolean validBlockHeight = blockY < halfHeightBlocks;
					boolean validClearWeather = weatherManager.getCurrentWeather() instanceof WeatherClear;
					boolean validSeason = !(seasonManager.getCurrentSeason() instanceof SeasonWinter);
					int range = 150;

					if (id == 0) {
						Player closestPlayer = getClosestPlayer((double) blockX + (double) 0.5F, (double) blockY + (double) 0.5F, (double) blockZ + (double) 0.5F, 8);
						if (closestPlayer != null && closestPlayer.distanceToSqr((double) blockX + (double) 0.5F, (double) blockY + (double) 0.5F, (double) blockZ + (double) 0.5F) > (double) 4) {
							// Overworld
							if (worldType instanceof WorldTypeOverworld) {

								// Forests
								for (Biome forest : Atmosia.forestBiomes) {
									if (biomeProvider.getBiome(blockX, blockY, blockZ) == forest) {
										if (validSkylight && validBlockHeight && validClearWeather && validSeason) {
											s = isDaytime() ? "atmosia:ambience.forest" : "atmosia:ambience.night";
											vol = isDaytime() ? SoundSettings.forestAmbienceVolume.value : SoundSettings.nightAmbienceVolume.value;
											range = 90;
										}

										// Snow and Rain
										if (getCurrentWeather() instanceof WeatherRain ||
											getCurrentWeather() instanceof WeatherSnow) {
											s = "atmosia:ambience.forest.weather";
											vol = SoundSettings.weatherAmbienceVolume.value;
										}
									}
								}

								// Plains
								for (Biome plains : Atmosia.plainsBiomes) {
									if (biomeProvider.getBiome(blockX, blockY, blockZ) == plains) {
										if (validBlockHeight && validClearWeather && validSkylight) {
											s = "atmosia:ambience.plains";
											vol = SoundSettings.plainsAmbienceVolume.value;
										}
									}
								}

								// Swamps
								for (Biome swamp : Atmosia.swampBiomes) {
									if (getBiomeProvider().getBiome(blockX, blockY, blockZ) == swamp) {
										if (validBlockHeight && validSkylight) {
											s = "atmosia:ambience.swamp";
											vol = SoundSettings.swampAmbienceVolume.value;
											range = 90;
										}
									}
								}

								// Stormy
								if (validBlockHeight && validSkylight && getCurrentWeather() instanceof WeatherStorm) {
									s = "atmosia:ambience.weather.storm";
									vol = SoundSettings.weatherAmbienceVolume.value;
								}

								// Too High
								if (validSkylight && !validBlockHeight) {
									s = "atmosia:ambience.height";
									vol = SoundSettings.heightAmbienceVolume.value;
								}

								if (!validSkylight && blockY < halfHeightBlocks * 0.4) {
									s = "atmosia:ambience.cave";
									vol = SoundSettings.caveAmbienceVolume.value;
								}
							} else if (worldType instanceof WorldTypeNether || worldType instanceof WorldTypeOverworldHell) {
								s = "atmosia:ambience.nether";
								vol = SoundSettings.hellAmbienceVolume.value;
							}

							playSoundAtEntity(closestPlayer, closestPlayer, s, vol, 0.8F + rand.nextFloat() * 0.2F);

							atmosia_ambientSoundCounter = rand.nextInt(range);
						}
					}
				}
			}
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void atmosia_worldTick(CallbackInfo ci) {
		atmosia_playAmbientSounds();
	}
}
