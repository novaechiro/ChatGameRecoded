package com.novaechiro.chatgamerecoded;

import com.novaechiro.chatgamerecoded.Config.ConfigManager;
import com.novaechiro.chatgamerecoded.Config.YamlConfig;
import com.novaechiro.chatgamerecoded.Manager.ChatManager;
import com.novaechiro.chatgamerecoded.Manager.GameManager;
import com.novaechiro.chatgamerecoded.Manager.PlaceHolderHook;
import com.novaechiro.chatgamerecoded.Manager.RewardManager;
import com.novaechiro.chatgamerecoded.Manager.TaskManager;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class ChatGameRecoded extends JavaPlugin implements Listener {
    public static ChatGameRecoded instance;
    public boolean inGame;
    public String selected;
    public String selectedGame;
    public String equation = "";
    public final ArrayList<String> words = new ArrayList();
    public final ArrayList<String> reactionWords = new ArrayList();
    public final ArrayList<String> filloutWords = new ArrayList();
    public final ArrayList<String> unreverseWords = new ArrayList();
    public final ArrayList<String> variables = new ArrayList();
    public final ArrayList<String> trivias = new ArrayList();
    public final ArrayList<String> games = new ArrayList();
    public final ArrayList<String> triviaAnswers = new ArrayList();
    public HashMap<String, Integer> points = new HashMap();
    public BukkitTask mainTask;
    public GameManager gameManager;
    public RewardManager rewardManager;
    public TaskManager taskManager;
    public ConfigManager configManager;

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.gameManager = new GameManager(this);
        this.rewardManager = new RewardManager(this);
        this.configManager = new ConfigManager(this);
        this.taskManager = new TaskManager(this);
        this.getLogger().info("ChatGames is now enabled!");
        this.getServer().getPluginManager().registerEvents(new ChatManager(this), this);
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("chatgames").setExecutor(new com.novaechiro.chatgamerecoded.Comando(this));
        File config = new File(this.getDataFolder() + File.separator + "config.yml");
        if (!config.exists()) {
            this.saveDefaultConfig();
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            (new PlaceHolderHook(this)).register();
            this.getLogger().info("PlaceholderAPI Hooked!");
        }

        this.correctSound();
        this.loadPointsValues();
        this.loadGames();
        this.loadWords();
        this.taskManager.initTask();
    }

    public void onDisable() {
        this.getLogger().info("ChatGames is now disabled!");
    }

    public void loadGames() {
        if (this.getConfig().getBoolean("unscramble.enable")) {
            this.games.add("unscramble");
        }

        if (this.getConfig().getBoolean("unreverse.enable")) {
            this.games.add("unreverse");
        }

        if (this.getConfig().getBoolean("reaction.enable")) {
            this.games.add("reaction");
        }

        if (this.getConfig().getBoolean("fillout.enable")) {
            this.games.add("fillout");
        }

        if (this.getConfig().getBoolean("random.enable")) {
            this.games.add("random");
        }

        if (this.getConfig().getBoolean("math.enable")) {
            this.games.add("math");
        }

        if (this.getConfig().getBoolean("variable.enable")) {
            this.games.add("variable");
        }

        if (this.getConfig().getBoolean("trivia.enable")) {
            this.games.add("trivia");
        }

    }

    public void loadWords() {
        YamlConfig config = this.configManager.words;
        if (this.games.contains("unscramble")) {
            this.words.addAll(config.getStringList("unscramble.Words"));
        }

        if (this.games.contains("unreverse")) {
            this.unreverseWords.addAll(config.getStringList("unreverse.Words"));
        }

        if (this.games.contains("reaction")) {
            this.reactionWords.addAll(config.getStringList("reaction.Words"));
        }

        if (this.games.contains("fillout")) {
            this.filloutWords.addAll(config.getStringList("fillout.Words"));
        }

        if (this.games.contains("variable")) {
            this.variables.addAll(this.getConfig().getStringList("variable.data"));
        }

        if (this.games.contains("trivia")) {
            this.trivias.addAll(this.getConfig().getConfigurationSection("trivia.data").getKeys(false));
        }

    }

    public void loadPointsValues() {
        try {
            this.configManager.pointsdata.getConfigurationSection("Points").getKeys(false).stream().filter((path) -> {
                return path != null;
            }).forEachOrdered((path) -> {
                this.points.put(path, this.configManager.pointsdata.getInt("Points." + path + ".amount"));
            });
            this.getLogger().info("Points loaded successfully!");
        } catch (NullPointerException ex) {
            this.getLogger().info("No points info was found! If this is your first time enabling the plugin, it is normal.");
        }

    }

    public void reloadFiles() {
        this.words.clear();
        this.reactionWords.clear();
        this.unreverseWords.clear();
        this.trivias.clear();
        this.games.clear();
        this.reloadConfig();
        this.configManager.reload();
        this.loadGames();
        this.loadWords();
        this.taskManager.restartTask();
    }

    public void correctSound() {
        if (this.getMCVersion() <= 8) {
            this.getConfig().set("sound", "LEVEL_UP");
            this.saveConfig();
        }

    }

    public int getMCVersion() {
        String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        String verNum = split[1];
        return Integer.parseInt(verNum);
    }

    public boolean textEqualsWord(String game, String chatText, String word) {
      String stripped = ChatColor.stripColor(chatText);
      boolean caseSensitive = this.getConfig().getBoolean(game + ".case-sensitive");

      if ("trivia".equalsIgnoreCase(game) && !this.triviaAnswers.isEmpty()) {
         for (String answer : this.triviaAnswers) {
            if (caseSensitive ? stripped.equals(answer) : stripped.equalsIgnoreCase(answer)) {
               return true;
            }
         }
         return false;
      }

      return caseSensitive ? stripped.equals(word) : stripped.equalsIgnoreCase(word);
   }

    public String Color(String message1) {
        String message = message1.replaceAll("%prefix%", this.getConfig().getString("prefix"));
        if (this.getMCVersion() < 16) {
            return message.replace("&", "§");
        } else {
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");

            for(Matcher matcher = pattern.matcher(message); matcher.find(); matcher = pattern.matcher(message)) {
                String hexCode = message.substring(matcher.start(), matcher.end());
                String replaceSharp = hexCode.replace('#', 'x');
                char[] chars = replaceSharp.toCharArray();
                StringBuilder builder = new StringBuilder();

                for(char c : chars) {
                    builder.append('&').append(c);
                }

                message = message.replace(hexCode, builder.toString());
            }

            return ChatColor.translateAlternateColorCodes('&', message);
        }
    }
}

