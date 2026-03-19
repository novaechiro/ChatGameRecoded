package chatgames;

import Config.ConfigManager;
import Config.YamlConfig;
import Manager.GameManager;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Comando implements CommandExecutor {
   private final ChatGames plugin;

   public Comando(ChatGames instance) {
      this.plugin = instance;
   }

   public void startGameByCommand(CommandSender sender, String game) {
      YamlConfig messages = this.plugin.configManager.messages;
      if (this.plugin.games.contains(game)) {
         if (!sender.hasPermission("chatgames.start." + game) && !sender.hasPermission("chatgames.start.*")) {
            sender.sendMessage(ConfigManager.NOT_ENOUGH_PERMISSIONS);
         } else {
            if (!this.startFromDisabledWorld(sender) && !sender.hasPermission("chatgames.start.*")) {
               sender.sendMessage(ConfigManager.CANNOT_START_DISABLED);
               return;
            }

            if (this.plugin.inGame) {
               Bukkit.getScheduler().cancelTask(this.plugin.taskManager.taskID);
            }

            this.plugin.selectedGame = game;
            sender.sendMessage(this.plugin.Color(messages.getString("game_started").replaceAll("%game%", this.plugin.selectedGame)));
            this.plugin.gameManager.startGame(GameManager.GameType.valueOf(game.toUpperCase()));
         }
      }

   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (cmd.getName().equalsIgnoreCase("chatgames")) {
         YamlConfig messages = this.plugin.configManager.messages;
         String value;
         if (args.length == 0) {
            if (!sender.hasPermission("chatgames.help")) {
               sender.sendMessage(ConfigManager.NOT_ENOUGH_PERMISSIONS);
               return true;
            }

            Iterator var8 = messages.getStringList("help_command").iterator();

            while(var8.hasNext()) {
               value = (String)var8.next();
               sender.sendMessage(this.plugin.Color(value));
            }

            return true;
         }

         if (args.length == 1) {
            if (args[0].equalsIgnoreCase("top") && sender.hasPermission("chatgames.top")) {
               this.plugin.gameManager.getTop(sender);
               return true;
            }

            if (args[0].equalsIgnoreCase("unscramble")) {
               this.startGameByCommand(sender, "unscramble");
               return true;
            }

            if (args[0].equalsIgnoreCase("unreverse")) {
               this.startGameByCommand(sender, "unreverse");
               return true;
            }

            if (args[0].equalsIgnoreCase("reaction")) {
               this.startGameByCommand(sender, "reaction");
               return true;
            }

            if (args[0].equalsIgnoreCase("fillout")) {
               this.startGameByCommand(sender, "fillout");
               return true;
            }

            if (args[0].equalsIgnoreCase("random")) {
               this.startGameByCommand(sender, "random");
               return true;
            }

            if (args[0].equalsIgnoreCase("math")) {
               this.startGameByCommand(sender, "math");
               return true;
            }

            if (args[0].equalsIgnoreCase("variable")) {
               this.startGameByCommand(sender, "variable");
               return true;
            }

            if (args[0].equalsIgnoreCase("trivia")) {
               this.startGameByCommand(sender, "trivia");
               return true;
            }

            if (args[0].equalsIgnoreCase("toggle")) {
               if (sender instanceof Player) {
                  Player p = (Player)sender;
                  if (p.hasPermission("chatgames.toggle")) {
                     if (!this.plugin.gameManager.gamesIsToggled(p)) {
                        this.plugin.gameManager.toggleGames(p, true);
                        value = "ON";
                     } else {
                        this.plugin.gameManager.toggleGames(p, false);
                        value = "OFF";
                     }

                     sender.sendMessage(this.plugin.Color(messages.getString("toggle_game").replaceAll("%state%", value)));
                     return true;
                  }

                  sender.sendMessage(ConfigManager.NOT_ENOUGH_PERMISSIONS);
                  return true;
               }

               sender.sendMessage(ConfigManager.PLAYER_ONLY_COMMAND);
               return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
               if (sender.hasPermission("chatgames.reload")) {
                  this.plugin.reloadFiles();
                  sender.sendMessage(ConfigManager.CONFIG_RELOADED);
                  return true;
               }

               sender.sendMessage(ConfigManager.NOT_ENOUGH_PERMISSIONS);
               return true;
            }
         }
      }

      sender.sendMessage(ConfigManager.UNKNOWN_COMMAND);
      return true;
   }

   public boolean startFromDisabledWorld(CommandSender sender) {
      if (sender instanceof Player) {
         Player p = (Player)sender;
         Iterator var3 = this.plugin.getConfig().getStringList("disabled-worlds").iterator();

         String world;
         do {
            if (!var3.hasNext()) {
               return true;
            }

            world = (String)var3.next();
         } while(!world.equalsIgnoreCase(p.getWorld().getName()));

         return this.plugin.getConfig().getBoolean("allow_starting_from_disabled");
      } else {
         return true;
      }
   }
}
