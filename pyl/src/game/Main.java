package game;

import java.util.stream.IntStream;

/**
 * Main class to run the project.
 *
 * @author Archer Murray
 */
public final class Main
{
	public static void main(String[] args)
	{
		Player[] players = IntStream.range(0, 3)
				.mapToObj(i -> new ManualPlayer()).toArray(ManualPlayer[]::new);
		Board[] boards = {new Board("board1.txt"), new Board("board2.txt")};
		Game game = new Game(players, boards);
		System.out.println(game.play(true));
	}
}