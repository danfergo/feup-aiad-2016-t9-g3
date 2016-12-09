package classes;

import java.util.HashMap;
import java.util.Random;

import jadex.commons.transformation.annotations.IncludeFields;

@IncludeFields
public class Market {

	@IncludeFields
	public enum Color {
		Blue, Green, Yellow, Red
	}

	private Random rnd;

	@IncludeFields
	public class Fluctuation {

		
		public int[] dice;
		public int[] values;
		private static final int startValue = 30;
		private static final int startPosition = 3;

		int currentPosition = startPosition;

		Fluctuation(int[] values, int[] dice) {
			this.values = values;
			this.dice = dice;

		}

		int rollTheDice() {
			int i = rnd.nextInt(dice.length);
			int newPosition = currentPosition + dice[i];
			currentPosition = newPosition < 0 ? 0
					: (newPosition > (values.length - 1) ? (values.length - 1) : newPosition);
			return getCurrentValue();
		}

		int getCurrentValue() {
			return values[currentPosition];
		}
		
		int getMaxValue(){
			return values[values.length-1];
		}
		
		int getMinValue(){
			return values[0];
		}
		
	}

	public HashMap<Color, Fluctuation> fluctuations = new HashMap<>();

	public Market() {
		rnd = new Random(System.nanoTime());

		fluctuations.put(Color.Blue,
				new Fluctuation(new int[] { 20, 20, 20, 30, 30, 30, 40, 40 }, new int[] { -1, -1, 0, 0, +1, +1 }));
		
		fluctuations.put(Color.Green,
				new Fluctuation(new int[] { 20, 20, 20, 30, 30, 30, 40, 40 }, new int[] { -1, -1, 0, 0, +1, +1 }));
		
		fluctuations.put(Color.Yellow,
				new Fluctuation(new int[] { 20, 20, 20, 30, 30, 30, 40, 40 }, new int[] { -1, -1, 0, 0, +1, +1 }));
		
		fluctuations.put(Color.Red,
				new Fluctuation(new int[] { 20, 20, 20, 30, 30, 30, 40, 40 }, new int[] { -1, -1, 0, 0, +1, +1 }));
	
	}
	
	
	public void rollTheDices(){
	    for(Color color :  fluctuations.keySet()){
	    	fluctuations.get(color).rollTheDice();
	    }
	}


	public int calcCompanyValue(Company company) {
		Fluctuation fluctuation = fluctuations.get(company.color);
		
		return company.x2 ? 2*fluctuation.getCurrentValue() : fluctuation.getCurrentValue() ;
	}
	

}
