package game;

import java.util.Random;

/**
 * Class representing a space on the board in Press Your Luck.
 * <p>
 * Spaces are represented by lists of strings representing their values, where
 * the strings are in "space notation":
 * <p>
 * A bare number indicates a cash amount.
 * <p>
 * A number followed by an S indicates the amount plus one spin.
 * <p>
 * A number followed by an L indicates the amount or lose one Whammy.
 * <p>
 * W indicates a Whammy.
 * <p>
 * P indicates a prize.
 * <p>
 * D indicates Double Your Money + One Spin.
 * <p>
 * A indicates Add-a-One.
 * <p>
 * B indicates Big Bucks.
 * <p>
 * M followed by a number N indicates Move N Space(s).
 * <p>
 * < followed by a number N indicates Go Back N Space(s).
 * <p>
 * > followed by a number N indicates Advance N Space(s).
 * <p>
 * C indicates Pick-a-Corner.
 *
 * @author Archer Murray
 */
public class Space
{
	private final Random rng;
	private final String[] values;
	private int pos;
	
	/**
	 * Constructs a new space with the given values.
	 *
	 * @param values The list of values, in "space notation".
	 */
	public Space(String[] values)
	{
		this.rng = new Random();
		this.values = values;
		this.pos = 0;
	}
	
	/**
	 * Returns this space's current value.
	 *
	 * @return This space's current value.
	 */
	public String getValue()
	{
		return this.values[this.pos];
	}
	
	/**
	 * Sets this space's current value to the passed-in new value.
	 *
	 * @param newValue The value to change this space's current value to.
	 */
	public void setCurrentValue(String newValue)
	{
		this.values[this.pos] = newValue;
	}
	
	/**
	 * Sets this space's current value to a random value in its list of possible
	 * values.
	 */
	public void randomizeValue()
	{
		this.pos = this.rng.nextInt(this.values.length);
	}
	
	/**
	 * Sets this space's current position to the passed-in index.
	 *
	 * @param pos The position index.
	 */
	public void setPos(int pos)
	{
		this.pos = pos;
	}
	
	/**
	 * Returns the number of possible values this space can take on.
	 *
	 * @return The number of possible values this space can take on.
	 */
	public int getNumValues()
	{
		return this.values.length;
	}
	
	/**
	 * Returns the full name of this space's current value.
	 *
	 * @return The full name of this space's current value.
	 */
	public String getName()
	{
		String currValue = this.getValue();
		char firstChar = currValue.charAt(0);
		// Non-cash and non-variable movement spaces
		if (firstChar == 'W') {
			return "Whammy";
		}
		if (firstChar == 'P') {
			return "Prize";
		}
		if (firstChar == 'D') {
			return "Double Your $$ + 1 Spin";
		}
		if (firstChar == 'A') {
			return "Add-a-One";
		}
		if (firstChar == 'B') {
			return "Big Bucks";
		}
		if (firstChar == 'C') {
			return "Pick-a-Corner";
		}
		if (firstChar == 'M' || firstChar == '<' || firstChar == '>') {
			// Variable movement spaces
			int moveAmt = Integer.parseInt(currValue.substring(1));
			StringBuilder ret = new StringBuilder();
			if (firstChar == 'M') {
				ret.append("Move ");
			} else if (firstChar == '<') {
				ret.append("Go Back ");
			} else {
				ret.append("Advance ");
			}
			ret.append(moveAmt);
			ret.append(moveAmt == 1 ? " Space" : " Spaces");
			return ret.toString();
		}
		// Cash spaces
		char lastChar = currValue.charAt(currValue.length() - 1);
		int cashAmt = Integer.parseInt(currValue.substring(0,
				currValue.length() - 1));
		if (lastChar == 'S') {
			return String.format("$%1$,d + One Spin", cashAmt);
		}
		if (lastChar == 'L') {
			return String.format("$%1$,d or Lose-1-Whammy", cashAmt);
		}
		return String.format("$%1$,d", Integer.parseInt(currValue));
	}
	
	/**
	 * Returns this space's current cash amount, or 0 if this space currently
	 * has a non-cash value.
	 * <p>
	 * This method is used when determining which space Big Bucks redirects to.
	 *
	 * @return This space's current cash amount, or 0 if this space currently
	 * has a non-cash value.
	 */
	public int getCashAmount()
	{
		String currValue = this.getValue();
		if (!Character.isDigit(currValue.charAt(0))) {
			return 0;
		}
		if (!Character.isDigit(currValue.charAt(currValue.length() - 1))) {
			return Integer.parseInt(currValue.substring(0,
					currValue.length() - 1));
		}
		return Integer.parseInt(currValue);
	}
	
	/**
	 * Returns a {@code String} representation of this space, which is currently
	 * the space's current value's full name.
	 *
	 * @return A {@code String} representation of this space.
	 */
	public String toString()
	{
		return this.getName();
	}
}