package com.novaechiro.chatgamerecoded.Manager;

import com.novaechiro.chatgamerecoded.ChatGameRecoded;
import org.bukkit.entity.Player;

public class PlaceHolderHook {
   private final ChatGameRecoded plugin;

   public PlaceHolderHook(ChatGameRecoded instance) {
      this.plugin = instance;
   }

   public boolean register() {
      return true;
   }

   public String getAuthor() {
      return "Nsider";
   }

   public String getIdentifier() {
      return "chatgames";
   }

   public String getVersion() {
      return "1.0.0";
   }

   public String onPlaceholderRequest(Player p, String identifier) {
      if (identifier.equals("points")) {
         return "" + this.plugin.gameManager.getPoints(p);
      } else {
         int top;
         if (identifier.startsWith("top_")) {
            try {
               top = Integer.parseInt(identifier.split("_")[1]);
               return this.plugin.gameManager.getSpecific(top);
            } catch (NumberFormatException var4) {
               this.plugin.getLogger().severe("Error while trying to get a player in top 10 from placeholder.");
               return "";
            }
         } else if (identifier.startsWith("points_top_")) {
            try {
               top = Integer.parseInt(identifier.split("_")[2]);
               return this.plugin.gameManager.getSpecificPoints(top);
            } catch (NumberFormatException var5) {
               this.plugin.getLogger().severe("Error while trying to get a player's points in top 10 from placeholder.");
               return "";
            }
         } else {
            return null;
         }
      }
   }
}
