package cookie.atmosia.extra.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.net.handler.NetClientHandler;
import net.minecraft.client.world.WorldClient;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biomes;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkCoordinate;
import net.minecraft.core.world.season.SeasonWinter;
import net.minecraft.core.world.type.WorldTypeNether;
import net.minecraft.core.world.type.WorldTypeOverworld;
import net.minecraft.core.world.type.WorldTypeOverworldHell;
import net.minecraft.core.world.type.WorldTypes;
import net.minecraft.core.world.weather.WeatherClear;
import net.minecraft.core.world.weather.WeatherRain;
import net.minecraft.core.world.weather.WeatherSnow;
import net.minecraft.core.world.weather.WeatherStorm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = WorldClient.class, remap = false)
@Environment(EnvType.CLIENT)
public abstract class WorldClientMixin extends World {

	@Unique
	private int atmosia_ambientSoundCounter;

	@Unique
	private final Set<ChunkCoordinate> atmosia_positionsToUpdate = new HashSet<>();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void atmosia_initCounter(Minecraft minecraft, NetClientHandler netclienthandler, long seed, int dimensionId, int worldTypeId, CallbackInfo ci) {
		atmosia_ambientSoundCounter = rand.nextInt(150);
	}

	@Unique
	public void atmosia_playAmbientSounds() {
		atmosia_positionsToUpdate.clear();

		for (EntityPlayer player : players) {
			int playerChunkX = MathHelper.floor_double(player.x / (double) 16.0F);
			int playerChunkZ = MathHelper.floor_double(player.z / (double) 16.0F);
			byte radius = 9;

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
					int halfHeightBlocks = worldType == WorldTypes.OVERWORLD_EXTENDED ? (int) (getHeightBlocks() * 0.7) : getHeightBlocks() / 2;

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
										if (blockY < halfHeightBlocks) {
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
								if ((getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_GRASSLANDS ||
									getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_SHRUBLAND ||
									getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_MEADOW ||
									getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_PLAINS ||
									getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_TUNDRA ||
									getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_GLACIER) &&
									blockY < halfHeightBlocks) {
									s = "atmosia.wind_snippet";
								}

								// Swamps
								if (getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_SWAMPLAND ||
									getBiomeProvider().getBiome(blockX, blockY, blockZ) == Biomes.OVERWORLD_SWAMPLAND_MUDDY) {
									if (canBlockSeeTheSky(blockX, blockY, blockZ) && blockY < halfHeightBlocks) {
										s = "atmosia.critter";
									}
								}

								// Stormy
								if (canBlockSeeTheSky(blockX, blockY, blockZ) && blockY < halfHeightBlocks && getCurrentWeather() instanceof WeatherStorm) {
									s = "atmosia.wind_hit";
								}

								// Too High
								if (canBlockSeeTheSky(blockX, blockY, blockZ) && blockY >= halfHeightBlocks) {
									s = "atmosia.wind_hit";
								}

								if (!canBlockSeeTheSky(blockX, blockY, blockZ) && blockY < halfHeightBlocks * 0.7) {
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
	private void atmosia_tick(CallbackInfo ci) {
		atmosia_playAmbientSounds();
	}
}
