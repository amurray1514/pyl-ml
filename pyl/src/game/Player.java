package game;

import java.util.List;

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
	public abstract Player choosePassTarget(Player[] targets);
	
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