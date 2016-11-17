import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Observer;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link HangmanGame}. The test names are of the structure:
 * 'methodUnderTest'_should'expectedValue'_when'conditionForTest'. All tests follow the arrange-act-assert
 * pattern, where in the act part the method under test is executed.
 *
 * @author <a href="mailto:konwinkler@gmail.com">Konrad Winkler</a>
 */
@RunWith(JUnitParamsRunner.class)
public class HangmanGameTest
{
  /**
   * Confirm the game is won after the correct guesses are taken.
   */
  @Test
  public void state_shouldBeWin_whenAllLettersOfWordAreGuessedCorrectly()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("world");
    game.addObserver(new ConsoleView());
    game.guess('w');
    game.guess('o');
    game.guess('a');
    game.guess('r');
    game.guess('l');
    game.guess('d');

    //ACT
    GameState actualState = game.getState();

    //ASSERT
    assertEquals("game is in incorrect state after all letters are guessed correctly", GameState.WIN, actualState);
  }

  /**
   * Confirm the game is lost after too many wrong guesses are taken.
   */
  @Test
  public void state_shouldBeLose_whenOneMoreThanAllowedWrongGuesses()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("world");
    for ( int i = 0; i < HangmanGame.ALLOWED_WRONG_GUESSES + 1; i++ )
    {
      game.guess('a');
    }

    //ACT
    GameState actualState = game.getState();

    //ASSERT
    assertEquals(GameState.LOSE, actualState);
  }

  /**
   * Confirm the game is still going before any end game state is reached.
   */
  @Test
  public void state_shouldBeOngoing_whenAllAllowedWrongGuesses()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("world");
    for ( int i = 0; i < HangmanGame.ALLOWED_WRONG_GUESSES; i++ )
    {
      game.guess('a');
    }

    //ACT
    GameState actualState = game.getState();

    //ASSERT
    assertEquals(GameState.ONGOING, actualState);
  }

  /**
   * Confirm the game is also winnable with a two word phrase.
   */
  @Test
  public void state_shouldBeWin_whenAllLettersOfPhraseAreGuessedCorrectly()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("two pies");
    game.addObserver(new ConsoleView());
    game.guess('p');
    game.guess('i');
    game.guess('e');
    game.guess('s');
    game.guess('t');
    game.guess('w');
    game.guess('o');

    //ACT
    GameState actualState = game.getState();

    //ASSERT
    assertEquals(GameState.WIN, actualState);
  }

  /**
   * Confirm game is winnable if the same word appears twice in the phrase.
   */
  @Test
  public void state_shouldBeWin_whenWordTwiceInPhraseAndAllLettersGuessed()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("pie pie");
    game.guess('i');
    game.guess('p');
    game.guess('e');

    //ACT
    GameState actualState = game.getState();

    //ASSERT
    assertEquals(GameState.WIN, actualState);
  }

  /**
   * Avoid regressions of bug, where in a phrase with two words, the wrong guesses counter got increased by 2 for
   * each wrong guess.
   */
  @Test
  public void state_shouldBeOngoing_whenAllAllowedWrongGuessOfPhrase()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("two words");

    for ( int i = 0; i < HangmanGame.ALLOWED_WRONG_GUESSES; i++ )
    {
      game.guess('a');
    }

    //ACT
    GameState actualState = game.getState();

    //ASSERT
    assertEquals(GameState.ONGOING, actualState);
  }

  /**
   * Confirm return value is positive for a correct letter.
   */
  @Test
  public void guess_shouldAcceptLetter_whenLetterInWord()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("included");

    //ACT
    boolean guessResult = game.guess('u');

    //ASSERT
    assertTrue("Game did not accept correct letter.", guessResult);
  }

  /**
   * Confirm return value is negative for a wrong letter.
   */
  @Test
  public void guess_shouldRejectLetter_whenLetterNotInWord()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("not");

    //ACT
    boolean guessResult = game.guess('a');

    //ASSERT
    assertFalse("Game accepted incorrect letter.", guessResult);
  }

  /**
   * Special characters should not be accepted by the guess method.
   *
   * @param specialCharacter the special character to check
   */
  @Test
  @Parameters(method = "specialCharacter")
  public void guess_shouldRejectLetter_whenGuessSpecialCharacter(char specialCharacter)
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("space is big");

    //ACT
    boolean guessResult = game.guess(specialCharacter);

    //ASSERT
    assertFalse("Game accepted special character", guessResult);
  }

  /**
   * Provides the special character to test for method
   * {@link #guess_shouldRejectLetter_whenGuessSpecialCharacter(char)}.  This
   * method allows to have char primitives as parameters.
   *
   * @return each array object is one char primitive which is not a letter or digit
   */
  private Object[] specialCharacter()
  {
    return new Object[] {' ', 'ä', '%', '$'};
  }

  /**
   * For every correct guess, the view should get a positive callback.
   */
  @Test
  public void guess_shouldNotifyObserversWithCorrect_whenCorrectLetterGuessed()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("observer");
    Observer mockView = mock(Observer.class);
    game.addObserver(mockView);

    //ACT
    game.guess('o');

    //ASSERT

    //assert the view has been notified exactly once with this game and the true value
    verify(mockView, times(1)).update(game, true);
  }

  /**
   * For every incorrect guess the view should get a negative callback.
   */
  @Test
  public void guess_shouldNotifyObserversWithIncorrect_whenIncorrectLetterGuessed()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("observer");
    Observer mockView = mock(Observer.class);
    game.addObserver(mockView);

    //ACT
    game.guess('a');

    //ASSERT

    //assert the view has been notified exactly once with this game and the false value
    verify(mockView, times(1)).update(game, false);
  }

  /**
   * Confirm after correctly guessing a letter, it is added to the known state of the phrase.
   */
  @Test
  public void knownLetters_shouldDisplayLetter_whenCorrectLetterGuessed()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("known");
    game.guess('o');

    //ACT
    String knownLetters = game.knownLetters();

    //ASSERT
    assertEquals(1, countMatches(knownLetters, 'o'));
  }

  /**
   * Confirm a letter which appears twice in the phrase is displayed twice after being guessed correctly.
   */
  @Test
  public void knownLetter_shouldDisplayLetterTwice_whenLetterGuessedExistsTwiceInWord()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("Dubble");
    game.guess('b');

    //ACT
    String knownLetters = game.knownLetters();

    //ASSERT
    assertEquals(2, StringUtils.countMatches(knownLetters, 'b'));
  }

  /**
   * Confirm a guessed letter which is not in the phrase is also not shown in the known letters.
   */
  @Test
  public void knownLetters_shouldNotDisplayLetters_whenGuessedLetterIncorrect()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("incorrect");
    game.guess('a');

    //ACT
    String knownLetters = game.knownLetters();

    //ASSERT
    assertFalse(knownLetters.contains("a"));
  }

  /**
   * Confirm start notifies all observers once without any information about a last guess because there has been
   * none yet.
   */
  @Test
  public void start_shouldNotifyObservers_whenCalled()
  {
    //ARRANGE
    HangmanGame game = new HangmanGame("observer");
    Observer mockView = mock(Observer.class);
    game.addObserver(mockView);

    //ACT
    game.start();

    //ASSERT

    //assert the start method notifies the observer exactly once
    //the object should be the same game which got arranged and the second parameter should be null since there
    // have been no guesses yet
    verify(mockView, times(1)).update(game, null);
  }
}
