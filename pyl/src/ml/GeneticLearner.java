package ml;

import game.Board;
import game.Game;
import game.HeuristicPlayer;
import game.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class that uses a genetic algorithm to train a neural network to play Press
 * Your Luck.
 *
 * @author Archer Murray
 */
public class GeneticLearner
{
	public static final Board[] BOARDS = {
			new Board("board1.txt"), new Board("board2.txt")
	};
	
	private final List<NeuralNetPlayer> players;
	private int numGens;
	
	/**
	 * Creates a new genetic learner with a population size of 150.
	 */
	public GeneticLearner()
	{
		this(150);
	}
	
	/**
	 * Creates a new genetic learner with the specified population size.
	 *
	 * @param numPlayers The population size, which must be divisible by 3.
	 */
	public GeneticLearner(int numPlayers)
	{
		assert numPlayers % 3 == 0 : "Number of players must be divisible by 3";
		this.players = new ArrayList<>();
		for (int i = 0; i < numPlayers; i++) {
			this.players.add(new NeuralNetPlayer());
		}
		this.numGens = 0;
	}
	
	/**
	 * Plays one generation of the genetic algorithm.
	 *
	 * @param printStatus If {@code true}, prints the progress of the current
	 * generation to the console while this method runs.
	 */
	public void playGeneration(boolean printStatus)
	{
		this.numGens++;
		Collections.shuffle(this.players);
		List<NeuralNetPlayer> survivors = new ArrayList<>();
		// Have players play each other in groups of three
		for (int i = 0; i < this.players.size(); i += 3) {
			NeuralNetPlayer[] competitors = {
					this.players.get(i),
					this.players.get(i + 1), this.players.get(i + 2)
			};
			int[] winCounts = {0, 0, 0};
			// Play 30 games
			for (int j = 0; j < 30; j++) {
				List<NeuralNetPlayer> gCompList = Arrays.asList(competitors);
				Collections.shuffle(gCompList);
				List<Player> winners = new Game(gCompList.toArray(
						new NeuralNetPlayer[0]), BOARDS).play(false);
				// For each player, see if they are a winner
				for (int k = 0; k < 3; k++) {
					for (Player p: winners) {
						if (competitors[k].compareTo(p) == 0) {
							winCounts[k]++;
							break;
						}
					}
				}
			}
			// The player with the most wins survives
			int maxWins = Arrays.stream(winCounts).max().orElse(-1);
			IntStream.range(0, 3).filter(j -> winCounts[j] == maxWins)
					.findFirst().ifPresent(j -> survivors.add(competitors[j]));
			if (printStatus) {
				System.out.print("\rGeneration " + this.numGens + ": Game set "
						+ (i / 3 + 1) + " of " + (this.players.size() / 3) +
						" complete");
			}
		}
		// Survivors reproduce and mutate
		this.players.clear();
		this.players.addAll(survivors);
		for (NeuralNetPlayer p: survivors) {
			for (int i = 0; i < 2; i++) {
				this.players.add(new NeuralNetPlayer(
						p.getNeuralNet().mutate()));
			}
		}
		if (printStatus) {
			System.out.print("\r");
		}
	}
	
	/**
	 * Evaluates the learning progress by determining the population's win rate
	 * against two heuristic players.
	 * <p>
	 * Also, writes the top-performing neural network in the population to the
	 * file "gl_net_[generation_number].txt".
	 *
	 * @param printStatus If {@code true}, prints the progress of the evaluation
	 * to the console while this method runs.
	 * @return The proportion of games won by the neural network players.
	 */
	public double evaluate(boolean printStatus)
	{
		int[] winCounts = new int[this.players.size() / 3];
		for (int i = 0; i < this.players.size() / 3; i++) {
			Player[] competitors = {
					this.players.get(i), new HeuristicPlayer(),
					new HeuristicPlayer()
			};
			// Play 100 games
			for (int j = 0; j < 100; j++) {
				List<Player> gCompList = Arrays.asList(competitors);
				Collections.shuffle(gCompList);
				List<Player> winners = new Game(gCompList.toArray(
						new Player[0]), BOARDS).play(false);
				// See if the neural net player is a winner
				for (Player p: winners) {
					if (this.players.get(i).compareTo(p) == 0) {
						winCounts[i]++;
						break;
					}
				}
				if (printStatus) {
					System.out.print("\rEvaluation: Game " + (i * 100 + j) +
							" of " + (100 * this.players.size() / 3) +
							" complete");
				}
			}
		}
		// Export the player with the most wins
		int maxWins = Arrays.stream(winCounts).max().orElse(-1);
		IntStream.range(0, this.players.size() / 3)
				.filter(j -> winCounts[j] == maxWins)
				.findFirst().ifPresent(j -> {
					try {
						this.players.get(j).getNeuralNet()
								.export("gl_net_" + this.numGens);
					} catch (IOException e) {
						System.out.println("Error writing to file: " + e);
					}
				});
		if (printStatus) {
			System.out.print("\r");
		}
		return 3.0 * Arrays.stream(winCounts).sum() / (100 *
				this.players.size());
	}
}