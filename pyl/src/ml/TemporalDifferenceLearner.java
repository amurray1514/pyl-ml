package ml;

import game.Board;
import game.Game;
import game.HeuristicPlayer;
import game.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class that uses a temporal difference algorithm to train a neural network to
 * play Press Your Luck.
 *
 * @author Archer Murray
 */
public class TemporalDifferenceLearner
{
	public static final Board[] BOARDS = {
			new Board("board1.txt"), new Board("board2.txt")
	};
	
	private final TemporalDifferencePlayer[] players;
	private long numGames;
	
	/**
	 * Creates a new temporal difference learner.
	 */
	public TemporalDifferenceLearner()
	{
		this.players = new TemporalDifferencePlayer[]{
				new TemporalDifferencePlayer(), new TemporalDifferencePlayer(),
				new TemporalDifferencePlayer()
		};
		this.numGames = 0;
	}
	
	/**
	 * Plays a game between the temporal difference players to help them learn.
	 *
	 * @param printStatus If {@code true}, prints the total number of games
	 * played to the console.
	 */
	public void playGame(boolean printStatus)
	{
		if (printStatus) {
			System.out.print("\rTotal games played: " + this.numGames);
		}
		this.numGames++;
		// Shuffle players
		List<TemporalDifferencePlayer> playersList = Arrays.asList(
				this.players);
		Collections.shuffle(playersList);
		for (int i = 0; i < 3; i++) {
			this.players[i] = playersList.get(i);
		}
		// Play game
		List<Player> winners = new Game(this.players, BOARDS).play(false);
		// Learn based on final evaluations
		for (int i = 0; i < 3; i++) {
			boolean isWinner = false;
			for (Player p: winners) {
				if (this.players[i].compareTo(p) == 0) {
					isWinner = true;
					break;
				}
			}
			this.players[i].learn(isWinner ? 1.0 : 0.0);
		}
	}
	
	/**
	 * Evaluates the learning progress by determining the players' win rate
	 * against two heuristic players.
	 * <p>
	 * Also, writes the top-performing neural network in the population to the
	 * file "td_net_[game_number].txt".
	 *
	 * @param printStatus If {@code true}, prints the progress of the evaluation
	 * to the console while this method runs.
	 * @return The proportion of games won by the neural network players.
	 */
	public double evaluate(boolean printStatus)
	{
		int[] winCounts = new int[3];
		for (int i = 0; i < 3; i++) {
			Player[] competitors = {
					new NeuralNetPlayer(this.players[i].getNeuralNet()),
					new HeuristicPlayer(), new HeuristicPlayer()
			};
			// Play 10000 games
			for (int j = 0; j < 10000; j++) {
				List<Player> gCompList = Arrays.asList(competitors);
				Collections.shuffle(gCompList);
				List<Player> winners = new Game(gCompList.toArray(
						new Player[0]), BOARDS).play(false);
				// See if the neural net player is a winner
				if (winners.stream()
						.anyMatch(p -> p instanceof NeuralNetPlayer)) {
					winCounts[i]++;
				}
				if (printStatus) {
					System.out.print("\rEvaluation: Game " + (i * 10000 + j) +
							" of 30000 complete");
				}
			}
		}
		// Export the player with the most wins
		int maxWins = Arrays.stream(winCounts).max().orElse(-1);
		IntStream.range(0, 3).filter(j -> winCounts[j] == maxWins).findFirst()
				.ifPresent(j -> {
					try {
						this.players[j].getNeuralNet()
								.export("td_net_" + this.numGames);
					} catch (IOException e) {
						System.out.println("Error writing to file: " + e);
					}
				});
		if (printStatus) {
			System.out.print("\r");
		}
		return Arrays.stream(winCounts).sum() / 30000.0;
	}
}