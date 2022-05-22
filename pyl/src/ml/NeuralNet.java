package ml;

import java.io.*;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Class representing a Press Your Luck neural network.
 * <p>
 * The neural network takes in several variables about the game in input nodes,
 * then outputs an estimated probability of winning in a single output node. The
 * neural network has one hidden layer.
 * <p>
 * The 34 input nodes are as follows:
 * <p>
 * 1 node that always contains 1.
 * <p>
 * 1 node that is 0 when it is round 1 and 1 when it is round 2.
 * <p>
 * 1 node that is 1 if and only if Double Your $$ + One Spin is in play.
 * <p>
 * 1 node that is 1 if and only if the active player is about to spin.
 * <p>
 * The following set of 10 nodes for each of the three players:
 * <p>
 * - 2 binary (0/1) nodes representing turn order. One node is 1 when the player
 * is currently taking their turn, and the other is 1 when the player is next in
 * the turn order.
 * <p>
 * - 4 binary (0/1) nodes representing Whammy count. Each node successively
 * switches from 0 to 1 as the player's Whammy count increases.
 * <p>
 * - 1 node containing the player's score divided by 6700.
 * <p>
 * - 1 node containing the player's earned spin count divided by 4.
 * <p>
 * - 1 node containing the player's passed spin count divided by 0.8.
 * <p>
 * - 1 binary (0/1) node that is 1 when the player is a valid pass target (not
 * used for the player run by this neural network).
 *
 * @author Archer Murray
 */
public class NeuralNet
{
	/**
	 * The number of values to input.
	 */
	public static final int INPUT_LENGTH = 33;
	
	private final Random rng;
	private final int hiddenLength;
	private final double[] weights;
	
	/**
	 * Creates a new neural network with a hidden layer of size 40.
	 */
	public NeuralNet()
	{
		this(40);
	}
	
	/**
	 * Creates a new neural network with the given hidden layer size.
	 *
	 * @param hiddenLayerSize The number of nodes in the hidden layer.
	 */
	public NeuralNet(int hiddenLayerSize)
	{
		this.rng = new Random();
		this.hiddenLength = hiddenLayerSize;
		this.weights = new double[hiddenLayerSize * (INPUT_LENGTH + 1)];
		// Initialize input-side weights
		for (int i = 0; i < hiddenLayerSize * INPUT_LENGTH; i++) {
			this.weights[i] = rng.nextGaussian() / Math.sqrt(INPUT_LENGTH);
		}
		// Initialize output-side weights
		for (int i = hiddenLayerSize * INPUT_LENGTH; i < this.weights.length;
				i++) {
			this.weights[i] = rng.nextGaussian() / Math.sqrt(hiddenLayerSize);
		}
	}
	
	/**
	 * Creates a neural network with weights imported from the given file.
	 *
	 * @param fn The filename to import weights from.
	 * @throws IOException If an I/O error occurs.
	 */
	public NeuralNet(String fn) throws IOException
	{
		// Initialize RNG
		this.rng = new Random();
		// Set up file reading
		BufferedReader in = new BufferedReader(new FileReader(fn));
		String line = in.readLine();
		int lineNum = 1;
		assert line != null : "Unexpected EOF while reading " + fn;
		// Read hidden layer size first
		int hiddenLayerSize = -1;
		try {
			hiddenLayerSize = Integer.parseInt(line);
		} catch (NumberFormatException e) {
			System.out.println("Line 1 of file " + fn + " must be an integer");
			System.exit(1);
		}
		this.hiddenLength = hiddenLayerSize;
		// Then read the weights
		this.weights = new double[this.hiddenLength * (INPUT_LENGTH + 1)];
		while (lineNum <= this.weights.length + 1) {
			line = in.readLine();
			lineNum++;
			assert line != null : "Unexpected EOF while reading " + fn;
			try {
				this.weights[lineNum - 2] = Double.parseDouble(line);
			} catch (NumberFormatException e) {
				this.weights[lineNum - 2] = 0.0;
				System.out.println("Warning - malformed weight at line " +
						lineNum + " of file " + fn + "; weight set to 0");
			}
		}
	}
	
	/**
	 * The sigmoid function. Returns 1/(1+e^(-x)).
	 *
	 * @param x The input to the sigmoid function.
	 * @return The output of the sigmoid function.
	 */
	public static double sigmoid(double x)
	{
		if (x > 36.73680056967711) {
			// Result is sufficiently close to 1 to return 1
			return 1.0;
		}
		if (x < -709.7827128933831) {
			// Result is sufficiently close to 0 to return 0
			return 0.0;
		}
		return 1.0 / (1 + Math.exp(-x));
	}
	
	/**
	 * Adjusts the given input to be better suited to a Press Your Luck neural
	 * network.
	 * <p>
	 * Specifically, this method divides each player's score by 6700, each
	 * player's number of earned spins by 4, and each player's number of passed
	 * spins by 0.8. Data collection from heuristic play shows that these
	 * adjustments should give the respective nodes mean values around 0.5.
	 *
	 * @param input The input to adjust.
	 * @return The adjusted input.
	 */
	public static double[] adjustInput(double[] input)
	{
		double[] ret = new double[33];
		System.arraycopy(input, 0, ret, 0, 33);
		// Score
		ret[10] /= 6700;
		ret[19] /= 6700;
		ret[29] /= 6700;
		// Earned spins
		ret[11] /= 4;
		ret[20] /= 4;
		ret[30] /= 4;
		// Passed spins
		ret[12] /= 0.8;
		ret[21] /= 0.8;
		ret[31] /= 0.8;
		return ret;
	}
	
	/**
	 * Evaluates the neural network on the given input values.
	 *
	 * @param input The input values.
	 * @return The output of the neural network.
	 */
	public double evaluate(double[] input)
	{
		// Input must be the correct size
		assert input.length == INPUT_LENGTH : "input must be length " +
				INPUT_LENGTH;
		int weightIdx = 0;
		double[] adjIn = adjustInput(input);
		// Calculate hidden layer
		double[] hiddenLayer = new double[this.hiddenLength];
		for (int i = 0; i < this.hiddenLength; i++) {
			hiddenLayer[i] = 0;
			for (int j = 0; j < INPUT_LENGTH; j++) {
				hiddenLayer[i] += adjIn[j] * this.weights[weightIdx];
				weightIdx++;
			}
			hiddenLayer[i] = sigmoid(hiddenLayer[i]);
		}
		// Calculate output
		double output = 0;
		for (int i = 0; i < this.hiddenLength; i++) {
			output += hiddenLayer[i] * this.weights[weightIdx];
			weightIdx++;
		}
		return sigmoid(output);
	}
	
	/**
	 * Returns the gradient of this neural network at the given input values,
	 * with values scaled so that the length of the gradient vector is 1.
	 *
	 * @param input The input values.
	 * @return The unit gradient of this neural network.
	 */
	public double[] unitGradient(double[] input)
	{
		// Input must be the correct size
		assert input.length == INPUT_LENGTH : "input must be length " +
				INPUT_LENGTH;
		// Set up variables
		double[] adjIn = adjustInput(input);
		double[] gradient = new double[this.weights.length];
		double vecLen = 0.0;
		// Evaluate at original input ("pre" values are pre-sigmoid)
		double[] hl1pre = new double[this.hiddenLength];
		double[] hl1 = new double[this.hiddenLength];
		for (int i = 0; i < this.hiddenLength; i++) {
			hl1pre[i] = 0;
			for (int j = 0; j < INPUT_LENGTH; j++) {
				hl1pre[i] += adjIn[j] * this.weights[i * INPUT_LENGTH + j];
			}
			hl1[i] = sigmoid(hl1pre[i]);
		}
		double eval1pre = IntStream.range(0, this.hiddenLength)
				.mapToDouble(i -> hl1[i] * this.weights[
						this.hiddenLength * INPUT_LENGTH + i]).sum();
		double eval1 = sigmoid(eval1pre);
		// Set up work variables for fast evaluation with small weight changes
		double[] hl2pre = new double[this.hiddenLength];
		System.arraycopy(hl1pre, 0, hl2pre, 0, this.hiddenLength);
		double[] hl2 = new double[this.hiddenLength];
		System.arraycopy(hl1, 0, hl2, 0, this.hiddenLength);
		double eval2pre = eval1pre;
		// Estimate partial derivative with respect to each weight by evaluating
		// after increasing each weight by 1/8192 (using work variables above to
		// speed up evaluation)
		int weightIdx = 0;
		for (int i = 0; i < this.hiddenLength; i++) {
			for (int j = 0; j < INPUT_LENGTH; j++) {
				// Recalculate relevant nodes
				hl2pre[i] += adjIn[j] / 8192;
				hl2[i] = sigmoid(hl2pre[i]);
				eval2pre += (hl2[i] - hl1[i]) * this.weights[
						this.hiddenLength * INPUT_LENGTH + i];
				// Estimate partial derivative
				gradient[weightIdx] = 8192 * (sigmoid(eval2pre) - eval1);
				vecLen += gradient[weightIdx] * gradient[weightIdx];
				weightIdx++;
				// Reset relevant nodes
				hl2pre[i] = hl1pre[i];
				eval2pre = eval1pre;
			}
		}
		for (int i = 0; i < this.hiddenLength; i++) {
			// Recalculate relevant node
			eval2pre += hl1[i] / 8192;
			// Estimate partial derivative
			gradient[weightIdx] = 8192 * (sigmoid(eval2pre) - eval1);
			vecLen += gradient[weightIdx] * gradient[weightIdx];
			weightIdx++;
			// Reset relevant node
			eval2pre = eval1pre;
		}
		// Scale gradient so that the length of the gradient vector is 1
		vecLen = Math.sqrt(vecLen);
		for (int i = 0; i < this.weights.length; i++) {
			gradient[i] /= vecLen;
		}
		return gradient;
	}
	
	/**
	 * Exports the weights of this neural network to the specified filename.
	 *
	 * @param fn The filename to export to.
	 * @throws IOException If an I/O error occurs.
	 */
	public void export(String fn) throws IOException
	{
		PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(fn)));
		// Write hidden layer size
		out.println(this.hiddenLength);
		// Write weights
		for (double w: this.weights) {
			out.println(w);
		}
		// Close writer
		out.close();
	}
}