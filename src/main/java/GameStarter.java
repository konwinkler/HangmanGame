/**
 * Main class to be able to play the game in the console and provide an executable jar.
 *
 * @author <a href="mailto:konwinkler@gmail.com">Konrad Winkler</a>
 */
public class GameStarter
{
  private static final String EXAMPLE_SECRET_PHRASE = "two pies";

  /**
   * Builds the game with an example secret phrase and starts it with the console view so it is ready to be
   * played in the console.
   *
   * @param args any arguments are ignored
   */
  public static void main(String[] args)
  {
    //build the example game by connecting model, view and controller
    HangmanGame game = new HangmanGame(EXAMPLE_SECRET_PHRASE);
    ConsoleView view = new ConsoleView();
    GameController controller = new GameController(game, view);
    game.addObserver(view);

    //start the game
    game.start();
    controller.run();
  }
}
