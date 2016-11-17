import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Observer;

/**
 * Optional view for the class {@link HangmanGame}. Responsible for displaying information in the console and
 * reading input from the console. To use add an instance of this class to
 * {@link HangmanGame#addObserver(Observer)}.
 *
 * @author <a href="mailto:konwinkler@gmail.com">Konrad Winkler</a>
 */
public class ConsoleView implements Observer
{
  private static final Logger logger         = LogManager.getLogger();
  private final        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

  /**
   * Display the hangman game rules at instantiation of class.
   */
  public ConsoleView()
  {
    System.out.println("Welcome to the game hangman.");
    System.out.println("Try to guess the secret phrase.");
    System.out.println("Each turn you can guess one letter (confirm with ENTER).\n");
  }

  /**
   * @param game      the observable object
   * @param lastGuess argument passed to the <code>notifyObservers</code> method: result of last <code>guess</code>
   * @see Observer#update(Observable, Object)
   */
  @Override
  public void update(Observable game, Object lastGuess)
  {
    //could avoid casting by defining custom observer
    HangmanGame hangmanGame = (HangmanGame) game;

    //display information about the last guess
    if ( lastGuess != null )
    {
      boolean guessResult = (boolean) lastGuess;
      if ( guessResult )
      {
        System.out.println("Correct Guess!\n");
      }
      else if ( !guessResult )
      {
        System.out.println("incorrect Guess!\n");
      }
    }

    //display information for last turn
    System.out.format("Phrase: %s\n", hangmanGame.knownLetters());
    System.out.format("Misses: %s out of allowed %s\n\n",
      hangmanGame.getWrongGuesses(),
      HangmanGame.ALLOWED_WRONG_GUESSES);

    //if the game is over display yhe result
    GameState state = hangmanGame.getState();
    if ( state == GameState.WIN )
    {
      System.out.println("You win!");
    }
    else if ( state == GameState.LOSE )
    {
      System.out.println("You lose. :(");
    }
  }

  /**
   * Read text written into console. Only the first character is considered.
   *
   * @return first character of console input
   */
  public char readInput()
  {
    try
    {
      String input = reader.readLine();
      logger.trace("Input {}", input);
      return input.charAt(0);
    }
    catch (IOException e)
    {
      logger.error(e);
      return ' ';
    }
  }
}
