package ml;

import java.util.Arrays;

/**
 * Class representing a neural-network-controlled player that trains its neural
 * network using temporal difference (TD) learning.
 *
 * @author Archer Murray
 */
public class TemporalDifferencePlayer extends NeuralNetPlayer
{
	private final double learningRate;
	private boolean hasLastState;
	private double[] lastState;
	
	/**
	 * Creates a new temporal difference learning player a random neural network
	 * and a learning rate of 0.1.
	 * <p>
	 * Note that this player only works in 3-player, 2-round games.
	 */
	public TemporalDifferencePlayer()
	{
		this(new NeuralNet());
	}
	
	/**
	 * Creates a new temporal difference learning player with the passed-in
	 * neural network and a learning rate of 0.1.
	 * <p>
	 * Note that this player only works in 3-player, 2-round games.
	 *
	 * @param nn The neural network this player is controlled by.
	 */
	public TemporalDifferencePlayer(NeuralNet nn)
	{
		this(nn, 0.1);
	}
	
	/**
	 * Creates a new temporal difference learning player with the passed-in
	 * neural network and learning rate.
	 * <p>
	 * Note that this player only works in 3-player, 2-round games.
	 *
	 * @param nn The neural network this player is controlled by.
	 * @param learningRate The learning rate of the temporal difference process,
	 * which should be a small positive value.
	 */
	public TemporalDifferencePlayer(NeuralNet nn, double learningRate)
	{
		super(nn);
		this.learningRate = learningRate;
		this.hasLastState = false;
		this.lastState = new double[33];
	}
	
	@Override
	public void learn()
	{
		double[] newState = this.getGame().getNeuralNetInput(
				this.getPlayerNum());
		this.learn(this.getNeuralNet().evaluate(newState));
	}
	
	@Override
	public void learn(double newEval)
	{
		// Get new game state
		double[] newState = this.getGame().getNeuralNetInput(
				this.getPlayerNum());
		double oldEval = this.getNeuralNet().evaluate(this.lastState);
		// Adjust neural network if there was a previous game state
		if (this.hasLastState) {
			double diff = this.learningRate * (newEval - oldEval);
			this.getNeuralNet().gradientDescent(this.lastState, diff);
		} else {
			this.hasLastState = true;
		}
		// Set last state to new state
		this.lastState = newState;
		System.out.print("\rLearn complete. Round: " +
				(this.getGame().isFinalRound() ? 2 : 1) + ", Spins Left: " +
				Arrays.stream(this.getGame().getPlayers()).mapToInt(
						p -> p.getEarnedSpins() + p.getPassedSpins()).sum());
	}
}