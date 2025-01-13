package cookie.atmosia;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.sound.SoundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ClientStartEntrypoint;


public class Atmosia implements ModInitializer, ClientStartEntrypoint {
    public static final String MOD_ID = "atmosia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("Atmosia initialized.");
    }

	@Override
	public void beforeClientStart() {

	}

	@Override
	public void afterClientStart() {
		SoundRepository.SOUNDS.registerNamespace(MOD_ID);
	}
}
