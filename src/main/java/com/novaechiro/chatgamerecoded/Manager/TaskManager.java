package com.novaechiro.chatgamerecoded.Manager;

import com.novaechiro.chatgamerecoded.ChatGameRecoded;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskManager {
   private final ChatGameRecoded plugin;
   public int taskID;

   public TaskManager(ChatGameRecoded instance) {
      this.plugin = instance;
   }

   public void restartTask() {
      Bukkit.getScheduler().cancelTask(this.plugin.mainTask.getTaskId());
      this.initTask();
   }

   public int timeExpiredTask() {
      this.taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
         if (this.plugin.inGame && this.plugin.selected != null) {
            this.plugin.inGame = false;
            this.plugin.gameManager.sendTimeExpiredToWorlds(this.plugin.selected, this.plugin.selectedGame);
            this.plugin.selected = null;
            this.plugin.selectedGame = null;
            this.plugin.gameManager.stopTimer();
         }

      }, (long)this.plugin.getConfig().getInt("timeToGuess_seconds") * 20L);
      return this.taskID;
   }

   public void initTask() {
      this.plugin.mainTask = (new BukkitRunnable() {
         public void run() {
            synchronized(TaskManager.this.plugin.games) {
               if (this.checkPlayersOnline()) {
                  TaskManager.this.plugin.selectedGame = TaskManager.this.plugin.games.get(ThreadLocalRandom.current().nextInt(TaskManager.this.plugin.games.size()));
                  TaskManager.this.plugin.gameManager.startGame(GameManager.GameType.valueOf(TaskManager.this.plugin.selectedGame.toUpperCase()));
               }

            }
         }

         public boolean checkPlayersOnline() {
            int vanished = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
               for (MetadataValue meta : player.getMetadata("vanished")) {
                  if (meta.asBoolean()) {
                     vanished++;
                  }
               }
            }

            return Bukkit.getOnlinePlayers().size() - vanished >= TaskManager.this.plugin.getConfig().getInt("min_players_online");
         }
      }).runTaskTimer(this.plugin, (long)(this.plugin.getConfig().getInt("time_minutes") * 20) * 60L, (long)(this.plugin.getConfig().getInt("time_minutes") * 20) * 60L);
   }
}
