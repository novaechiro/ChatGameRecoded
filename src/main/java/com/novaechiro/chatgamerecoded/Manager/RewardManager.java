package com.novaechiro.chatgamerecoded.Manager;

import com.novaechiro.chatgamerecoded.Rewards.RandomReward;
import com.novaechiro.chatgamerecoded.ChatGameRecoded;
import com.novaechiro.chatgamerecoded.Config.YamlConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class RewardManager extends RandomReward {
   private final ChatGameRecoded plugin;

   public RewardManager(ChatGameRecoded instance) {
      this.plugin = instance;
   }

   public List<String> loadGameRewards(String game, Player player, List<String>... list) {
      String section = game + ".rewards";
      List<String> commands;
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
         List<String> rewards;
         if (list.length == 1) {
            rewards = list[0];
         } else {
            rewards = this.convertToList(rewardsSection.getKeys(false));
         }

         List<String> chosenRewards = new ArrayList();
         for (String reward : rewards) {
            ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(reward);
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

         for (String reward : chosenRewards) {
            ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(reward);
            if (rewardSection == null) continue;
            String option = rewardSection.getString("option", "exclusive");
            boolean independent = option.equalsIgnoreCase("independent");
            List<String> rewardData = rewardSection.getStringList("data");
            for (String command : rewardData) {
               String permission;
               if (command.contains("has:")) {
                  int startIndex = command.indexOf("has:") + 4;
                  int endIndex = command.indexOf(" ", startIndex);
                  permission = endIndex == -1 ? command.substring(startIndex) : command.substring(startIndex, endIndex);
                  command = command.replaceAll("has:" + permission, "");
                  if (!player.hasPermission(permission)) {
                     continue;
                  }
               }

               if (!command.contains("%~ ")) {
                  commands.add(command);
               } else {
                  try {
                     String[] split = command.split("%~ ", 2);
                     double parsedChance = Double.parseDouble(split[0]);
                     String rewardCommand = split[1];
                     if (independent) {
                        if (ThreadLocalRandom.current().nextDouble(100.0D) < parsedChance) {
                           commands.add(rewardCommand);
                        }
                     } else {
                        this.add(parsedChance, rewardCommand);
                     }
                  } catch (NumberFormatException ex) {
                     this.plugin.getLogger().log(Level.SEVERE, "Error while trying to parse chance for game {0} in command line:", game.toUpperCase());
                     this.plugin.getLogger().log(Level.SEVERE, "- {0}", command);
                  }
               }
            }
         }

         if (!this.isEmpty()) {
            commands.add(this.getRandomValue());
            this.resetValues();
         }

         return commands;
      }
   }

   public List<String> convertToList(Collection<String> strings) {
      return new ArrayList(strings);
   }

   public void executeCommands(List<String> commands, Player p) {
      for (String command : commands) {
         String cmd = command.replaceAll("%player%", p.getName());
         if (cmd.contains("[playercmd]")) {
            Bukkit.dispatchCommand(p, cmd.replace("[playercmd] ", ""));
         } else if (cmd.contains("[consolecmd]")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("[consolecmd] ", ""));
         } else if (cmd.contains("[playermsg]")) {
            p.sendMessage(this.plugin.Color(cmd.replace("[playermsg] ", "")));
         } else if (cmd.contains("[broadcast]")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
               if (this.plugin.gameManager.worldEnabled(p)) {
                  player.sendMessage(this.plugin.Color(cmd.replace("[broadcast] ", "")));
               }
            }
         }
      }
   }
}
