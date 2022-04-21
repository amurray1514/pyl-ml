package game;

import java.util.Scanner;

/**
 * Helper class designed to get user input from the keyboard.
 *
 * @author Archer Murray
 */
public final class InputUtil
{
	private static final Scanner IN = new Scanner(System.in);
	
	/**
	 * Don't let anyone instantiate this class.
	 */
	private InputUtil()
	{
	}
	
	/**
	 * Get a line of user input after displaying the passed-in prompt.
	 *
	 * @param prompt The prompt to display.
	 * @return The line the user input.
	 */
	public static String getLine(String prompt)
	{
		System.out.print(prompt);
		IN.nextLine();  // flush the input queue
		return IN.nextLine();
	}
	
	/**
	 * Get the user's selection of one or more options.
	 *
	 * @param prompt The prompt to display before listing the options.
	 * @param opts The options.
	 * @return The user's selected option.
	 */
	public static Object getOption(String prompt, Object... opts)
	{
		IN.nextLine();  // flush the input queue
		System.out.println(prompt);
		for (int i = 0; i < opts.length; i++) {
			System.out.println((i + 1) + ". " + opts[i]);
		}
		while (true) {
			System.out.print("Enter the number of your desired option: ");
			try {
				int optNum = Integer.parseInt(IN.nextLine());
				if (optNum >= 1 && optNum <= opts.length) {
					return opts[optNum - 1];
				}
			} catch (Exception e) {
				// do nothing
			}
		}
	}
}