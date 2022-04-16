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
	private boolean doubleInPlay;
	private int prizeMin, prizeMax;
	private int lightPos;
	
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
		this.doubleInPlay = this.rng.nextDouble() < DOUBLE_IN_PLAY_CHANCE;
		this.lightPos = 0;
		boolean doublePresent = false;
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
					if (this.doubleInPlay) {
						doublePresent = true;
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
		this.doubleInPlay = doublePresent;
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
		return this.doubleInPlay;
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
		this.doubleInPlay = false;
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
	 * Stops the board, randomizing the value of every space and the position
	 * of the light, and returns the lit space.
	 *
	 * @return The lit space after stopping the board.
	 */
	public Space stopBoard()
	{
		for (Space s: this.spaces) {
			s.randomizeValue();
		}
		this.lightPos = this.rng.nextInt(this.spaces.size());
		return this.spaces.get(this.lightPos);
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
				ret.add(this.spaces.get((this.lightPos - moveAmt) %
						this.spaces.size()));
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