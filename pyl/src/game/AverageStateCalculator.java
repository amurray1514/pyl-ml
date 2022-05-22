package game;

import java.util.Arrays;

/**
 * Class that calculates the average game state in Press Your Luck, in order to
 * assist in calculating adjustments to neural network inputs.
 *
 * @author Archer Murray
 */
public class AverageStateCalculator
{
	private final StateRecordingPlayer[] players;
	private final String[] boardFilenames;
	
	/**
	 * Constructs a new {@code AverageStateCalculator} with the passed-in
	 * boards.
	 *
	 * @param fns The filenames of the boards.
	 */
	public AverageStateCalculator(String[] fns)
	{
		this.players = new StateRecordingPlayer[3];
		for (int i = 0; i < 3; i++) {
			this.players[i] = new StateRecordingPlayer();
		}
		this.boardFilenames = fns;
	}
	
	/**
	 * Plays the passed-in number of games and returns the average measured
	 * game state.
	 *
	 * @param numGames The number of games to play.
	 * @return The average measured game state.
	 */
	public double[] runGames(int numGames)
	{
		for (int g = 0; g < numGames; g++) {
			Board[] boards = Arrays.stream(this.boardFilenames).map(Board::new)
					.toArray(Board[]::new);
			new Game(this.players, boards).play(false);
			System.out.print("\rGames completed: " + (g + 1));
		}
		System.out.println("\r");
		double[] ret = new double[33];
		for (int i = 0; i < 33; i++) {
			for (StateRecordingPlayer p: this.players) {
				ret[i] += p.getAverageState()[i];
			}
			ret[i] /= 3;
		}
		return ret;
	}
}