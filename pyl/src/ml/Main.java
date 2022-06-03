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
	public static void main(String[] args)
	{
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter("evals.csv")));
			out.println("Minutes,GA Evaluation,TD Evaluation");
		} catch (IOException e) {
			System.out.println("Error opening evals.csv: " + e);
			System.exit(1);
		}
		GeneticLearner gl = new GeneticLearner(1500);
		TemporalDifferenceLearner tdl = new TemporalDifferenceLearner();
		double gl_eval = gl.evaluate(true);
		System.out.println("0 min. GA evaluation: " + gl_eval);
		double tdl_eval = tdl.evaluate(true);
		System.out.println("0 min. TD evaluation: " + tdl_eval);
		out.println("0," + gl_eval + ',' + tdl_eval);
		// Run each learner for 8 hours
		for (int i = 1; i <= 480; i++) {
			long t1 = System.currentTimeMillis();
			long t2 = t1;
			while (t2 - t1 < 60000) {
				gl.playGeneration(true);
				t2 = System.currentTimeMillis();
			}
			gl_eval = gl.evaluate(true);
			System.out.println(i + " min. GA evaluation: " + gl_eval);
			t1 = System.currentTimeMillis();
			t2 = t1;
			while (t2 - t1 < 60000) {
				tdl.playGame(true);
				t2 = System.currentTimeMillis();
			}
			tdl_eval = tdl.evaluate(true);
			System.out.println(i + " min. TD evaluation: " + tdl_eval);
			out.println(i + ',' + gl_eval + ',' + tdl_eval);
		}
		out.close();
	}
}