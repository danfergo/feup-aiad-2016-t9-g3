package classes;

import java.util.ArrayList;
import java.util.Collections;

import jadex.commons.transformation.annotations.IncludeFields;

@IncludeFields
public class Deck {

	private static int idGenerator = 1;
	Market market;
	ArrayList<Company> companies = new ArrayList<>();

	private static ArrayList<Company> generateCompanies(int quantity, Market.Color color, boolean x2) {
		ArrayList<Company> companies = new ArrayList<>();
		for (int i = 0; i < quantity; i++) {
			companies.add(new Company(color, x2, idGenerator++));
		}
		return companies;
	}

	public Deck(Market market) {
		this.market = market;

		companies.addAll(generateCompanies(11, Market.Color.Blue, false));
		companies.addAll(generateCompanies(11, Market.Color.Yellow, false));
		companies.addAll(generateCompanies(11, Market.Color.Green, false));
		companies.addAll(generateCompanies(11, Market.Color.Red, false));

		companies.addAll(generateCompanies(2, Market.Color.Blue, true));
		companies.addAll(generateCompanies(2, Market.Color.Yellow, true));
		companies.addAll(generateCompanies(2, Market.Color.Green, true));
		companies.addAll(generateCompanies(1, Market.Color.Red, true));
		
		Collections.shuffle(companies);
		
	}

	public ArrayList<Company> fetchCompanies(int number){
		ArrayList<Company> companies = new ArrayList<>();
		for(int i = 0; i < number; i++){
			companies.add(this.companies.remove(0));
		}
		return companies;
	}
	
	
}
