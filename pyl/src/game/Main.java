package game;

/**
 * Main class to run the project.
 *
 * @author Archer Murray
 */
public final class Main
{
	public static void main(String[] args)
	{
		Board b = new Board("board1.txt");
		b.stopBoard();
		System.out.println(b);
	}
}