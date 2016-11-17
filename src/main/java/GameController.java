/**
 * Controller to connect {@link HangmanGame} and {@link ConsoleView} (MVC pattern). This controller runs a game
 * loop. In the loop the input is read from the view and the game is updated with the input. If the game is in a
 * finished state the loop ends. Use {@link #run()} to start the game loop.
 *
 * @author <a href="mailto:konwinkler@gmail.com">Konrad Winkler</a>
 */
public class GameController
{
  private final ConsoleView view;
  private final HangmanGame game;

  /**
   * Sets up the connection between the model and the view.
   *
   * @param game model which is updated by the controller
   * @param view to read the input from
   */
  public GameController(HangmanGame game, ConsoleView view)
  {
    this.game = game;
    this.view = view;
  }

  /**
   * Starts a game loop until the game is in an end state. The basic content of the loop is:
   * <ul>
   *   <li>read input from view</li>
   *   <li>update model with input</li>
   *   <li>exit loop if necessary</li>
   * </ul>
   */
  public void run()
  {
    char input;
    boolean running; //if false exit the game loop

    //game loop
    do
    {
      input = view.readInput();
      updateGame(input);
      running = evaluateGameState();
    }
    while (running);
  }

  private boolean evaluateGameState()
  {
    GameState state = game.getState();
    //ongoing is currently the only state for a running game
    return (state == GameState.ONGOING);
  }

  private void updateGame(char input)
  {
    game.guess(input);
  }
}
