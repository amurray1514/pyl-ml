package ml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Main class to run machine learning.
 *
 * @author Archer Murray
 */
public final class Main
{
	/**
	 * Don't let anyone instantiate this class.
	 */
	private Main()
	{
	}
	
	/**
	 * Returns the margin of error with 99% confidence for the given proportion
	 * of successes (p) and number of trials (n).
	 *
	 * @param p The proportion of successes.
	 * @param n The number of trials.
	 * @return The margin of error with 99% confidence.
	 */
	public static double getMOE(double p, int n)
	{
		return 2.5758293035489 * Math.sqrt(p * (1 - p) / n);
	}
	
	public static void main(String[] args)
	{
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter("evals.csv")));
			out.println("Minutes,GA Evaluation,GA MOE,TD Evaluation,TD MOE");
		} catch (IOException e) {
			System.out.println("Error opening evals.csv: " + e);
			System.exit(1);
		}
		GeneticLearner gl = new GeneticLearner();
		TemporalDifferenceLearner tdl = new TemporalDifferenceLearner();
		double gl_eval = gl.evaluate(true);
		System.out.printf("GA win rate after %1$3d minutes: %2$7.3f%%\n", 0,
				100 * gl_eval);
		double tdl_eval = tdl.evaluate(true);
		System.out.printf("TD win rate after %1$3d minutes: %2$7.3f%%\n", 0,
				100 * tdl_eval);
		out.println("0," + gl_eval + ',' + getMOE(gl_eval, 50000) +
				',' + tdl_eval + ',' + getMOE(tdl_eval, 30000));
		// Run each learner for 8 hours
		for (int i = 1; i <= 480; i++) {
			long t1 = System.currentTimeMillis();
			long t2 = t1;
			while (t2 - t1 < 60000) {
				gl.playGeneration(true);
				t2 = System.currentTimeMillis();
			}
			gl_eval = gl.evaluate(true);
			System.out.printf("GA win rate after %1$3d minutes: %2$7.3f%%\n", i,
					100 * gl_eval);
			t1 = System.currentTimeMillis();
			t2 = t1;
			while (t2 - t1 < 60000) {
				tdl.playGame(true);
				t2 = System.currentTimeMillis();
			}
			tdl_eval = tdl.evaluate(true);
			System.out.printf("TD win rate after %1$3d minutes: %2$7.3f%%\n", i,
					100 * tdl_eval);
			out.println(i + "," + gl_eval + ',' + getMOE(gl_eval, 50000) +
					',' + tdl_eval + ',' + getMOE(tdl_eval, 30000));
		}
		out.close();
	}
}