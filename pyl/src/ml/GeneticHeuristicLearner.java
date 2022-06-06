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
 * Your Luck against the heuristic strategy.
 *
 * @author Archer Murray
 */
public class GeneticHeuristicLearner
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
	private class GeneticHeuristicLearnerThread extends Thread
	{
		private final List<NeuralNetPlayer> players;
		
		/**
		 * Creates a new thread with the given players.
		 *
		 * @param players The players to use.
		 */
		public GeneticHeuristicLearnerThread(List<NeuralNetPlayer> players)
		{
			this.players = players;
		}
		
		@Override
		public void run()
		{
			int[] winCounts = new int[this.players.size()];
			// Have each player play 100 games against two heuristic players
			for (int i = 0; i < this.players.size(); i++) {
				Player[] competitors = {
						this.players.get(i), new HeuristicPlayer(),
						new HeuristicPlayer()
				};
				for (int j = 0; j < 100; j++) {
					List<Player> compList = Arrays.asList(competitors);
					Collections.shuffle(compList);
					List<Player> winners;
					synchronized (BOARDS) {
						winners = new Game(compList.toArray(new Player[0]),
								BOARDS).play(false);
					}
					// See if the neural net player is a winner
					if (winners.stream().anyMatch(p ->
							p instanceof NeuralNetPlayer)) {
						winCounts[i]++;
					}
				}
			}
			// The top half of players survive
			for (int i = 0; i < this.players.size(); i++) {
				this.players.get(i).setScore(winCounts[i]);
				this.players.get(i).setPlayerNum(i);
			}
			Collections.sort(this.players);
			synchronized (survivors) {
				for (int i = this.players.size() - 1; i >= this.players.size() /
						2; i--) {
					survivors.add(this.players.get(i));
				}
			}
		}
	}
	
	/**
	 * Creates a new genetic heuristic learner with a population size of 1000.
	 */
	public GeneticHeuristicLearner()
	{
		this(1000);
	}
	
	/**
	 * Creates a new genetic heuristic learner with the specified population
	 * size.
	 *
	 * @param numPlayers The population size, which must be divisible by 2.
	 */
	public GeneticHeuristicLearner(int numPlayers)
	{
		assert numPlayers % 2 == 0 : "Number of players must be divisible by 2";
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
		// Assign each pair of players to a group
		List<List<NeuralNetPlayer>> groups = IntStream.range(0, NUM_THREADS)
				.<List<NeuralNetPlayer>>mapToObj(i -> new ArrayList<>())
				.collect(Collectors.toList());
		int gn = -1;
		for (int i = 0; i < this.players.size(); i++) {
			if (i % 2 == 0) {
				gn++;
			}
			groups.get(gn % NUM_THREADS).add(this.players.get(i));
		}
		// Run a thread for each group
		List<GeneticHeuristicLearnerThread> threads =
				IntStream.range(0, NUM_THREADS).mapToObj(i -> new
								GeneticHeuristicLearnerThread(groups.get(i)))
				.collect(Collectors.toList());
		for (GeneticHeuristicLearnerThread t: threads) {
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
			this.players.add(new NeuralNetPlayer(p.getNeuralNet().mutate()));
		}
		this.survivors.clear();
	}
	
	/**
	 * Evaluates the learning progress by determining the population's win rate
	 * against two heuristic players.
	 * <p>
	 * Also, writes the top-performing neural network in the population to the
	 * file "gah_net_[generation_number].txt".
	 *
	 * @param printStatus If {@code true}, prints the progress of the evaluation
	 * to the console while this method runs.
	 * @return The proportion of games won by the neural network players.
	 */
	public double evaluate(boolean printStatus)
	{
		int[] winCounts = new int[this.players.size() / 2];
		for (int i = 0; i < this.players.size() / 2; i++) {
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
							(i * 100.0 + j) / (this.players.size() / 2));
				}
			}
		}
		// Export the player with the most wins
		int maxWins = Arrays.stream(winCounts).max().orElse(-1);
		IntStream.range(0, this.players.size() / 2)
				.filter(j -> winCounts[j] == maxWins)
				.findFirst().ifPresent(j -> {
					try {
						this.players.get(j).getNeuralNet()
								.export("gah_net_" + this.numGens);
					} catch (IOException e) {
						System.out.println("Error writing to file: " + e);
					}
				});
		if (printStatus) {
			System.out.print("\r");
		}
		return 2.0 * Arrays.stream(winCounts).sum() / (100 *
				this.players.size());
	}
}