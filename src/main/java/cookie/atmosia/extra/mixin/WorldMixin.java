package cookie.atmosia.extra.mixin;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.Biomes;
import net.minecraft.core.world.biome.provider.BiomeProvider;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkCoordinate;
import net.minecraft.core.world.season.SeasonManager;
import net.minecraft.core.world.season.SeasonWinter;
import net.minecraft.core.world.type.WorldType;
import net.minecraft.core.world.type.WorldTypeNether;
import net.minecraft.core.world.type.WorldTypeOverworld;
import net.minecraft.core.world.type.WorldTypeOverworldHell;
import net.minecraft.core.world.weather.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.Set;

@Mixin(value = World.class, remap = false)
public abstract class WorldMixin implements WorldSource {

	@Shadow
	@Final
	private Set<ChunkCoordinate> positionsToUpdate;

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
	public abstract EntityPlayer getClosestPlayer(double x, double y, double z, double radius);

	@Shadow
	public abstract void playSoundEffect(Entity player, SoundCategory category, double x, double y, double z, String soundPath, float volume, float pitch);

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
	@Final
	public WorldType worldType;
	@Shadow
	@Final
	private int heightBlocks;
	@Shadow
	@Final
	private BiomeProvider biomeProvider;
	@Shadow
	@Final
	public SeasonManager seasonManager;
	@Unique
	private int atmosia_ambientSoundCounter;

	@Inject(method = "<init>()V", at = @At("TAIL"))
	private void atmosia_initCounter(CallbackInfo ci) {
		atmosia_ambientSoundCounter = rand.nextInt(100);
	}

	@Unique
	protected void atmosia_playAmbientSounds() {
		if (atmosia_ambientSoundCounter > 0) {
			--atmosia_ambientSoundCounter;
		}

		for (ChunkCoordinate coordinate : positionsToUpdate) {
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

					if (id == 0) {
						EntityPlayer closestPlayer = getClosestPlayer((double) blockX + (double) 0.5F, (double) blockY + (double) 0.5F, (double) blockZ + (double) 0.5F, 8);
						if (closestPlayer != null && closestPlayer.distanceToSqr((double) blockX + (double) 0.5F, (double) blockY + (double) 0.5F, (double) blockZ + (double) 0.5F) > (double) 4) {
							if (worldType instanceof WorldTypeOverworld) {
								// Forests
								if (getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_FOREST ||
									getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_BIRCH_FOREST ||
									getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_BOREAL_FOREST ||
									getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_SEASONAL_FOREST) {
									if (canBlockSeeTheSky(blockX, blockY, blockZ)) {
										if (blockY < heightBlocks / 2) {
											if (getCurrentWeather() instanceof WeatherClear) {
												if (!(seasonManager.getCurrentSeason() instanceof SeasonWinter)) {
													if (isDaytime()) {
														switch (rand.nextInt(4)) {
															case 3:
																s = "atmosia.flit";
																break;
															case 2:
																s = "atmosia.chicka";
																break;
															case 1:
																s = "atmosia.chirp";
																break;
															case 0:
															default:
																s = "atmosia.squeek";
																break;
														}
													} else {
														s = "atmosia.critter";
													}
												}
											}
										}
									}
								}

								// Snow and Rain
								if (getCurrentWeather() instanceof WeatherRain || getCurrentWeather() instanceof WeatherSnow) {
									s = "atmosia.treewind";
								}

								// Grasslands & Cold Flat
								if ((biomeProvider.getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_GRASSLANDS ||
									biomeProvider.getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_SHRUBLAND ||
									biomeProvider.getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_MEADOW ||
									biomeProvider.getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_PLAINS ||
									biomeProvider.getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_TUNDRA ||
									biomeProvider.getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_GLACIER) &&
									blockY < getHeightBlocks() / 2) {
									s = "atmosia.wind_snippet";
								}

								// Swamps
								if (getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_SWAMPLAND ||
									getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_SWAMPLAND_MUDDY) {
									s = "atmosia.critter";
								}

								// Stormy
								if (canBlockSeeTheSky(blockX, blockY, blockZ) && blockY < getHeightBlocks() / 2 && getCurrentWeather() instanceof WeatherStorm) {
									s = "atmosia.wind_hit";
								}

								// Too High
								if (canBlockSeeTheSky(blockX, blockY, blockZ) && blockY >= getHeightBlocks() / 2) {
									s = "atmosia.windgust";
								}

								if (!canBlockSeeTheSky(blockX, blockY, blockZ) && blockY < getHeightBlocks() * 0.21) {
									switch (rand.nextInt(3)) {
										case 2:
											s = "atmosia.cave_hit";
											break;
										case 1:
											s = "atmosia.rain_drip";
											break;
										case 0:
										default:
											s = "atmosia.rumble";
											break;
									}
								}
							} else if (worldType instanceof WorldTypeNether || worldType instanceof WorldTypeOverworldHell) {
								s = "atmosia.wind_moan";
							}

							playSoundEffect(null,
								SoundCategory.CAVE_SOUNDS,
								(double) blockX + (double) 0.5F,
								(double) blockY + (double) 0.5F,
								(double) blockZ + (double) 0.5F,
								s,
								0.7F,
								0.8F + rand.nextFloat() * 0.2F);

							atmosia_ambientSoundCounter = rand.nextInt(150);
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
