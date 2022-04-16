package game;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class representing a Press Your Luck player.
 *
 * @author Archer Murray
 */
public abstract class Player implements Comparable<Player>
{
	private int playerNum, earnedSpins, passedSpins, whammies;
	private long score;
	private Game game;
	
	/**
	 * Constructs a new player.
	 */
	protected Player()
	{
		this.playerNum = 1;
		this.score = 0;
		this.earnedSpins = 0;
		this.passedSpins = 0;
		this.whammies = 0;
		this.game = null;
	}
	
	/**
	 * Returns this player's player number.
	 *
	 * @return This player's player number.
	 */
	public int getPlayerNum()
	{
		return playerNum;
	}
	
	/**
	 * Sets this player's player number to the passed-in value.
	 *
	 * @param playerNum This player's new player number.
	 */
	public void setPlayerNum(int playerNum)
	{
		this.playerNum = playerNum;
	}
	
	/**
	 * Returns this player's number of earned spins.
	 *
	 * @return This player's number of earned spins.
	 */
	public int getEarnedSpins()
	{
		return earnedSpins;
	}
	
	/**
	 * Sets this player's number of earned spins to the passed-in value.
	 *
	 * @param earnedSpins This player's new number of earned spins.
	 */
	public void setEarnedSpins(int earnedSpins)
	{
		this.earnedSpins = earnedSpins;
	}
	
	/**
	 * Adds the passed-in number of spins to the player's earned spins.
	 *
	 * @param addedSpins The number of spins to add.
	 */
	public void addEarnedSpins(int addedSpins)
	{
		this.earnedSpins += addedSpins;
	}
	
	/**
	 * Returns this player's number of passed spins.
	 *
	 * @return This player's number of passed spins.
	 */
	public int getPassedSpins()
	{
		return passedSpins;
	}
	
	/**
	 * Sets this player's number of passed spins to the passed-in value.
	 *
	 * @param passedSpins This player's new number of passed spins.
	 */
	public void setPassedSpins(int passedSpins)
	{
		this.passedSpins = passedSpins;
	}
	
	/**
	 * Adds the passed-in number of spins to the player's passed spins.
	 *
	 * @param addedSpins The number of spins to add.
	 */
	public void addPassedSpins(int addedSpins)
	{
		this.passedSpins += addedSpins;
	}
	
	/**
	 * Returns this player's number of whammies.
	 *
	 * @return This player's number of whammies.
	 */
	public int getWhammies()
	{
		return whammies;
	}
	
	/**
	 * Sets this player's number of whammies to the passed-in value.
	 *
	 * @param whammies This player's new number of whammies.
	 */
	public void setWhammies(int whammies)
	{
		this.whammies = whammies;
	}
	
	/**
	 * Returns this player's score.
	 *
	 * @return This player's score.
	 */
	public long getScore()
	{
		return score;
	}
	
	/**
	 * Sets this player's score to the passed-in value.
	 *
	 * @param score This player's new score.
	 */
	public void setScore(long score)
	{
		this.score = score;
	}
	
	/**
	 * Returns the game this player is playing.
	 *
	 * @return The game this player is playing.
	 */
	public Game getGame()
	{
		return game;
	}
	
	/**
	 * Sets this player's game reference to the passed-in game reference.
	 *
	 * @param game The reference to the game this player is now playing.
	 */
	public void setGame(Game game)
	{
		this.game = game;
	}
	
	/**
	 * Compares this player against another, first by score, then by player
	 * number. Based on these comparisons, returns -1 if this player is "less
	 * than" {@code o}, 1 if this player is "greater than" {@code o}, and 0 if
	 * this player is "equal to" {@code o}.
	 *
	 * @param o The player to compare this player against.
	 * @return -1, 0, or 1 as described above.
	 */
	@Override
	public int compareTo(Player o)
	{
		if (this.score == o.score) {
			if (this.playerNum == o.playerNum) {
				return 0;
			}
			return this.playerNum < o.playerNum ? -1 : 1;
		}
		return this.score < o.score ? -1 : 1;
	}
	
	/**
	 * Returns {@code true} if this player is equal to the passed-in object and
	 * {@code false} otherwise.
	 *
	 * @param obj The object to compare against.
	 * @return {@code true} if this player is equal to the passed-in object and
	 * {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Player && this.score == ((Player)obj).score &&
				this.playerNum == ((Player)obj).playerNum;
	}
	
	/**
	 * Returns this player's name, which is "Player <{@code playerNum}>".
	 *
	 * @return This player's name.
	 */
	public String getName()
	{
		return "Player " + this.playerNum;
	}
	
	/**
	 * Returns {@code true} if this player has spins left and {@code false}
	 * otherwise.
	 *
	 * @return {@code true} if this player has spins left and {@code false}
	 * otherwise.
	 */
	public boolean hasSpins()
	{
		return this.earnedSpins > 0 || this.passedSpins > 0;
	}
	
	/**
	 * Plays a spin on the passed-in board.
	 *
	 * @param board The board to use the spin on.
	 * @param printSpin Whether to print the result of the spin.
	 */
	public void playSpin(Board board, boolean printSpin)
	{
		// Deduct a spin from the player
		if (this.passedSpins > 0) {
			this.passedSpins--;
		} else {
			this.earnedSpins--;
		}
		// Stop the board
		if (printSpin) {
			InputUtil.getLine("Press Enter to stop the board...");
		}
		Space space = board.stopBoard();
		if (printSpin) {
			System.out.println(board);
			System.out.println(this.getName() + " stopped on " + space + '.');
		}
		// Move the light if necessary
		List<Space> moveTargets = board.getMoveTargets();
		if (moveTargets.size() == 1) {
			space = moveTargets.get(0);
			if (printSpin) {
				System.out.println("The light moves to " + space + '.');
			}
		} else if (moveTargets.size() > 1) {
			if (printSpin) {
				System.out.println(
						"The light can move to one of the following spaces: " +
								moveTargets.stream().map(Space::toString)
										.collect(Collectors.joining(", "))
				+ '.');
			}
			space = this.chooseMoveTarget(moveTargets);
			if (printSpin) {
				System.out.println(this.getName() + " chooses " + space + '.');
			}
		}
		// Perform the effect of the space
		String value = space.getValue();
		char firstChar = value.charAt(0);
		StringBuilder printStr = new StringBuilder();
		if (firstChar == 'W') {
			// Whammy
			this.score = 0;
			this.whammies += 1;
			if (printSpin) {
				printStr.append("A Whammy reduces ");
				printStr.append(this.getName());
				printStr.append("'s score to $0! ");
				printStr.append(this.getName());
				printStr.append(" now has ");
				printStr.append(this.whammies);
				printStr.append(this.whammies == 1 ? " Whammy." : " Whammies.");
			}
			// If 4 whammies, also remove all spins
			if (this.whammies == 4) {
				this.earnedSpins = 0;
				this.passedSpins = 0;
				if (printSpin) {
					printStr.append("\nWith 4 Whammies, ");
					printStr.append(this.getName());
					printStr.append(" is out of the game!");
				}
			}
			// If any passed spins, move to "earned" column
			if (this.passedSpins > 0) {
				this.earnedSpins += this.passedSpins;
				this.passedSpins = 0;
				if (printSpin) {
					printStr.append("\nOn the bright side, all of ");
					printStr.append(this.getName());
					printStr.append("'s passed spins have been moved ");
					printStr.append("to the \"earned\" column.");
				}
			}
		} else if (firstChar == 'P') {
			// Prize
			int prizeValue = board.getPrizeValue();
			this.score += prizeValue;
			if (printSpin) {
				printStr.append("The prize is worth ");
				printStr.append(String.format("$%1$,d!", prizeValue));
				printStr.append(" Let's add that to ");
				printStr.append(this.getName());
				printStr.append("'s score.");
			}
		} else if (firstChar == 'D') {
			// Double Your $$ + One Spin
			this.score *= 2;
			this.earnedSpins++;
			board.removeDoubleFromPlay();
		} else if (firstChar == 'A') {
			// Add-a-One
			this.score += IntStream.range(0, Long.toString(this.score).length())
					.mapToLong(i -> 10).reduce(1, (a, b) -> a * b);
			if (printSpin) {
				printStr.append("The digit 1 is put in front of ");
				printStr.append(this.getName());
				printStr.append("'s score!");
			}
		} else {
			// Cash space
			char lastChar = value.charAt(value.length() - 1);
			int cashAmt = space.getCashAmount();
			if (lastChar == 'S') {
				// Cash + One Spin
				this.score += cashAmt;
				this.earnedSpins++;
			} else if (lastChar == 'L') {
				// Cash or Lose-1-Whammy
				if (this.whammies == 0) {
					this.score += cashAmt;
				} else {
					if (this.moneyOrLoseWhammy(cashAmt)) {
						this.score += cashAmt;
						if (printSpin) {
							printStr.append(this.getName());
							printStr.append(" chooses to take ");
							printStr.append(String.format("$%1$,d.", cashAmt));
						}
					} else {
						this.whammies--;
						if (printSpin) {
							printStr.append(this.getName());
							printStr.append(" chooses to lose one Whammy.");
						}
					}
				}
			} else {
				// Plain cash
				this.score += cashAmt;
			}
		}
		if (printStr.length() > 0) {
			System.out.println(printStr);
		}
	}
	
	/**
	 * Returns {@code true} if this player presses their luck and {@code false}
	 * if this player passes their spins.
	 *
	 * @return {@code true} if this player presses their luck and {@code false}
	 * if this player passes their spins.
	 */
	public abstract boolean pressOrPass();
	
	/**
	 * Returns the space this player chooses to move to, when there are multiple
	 * options available.
	 *
	 * @param moveTargets The list of spaces this player may move to.
	 * @return The space this player chooses to move to.
	 */
	public abstract Space chooseMoveTarget(List<Space> moveTargets);
	
	/**
	 * When this player lands on "Money or Lose-1-Whammy", returns {@code true}
	 * if this player chooses the money and {@code false} if this player chooses
	 * the loss of one whammy.
	 *
	 * @param amount The amount of money being offered.
	 * @return {@code true} if this player chooses the money and {@code false}
	 * if this player chooses the loss of one whammy.
	 */
	public abstract boolean moneyOrLoseWhammy(int amount);
	
	/**
	 * When there are multiple players this player can pass spins to, returns
	 * the player this player chooses to pass their spins to.
	 *
	 * @param targets The list of possible pass targets.
	 * @return The player this player chooses to pass their spins to.
	 */
	public abstract Player choosePassTarget(List<Player> targets);
	
	/**
	 * Adjusts this player's strategy based on the change in game state,
	 * according to its own evaluation of the previous and current game state.
	 */
	public void learn()
	{
		// do nothing
	}
	
	/**
	 * Adjusts this player's strategy based on the change in game state,
	 * according to its own evaluation of the previous game state and the
	 * passed-in evaluation of the current game state.
	 *
	 * @param newEval The evaluation of the new game state.
	 */
	public void learn(double newEval)
	{
		// do nothing
	}
	
	/**
	 * Returns a {@code String} representation of this player, which is
	 * currently the player's name.
	 *
	 * @return a {@code String} representation of this player
	 */
	public String toString()
	{
		return this.getName();
	}
}