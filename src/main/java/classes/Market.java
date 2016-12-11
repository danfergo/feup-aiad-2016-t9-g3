package classes;

import java.util.HashMap;
import java.util.Random;

import agents.WallStreetAgent;
import jadex.commons.transformation.annotations.IncludeFields;

@IncludeFields
public class Market {

	@IncludeFields
	public enum Color {
		Blue, Green, Yellow, Red
	}

	private Random rnd;
	
	private int round;

	@IncludeFields
	public class Fluctuation {

		public int[] dice;
		public int[] values;
		private static final int startValue = 30;
		private static final int startPosition = 3;

		public int currentPosition = startPosition;
		public int currentDiceIndex = -1;

		Fluctuation(int[] values, int[] dice) {
			this.values = values;
			this.dice = dice;

		}

		int calcRevenue(int dicePosition){
			int newPosition = currentPosition + dice[dicePosition];
			return Math.max(0, Math.min(newPosition, values.length - 1));
		}
		
		int rollTheDice() {
			currentDiceIndex = rnd.nextInt(dice.length);
			currentPosition = calcRevenue(currentDiceIndex);
			return getCurrentValue();
		}

		int getCurrentValue() {
			return values[currentPosition];
		}

		int getMaxValue() {
			return values[values.length - 1];
		}

		int getMinValue() {
			return values[0];
		}

	}

	public HashMap<Color, Fluctuation> fluctuations = new HashMap<>();

	public Market() {
		rnd = new Random(System.nanoTime());
		this.round = 0;

		fluctuations.put(Color.Blue,
				new Fluctuation(new int[] { 20, 20, 20, 30, 30, 30, 40, 40 }, new int[] { -1, -1, 0, 0, +1, +1 }));

		fluctuations.put(Color.Green,
				new Fluctuation(new int[] { 0, 10, 20, 30, 30, 40, 50, 60 }, new int[] { -2, -1, 0, 0, +1, +2 }));

		fluctuations.put(Color.Yellow,
				new Fluctuation(new int[] { -10, 0, 0, 30, 40, 40, 60, 60 }, new int[] { -3, -2, -1, +1, +2, +3 }));

		fluctuations.put(Color.Red,
				new Fluctuation(new int[] { -20, -10, 0, 30, 40, 50, 60, 70 }, new int[] { -7, -3, -2, +2, +3, +7 }));

	}

	public void rollTheDices() {
		for (Color color : fluctuations.keySet()) {
			fluctuations.get(color).rollTheDice();
		}
	}

	public int calcCompanyRevenue(Company company) {
		Fluctuation fluctuation = fluctuations.get(company.color);
		return company.x2 ? 2 * fluctuation.getCurrentValue() : fluctuation.getCurrentValue();
	}

	/**
	 * balance + k*companySellValue >= 0 => k >= balance/companySellValue
	 * 
	 * @param balance
	 * @param nCompanies
	 * @return
	 */
	public static int numberOfCompaniesRequiredToSell(int balance) {
		return -1 * (int) Math.ceil(balance / (double) WallStreetAgent.valueOfCompany);
	}
	
	/**
	 * E = sum(1/dice.length * calcRevenue(i))
	 * calcRevenue = dice[i + p]; where, p is dice current position and i + p constrained to 0 .. dice.length-1.  
	 * @param company
	 * @return company next round expected value
	 */
	public float companyNextRoundExpectedRevenue(Company company){
		float E = 0;
		Fluctuation fluctuation = fluctuations.get(company.color);
		float pX =  (1/(float)fluctuation.dice.length);
		for(int i = 0; i < fluctuation.dice.length; i++){
			E += pX * fluctuation.calcRevenue(i);
		}
		return company.x2 ? 2 * E : E;
	}
	
	public void incRound() {
		this.round++;
	}
	
	public int getRound() {
		return this.round;
	}

}
