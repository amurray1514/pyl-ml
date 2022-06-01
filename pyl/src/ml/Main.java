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
		GeneticLearner gl = new GeneticLearner();
		double eval = gl.evaluate(true);
		System.out.println("Generation 0 evaluation: " + eval);
		for (int i = 1; i <= 10; i++) {
			gl.playGeneration(true);
			eval = gl.evaluate(true);
			System.out.println("Generation " + i + " evaluation: " + eval);
		}
	}
}