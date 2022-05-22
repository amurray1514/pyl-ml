package ml;

import game.Board;
import game.Player;
import game.Space;

import java.util.Collections;
import java.util.List;

/**
 * Class representing a player who plays according to a neural network.
 * <p>
 * Note that this player only works in 3-player, 2-round games.
 *
 * @author Archer Murray
 */
public class NeuralNetPlayer extends Player
{
	private final NeuralNet net;
	
	/**
	 * Creates a new player controlled by a random neural network.
	 */
	public NeuralNetPlayer()
	{
		this.net = new NeuralNet();
	}
	
	/**
	 * Creates a new player controlled by the passed-in neural network.
	 *
	 * @param nn The neural network controlling this player.
	 */
	public NeuralNetPlayer(NeuralNet nn)
	{
		this.net = nn;
	}
	
	@Override
	public boolean pressOrPass()
	{
		double[] input = this.getGame().getNeuralNetInput(this.getPlayerNum());
		// Evaluate "press your luck"
		input[3] = 1;
		double pressEval = this.net.evaluate(input);
		input[3] = 0;
		// Evaluate "pass" to each eligible opponent
		double pass1Eval = -1, pass2Eval = -1;
		if (input[22] == 1) {
			// Opponent 1 is an eligible pass target
			input[21] += this.getEarnedSpins();
			input[12] = 0;
			pass1Eval = this.net.evaluate(input);
			input[21] -= this.getEarnedSpins();
			input[12] = this.getEarnedSpins();
		}
		if (input[32] == 1) {
			// Opponent 2 is an eligible pass target
			input[31] += this.getEarnedSpins();
			input[12] = 0;
			pass1Eval = this.net.evaluate(input);
			input[31] -= this.getEarnedSpins();
			input[12] = this.getEarnedSpins();
		}
		// Get overall pass evaluation
		double passEval = Math.max(pass1Eval, pass2Eval);
		if (passEval < 0) {
			// No pass targets; assume last one standing
			input[12] = 0;
			passEval = this.net.evaluate(input);
		}
		return pressEval > passEval;
	}
	
	@Override
	public Space chooseMoveTarget(List<Space> moveTargets)
	{
		double[] input = this.getGame().getNeuralNetInput(this.getPlayerNum());
		double[] spaceEvals = new double[moveTargets.size()];
		for (int i = 0; i < moveTargets.size(); i++) {
			Space space = moveTargets.get(i);
			double[] newIn = new double[33];
			System.arraycopy(input, 0, newIn, 0, 33);
			// Simulate the effect of the space
			String value = space.getValue();
			char firstChar = value.charAt(0);
			if (firstChar == 'W') {
				// Whammy
				newIn[10] = 0;
				newIn[6 + this.getWhammies()] = 1;
				if (this.getWhammies() == 3) {
					// If there would be 4 whammies, remove all spins
					newIn[11] = 0;
					newIn[12] = 0;
				}
				if (newIn[12] > 0) {
					// If there are passed spins, move them to the earned column
					newIn[11] += newIn[12];
					newIn[12] = 0;
				}
			} else if (firstChar == 'P') {
				// Prize: Simulate 10 different prize values
				Board board = this.getGame().getCurrentBoard();
				int prizeMin = board.getMinPrizeValue();
				int prizeMax = board.getMaxPrizeValue();
				double interval = (prizeMax - prizeMin) / 9.0;
				for (double pv = prizeMin; pv < prizeMax + 1; pv += interval) {
					newIn[10] += pv;
					spaceEvals[i] += this.net.evaluate(newIn);
					newIn[10] -= pv;
				}
				spaceEvals[i] /= 10;
				continue;
			} else if (firstChar == 'D') {
				// Double Your $$ + One Spin
				newIn[2] = 0;
				newIn[10] *= 2;
				newIn[11]++;
			} else if (firstChar == 'A') {
				// Add-a-One
				newIn[10] += Math.pow(10,
						Long.toString(this.getScore()).length());
			} else {
				// Cash space
				char lastChar = value.charAt(value.length() - 1);
				int cashAmt = space.getCashAmount();
				if (lastChar == 'S') {
					// Cash + One Spin
					newIn[10] += cashAmt;
					newIn[11]++;
				} else if (lastChar == 'L') {
					// Cash or Lose-1-Whammy
					if (this.getWhammies() == 0) {
						newIn[10] += cashAmt;
					} else {
						if (this.moneyOrLoseWhammy(cashAmt)) {
							newIn[10] += cashAmt;
						} else {
							newIn[5 + this.getWhammies()] = 0;
						}
					}
				} else {
					// Plain cash
					newIn[10] += cashAmt;
				}
			}
			spaceEvals[i] = this.net.evaluate(newIn);
		}
		int maxIdx = 0;
		double maxEval = spaceEvals[0];
		for (int i = 1; i < moveTargets.size(); i++) {
			if (spaceEvals[i] > maxEval) {
				maxEval = spaceEvals[i];
				maxIdx = i;
			}
		}
		return moveTargets.get(maxIdx);
	}
	
	@Override
	public boolean moneyOrLoseWhammy(int amount)
	{
		double[] input = this.getGame().getNeuralNetInput(this.getPlayerNum());
		// Evaluate "money"
		input[10] += amount;
		double moneyEval = this.net.evaluate(input);
		input[10] -= amount;
		// Evaluate "lose one Whammy"
		double loseEval = -1;
		if (this.getWhammies() > 0) {
			input[5 + this.getWhammies()] = 0;
			loseEval = this.net.evaluate(input);
			input[5 + this.getWhammies()] = 1;
		}
		return moneyEval > loseEval;
	}
	
	@Override
	public Player choosePassTarget(List<Player> targets)
	{
		double[] input = this.getGame().getNeuralNetInput(this.getPlayerNum());
		Collections.sort(targets);
		double[] targetEvals = new double[targets.size()];
		for (int i = 0; i < targets.size(); i++) {
			input[21 + 10 * i] += this.getEarnedSpins();
			input[12] = 0;
			targetEvals[i] = this.net.evaluate(input);
			input[21 + 10 * i] -= this.getEarnedSpins();
			input[12] = this.getEarnedSpins();
		}
		int maxIdx = 0;
		double maxEval = targetEvals[0];
		for (int i = 1; i < targets.size(); i++) {
			if (targetEvals[i] > maxEval) {
				maxEval = targetEvals[i];
				maxIdx = i;
			}
		}
		return targets.get(maxIdx);
	}
}