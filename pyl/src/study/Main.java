package study;

import game.Board;
import game.Game;
import game.InputUtil;
import ml.NeuralNet;

/**
 * Main class to run the study.
 *
 * @author Archer Murray
 */
public final class Main
{
	/**
	 * The boards to use in the game.
	 */
	public static final Board[] BOARDS = {
			new Board("board1.txt"), new Board("board2.txt")
	};
	
	public static void main(String[] args)
	{
		// Get filenames
		String net_fn = InputUtil.getLine("Enter filename for network: ");
		NeuralNet nn = new NeuralNet(net_fn);
		String rep_fn = InputUtil.getLine("Enter filename for reports: ");
		AnalysisNeuralNetPlayer[] players = new AnalysisNeuralNetPlayer[3];
		int numHuman = 0, numAI = 0;
		// Determine player types
		for (int i = 0; i < 3; i++) {
			while (true) {
				String ipt = InputUtil.getLine("Is player " + (i + 1) +
								" human (h) or AI (a)? ").toLowerCase();
				if (!ipt.isEmpty()) {
					char ic = ipt.charAt(0);
					if (ic == 'h') {
						numHuman++;
						players[i] = new AnalysisManualPlayer(nn, rep_fn +
								'_' + (i + 1));
						players[i].setName("Human " + numHuman);
						break;
					}
					if (ic == 'a') {
						numAI++;
						players[i] = new AnalysisNeuralNetPlayer(nn, rep_fn +
								'_' + (i + 1));
						players[i].setName("AI " + numAI);
						break;
					}
				}
			}
		}
		// Play the game
		new Game(players, BOARDS).play(true);
		// Close analysis file writers
		for (AnalysisNeuralNetPlayer p: players) {
			p.close();
		}
	}
}