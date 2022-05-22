package game;

import java.util.stream.IntStream;

/**
 * Class representing a state-recording player, which is a heuristic player
 * that records information about the game state.
 *
 * @author Archer Murray
 */
public class StateRecordingPlayer extends HeuristicPlayer
{
	private final double[] totalStateValues;
	private long statesMeasured;
	
	/**
	 * Creates a new state-recording player.
	 */
	public StateRecordingPlayer()
	{
		this.totalStateValues = new double[33];
		this.statesMeasured = 0;
	}
	
	/**
	 * Measures the current game state and stores the values.
	 */
	@Override
	public void learn()
	{
		double[] state = this.getGame().getNeuralNetInput(this.getPlayerNum());
		for (int i = 0; i < 33; i++) {
			this.totalStateValues[i] += state[i];
		}
		this.statesMeasured++;
	}
	
	/**
	 * Returns the average of all game states this player has measured.
	 *
	 * @return The average of all game states this player has measured.
	 */
	public double[] getAverageState()
	{
		return IntStream.range(0, 33).mapToDouble(
				i -> this.totalStateValues[i] / this.statesMeasured).toArray();
	}
}