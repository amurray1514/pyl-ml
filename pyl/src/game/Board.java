package game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class representing the Press Your Luck board.
 *
 * @author Archer Murray
 */
public class Board
{
	private final Random rng;
	private final List<Space> spaces;
	private int doublesInPlay;
	private int prizeMin, prizeMax;
	private int lightPos;
	
	// Values used to calculate expected value of a spin
	private int numSpaces;
	private double expCash, expSpins, expWhammies;
	
	/**
	 * Player class used to make EV calculation easier.
	 */
	private class EVPlayer extends Player
	{
		@Override
		public boolean pressOrPass()
		{
			return true;
		}
		
		@Override
		public Space chooseMoveTarget(List<Space> moveTargets)
		{
			int maxValue = -1;
			Space maxSpace = null;
			for (Space s: moveTargets) {
				int amt = -1;
				if (s.getCashAmount() > 0) {
					amt = s.getCashAmount();
				} else if (s.getValue().equals("P")) {
					amt = (prizeMax + prizeMin) / 2;
				}
				if (amt > maxValue) {
					maxValue = amt;
					maxSpace = s;
				}
			}
			return maxSpace;
		}
		
		@Override
		public boolean moneyOrLoseWhammy(int amount)
		{
			return true;
		}
		
		@Override
		public Player choosePassTarget(List<Player> targets)
		{
			return targets.get(0);
		}
	}
	
	/**
	 * The probability that "Double Your $$ + One Spin" will be available, if
	 * the board contains that space.
	 */
	private static final double DOUBLE_IN_PLAY_CHANCE = 0.16666666666666666;
	/**
	 * The indices of the corner spaces.
	 */
	private static final int[] CORNER_SPACES = {0, 5, 9, 14};
	
	/**
	 * Creates a new board with spaces read from the specified text file.
	 * <p>
	 * This text file should contain one space per line, where each space is
	 * represented as a series of comma-separated values in space notation.
	 * <p>
	 * The exception is the first line, which should contain the minimum and
	 * maximum prize values, separated by a comma. The file should not end with
	 * a blank line.
	 *
	 * @param fn The filename to read space data from.
	 */
	public Board(String fn)
	{
		this.rng = new Random();
		this.spaces = new ArrayList<>();
		this.doublesInPlay = 0;
		boolean useDouble = this.rng.nextDouble() < DOUBLE_IN_PLAY_CHANCE;
		this.lightPos = 0;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fn));
			// Read first line (prize min/max)
			String line = in.readLine();
			String[] tokens = line.split(",");
			this.prizeMin = Integer.parseInt(tokens[0]);
			this.prizeMax = Integer.parseInt(tokens[1]);
			// Read remaining lines (spaces)
			while ((line = in.readLine()) != null) {
				if (line.contains("D")) {
					// Space may have "Double Your $$ + One Spin"
					if (useDouble) {
						this.doublesInPlay++;
					} else {
						line = line.replace('D', 'P');
					}
				}
				this.spaces.add(new Space(line.split(",")));
			}
		} catch (Exception e) {
			System.out.println("Error reading file " + fn +
					". File is missing or malformed.");
			System.exit(1);
		}
		// Calculate board statistics
		this.expCash = 0;
		this.expSpins = 0;
		this.expWhammies = 0;
		this.numSpaces = 0;
		// Store previous prize min and prize max (to make prizes consistent)
		int prevPrizeMin = this.prizeMin;
		int prevPrizeMax = this.prizeMax;
		this.prizeMin = (prevPrizeMax + prevPrizeMin) / 2;
		this.prizeMax = (prevPrizeMax + prevPrizeMin) / 2;
		// Loop through spaces and update values
		EVPlayer player = new EVPlayer();
		for (int i = 0; i < this.spaces.size(); i++) {
			Space s = this.spaces.get(i);
			for (int j = 0; j < s.getNumValues(); j++) {
				this.numSpaces++;
				// Have the player play 30 spins and average the results
				for (int n = 0; n < 30; n++) {
					player.setScore(0);
					player.setEarnedSpins(1);
					player.setWhammies(0);
					this.stopBoard();
					this.lightPos = i;
					s.setPos(j);
					player.playSpin(this, false, false);
					this.expCash += player.getScore();
					this.expSpins += player.getEarnedSpins();
					this.expWhammies += player.getWhammies();
				}
			}
		}
		this.expCash /= 30 * this.numSpaces - this.expWhammies;
		this.expSpins /= 30 * this.numSpaces - this.expWhammies;
		this.expWhammies /= 30 * this.numSpaces;
		// Restore prior values
		this.lightPos = 0;
		this.prizeMin = prevPrizeMin;
		this.prizeMax = prevPrizeMax;
	}
	
	/**
	 * Returns {@code true} if "Double Your $$ + One Spin" is in play with this
	 * board and {@code false} otherwise.
	 *
	 * @return {@code true} if "Double Your $$ + One Spin" is in play with this
	 * board and {@code false} otherwise.
	 */
	public boolean isDoubleInPlay()
	{
		return this.doublesInPlay > 0;
	}
	
	/**
	 * Removes "Double Your $$ + One Spin" from play by replacing it with a
	 * prize space.
	 * <p>
	 * This method only works when the light is currently on "Double Your $$ +
	 * One Spin".
	 */
	public void removeDoubleFromPlay()
	{
		this.doublesInPlay--;
		this.spaces.get(this.lightPos).setCurrentValue("P");
	}
	
	/**
	 * Returns a random prize value for this board.
	 *
	 * @return A random prize value for this board.
	 */
	public int getPrizeValue()
	{
		return this.prizeMin +
				rng.nextInt(this.prizeMax - this.prizeMin + 1);
	}
	
	/**
	 * Returns the average prize value for this board.
	 *
	 * @return The average prize value for this board.
	 */
	public int getAveragePrizeValue()
	{
		return (this.prizeMax - this.prizeMin) / 2;
	}
	
	/**
	 * Returns the maximum prize value for this board.
	 *
	 * @return The maximum prize value for this board.
	 */
	public int getMaxPrizeValue()
	{
		return this.prizeMax;
	}
	
	/**
	 * Returns the minimum prize value for this board.
	 *
	 * @return The minimum prize value for this board.
	 */
	public int getMinPrizeValue()
	{
		return this.prizeMin;
	}
	
	/**
	 * Stops the board, randomizing the value of every space and the position of
	 * the light.
	 */
	public void stopBoard()
	{
		for (Space s: this.spaces) {
			s.randomizeValue();
		}
		this.lightPos = this.rng.nextInt(this.spaces.size());
	}
	
	/**
	 * Returns the currently lit space.
	 *
	 * @return The currently lit space.
	 */
	public Space getLitSpace()
	{
		return this.spaces.get(this.lightPos);
	}
	
	/**
	 * Returns a list of all spaces that can be moved to from the currently lit
	 * space. If the currently lit space is not a movement space, returns an
	 * empty list.
	 *
	 * @return A list of movement targets, as described above.
	 */
	public List<Space> getMoveTargets()
	{
		List<Space> ret = new ArrayList<>();
		String value = this.getLitSpace().getValue();
		char firstChar = value.charAt(0);
		if (firstChar == 'M' || firstChar == '<' || firstChar == '>') {
			// Variable movement space
			int moveAmt = Integer.parseInt(value.substring(1));
			if (firstChar == 'M' || firstChar == '<') {
				ret.add(this.spaces.get((this.spaces.size() + this.lightPos -
						moveAmt) % this.spaces.size()));
			}
			if (firstChar == 'M' || firstChar == '>') {
				ret.add(this.spaces.get((this.lightPos + moveAmt) %
						this.spaces.size()));
			}
		}
		if (firstChar == 'C') {
			// Pick-a-Corner - assume board has 18 spaces for simplicity
			for (int i: CORNER_SPACES) {
				if (i != this.lightPos) {
					ret.add(this.spaces.get(i));
				}
			}
		}
		if (firstChar == 'B') {
			// Big Bucks - move to max cash amount
			int maxCash = this.spaces.get(0).getCashAmount();
			int maxCashIdx = 0;
			for (int i = 1; i < this.spaces.size(); i++) {
				int cashAmt = this.spaces.get(i).getCashAmount();
				if (cashAmt > maxCash) {
					maxCash = cashAmt;
					maxCashIdx = i;
				}
			}
			ret.add(this.spaces.get(maxCashIdx));
		}
		return ret;
	}
	
	/**
	 * Returns the average amount of cash earned when landing on a non-Whammy
	 * space.
	 *
	 * @param score The player's current score.
	 * @return The average amount of cash earned when landing on a non-Whammy
	 * space.
	 */
	public double getExpCash(long score)
	{
		return this.expCash + (double)(score * this.doublesInPlay) /
				this.numSpaces;
	}
	
	/**
	 * Returns the probability of earning an extra spin on any given spin that
	 * does not hit a Whammy.
	 *
	 * @return The probability of earning an extra spin on any given spin that
	 * does not hit a Whammy.
	 */
	public double getExpSpins()
	{
		return this.expSpins;
	}
	
	/**
	 * Returns the probability of landing on a Whammy on any given spin.
	 *
	 * @return The probability of landing on a Whammy on any given spin.
	 */
	public double getExpWhammies()
	{
		return this.expWhammies;
	}
	
	/**
	 * Returns a {@code String} representation of this board.
	 * <p>
	 * Currently, this is only supported for 18-space boards.
	 *
	 * @return A {@code String} representation of this board.
	 */
	public String toString()
	{
		if (this.spaces.size() != 18) {
			return "Unsupported board size: " + this.spaces.size() + '.';
		}
		// Top row
		StringBuilder ret = new StringBuilder();
		ret.append("+-----+-----+-----+-----+-----+-----+\n");
		for (int i = 0; i < 6; i++) {
			ret.append(String.format("|%1$5s", this.spaces.get(i).getValue()));
		}
		ret.append("|\n+-----+-----+-----+-----+-----+-----+\n");
		// Middle rows
		ret.append(String.format("|%1$5s|", this.spaces.get(17).getValue()));
		ret.append("                       ");
		ret.append(String.format("|%1$5s|\n", this.spaces.get(6).getValue()));
		ret.append("+-----+                       +-----+\n");
		ret.append(String.format("|%1$5s", this.spaces.get(16).getValue()));
		ret.append(String.format("|%1$-23s", this.getLitSpace().getName()));
		ret.append(String.format("|%1$5s|\n", this.spaces.get(7).getValue()));
		ret.append("+-----+                       +-----+\n");
		ret.append(String.format("|%1$5s|", this.spaces.get(15).getValue()));
		ret.append("                       ");
		ret.append(String.format("|%1$5s|\n", this.spaces.get(8).getValue()));
		// Bottom row
		ret.append("+-----+-----+-----+-----+-----+-----+\n");
		for (int i = 14; i > 8; i--) {
			ret.append(String.format("|%1$5s", this.spaces.get(i).getValue()));
		}
		ret.append("|\n+-----+-----+-----+-----+-----+-----+");
		return ret.toString();
	}
}