package Config;

import chatgames.ChatGames;

public class ConfigManager {
   private final ChatGames plugin;
   public final YamlConfig words;
   public final YamlConfig rewards;
   public final YamlConfig messages;
   public final YamlConfig pointsdata;
   public static String CONFIG_RELOADED;
   public static String UNKNOWN_COMMAND;
   public static String NOT_ENOUGH_PERMISSIONS;
   public static String CANNOT_START_DISABLED;
   public static String PLAYER_ONLY_COMMAND;

   public ConfigManager(ChatGames instance) {
      this.plugin = instance;
      this.words = new YamlConfig(this.plugin, "words");
      this.rewards = new YamlConfig(this.plugin, "rewards");
      this.pointsdata = new YamlConfig(this.plugin, "pointsdata");
      this.messages = new YamlConfig(this.plugin, "messages");
      this.loadMessages();
   }

   public boolean reload() {
      return this.words.reload() && this.rewards.reload() && this.messages.reload() && this.pointsdata.reload();
   }

   private void loadMessages() {
      YamlConfig config = this.messages;
      CONFIG_RELOADED = this.color(config.getString("config_reloaded", "&cMessage has not been configured"));
      UNKNOWN_COMMAND = this.color(config.getString("unknown_command", "&cMessage has not been configured"));
      NOT_ENOUGH_PERMISSIONS = this.color(config.getString("not_enough_permissions", "&cMessage has not been configured"));
      CANNOT_START_DISABLED = this.color(config.getString("cannot_start_from_disabled", "&cMessage has not been configured"));
      PLAYER_ONLY_COMMAND = this.color(config.getString("player_only_command", "&cMessage has not been configured"));
   }

   public String color(String message) {
      return this.plugin.Color(message);
   }
}
