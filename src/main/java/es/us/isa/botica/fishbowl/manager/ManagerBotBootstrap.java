package es.us.isa.botica.fishbowl.manager;

import es.us.isa.botica.bot.BotLauncher;

public class ManagerBotBootstrap {
  public static void main(String[] args) {
    BotLauncher.run(new ManagerBot(), args);
  }
}
