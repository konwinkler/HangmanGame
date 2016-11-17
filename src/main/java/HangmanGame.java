import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Implementation of game <a href=https://en.wikipedia.org/wiki/Hangman_%28game%29>hangman</a>.
 * <p>
 * <p>Rules</p>
 * <ul><li>setup a game with a word or phrase:<ul><li>only letters a to z allowed in the words</li>
 * <li>words are separated by spaces</li></ul></li>
 * <li>guess one letter at a time</li>
 * <li>if there are more than 8 wrong guesses the player loses the game</li>
 * <li>if all letters of the word or phrase are guessed correctly the game is won</li>
 * </ul>
 * <p>Use of class</p>
 * <ul>
 * <li>setup game with word or phrase in constructor {@link #HangmanGame(String)}</li>
 * <li>guess letter with {@link #guess(Character)}</li>
 * <li>get getState win or lose from {@link #getState()}</li>
 * <li>get phrase with filled in known letters from {@link #knownLetters()}</li>
 * <li>hook in view (to display game) at {@link #addObserver(Observer)}</li>
 * </ul>
 * The game is not able to be reset at any point.
 *
 * @author <a href="mailto:konwinkler@gmail.com">Konrad Winkler</a>
 */
public class HangmanGame extends Observable
{
  /**
   * The amount {@link #guess(Character)} can be called with a letter not included in the secret phrase before
   * losing the game.
   */
  public static final    int    ALLOWED_WRONG_GUESSES      = 8;
  protected static final String UNKNOWN_LETTER_REPLACEMENT = "_";
  protected static final String WORD_SEPARATOR             = " ";
  private static final   Logger logger                     = LogManager.getLogger();
  //array to be able to have a game with a single word or a phrase
  private final String[]            secretWords;
  private       Map<String, BitSet> foundLetters;
  private int wrongGuesses = 0;
  private GameState currentState;

  /**
   * Setup of the game, for rules see {@link HangmanGame}. The game is immediately ready to be played by calling
   * {@link #guess(Character)} and checking for the current condition with {@link #getState()} and
   * {@link #knownLetters()}. If the secret phrase does not match the requirements an unchecked exception is thrown.
   *
   * @param secretPhrase to be guessed, allowed letter between a to z, words must be separated by space
   * @throws IllegalArgumentException if the secret phrase does not match the requirements
   */
  public HangmanGame(String secretPhrase) throws IllegalArgumentException
  {
    if ( secretPhrase == null || secretPhrase.isEmpty() )
    {
      throw new IllegalArgumentException("The target word should not be null or empty.");
    }

    secretWords = secretPhrase.split(WORD_SEPARATOR);

    //validate input
    for ( String word : secretWords )
    {
      //use the validate method to check each letter of each word
      for ( int i = 0; i < word.length(); i++ )
      {
        char letter = word.charAt(i);
        if ( !validateInput(letter) )
        {
          throw new IllegalArgumentException("Letter "
            + letter
            + " of word "
            + word
            + " is not allowed for the game.");
        }
      }
    }

    logger.info("Setup a new game of hangman with the secret phrase \"{}\"", secretPhrase);

    foundLetters = new HashMap<>();
    for ( String word : secretWords )
    {
      foundLetters.put(word, new BitSet(word.length()));//all bits are initially false
    }

    updateCurrentState();
  }

  /**
   * Method to play one turn of the game. In a turn a letter is guessed (char parameter) and if the letter
   * appears in the secret phrase this letter is added to the known letters of the phrase, therefore getting one
   * step closer to solving the game. If the letter is not contained in the phrase the counter of wrong guesses
   * is increased by one.
   *
   * @param guessLetter the letter which is guessed, only characters between a to z (upper or lower case) are
   *                    allowed
   * @return true if letter is contained in phrase, false if letter is not contained or letter is not allowed or
   * the game is over
   */
  public boolean guess(Character guessLetter) //object is used instead of primitive so toString can be called
  {
    //if the game is over exit method without action
    if ( currentState != GameState.ONGOING )
    {
      logger.warn("Game is already over (current getState {})", currentState);
      return false;
    }

    //if the input is not valid exit the method without action
    if ( !validateInput(guessLetter) )
    {
      logger.warn("Input character ({}) is not a valid letter.", guessLetter);
      return false;
    }

    logger.debug("Guessing letter \'{}\'", guessLetter);
    boolean correctGuess = false;

    //check every word in the phrase if the letter appears in the word
    for ( String word : secretWords )
    {
      if ( word.toLowerCase().contains(guessLetter.toString().toLowerCase()) )
      {
        logger.debug("Letter is contained in word \"{}\"", word);

        //the letter appears in a word so the known letters get updated
        correctGuess = true;
        for ( int i = 0; i < word.length(); i++ )
        {
          if ( Character.toLowerCase(word.charAt(i)) == Character.toLowerCase(guessLetter) )
          {
            foundLetters.get(word).set(i);
          }
        }
      }
      else
      {
        logger.debug("Letter is not contained in word \"{}\"", word);
      }
    }

    //if the letter was not in the phrase increase the counter for wrong guesses
    if ( !correctGuess )
    {
      wrongGuesses++;
    }

    //evaluate if the game is won or lost
    updateCurrentState();

    //let view know about changed model
    setChanged();
    notifyObservers(correctGuess);
    return correctGuess;
  }

  /**
   * Defines the range of allowed letters for the secret phrase and the method {@link #guess(Character)}.
   *
   * @param letter
   * @return
   */
  private boolean validateInput(Character letter)
  {
    return letter.toString().matches("[a-zA-Z]");
  }

  /**
   * Used to retrieve the current known getState of the secret phrase. The phrase is returned with all unknown
   * letters replaced by the underscore character. For example: "_ello world" where only 'h' is not guessed yet.
   *
   * @return the secret phrase with all unknown letters replaced by '_'
   */
  public String knownLetters()
  {
    StringBuilder stringBuilder = new StringBuilder();

    //based on the bit set for each letter it is either displaced if the bit set value is true or replaced by '_'
    // otherwise
    for ( String word : secretWords )
    {
      for ( int i = 0; i < word.length(); i++ )
      {
        if ( foundLetters.get(word).get(i) )
        {
          //display letter if bit set value is true
          stringBuilder.append(word.charAt(i));
        }
        else
        {
          //otherwise display replacement
          stringBuilder.append(UNKNOWN_LETTER_REPLACEMENT);
        }
      }
      //adds a trailing space after every word to separate them, note: this includes the last word
      stringBuilder.append(WORD_SEPARATOR);
    }

    //trim to remove the space after the last word
    return stringBuilder.toString().trim();
  }

  /**
   * Evaluate the current known letters and wrong guesses to see if the game is over and update
   * {@link #currentState} accordingly.
   */
  private void updateCurrentState()
  {
    //check for win
    boolean allWordsKnown = true;
    for ( String word : secretWords )
    {
      //if all bits are true, then all letters of this word are known
      boolean wordIsKnown = foundLetters.get(word).cardinality() == word.length();

      //if one of the words is not known, then allWordsKnown is set to false
      allWordsKnown = allWordsKnown && wordIsKnown;
    }

    //all words known means the game is won
    if ( allWordsKnown )
    {
      currentState = GameState.WIN;
    }
    //if too many wrong guesses, then the game is lost
    else if ( wrongGuesses > ALLOWED_WRONG_GUESSES )
    {
      currentState = GameState.LOSE;
    }
    else
    {
      //neither win nor lose means it is still going
      currentState = GameState.ONGOING;
    }
    logger.debug("Current game getState is {}", getState());
  }

  /**
   * Used to signal if the game is over and what the end result of the game is.
   *
   * @return current getState of the game
   */
  public GameState getState()
  {
    return currentState;
  }

  /**
   * Used to know how close the count of wrong guesses is to {@link #ALLOWED_WRONG_GUESSES} and therefore how
   * close the game is to a loss.
   *
   * @return times a valid letter was guessed previously which did not appear in the secret phrase
   */
  public int getWrongGuesses()
  {
    return wrongGuesses;
  }

  /**
   * This method allows the observers to get a first message without any guesses. This means calling this method
   * has no influence on the state of the game and is therefore optional.
   */
  public void start()
  {
    setChanged();
    notifyObservers();
  }

}
