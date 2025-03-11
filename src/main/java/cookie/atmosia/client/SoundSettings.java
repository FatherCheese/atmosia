package cookie.atmosia.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.components.FloatOptionComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionFloat;

@Environment(EnvType.CLIENT)
public class SoundSettings {
	private static final Minecraft minecraft = Minecraft.getMinecraft();
	private static final GameSettings gameSettings = minecraft.gameSettings;
	public static OptionFloat beachAmbienceVolume = new OptionFloat(gameSettings, "beachAmbience", 1);
	public static OptionFloat caveAmbienceVolume = new OptionFloat(gameSettings, "caveAmbience", 1);
	public static OptionFloat forestAmbienceVolume = new OptionFloat(gameSettings, "forestAmbience", 1);
	public static OptionFloat heightAmbienceVolume = new OptionFloat(gameSettings, "heightAmbience", 1);
	public static OptionFloat hellAmbienceVolume = new OptionFloat(gameSettings, "hellAmbience", 1);
	public static OptionFloat nightAmbienceVolume = new OptionFloat(gameSettings, "nightAmbience", 1);
	public static OptionFloat plainsAmbienceVolume = new OptionFloat(gameSettings, "plainsAmbience", 1);
	public static OptionFloat swampAmbienceVolume = new OptionFloat(gameSettings, "swampAmbience", 1);
	public static OptionFloat weatherAmbienceVolume = new OptionFloat(gameSettings, "weatherAmbience", 1);

	public SoundSettings() {
		OptionsPages.AUDIO
			.withComponent(new OptionsCategory("gui.options.page.audio.ambience")
			.withComponent(new FloatOptionComponent(beachAmbienceVolume))
			.withComponent(new FloatOptionComponent(caveAmbienceVolume))
			.withComponent(new FloatOptionComponent(forestAmbienceVolume))
			.withComponent(new FloatOptionComponent(heightAmbienceVolume))
			.withComponent(new FloatOptionComponent(hellAmbienceVolume))
			.withComponent(new FloatOptionComponent(nightAmbienceVolume))
			.withComponent(new FloatOptionComponent(plainsAmbienceVolume))
			.withComponent(new FloatOptionComponent(swampAmbienceVolume))
			.withComponent(new FloatOptionComponent(weatherAmbienceVolume)));
	}
}
