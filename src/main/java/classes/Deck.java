package classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jadex.commons.transformation.annotations.IncludeFields;

@IncludeFields
public class Deck {

	private static int idGenerator = 1;
	ArrayList<Company> companies = new ArrayList<>();

	private static ArrayList<Company> generateCompanies(int quantity, Market.Color color, boolean x2) {
		ArrayList<Company> companies = new ArrayList<>();
		for (int i = 0; i < quantity; i++) {
			companies.add(new Company(color, x2, idGenerator++));
		}
		return companies;
	}

	public Deck() {

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

	public ArrayList<Company> fetchCompanies(int number) {
		ArrayList<Company> companies = new ArrayList<>();
		for (int i = 0; i < Math.min(this.companies.size(), number); i++) {
			companies.add(this.companies.remove(0));
		}
		return companies;
	}

	public void addCompany(Company company) {
		company.owner = null;
		company.currentInvestor = null;
		company.currentOffer = 0;
		Collections.shuffle(companies);
	}

	public void addCompanies(List<Company> companies) {
		for (Company company : companies) {
			company.owner = null;
			company.currentInvestor = null;
			company.currentOffer = 0;
		}
		this.companies.addAll(companies);
		Collections.shuffle(this.companies);
	}

}
