package de.mg127.TiaEssentials;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileConfig
{
	private File configFile = null;
	private FileConfiguration newConfig = null;
	private Logger logger= Logger.getLogger("Minecraft");
	private String filename = null;

	private TiaEssentials plugin;
	public FileConfig(TiaEssentials instance, String name){
		plugin=instance;
		filename=name+".yml";
	}

	public void saveDefaultConfig() {
		if (!this.configFile.exists()) {
			plugin.saveResource(filename, false);
		}
	}
	public void saveConfig() {
		try {
			getConfig().save(this.configFile);
		} catch (IOException ex) {
			this.logger.log(Level.SEVERE, "Could not save "+filename+" to " + this.configFile, ex);
		}
	}
	public FileConfiguration getConfig() {
		if (this.newConfig == null) {
			reloadConfig();
		}
		return this.newConfig;
	}

	public void reloadConfig() {
		this.newConfig = YamlConfiguration.loadConfiguration(this.configFile);

		InputStream defConfigStream = plugin.getResource(filename);
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.newConfig.setDefaults(defConfig);
		}
	}
}
