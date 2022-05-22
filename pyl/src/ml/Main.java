package ml;

import game.Board;
import game.Game;
import game.HeuristicPlayer;
import game.Player;

/**
 * Main class to run machine learning.
 *
 * @author Archer Murray
 */
public final class Main
{
	public static void main(String[] args)
	{
		Board[] boards = {
				new Board("board1.txt"),
				new Board("board2.txt")
		};
		Player[] players = {
				new HeuristicPlayer(),
				new NeuralNetPlayer(),
				new NeuralNetPlayer()
		};
		new Game(players, boards).play(true);
	}
}