package game;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class representing a Press Your Luck game.
 *
 * @author Archer Murray
 */
public class Game
{
	private final Random rng;
	private final Player[] players;
	private final Board[] boards;
	private int round;
	private Player currentTurn, nextTurn;
	
	/**
	 * Creates a new game with the passed-in arrays of players and boards.
	 *
	 * @param players The array of players competing.
	 * @param boards The array of boards to be used in the game.
	 */
	public Game(Player[] players, Board[] boards) {
		this.rng = new Random();
		this.players = players;
		this.boards = boards;
		this.round = 1;
		this.currentTurn = null;
		this.nextTurn = null;
		for (int i = 0; i < players.length; i++) {
			this.players[i].setPlayerNum(i + 1);
			this.players[i].setGame(this);
		}
	}
	
	/**
	 * Plays the game and returns a list of all winning players.
	 *
	 * @param printResults Whether ot print the game state after each event.
	 * @return A list containing all winning players.
	 */
	public List<Player> play(boolean printResults)
	{
		// Reset player and game variables
		for (int i = 0; i < this.players.length; i++) {
			this.players[i].setPlayerNum(i + 1);
			this.players[i].setScore(0);
			this.players[i].setEarnedSpins(0);
			this.players[i].setPassedSpins(0);
			this.players[i].setWhammies(0);
			this.players[i].setGame(this);
		}
		List<Player> turnOrder = new ArrayList<>();
		Collections.addAll(turnOrder, this.players);
		this.round = 0;
		for (Board board: this.boards) {
			this.round++;
			if (printResults) {
				System.out.println("It's time for round " + this.round + "!\n");
			}
			// Randomly distribute spins (no trivia in this game)
			for (int q = 0; q < 4; q++) {
				int buzzIdx = rng.nextInt(turnOrder.size());
				for (int i = 0; i < turnOrder.size(); i++) {
					if (rng.nextDouble() < (i == buzzIdx ? 0.6 : 0.8)) {
						turnOrder.get(i).addEarnedSpins(i == buzzIdx ? 3 : 1);
					}
				}
			}
			if (printResults) {
				for (Player p: this.players) {
					System.out.println(p.getName() + " earned " +
							p.getEarnedSpins() + " spin" +
							(p.getEarnedSpins() == 1 ? "" : "s") + '.');
				}
			}
			// Determine turn order (spin count in round 1; money afterward)
			if (this.round == 1) {
				for (Player p: this.players) {
					p.setScore(p.getEarnedSpins());
				}
			}
			Collections.sort(turnOrder);
			if (this.round == 1) {
				for (Player p: this.players) {
					p.setScore(0);
				}
			}
			if (printResults) {
				System.out.println(this);
				System.out.println("The turn order will be as follows: " +
						turnOrder.stream().map(Player::getName)
								.collect(Collectors.joining(", ")) + '.');
				InputUtil.getLine("Press Enter to continue...");
			}
			// Play the Big Board until all spins are used
			while (true) {
				// Determine the next player to play
				int activeIdx = IntStream.range(0, turnOrder.size())
						.filter(i -> turnOrder.get(i).hasSpins()).findFirst()
						.orElse(-1);
				// If no player has spins, end the round
				if (activeIdx < 0) {
					break;
				}
				// Set current turn and next turn values
				this.currentTurn = turnOrder.get(activeIdx);
				this.nextTurn = null;
				IntStream.range(activeIdx + 1, turnOrder.size())
						.filter(i -> turnOrder.get(i).hasSpins()).findFirst()
						.ifPresent(i -> this.nextTurn = turnOrder.get(i));
				if (printResults) {
					System.out.println("\nIt is " + this.currentTurn.getName() +
							"'s turn.");
					if (turnOrder.size() == 1) {
						System.out.print("They are playing against the house.");
						System.out.print(" They can keep pressing their luck ");
						System.out.print("to build their score or pass to end");
						System.out.println(" the round.");
					}
				}
				for (Player p: this.players) {
					p.learn();
				}
				// Play all passed spins
				if (printResults && this.currentTurn.getPassedSpins() > 0) {
					System.out.println(this.currentTurn.getName() + " has " +
							this.currentTurn.getPassedSpins() + " passed spin" +
							(this.currentTurn.getPassedSpins() > 1 ? "s" : "") +
							" they must take.");
				}
				while (this.currentTurn.getPassedSpins() > 0) {
					if (printResults) {
						System.out.println(this);
					}
					this.currentTurn.playSpin(board, printResults, true);
					for (Player p: this.players) {
						p.learn();
					}
				}
				// Play or pass earned spins
				while (this.currentTurn.getEarnedSpins() > 0) {
					if (printResults) {
						System.out.println(this);
					}
					if (this.currentTurn.pressOrPass()) {
						// Player presses their luck
						if (printResults) {
							System.out.println(this.currentTurn.getName() +
									" presses their luck!");
						}
						this.currentTurn.playSpin(board, printResults, true);
						for (Player p: this.players) {
							p.learn();
						}
					} else {
						// Player passes
						if (printResults) {
							System.out.println(this.currentTurn.getName() +
									" passes!");
						}
						// Determine pass targets
						long maxScore = -1;
						List<Player> passTargets = new ArrayList<>();
						for (Player p: turnOrder) {
							if (!p.equals(this.currentTurn)) {
								if (p.getScore() >= maxScore) {
									if (p.getScore() > maxScore) {
										maxScore = p.getScore();
										passTargets.clear();
									}
									passTargets.add(p);
								}
							}
						}
						if (passTargets.isEmpty()) {
							// No opponents (active player is last one left)
							this.currentTurn.setEarnedSpins(0);
						} else if (passTargets.size() == 1) {
							// One opponent has the highest score
							Player target = passTargets.get(0);
							int numSpins = this.currentTurn.getEarnedSpins();
							target.addPassedSpins(numSpins);
							this.currentTurn.setEarnedSpins(0);
							if (printResults) {
								System.out.println("The " + numSpins + " spin" +
										(numSpins == 1 ? " goes" : "s go") +
										" to " + target.getName() + '!');
							}
						} else {
							// Multiple opponents tied for the highest score
							if (printResults) {
								System.out.println("They can pass to one of: " +
										passTargets.stream()
												.map(Player::getName)
												.collect(Collectors.joining(", "))
										+ '.');
							}
							Player target = this.currentTurn
									.choosePassTarget(passTargets);
							int numSpins = this.currentTurn.getEarnedSpins();
							target.addPassedSpins(numSpins);
							this.currentTurn.setEarnedSpins(0);
							if (printResults) {
								System.out.println("The " + numSpins + " spin" +
										(numSpins == 1 ? " goes" : "s go") +
										" to " + target.getName() + '!');
							}
						}
						for (Player p: this.players) {
							p.learn();
						}
					}
				}
				// If player has 4 whammies, remove them from the game
				if (this.currentTurn.getWhammies() == 4) {
					turnOrder.remove(this.currentTurn);
				}
			}
			if (printResults) {
				System.out.println("Round " + this.round + " is over!\n");
			}
		}
		// All rounds played; determine the winner(s)
		Collections.sort(turnOrder);
		Collections.reverse(turnOrder);
		List<Player> winners = new ArrayList<>();
		for (Player p: turnOrder) {
			if (p.getScore() == turnOrder.get(0).getScore()) {
				winners.add(p);
			} else {
				break;
			}
		}
		if (printResults) {
			if (winners.isEmpty()) {
				System.out.println("No-one won the game!");
			} else if (winners.size() == 1) {
				System.out.println(winners.get(0).getName() +
						" won the game with a score of " +
						String.format("$%1$,d", winners.get(0).getScore()) +
						'!');
			} else {
				System.out.println("We have joint winners with a score of " +
						String.format("$%1$,d", winners.get(0).getScore()) +
						": " + winners.stream().map(Player::getName)
						.collect(Collectors.joining(", ")) + '!');
			}
		}
		return winners;
	}
	
	/**
	 * Returns an array of all players in this game.
	 *
	 * @return An array of all players in this game.
	 */
	public Player[] getPlayers()
	{
		return this.players;
	}
	
	/**
	 * Returns the board currently in use.
	 *
	 * @return The board currently in use.
	 */
	public Board getCurrentBoard()
	{
		return this.boards[this.round - 1];
	}
	
	/**
	 * Returns {@code true} if it is the final round and {@code false}
	 * otherwise.
	 *
	 * @return {@code true} if it is the final round and {@code false}
	 * otherwise.
	 */
	public boolean isFinalRound()
	{
		return this.round == this.boards.length;
	}
	
	/**
	 * Returns {@code true} if there is only one spin left in the game and
	 * {@code false} otherwise.
	 *
	 * @return {@code true} if there is only one spin left in the game and
	 * {@code false} otherwise.
	 */
	public boolean isFinalSpin()
	{
		return this.isFinalRound() && Arrays.stream(this.players).mapToInt(
				p -> p.getEarnedSpins() + p.getPassedSpins()).sum() == 1;
	}
	
	/**
	 * Returns the player who is currently taking their turn.
	 *
	 * @return The player who is currently taking their turn.
	 */
	public Player getCurrentTurn()
	{
		return this.currentTurn;
	}
	
	/**
	 * Returns the player who is due to take their turn next, or {@code null} if
	 * there is no player next in the turn order.
	 *
	 * @return The player who is due to take their turn next.
	 */
	public Player getNextTurn()
	{
		return this.nextTurn;
	}
	
	/**
	 * Returns an array of values representing the game state to send as input
	 * to a neural network.
	 *
	 * @param playerNum The player number requesting the game state.
	 * @return An array of values representing the game state.
	 */
	public double[] getNeuralNetInput(int playerNum)
	{
		double[] ret = new double[33];
		// Global values (constant, round, double in play)
		ret[0] = 1;
		ret[1] = this.round - 1;
		ret[2] = this.getCurrentBoard().isDoubleInPlay() ? 1 : 0;
		// Player-specific values
		int oppNum = 0;
		long maxOppScore = 0;
		for (Player p: this.players) {
			int offset = 4;
			if (p.getPlayerNum() != playerNum) {
				oppNum++;
				offset += 10 * oppNum - 1;
				if (p.getScore() > maxOppScore) {
					maxOppScore = p.getScore();
				}
			}
			// Current and next turn
			if (p.equals(this.currentTurn)) {
				ret[offset] = 1;
				if (p.getPassedSpins() > 0) {
					ret[3] = 1;
				}
			} else if (p.equals(this.nextTurn)) {
				ret[offset + 1] = 1;
			}
			// Whammy count
			int wc = p.getWhammies();
			for (int i = 0; i < wc; i++) {
				ret[offset + 2 + i] = 1;
			}
			// Score, earned spin count, and passed spin count
			ret[offset + 6] = p.getScore();
			ret[offset + 7] = p.getEarnedSpins();
			ret[offset + 8] = p.getPassedSpins();
		}
		// Each opponent's pass target eligibility
		oppNum = 0;
		for (Player p: this.players) {
			if (p.getPlayerNum() != playerNum) {
				oppNum++;
				if (p.getScore() == maxOppScore) {
					ret[12 + 10 * oppNum] = 1;
				}
			}
		}
		return ret;
	}
	
	/**
	 * Returns a {@code String} representation of this game.
	 *
	 * @return a {@code String} representation of this game.
	 */
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		for (Player p: players) {
			int whammies = p.getWhammies();
			switch (whammies) {
				case 4:
					ret.append("|W  W  W  W");
					break;
				case 3:
					ret.append("|W  W  W   ");
					break;
				case 2:
					ret.append("|W  W      ");
					break;
				case 1:
					ret.append("|W         ");
					break;
				default:
					ret.append("|          ");
			}
		}
		ret.append("|\n");
		for (Player p: players) {
			ret.append(String.format("|%1$-10s", p.getName()));
		}
		ret.append("|\n");
		ret.append("+----------".repeat(this.players.length));
		ret.append("+\n");
		for (Player p: players) {
			ret.append(String.format("|$%1$,9d", p.getScore()));
		}
		ret.append("|\n");
		for (Player p: players) {
			ret.append(String.format("|E:%1$2d  P:%2$2d", p.getEarnedSpins(),
					p.getPassedSpins()));
		}
		ret.append("|\n");
		ret.append("+----------".repeat(this.players.length));
		ret.append("+\n");
		return ret.toString();
	}
}