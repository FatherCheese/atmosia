package cookie.atmosia;

import cookie.atmosia.core.ASounds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;


public class Atmosia implements ModInitializer, GameStartEntrypoint {
    public static final String MOD_ID = "atmosia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("Atmosia initialized.");
    }

	@Override
	public void beforeGameStart() {
		ASounds.initializeSounds();
	}

	@Override
	public void afterGameStart() {

	}
}
