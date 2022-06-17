package us.jusybiberman.carpetbag.util;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;
import us.jusybiberman.carpetbag.Carpetbag;

import java.io.File;

public class ModConfiguration {
	private static File modConfigDir;
	private static Configuration mainConfig;
	public static int DIMENSION_PROVIDER_ID = -1;
	public static int DUNGEON_DIMENSION_AMOUNT = 3;
	public static boolean randomizeSeed = false;
	public static boolean tallChunkFriendly = false;
	public static boolean dimensionFolderIsDeletedWithSafeDel = true;

	public static void init(Configuration cfg)
	{
		tallChunkFriendly = cfg.get("general", "tallChunkFriendly", tallChunkFriendly,
				"If true then mods using McJtyLib might try to be as friendly as possible to mods that support very tall chunks (taller then 256). No guarantees however! Set to false for more optimal performance").getBoolean();
		dimensionFolderIsDeletedWithSafeDel = cfg.get("general", "dimensionFolderIsDeletedWithSafeDel", dimensionFolderIsDeletedWithSafeDel,
				"If this is enabled the /deletedungeon <id> command will also delete the DIM<id> folder. If false then this has to be done manually").getBoolean();


		DIMENSION_PROVIDER_ID = cfg.get("Dimensions", "DIMENSION_PROVIDER_ID", DIMENSION_PROVIDER_ID, "Set the base ID for the Dungeon Dimensions. Multiple dimensions will be created with incrementing IDs. (-1 means try to find one automatically)").getInt();
		DUNGEON_DIMENSION_AMOUNT = cfg.getInt("DIMENSION_PROVIDER_ID", "Dimensions", DIMENSION_PROVIDER_ID, 1, 999, "Set the amount of Dungeon Dimensions to create.");
		randomizeSeed = cfg.get("Dimensions", "randomizeSeed", randomizeSeed, "Set the amount of Dungeon Dimensions to create.").getBoolean();

	}

	public static Configuration getMainConfig() {
		return mainConfig;
	}

	public static void registerConfig(FMLPreInitializationEvent event)
	{
		modConfigDir = event.getModConfigurationDirectory();
		mainConfig = new Configuration(new File(Carpetbag.config.getPath(), Carpetbag.MOD_ID + ".cfg"));
		try {
			mainConfig.load();
			mainConfig.addCustomCategoryComment("general", "General settings for all mods using mcjtylib");
			mainConfig.addCustomCategoryComment("Entity IDs", "Set the ID's for the entities to ensure that they don't clash with other mod's ids");
			mainConfig.addCustomCategoryComment("GUI IDs", "Set the ID's for the GUI's to ensure that they don't clash with other mod's ids");
			mainConfig.addCustomCategoryComment("Dimensions", "Settings for dimensions generated by carpetbag");
			mainConfig.addCustomCategoryComment("Biomes", "Set the ID's for the GUI's to ensure that they don't clash with other mod's ids");


			//mainConfig.addCustomCategoryComment(StyleConfig.CATEGORY_STYLE, "Style settings for all mods using mcjtylib");
			init(mainConfig);
			//StyleConfig.init(mainConfig);
		} catch (RuntimeException e1) {
			FMLLog.log(Level.ERROR, e1, "Problem loading config file: carpetbag.cfg!");
		} finally {
			if (mainConfig.hasChanged()) {
				mainConfig.save();
			}
		}
	}
}
