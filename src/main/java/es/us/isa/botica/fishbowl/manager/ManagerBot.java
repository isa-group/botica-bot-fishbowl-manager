package es.us.isa.botica.fishbowl.manager;

import es.us.isa.botica.bot.BaseBot;
import es.us.isa.botica.bot.DefaultOrderHandler;
import es.us.isa.botica.bot.shutdown.ShutdownRequestHandler;
import es.us.isa.botica.bot.shutdown.ShutdownResponse;
import es.us.isa.botica.fishbowl.Fishbowl;
import es.us.isa.botica.fishbowl.Position;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerBot extends BaseBot {
  private static final Logger log = LoggerFactory.getLogger(ManagerBot.class);
  private static final Path DIRECTORY_PATH = Path.of("/app/fishbowl");
  private static final int FISHBOWL_SIZE = 9;

  private final Fishbowl fishbowl = new Fishbowl(FISHBOWL_SIZE);
  private ScheduledExecutorService executor;
  private int fileVersion = 1;

  @Override
  public void configure() {
    int delay = Integer.parseInt(System.getenv("FISHBOWL_FILE_UPDATE_SECONDS"));

    this.executor = Executors.newSingleThreadScheduledExecutor();
    this.executor.scheduleWithFixedDelay(this::saveFile, delay, delay, TimeUnit.SECONDS);
  }

  @DefaultOrderHandler
  public void onOrderReceived(JSONObject message) {
    String fish = message.getString("silhouette");
    Position lastPosition = this.fishbowl.getPosition(fish);
    Position newPosition = new Position(message.getInt("x"), message.getInt("y"));

    log.info("Fish {} moved! {} -> {}", fish, lastPosition, newPosition);
    this.fishbowl.setPosition(fish, newPosition);
    log.info("\n{}", this.fishbowl.render());
  }

  @ShutdownRequestHandler
  public ShutdownResponse onShutdownRequest() throws InterruptedException {
    this.executor.awaitTermination(2, TimeUnit.SECONDS);
    return ShutdownResponse.ready();
  }

  private void saveFile() {
    Path filePath = DIRECTORY_PATH.resolve("v" + this.fileVersion++ + ".txt");
    try {
      Files.writeString(filePath, this.fishbowl.render());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
