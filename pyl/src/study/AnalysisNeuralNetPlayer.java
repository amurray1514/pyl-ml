package study;

import game.Game;
import game.Player;
import game.Space;
import ml.NeuralNet;
import ml.NeuralNetPlayer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class representing a neural-network-controlled Press Your Luck player that
 * records its analysis of each situation.
 *
 * @author Archer Murray
 */
public class AnalysisNeuralNetPlayer extends NeuralNetPlayer
{
	private PrintWriter txtOut = null, csvOut = null;
	private final double[] lastState;
	
	/**
	 * Creates a new analysis neural network player based on the passed-in
	 * neural network and that writes its analyses to the passed-in file name.
	 *
	 * @param nn The neural network to use for analysis.
	 * @param fn The file name to write analyses to.
	 */
	public AnalysisNeuralNetPlayer(NeuralNet nn, String fn)
	{
		super(nn);
		this.lastState = new double[NeuralNet.INPUT_LENGTH];
		try {
			this.txtOut = new PrintWriter(new BufferedWriter(new FileWriter(
					fn + ".txt")));
			this.txtOut.println("*".repeat(40) +
					"\nGame Analysis\n" + "*".repeat(40));
			this.csvOut = new PrintWriter(new BufferedWriter(new FileWriter(
					fn + ".csv")));
			for (int i = 0; i < NeuralNet.INPUT_LENGTH; i++) {
				this.csvOut.print(i + ",");
			}
			this.csvOut.println("Evaluation");
		} catch (IOException e) {
			System.out.println("Error writing to file: " + e);
			System.exit(1);
		}
	}
	
	/**
	 * Closes the analysis file writers. This method should be run at the end of
	 * the game to ensure analyses have been written properly.
	 */
	public void close()
	{
		this.txtOut.close();
		this.csvOut.close();
	}
	
	@Override
	public boolean pressOrPass()
	{
		// Evaluate current state
		this.learn();
		// Make decision
		boolean ret = super.pressOrPass();
		List<Double> analysis = super.getLastAnalysis();
		this.txtOut.println("=== Press or Pass Evaluation ===");
		this.txtOut.println("Press evaluation: " + analysis.get(0));
		this.txtOut.println("Pass evaluation: " + analysis.get(1));
		return ret;
	}
	
	@Override
	public Space chooseMoveTarget(List<Space> moveTargets)
	{
		// Evaluate current state
		this.learn();
		// Make decision
		Space ret = super.chooseMoveTarget(moveTargets);
		List<Double> analysis = super.getLastAnalysis();
		this.txtOut.println("=== Move Target Evaluation ===");
		for (int i = 0; i < moveTargets.size(); i++) {
			this.txtOut.println(moveTargets.get(i) + " evaluation: " +
					analysis.get(i));
		}
		return ret;
	}
	
	@Override
	public boolean moneyOrLoseWhammy(int amount)
	{
		// Evaluate current state
		this.learn();
		// Make decision
		boolean ret = super.moneyOrLoseWhammy(amount);
		List<Double> analysis = super.getLastAnalysis();
		this.txtOut.println("=== Money or Lose Whammy Evaluation ===");
		this.txtOut.println("Money evaluation: " + analysis.get(0));
		this.txtOut.println("Lose Whammy evaluation: " + analysis.get(1));
		return ret;
	}
	
	@Override
	public Player choosePassTarget(List<Player> targets)
	{
		// Evaluate current state
		this.learn();
		// Make decision
		Player ret = super.choosePassTarget(targets);
		List<Double> analysis = super.getLastAnalysis();
		this.txtOut.println("=== Pass Target Evaluation ===");
		for (int i = 0; i < targets.size(); i++) {
			this.txtOut.println(targets.get(i) + " evaluation: " +
					analysis.get(i));
		}
		return ret;
	}
	
	/**
	 * Evaluates the current state and records the evaluation.
	 */
	@Override
	public void learn()
	{
		this.txtOut.println();
		Game g = this.getGame();
		double[] state = g.getNeuralNetInput(this.getPlayerNum());
		if (IntStream.range(0, NeuralNet.INPUT_LENGTH)
				.noneMatch(i -> state[i] != this.lastState[i])) {
			// State is the same as last state evaluated
			return;
		}
		System.arraycopy(state, 0, this.lastState, 0, NeuralNet.INPUT_LENGTH);
		double eval = this.getNeuralNet().evaluate(state);
		this.txtOut.println("Current state: Round " + (int)(state[1] + 1) +
				", double " + (state[2] > 0 ? "" : "not") +
				" in play, current turn: " + g.getCurrentTurn() +
				", next turn: " + (g.getNextTurn() == null ? "(none)" :
				g.getNextTurn()));
		this.txtOut.print(g);
		this.txtOut.println("Evaluation of current state: " + eval);
		for (int i = 0; i < NeuralNet.INPUT_LENGTH; i++) {
			this.csvOut.print(state[i] + ",");
		}
		this.csvOut.println(eval);
	}
}