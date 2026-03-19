package com.novaechiro.chatgamerecoded.Manager;

import com.novaechiro.chatgamerecoded.ChatGameRecoded;
import com.novaechiro.chatgamerecoded.Config.YamlConfig;
import com.novaechiro.chatgamerecoded.Utils;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameManager {
   private final ChatGameRecoded plugin;
   private long startTime = 0L;
   private long stopTime = 0L;
   private boolean running = false;

   public GameManager(ChatGameRecoded instance) {
      this.plugin = instance;
   }

   public String Color(String message) {
      return this.plugin.Color(message);
   }

   public void setPoints(Player plr, int amount) {
      this.plugin.configManager.pointsdata.set("Points." + plr.getName() + ".amount", amount);
      this.plugin.configManager.pointsdata.save();
   }

   public int getPoints(Player plr) {
      if (!this.plugin.configManager.pointsdata.isSet("Points." + plr.getName() + ".amount")) {
         this.setPoints(plr, 0);
      }

      return this.plugin.configManager.pointsdata.getInt("Points." + plr.getName() + ".amount");
   }

   public List<Entry<String, Integer>> getTopChat() {
      if (!this.plugin.points.isEmpty()) {
         return this.plugin.points.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder())).limit(10L).collect(Collectors.toList());
      } else {
         return null;
      }
   }

   public String getSpecific(int spot) {
      return ((Entry)this.getTopChat().get(spot - 1)).getKey() != null ? (String)((Entry)this.getTopChat().get(spot - 1)).getKey() : null;
   }

   public String getSpecificPoints(int spot) {
      return ((Entry)this.getTopChat().get(spot - 1)).getValue() != null ? "" + ((Entry)this.getTopChat().get(spot - 1)).getValue() : null;
   }

   public void updateTop(String playername, int oldAmount, int newAmount) {
      if (this.plugin.points.containsKey(playername)) {
         this.plugin.points.replace(playername, oldAmount, newAmount);
      } else {
         this.plugin.points.put(playername, newAmount);
      }

   }

   public void getTop(CommandSender sender) {
      int spot = 0;
      YamlConfig messages = this.plugin.configManager.messages;

      try {
         this.sendConfigMessage(sender, messages.getString("top.header"));
         sender.sendMessage("");
         for (Entry<String, Integer> tops : this.getTopChat()) {
            ++spot;
            if (spot > 10) {
               break;
            }
            this.sendConfigMessage(sender, messages.getString("top.format").replaceAll("%spot%", "" + spot).replaceAll("%player%", tops.getKey()).replaceAll("%points%", "" + tops.getValue()));
         }

         sender.sendMessage("");
         if (sender instanceof Player) {
            Player p = (Player)sender;
            this.sendConfigMessage(sender, messages.getString("top.player-stat-format").replaceAll("%player_points%", "" + this.getPoints(p)));
         }

         this.sendConfigMessage(sender, messages.getString("top.footer"));
      } catch (NullPointerException var6) {
         sender.sendMessage(this.Color(messages.getString("top.no_data_message")));
      }

   }

   public void startTimer() {
      this.startTime = System.currentTimeMillis();
      this.running = true;
   }

   public void stopTimer() {
      this.stopTime = System.currentTimeMillis();
      this.running = false;
   }

   public double roundTo2Decimals(double val) {
      DecimalFormat df = new DecimalFormat("#.##");
      String num = df.format(val).replace(",", ".");
      double rounded = Double.valueOf(num);
      return rounded;
   }

   public void executeCommands(List<String> commands, Player p) {
      for (String command : commands) {
         String cmd = command.replaceAll("%player%", p.getName());
         if (cmd.contains("[playercmd]")) {
            Bukkit.dispatchCommand(p, cmd.replace("[playercmd] ", ""));
         } else if (cmd.contains("[consolecmd]")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("[consolecmd] ", ""));
         } else if (cmd.contains("[playermsg]")) {
            Utils.sendMessage(p, cmd.replace("[playermsg] ", ""));
         } else if (cmd.contains("[broadcast]")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
               if (this.worldEnabled(p)) {
                  Utils.sendMessage(player, cmd.replace("[broadcast] ", ""));
               }
            }
         }
      }
   }

   public String getElapsedTime() {
      long elapsed;
      if (this.running) {
         elapsed = System.currentTimeMillis() - this.startTime;
      } else {
         elapsed = this.stopTime - this.startTime;
      }

      return elapsed / 1000L + "." + elapsed % 1000L / 100L;
   }

   public String scrableWord(String word) {
      String[] words = word.split(" ");
      List<String> newWords = new ArrayList();

      String message;
      for(int w = 0; w < words.length; ++w) {
         message = words[w];
         char[] c = message.toCharArray();
         List<Character> c2 = new ArrayList();

         for(int i = 0; i < c.length; ++i) {
            c2.add(c[i]);
         }

         String newword = "";
         Random randomword = new Random();

         for(int i = 0; i < c.length; ++i) {
            int inumber2 = randomword.nextInt(c2.size());
            c[i] = c2.get(inumber2);
            c2.remove(inumber2);
         }

         for(int i = 0; i < c.length; ++i) {
            newword = newword + c[i];
         }

         newWords.add(newword);
      }

      word = "";
      for (String message2 : newWords) {
         if (newWords.size() > 1) {
            word = word + message2 + " ";
         } else {
            word = word + message2;
         }
      }

      return word;
   }

   public String reverse(String word) {
      return (new StringBuilder(word)).reverse().toString();
   }

   public String getTimesReplaced(String message, String symbol) {
      int totalreplaced;
      for(totalreplaced = 0; !message.replaceFirst("%symbol%", symbol).equals(message); ++totalreplaced) {
         message = message.replaceFirst("%symbol%", symbol);
      }

      return message + ";" + totalreplaced;
   }

   public List<String> createSymbolEquation() {
      ArrayList<Integer> numbers = new ArrayList();
      List<String> symbols = new ArrayList();
      List<String> message = new ArrayList();
      Random rand = new Random();

      int random;
      int value;
      while(numbers.size() < 2) {
         random = rand.nextInt(this.plugin.variables.size() + 1);
         if (random == 0) {
            random = 1;
         }

         String info = this.plugin.variables.get(random - 1);
         String symbol = info.split(";")[0];
         value = Integer.parseInt(info.split(";")[1]);
         if (!numbers.contains(value)) {
            symbols.add(symbol);
            numbers.add(value);
            String format = this.getTimesReplaced(this.plugin.configManager.messages.getString("variable.line1and2-format"), symbol);
            int additionResult = value * Integer.parseInt(format.split(";")[1]);
            message.add(format.split(";")[0] + additionResult);
         }
      }

      random = this.plugin.getConfig().getInt("variable.result-min");
      int max = this.plugin.getConfig().getInt("variable.result-max");
      int result = rand.nextInt(max - random + 1) + random;
      value = numbers.get(0);
      int value2 = numbers.get(1);
      message.add(this.plugin.configManager.messages.getString("variable.line3-format").replaceAll("%symbol_1%", symbols.get(0)).replaceAll("%symbol_2%", symbols.get(1)).replaceAll("%result%", "" + result).replaceAll("%toGet%", this.plugin.getConfig().getString("variable.toGet")));
      this.plugin.selected = "" + (result - value - value2);
      return message;
   }

   public String generateRandomCharacters(int length, GameManager.Type type) {
      StringBuilder builder = new StringBuilder();
      String characters = "";
      switch(type) {
      case APLHA:
         characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
         break;
      case ALPHANUMERIC:
         characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
         break;
      case NUMERIC:
         characters = "0123456789";
         break;
      case SYMBOLIC:
         characters = "~,.:?;[]{}´`&*()^!@#$%¨-_+=></ ";
      }

      for(int i = 0; i < length; ++i) {
         double index = Math.random() * (double)characters.length();
         builder.append(characters.charAt((int)index));
      }

      return builder.toString();
   }

   public boolean gamesIsToggled(Player p) {
      if (this.plugin.getConfig().getBoolean("use-permission-toplay") && !p.hasPermission("chatgames.play")) {
         return false;
      } else {
         return !this.plugin.configManager.pointsdata.isSet("Points." + p.getName() + ".toggled") || this.plugin.configManager.pointsdata.getBoolean("Points." + p.getName() + ".toggled");
      }
   }

   public void toggleGames(Player p, boolean value) {
      this.plugin.configManager.pointsdata.set("Points." + p.getName() + ".toggled", value);
      this.plugin.configManager.pointsdata.save();
   }

   public String randomMathQuestion() {
      int answer = 0;
      Random rand = new Random();
      List<String> operators = new ArrayList();
      for (String op : this.plugin.getConfig().getStringList("math.operators")) {
         operators.add(op);
      }

      int opToUse = rand.nextInt(operators.size());
      if (opToUse == 0) {
         opToUse = 1;
      }

      String op = operators.get(opToUse - 1);
      int difficulty = this.plugin.getConfig().getInt("math.difficulty");
      if (difficulty == 0) {
         ++difficulty;
      }

      int num1;
      int num2;
      switch(difficulty) {
      case 1:
         num1 = rand.nextInt(9);
         num2 = rand.nextInt(9);
         break;
      case 2:
         num1 = rand.nextInt(99);
         num2 = rand.nextInt(99);
         break;
      case 3:
         num1 = rand.nextInt(999);
         num2 = rand.nextInt(999);
         break;
      case 4:
         num1 = rand.nextInt(9999);
         num2 = rand.nextInt(9999);
         break;
      default:
         num1 = rand.nextInt(99999);
         num2 = rand.nextInt(99999);
      }

      switch(op) {
      case "+":
         answer = num1 + num2;
         break;
      case "-":
         answer = num1 - num2;
         break;
      case "*":
         answer = num1 * num2;
         break;
      case "/":
         answer = num1 / num2;
      }

      return "" + num1 + " " + op + " " + num2 + ";" + answer;
   }

   public String hideLetters(String word) {
      String[] words = word.split(" ");
      List<String> newWords = new ArrayList();

      for (String newWord : words) {
         String current = newWord;

         for(int i = 0; i < current.length() / 2; ++i) {
            Random rand = new Random();
            int hidden = rand.nextInt(current.length());
            if (hidden == 0) {
               hidden = 1;
            }

            String hiddenLetter = "" + current.charAt(hidden - 1);
            current = current.replaceFirst(hiddenLetter, "_");
         }

         newWords.add(current);
      }

      word = "";
      for (String newWord : newWords) {
         if (newWords.size() > 1) {
            word = word + newWord + " ";
         } else {
            word = word + newWord;
         }
      }

      return word;
   }

   public void startGame(GameManager.GameType type) {
      switch(type) {
      case UNSCRAMBLE:
         this.startUnscrambleGame();
         break;
      case UNREVERSE:
         this.startUnreverseGame();
         break;
      case REACTION:
         this.startReactionGame();
         break;
      case RANDOM:
         this.startRandomTypeGame();
         break;
      case FILLOUT:
         this.startFillOutGame();
         break;
      case MATH:
         this.startMathGame();
         break;
      case VARIABLE:
         this.startVariableGame();
         break;
      case TRIVIA:
         this.startTriviaGame();
      }

      Bukkit.getConsoleSender().sendMessage(this.plugin.Color("&8[&c&lC&d&lH&b&lA&a&lT &6&lG&e&lA&a&lM&c&lE&b&lS&8] &fA(n) &a" + type.toString() + " &fchat game has started in the server!"));
   }

   public void sendConfigMessage(CommandSender p, String message) {
      if (message.contains("<center>")) {
         Utils.sendCenteredMessage(p, this.Color(message.replaceAll("<center>", "")));
      } else {
         p.sendMessage(this.Color(message));
      }

   }

   public void sendListMessage(Player p, List<String> list, String replace, String replacement, String replace1, String replacement1) {
      if (this.gamesIsToggled(p)) {
         for (String msg : list) {
            if (msg.contains("<center>")) {
               Utils.sendCenteredMessage(p, this.plugin.Color(msg.replaceAll("<center>", "").replaceAll(replace, replacement).replaceAll(replace1, replacement1)));
            } else {
               Utils.sendMessage(p, this.plugin.Color(msg.replaceAll(replace, replacement).replaceAll(replace1, replacement1)));
            }
         }
      }

   }

   public void sendWinnerMessage(Player p, List<String> list, String replace, String replacement, String replace1, String replacement1, String replace2, String replacement2, String replace3, String replacement3, String replace4, String replacement4) {
      if (this.gamesIsToggled(p)) {
         for (String msg : list) {
            if (msg.contains("<center>")) {
               Utils.sendCenteredMessage(p, this.plugin.Color(msg.replaceAll("<center>", "").replaceAll(replace, replacement).replaceAll(replace1, replacement1).replaceAll(replace2, replacement2).replaceAll(replace3, replacement3).replaceAll(replace4, replacement4)));
            } else {
               Utils.sendMessage(p, this.plugin.Color(msg.replaceAll(replace, replacement).replaceAll(replace1, replacement1).replaceAll(replace2, replacement2).replaceAll(replace3, replacement3).replaceAll(replace4, replacement4)));
            }
         }
      }

   }

   public void sendVariableGameMessage(Player p, List<String> game, List<String> list, String replace, String replacement, String replace1, String replacement1) {
      if (this.gamesIsToggled(p)) {
         for (String msg : list) {
            String newMsg = msg;
            if (msg.contains("%line_1%")) {
               newMsg = msg.replaceAll("%line_1%", game.get(0));
            }

            if (msg.contains("%line_2%")) {
               newMsg = newMsg.replaceAll("%line_2%", game.get(1));
            }

            if (msg.contains("%line_3%")) {
               newMsg = newMsg.replaceAll("%line_3%", game.get(2));
            }

            if (msg.contains("<center>")) {
               Utils.sendCenteredMessage(p, this.Color(newMsg.replaceAll("<center>", "").replaceAll(replace, replacement).replaceAll(replace1, replacement1)));
            } else {
               Utils.sendMessage(p, this.Color(newMsg.replaceAll(replace, replacement).replaceAll(replace1, replacement1)));
            }
         }
      }

   }

   public void sendTimeExpiredToWorlds(String answer, String game) {
      YamlConfig messages = this.plugin.configManager.messages;
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (this.worldEnabled(player)) {
            this.sendListMessage(player, messages.getStringList(game + ".time_expired_message"), "%timeToGuess%", "" + this.plugin.getConfig().getInt("timeToGuess_seconds"), "%correct_answer%", answer);
         }
      }

   }

   public boolean worldEnabled(Player player) {
      for (String world : this.plugin.getConfig().getStringList("disabled-worlds")) {
         if (world.equalsIgnoreCase(player.getWorld().getName())) {
            return false;
         }
      }

      return true;
   }

   public void sendGameStartedMessage(String game, String variable, String word) {
      YamlConfig messages = this.plugin.configManager.messages;
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (this.worldEnabled(player) && this.gamesIsToggled(player)) {
            if (this.plugin.getConfig().getBoolean("playSound")) {
               player.getWorld().playSound(player.getLocation(), Sound.valueOf(this.plugin.getConfig().getString("sound")), 1.0F, 1.0F);
            }

            this.sendListMessage(player, messages.getStringList(game + ".gameStartAnnouncement"), variable, word, "%timeToGuess%", "" + this.plugin.getConfig().getInt("timeToGuess_seconds"));
            if (!this.plugin.getConfig().getString(game + ".titleMessage").isEmpty() && !this.plugin.getConfig().getString(game + ".subtitleMessage").isEmpty()) {
               player.sendTitle(this.Color(this.plugin.getConfig().getString(game + ".titleMessage").replaceAll(variable, word)), this.Color(this.plugin.getConfig().getString(game + ".subtitleMessage").replaceAll(variable, word)));
            }
         }
      }

   }

   public void startUnscrambleGame() {
      if (!this.plugin.words.isEmpty()) {
         String newWord;
         this.plugin.inGame = true;
         this.startTimer();
         Random rand = new Random();
         int getran = rand.nextInt(this.plugin.words.size());
         this.plugin.selected = this.plugin.words.get(getran);
         newWord = this.scrableWord(this.plugin.selected);
         if (newWord == null || newWord.equals(this.plugin.selected)) {
            newWord = this.scrableWord(this.plugin.selected);
         }

         this.sendGameStartedMessage("unscramble", "%word%", newWord);
         this.plugin.taskManager.timeExpiredTask();
      }

   }

   public void startUnreverseGame() {
      if (!this.plugin.unreverseWords.isEmpty()) {
         this.plugin.inGame = true;
         this.startTimer();
         Random rand = new Random();
         int getran = rand.nextInt(this.plugin.unreverseWords.size());
         this.plugin.selected = this.plugin.unreverseWords.get(getran);
         String newWord = this.reverse(this.plugin.selected);
         this.sendGameStartedMessage("unreverse", "%word%", newWord);
         this.plugin.taskManager.timeExpiredTask();
      }

   }

   public void startReactionGame() {
      if (!this.plugin.reactionWords.isEmpty()) {
         this.plugin.inGame = true;
         this.startTimer();
         Random rand = new Random();
         int getran = rand.nextInt(this.plugin.reactionWords.size());
         this.plugin.selected = this.plugin.reactionWords.get(getran);
         String newWord = this.plugin.selected;
         this.sendGameStartedMessage("reaction", "%word%", newWord);
         this.plugin.taskManager.timeExpiredTask();
      }

   }

   public void startFillOutGame() {
      if (!this.plugin.filloutWords.isEmpty()) {
         this.plugin.inGame = true;
         this.startTimer();
         Random rand = new Random();
         int getran = rand.nextInt(this.plugin.filloutWords.size());
         String word = this.plugin.filloutWords.get(getran);
         String hidden = this.hideLetters(word);
         this.plugin.selected = word;
         this.sendGameStartedMessage("fillout", "%word%", hidden);
         this.plugin.taskManager.timeExpiredTask();
      }

   }

   public void startRandomTypeGame() {
      this.plugin.inGame = true;
      this.startTimer();
      this.plugin.selected = this.generateRandomCharacters(this.plugin.getConfig().getInt("random.length"), GameManager.Type.valueOf(this.plugin.getConfig().getString("random.character_type")));
      String random = this.plugin.selected;
      this.sendGameStartedMessage("random", "%random%", random);
      this.plugin.taskManager.timeExpiredTask();
   }

   public void startMathGame() {
      this.plugin.inGame = true;
      this.startTimer();
      String[] question = this.randomMathQuestion().split(";");
      String equation = question[0];
      this.plugin.equation = equation;
      this.plugin.selected = question[1];
      this.sendGameStartedMessage("math", "%equation%", equation);
      this.plugin.taskManager.timeExpiredTask();
   }

   public void startVariableGame() {
      if (this.plugin.variables.size() >= 2) {
         this.plugin.inGame = true;
         this.startTimer();
         List<String> game = this.createSymbolEquation();
         YamlConfig messages = this.plugin.configManager.messages;
         for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.worldEnabled(player) && this.gamesIsToggled(player)) {
               if (this.plugin.getConfig().getBoolean("playSound")) {
                  player.getWorld().playSound(player.getLocation(), Sound.valueOf(this.plugin.getConfig().getString("sound")), 1.0F, 1.0F);
               }

               this.sendVariableGameMessage(player, game, messages.getStringList("variable.gameStartAnnouncement"), "%timeToGuess%", "" + this.plugin.getConfig().getInt("timeToGuess_seconds"), "%symbol%", this.plugin.getConfig().getString("variable.toGet"));
               if (!this.plugin.getConfig().getString("variable.titleMessage").isEmpty() && !this.plugin.getConfig().getString("variable.subtitleMessage").isEmpty()) {
                  player.sendTitle(this.Color(this.plugin.getConfig().getString("variable.titleMessage").replaceAll("%symbol%", this.plugin.getConfig().getString("variable.toGet"))), this.Color(this.plugin.getConfig().getString("variable.subtitleMessage").replaceAll("%symbol%", this.plugin.getConfig().getString("variable.toGet"))));
               }
            }
         }

         this.plugin.taskManager.timeExpiredTask();
      }

   }

   public void startTriviaGame() {
      if (!this.plugin.trivias.isEmpty()) {
         this.plugin.inGame = true;
         this.startTimer();
         Random rand = new Random();
         int getran = rand.nextInt(this.plugin.trivias.size());
         String choice = this.plugin.trivias.get(getran);
         String question = this.plugin.getConfig().getString("trivia.data." + choice + ".question");

         this.plugin.triviaAnswers.clear();
         if (this.plugin.getConfig().isList("trivia.data." + choice + ".answer")) {
            this.plugin.triviaAnswers.addAll(this.plugin.getConfig().getStringList("trivia.data." + choice + ".answer"));
         } else {
            this.plugin.triviaAnswers.add(this.plugin.getConfig().getString("trivia.data." + choice + ".answer"));
         }

         if (!this.plugin.triviaAnswers.isEmpty()) {
            this.plugin.selected = this.plugin.triviaAnswers.get(0);
         } else {
            this.plugin.selected = "";
         }

         this.sendGameStartedMessage("trivia", "%question%", question);
         this.plugin.taskManager.timeExpiredTask();
      }

   }

   public static enum Type {
      APLHA,
      ALPHANUMERIC,
      NUMERIC,
      SYMBOLIC;
   }

   public static enum GameType {
      UNSCRAMBLE,
      MATH,
      TRIVIA,
      REACTION,
      RANDOM,
      UNREVERSE,
      VARIABLE,
      FILLOUT;
   }
}
