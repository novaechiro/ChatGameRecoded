package com.novaechiro.chatgamerecoded.Config;

import com.novaechiro.chatgamerecoded.ChatGameRecoded;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlConfig extends YamlConfiguration {
   private final File file;
   private final String path;
   private final ChatGameRecoded plugin;

   public YamlConfig(ChatGameRecoded plugin, String path) {
      this.plugin = plugin;
      this.path = path + ".yml";
      this.file = new File(plugin.getDataFolder(), this.path);
      this.saveDefault();
      this.reload();
   }

   public boolean reload() {
      try {
         super.load(this.file);
         return true;
      } catch (InvalidConfigurationException | IOException exception) {
         this.plugin.getLogger().log(Level.SEVERE, "Failed to load configuration file {0}!", this.path);
         return false;
      }
   }

   public boolean save() {
      try {
         super.save(this.file);
         return true;
      } catch (IOException exception) {
         this.plugin.getLogger().log(Level.SEVERE, "Failed to save configuration file {0}!", this.path);
         return false;
      }
   }

   public void saveDefault() {
      try {
         if (!this.file.exists()) {
            if (this.plugin.getResource(this.path) != null) {
               this.plugin.saveResource(this.path, false);
            } else {
               this.file.createNewFile();
            }
         }
      } catch (IOException exception) {
         this.plugin.getLogger().log(Level.SEVERE, "Failed to create configuration file {0}!", this.path);
      }

   }
}
