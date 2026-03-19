package Manager;

import Rewards.RandomReward;
import chatgames.ChatGames;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class RewardManager extends RandomReward {
   private final ChatGames plugin;

   public RewardManager(ChatGames instance) {
      this.plugin = instance;
   }

   public List<String> loadGameRewards(String game, Player player, List<String>... list) {
      new ArrayList();
      String section = game + ".rewards";
      List commands;
      if (list.length == 1) {
         commands = this.getRewards(game, section, player, list[0]);
      } else {
         commands = this.getRewards(game, section, player);
      }

      if (commands.contains("")) {
         commands.remove("");
      }

      return commands;
   }

   private List<String> getRewards(String game, String section, Player player, List<String>... list) {
      List<String> commands = new ArrayList();
      ConfigurationSection rewardsSection = this.plugin.configManager.rewards.getConfigurationSection(section);
      if (rewardsSection == null) {
         return commands;
      } else {
         List rewards;
         if (list.length == 1) {
            rewards = list[0];
         } else {
            rewards = this.convertToList(rewardsSection.getKeys(false));
         }

         List<String> chosenRewards = new ArrayList();
         Iterator var9 = rewards.iterator();

         String reward;
         ConfigurationSection rewardSection;
         while(var9.hasNext()) {
            reward = (String)var9.next();
            rewardSection = rewardsSection.getConfigurationSection(reward);
            if (rewardSection != null) {
               if (rewardSection.isSet("chance")) {
                  double chance = rewardSection.getDouble("chance");
                  this.addReward(chance, reward);
               } else {
                  chosenRewards.add(reward);
               }
            }
         }

         if (!this.isEmptyReward()) {
            chosenRewards.add(this.getRandomReward());
            this.resetRewards();
         }

         var9 = chosenRewards.iterator();

         label74:
         while(true) {
            do {
               if (!var9.hasNext()) {
                  return commands;
               }

               reward = (String)var9.next();
               rewardSection = rewardsSection.getConfigurationSection(reward);
            } while(rewardSection == null);

            List<String> rewardData = rewardSection.getStringList("data");
            Iterator var13 = rewardData.iterator();

            while(true) {
               String command;
               String permission;
               do {
                  if (!var13.hasNext()) {
                     if (!this.isEmpty()) {
                        commands.add(this.getRandomValue());
                        this.resetValues();
                     }
                     continue label74;
                  }

                  command = (String)var13.next();
                  if (!command.contains("has:")) {
                     break;
                  }

                  int startIndex = command.indexOf("has:") + 4;
                  int endIndex = command.indexOf(" ", startIndex);
                  permission = endIndex == -1 ? command.substring(startIndex) : command.substring(startIndex, endIndex);
                  command = command.replaceAll("has:" + permission, "");
               } while(!player.hasPermission(permission));

               if (!command.contains("%~ ")) {
                  commands.add(command);
               } else {
                  try {
                     String[] split = command.split("%~ ");
                     double parsedChance = Double.parseDouble(split[0]);
                     this.add(parsedChance, split[1]);
                  } catch (NumberFormatException var18) {
                     this.plugin.getLogger().log(Level.SEVERE, "Error while trying to parse chance for game {0} in command line:", game.toUpperCase());
                     this.plugin.getLogger().log(Level.SEVERE, "- {0}", command);
                  }
               }
            }
         }
      }
   }

   public List<String> convertToList(Collection<String> strings) {
      return new ArrayList(strings);
   }

   public void executeCommands(List<String> commands, Player p) {
      Iterator var3 = commands.iterator();

      while(true) {
         while(var3.hasNext()) {
            String command = (String)var3.next();
            String cmd = command.replaceAll("%player%", p.getName());
            if (cmd.contains("[playercmd]")) {
               Bukkit.dispatchCommand(p, cmd.replace("[playercmd] ", ""));
            } else if (cmd.contains("[consolecmd]")) {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("[consolecmd] ", ""));
            } else if (cmd.contains("[playermsg]")) {
               p.sendMessage(this.plugin.Color(cmd.replace("[playermsg] ", "")));
            } else if (cmd.contains("[broadcast]")) {
               Iterator var6 = Bukkit.getOnlinePlayers().iterator();

               while(var6.hasNext()) {
                  Player player = (Player)var6.next();
                  if (this.plugin.gameManager.worldEnabled(p)) {
                     player.sendMessage(this.plugin.Color(cmd.replace("[broadcast] ", "")));
                  }
               }
            }
         }

         return;
      }
   }
}
