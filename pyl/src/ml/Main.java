package ml;

/**
 * Main class to run machine learning.
 *
 * @author Archer Murray
 */
public final class Main
{
	public static void main(String[] args)
	{
		GeneticLearner gl = new GeneticLearner(1500);
		double eval = gl.evaluate(true);
		System.out.println("0 min. evaluation: " + eval);
		// Run for 8 hours
		for (int i = 1; i <= 480; i++) {
			long t1 = System.currentTimeMillis();
			long t2 = t1;
			while (t2 - t1 < 60000) {
				gl.playGeneration(true);
				t2 = System.currentTimeMillis();
			}
			eval = gl.evaluate(true);
			System.out.println(i + " min. evaluation: " + eval);
		}
	}
}