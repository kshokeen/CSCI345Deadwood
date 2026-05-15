import controller.GameController;
import java.lang.NumberFormatException;

public class Deadwood {
    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                int nplayers = Integer.parseInt(args[0]);
                GameController controller = new GameController(nplayers);
            } catch (NumberFormatException e) {
                System.out.println("Invalid argument format: please use an integer argument for number of players.");
            }
        } else {
            System.out.println("Wrong number of arguments.");
            System.out.println("Usage: gradle run --args n");
            System.out.println("where n is the number of players (int)");
        }
    }
}
