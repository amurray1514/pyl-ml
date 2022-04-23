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
		String[] fns = {"board1.txt", "board2.txt"};
		AverageStateCalculator asc = new AverageStateCalculator(fns);
		double[] avg = asc.runGames(10000);
		String[] valueDescriptors = {
				"Constant", "IsRound2", "DoubleInPlay", "AboutToSpin",
				"MyTurnNow", "MyTurnNext", "MyWhammy1", "MyWhammy2",
				"MyWhammy3", "MyWhammy4", "MyScore", "MyEarnedSpins",
				"MyPassedSpins", "Opp1TurnNow", "Opp1TurnNext", "Opp1Whammy1",
				"Opp1Whammy2", "Opp1Whammy3", "Opp1Whammy4", "Opp1Score",
				"Opp1EarnedSpins", "Opp1PassedSpins", "Opp1IsTarget",
				"Opp2TurnNow", "Opp2TurnNext", "Opp2Whammy1", "Opp2Whammy2",
				"Opp2Whammy3", "Opp2Whammy4", "Opp2Score", "Opp2EarnedSpins",
				"Opp2PassedSpins", "Opp2IsTarget"
		};
		for (int i = 0; i < 33; i++) {
			System.out.println("Avg " + valueDescriptors[i] + " = " + avg[i]);
		}
	}
}