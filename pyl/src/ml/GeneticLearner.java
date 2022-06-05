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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class that uses a genetic algorithm to train a neural network to play Press
 * Your Luck.
 *
 * @author Archer Murray
 */
public class GeneticLearner
{
	/**
	 * The boards to use in the games.
	 */
	public static final Board[] BOARDS = {
			new Board("board1.txt"), new Board("board2.txt")
	};
	/**
	 * The number of threads to be running at a time.
	 */
	public static final int NUM_THREADS = 5;
	
	private final List<NeuralNetPlayer> players, survivors;
	private long numGens;
	
	/**
	 * This class implements a thread to perform multithreaded genetic
	 * learning.
	 */
	private class GeneticLearnerThread extends Thread
	{
		private final List<NeuralNetPlayer> players;
		
		/**
		 * Creates a new thread with the given players.
		 *
		 * @param players The players to use.
		 */
		public GeneticLearnerThread(List<NeuralNetPlayer> players)
		{
			this.players = players;
		}
		
		@Override
		public void run()
		{
			// Have players play each other in groups of three
			for (int i = 0; i < this.players.size(); i += 3) {
				NeuralNetPlayer[] competitors = {
						this.players.get(i), this.players.get(i + 1),
						this.players.get(i + 2)
				};
				int[] winCounts = {0, 0, 0};
				// Play 100 games
				for (int j = 0; j < 100; j++) {
					List<NeuralNetPlayer> compList = Arrays.asList(competitors);
					Collections.shuffle(compList);
					List<Player> winners = new Game(compList.toArray(
							new NeuralNetPlayer[0]), BOARDS).play(false);
					// For each player, see if they are a winner
					for (int k = 0; k < 3; k++) {
						for (Player p: winners) {
							if (competitors[k].equals(p)) {
								winCounts[k]++;
								break;
							}
						}
					}
				}
				// The player with the most wins survives
				int maxWins = Arrays.stream(winCounts).max().orElse(-1);
				synchronized (survivors) {
					IntStream.range(0, 3).filter(j -> winCounts[j] == maxWins)
							.findFirst().ifPresent(
									j -> survivors.add(competitors[j]));
				}
			}
		}
	}
	
	/**
	 * Creates a new genetic learner with a population size of 1500.
	 */
	public GeneticLearner()
	{
		this(1500);
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
		this.survivors = new ArrayList<>();
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
		if (printStatus) {
			System.out.printf("\rGenerations completed: %1$10d", this.numGens);
		}
		this.numGens++;
		// Shuffle players
		Collections.shuffle(this.players);
		// Assign each set of three players to a group
		List<List<NeuralNetPlayer>> groups = IntStream.range(0, NUM_THREADS)
				.<List<NeuralNetPlayer>>mapToObj(i -> new ArrayList<>())
				.collect(Collectors.toList());
		int gn = -1;
		for (int i = 0; i < this.players.size(); i++) {
			if (i % 3 == 0) {
				gn++;
			}
			groups.get(gn % NUM_THREADS).add(this.players.get(i));
		}
		// Run a thread for each group
		List<GeneticLearnerThread> threads = IntStream.range(0, NUM_THREADS)
				.mapToObj(i -> new GeneticLearnerThread(groups.get(i)))
				.collect(Collectors.toList());
		for (GeneticLearnerThread t: threads) {
			t.start();
		}
		for (int i = 0; i < NUM_THREADS;) {
			try {
				threads.get(i).join();
				i++;
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		// Survivors reproduce and mutate
		this.players.clear();
		this.players.addAll(this.survivors);
		for (NeuralNetPlayer p: this.survivors) {
			for (int i = 0; i < 2; i++) {
				this.players.add(new NeuralNetPlayer(
						p.getNeuralNet().mutate()));
			}
		}
		this.survivors.clear();
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
				if (winners.stream()
						.anyMatch(p -> p instanceof NeuralNetPlayer)) {
					winCounts[i]++;
				}
				if (printStatus) {
					System.out.printf("\rEvaluation: %1$6.2f%% complete",
							(i * 100.0 + j) / (this.players.size() / 3));
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