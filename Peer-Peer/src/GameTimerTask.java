
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/24/13
 * Time: 12:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class GameTimerTask extends TimerTask {

    private GameStatus gameStarted;

    private Game game;

    private CountDownLatch startNotification;

    public GameTimerTask(GameStatus gameStarted, Game game, CountDownLatch startNotification) {
        this.gameStarted = gameStarted;
        this.game = game;
        this.startNotification = startNotification;
    }

    @Override
    public void run() {
        gameStarted.status = StatusEnum.GAME_STARTED;
        System.out.println("Starting the game now...");
        startNotification.countDown();
    }
}
