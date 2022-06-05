package study;

import game.InputUtil;
import game.Player;
import game.Space;
import ml.NeuralNet;

import java.util.List;

/**
 * Class representing a manually-controlled Press Your Luck player that uses a
 * neural network to analyze each situation.
 *
 * @author Archer Murray
 */
public class AnalysisManualPlayer extends AnalysisNeuralNetPlayer
{
	/**
	 * Creates a new analysis manual player based on the passed-in neural
	 * network and that writes its analyses to the passed-in file name.
	 *
	 * @param nn The neural network to use for analysis.
	 * @param fn The file name to write analyses to.
	 */
	public AnalysisManualPlayer(NeuralNet nn, String fn)
	{
		super(nn, fn);
	}
	
	@Override
	public boolean pressOrPass()
	{
		super.pressOrPass();
		while (true) {
			String ipt = InputUtil.getLine(
					"Would you like to press your luck (p) or pass (s)? ")
					.toLowerCase();
			if (!ipt.isEmpty()) {
				char ic = ipt.charAt(0);
				if (ic == 'p') {
					return true;
				}
				if (ic == 's') {
					return false;
				}
			}
		}
	}
	
	@Override
	public Space chooseMoveTarget(List<Space> moveTargets)
	{
		super.chooseMoveTarget(moveTargets);
		return (Space)InputUtil.getOption("Choose a space to move to.",
				moveTargets.toArray());
	}
	
	@Override
	public boolean moneyOrLoseWhammy(int amount)
	{
		super.moneyOrLoseWhammy(amount);
		while (true) {
			String ipt = InputUtil.getLine(
					"Would you like to take the money (m) or lose one Whammy (l)? ")
					.toLowerCase();
			if (!ipt.isEmpty()) {
				char ic = ipt.charAt(0);
				if (ic == 'm') {
					return true;
				}
				if (ic == 'l') {
					return false;
				}
			}
		}
	}
	
	@Override
	public Player choosePassTarget(List<Player> targets)
	{
		super.choosePassTarget(targets);
		return (Player)InputUtil.getOption("Choose a player to pass to.",
				targets.toArray());
	}
}