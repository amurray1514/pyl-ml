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
		Board b = new Board("board2.txt");
		System.out.println(b.stopBoard());
		System.out.println(b);
		
		Player p1 = new ManualPlayer();
		p1.setPlayerNum(1);
		Player p2 = new ManualPlayer();
		p2.setPlayerNum(2);
		Player p3 = new ManualPlayer();
		p3.setPlayerNum(3);
		System.out.println(p1.choosePassTarget(new Player[]{p2, p3}));
	}
}