package game;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Class representing a "heuristic player", one that follows a set of heuristic
 * strategies inspired by "PYL Strategy" (Cheung).
 *
 * @author Archer Murray
 */
public class HeuristicPlayer extends Player
{
	@Override
	public boolean pressOrPass()
	{
		// Determine current position and pass target
		int pos = (int)Arrays.stream(this.getGame().getPlayers())
				.filter(p -> p.getScore() >= this.getScore()).count();
		Player target = Arrays.stream(this.getGame().getPlayers())
				.filter(p -> !this.equals(p))
				.max(Comparator.comparingLong(p -> p.getScore())).orElse(null);
		assert target != null;
		// Use a special strategy for the final spin
		if (this.getGame().isFinalSpin()) {
			long scoreDiff = Math.abs(this.getScore() - target.getScore());
			switch (pos) {
				case 1:
					// 1st place: press if 2nd's cash EV < score difference
					return scoreDiff < this.getGame().getCurrentBoard()
							.getExpCash(target.getScore());
				case 2:
					// 2nd place: press if your cash EV < score difference
					return scoreDiff < this.getGame().getCurrentBoard()
							.getExpCash(this.getScore());
				default:
					// 3rd place: always press
					return true;
			}
		}
		// Always press in 3rd place in the final round
		if (this.getGame().isFinalRound() && pos == 3) {
			return true;
		}
		// Always pass with 1 spin left in the final round
		if (this.getGame().isFinalRound() && this.getEarnedSpins() == 1) {
			return false;
		}
		// Always pass with 2 or more whammies
		if (this.getWhammies() >= 2) {
			return false;
		}
		// Always pass if pass target has 3 whammies
		if (target.getWhammies() == 3) {
			return false;
		}
		double expCash = this.getGame().getCurrentBoard().getExpCash(
				this.getScore());
		double expSpins = this.getGame().getCurrentBoard().getExpSpins();
		double expWhammies = this.getGame().getCurrentBoard().getExpWhammies();
		// Calculate expected amount to recoup if next spin hits a whammy
		double recoupAmt = (this.getEarnedSpins() - 1) * expCash /
				(1 - expSpins);
		// Calculate point at which spinning again has a negative EV
		double negExpAmt = expCash * (1 - expWhammies) / expWhammies;
		// Press if current score is below higher of the above values
		return this.getScore() < Math.max(recoupAmt, negExpAmt);
	}
	
	@Override
	public Space chooseMoveTarget(List<Space> moveTargets)
	{
		// Strategy: Take a spin if available and not final spin; otherwise take
		// largest cash amount without a spin
		int maxValue = -1;
		Space maxSpace = null;
		int maxSpinValue = -1;
		Space maxSpinSpace = null;
		for (Space s: moveTargets) {
			int amt = -1;
			if (s.getCashAmount() > 0) {
				amt = s.getCashAmount();
			} else if (s.getValue().equals("P")) {
				amt = this.getGame().getCurrentBoard().getAveragePrizeValue();
			}
			boolean hasSpin = s.getValue().charAt(s.getValue().length() - 1) ==
					'S';
			if (hasSpin) {
				if (amt > maxSpinValue) {
					maxSpinValue = amt;
					maxSpinSpace = s;
				}
			} else {
				if (amt > maxValue) {
					maxValue = amt;
					maxSpace = s;
				}
			}
		}
		if (maxSpace == null) {
			return maxSpinSpace;
		}
		if (maxSpinSpace == null || this.getGame().isFinalSpin()) {
			return maxSpace;
		}
		return maxSpinSpace;
	}
	
	@Override
	public boolean moneyOrLoseWhammy(int amount)
	{
		return this.getWhammies() >= 2;
	}
	
	@Override
	public Player choosePassTarget(List<Player> targets)
	{
		// Strategy: Pass to the player with the most whammies
		return targets.stream()
				.max(Comparator.comparingInt(Player::getWhammies)).orElse(null);
	}
}