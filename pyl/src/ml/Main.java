package ml;

/**
 * Main class to run machine learning.
 *
 * @author Archer Murray
 */
public final class Main
{
	public static void main(String[] args)
	{
		// Find the largest input to sigma that yields an output of 0
		double lowerBound = -750, upperBound = 0;
		double input = (upperBound + lowerBound) / 2, prevInput = -1;
		while (input != prevInput) {
			double result = NeuralNet.sigmoid(input);
			System.out.println("sigmoid(" + input + ") = " + result);
			if (result == 0.0) {
				lowerBound = input;
			} else {
				upperBound = input;
			}
			prevInput = input;
			input = (upperBound + lowerBound) / 2;
		}
		System.out.println("Lower bound: " + lowerBound);
		System.out.println("Upper bound: " + upperBound);
	}
}