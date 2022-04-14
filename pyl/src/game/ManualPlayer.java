package game;

import java.util.List;

/**
 * Class representing a Press Your Luck player.
 *
 * @author Archer Murray
 */
public class ManualPlayer extends Player
{
	@Override
	public boolean pressOrPass()
	{
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
		return (Space)InputUtil.getOption("Choose a space to move to.",
				moveTargets.toArray());
	}
	
	@Override
	public boolean moneyOrLoseWhammy(int amount)
	{
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
	public Player choosePassTarget(Player[] targets)
	{
		return (Player)InputUtil.getOption("Choose a player to pass to.",
				(Object[])targets);
	}
}