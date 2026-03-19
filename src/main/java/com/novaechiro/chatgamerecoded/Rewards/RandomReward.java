package com.novaechiro.chatgamerecoded.Rewards;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomReward {
   private final NavigableMap<Double, String> map = new TreeMap();
   private final NavigableMap<Double, String> map1 = new TreeMap();
   private final Random random = new Random();
   private double total = 0.0D;

   public void add(double chance, String value) {
      if (chance > 0.0D) {
         this.total += chance;
         this.map.put(this.total, value);
      }

   }

   public void addReward(double chance, String value) {
      if (chance > 0.0D) {
         this.total += chance;
         this.map1.put(this.total, value);
      }

   }

   public String getRandomValue() {
      if (this.total < 100.0D) {
         this.add(100.0D - this.total, "");
      }

      double value = this.random.nextDouble() * this.total;
      return this.map.higherEntry(value).getValue();
   }

   public String getRandomReward() {
      if (this.total < 100.0D) {
         this.addReward(100.0D - this.total, "");
      }

      double value = this.random.nextDouble() * this.total;
      return this.map1.higherEntry(value).getValue();
   }

   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   public boolean isEmptyReward() {
      return this.map1.isEmpty();
   }

   public void resetValues() {
      this.map.clear();
      this.total = 0.0D;
   }

   public void resetRewards() {
      this.map1.clear();
      this.total = 0.0D;
   }
}
