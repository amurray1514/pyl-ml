package ml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class to run the temporal difference algorithm.
 *
 * @author Archer Murray
 */
public final class TemporalDifferenceRunner
{
	/**
	 * Don't let anyone instantiate this class.
	 */
	private TemporalDifferenceRunner()
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
			out = new PrintWriter(new BufferedWriter(new FileWriter(
					"td_evals.csv")));
			out.println("Minutes,Evaluation,MOE");
		} catch (IOException e) {
			System.out.println("Error opening ga_evals.csv: " + e);
			System.exit(1);
		}
		TemporalDifferenceLearner tdl = new TemporalDifferenceLearner();
		double eval = tdl.evaluate(true);
		System.out.printf("Win rate after %1$3d minutes: %2$7.3f%%\n", 0,
				100 * eval);
		out.println("0," + eval + ',' + getMOE(eval, 30000));
		// Run learner for 8 hours
		for (int i = 1; i <= 480; i++) {
			long t1 = System.currentTimeMillis();
			long t2 = t1;
			while (t2 - t1 < 60000) {
				tdl.playGame(true);
				t2 = System.currentTimeMillis();
			}
			eval = tdl.evaluate(true);
			System.out.printf("Win rate after %1$3d minutes: %2$7.3f%%\n", i,
					100 * eval);
			out.println(i + "," + eval + ',' + getMOE(eval, 30000));
		}
		out.close();
	}
}