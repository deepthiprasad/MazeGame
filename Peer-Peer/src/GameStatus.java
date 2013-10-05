
/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/24/13
 * Time: 1:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class GameStatus {

    volatile StatusEnum status;

    public GameStatus(StatusEnum status) {
        this.status = status;
    }
}
