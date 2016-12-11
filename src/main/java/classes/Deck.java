package classes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jadex.commons.transformation.annotations.IncludeFields;

@IncludeFields
public class Deck {

	private static int idGenerator = 1;
	ArrayList<Company> companies = new ArrayList<>();
	private static ArrayList<String> companies_names = new ArrayList<>();;

	private static ArrayList<Company> generateCompanies(int quantity, Market.Color color, boolean x2) {
		ArrayList<Company> companies = new ArrayList<>();
		for (int i = 0; i < quantity; i++) {
			//System.out.println("COMPANY: " + companies_names.get(0));
			companies.add(new Company(color, x2, idGenerator++,companies_names.get(0)));
			companies_names.remove(companies_names.get(0));
		}
		return companies;
	}

	public Deck() {
		
		loadCompaniesNames();

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

	private void loadCompaniesNames() {
		
		try(BufferedReader br = new BufferedReader(new FileReader("company_names.txt"))) {
		    String line = br.readLine();

		    while (line != null) {
		       companies_names.add(line);
		       line = br.readLine();
		    }
		    
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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
