package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import agents.WallStreetAgent;
import classes.Company;
import classes.Deck;
import classes.Investor;
import classes.Manager;
import classes.Market;
import classes.Market.Fluctuation;
import classes.Player;

public class MarketWindow extends JFrame {

	static Map<Market.Color, Color> ColorMap = new HashMap<>();
	static {
		HashMap<Market.Color, Color> tmp = new HashMap<>();
		tmp.put(Market.Color.Blue, Color.CYAN);
		tmp.put(Market.Color.Green, Color.GREEN);
		tmp.put(Market.Color.Red, Color.RED);
		tmp.put(Market.Color.Yellow, Color.YELLOW);

		ColorMap = (Map<classes.Market.Color, Color>) Collections.unmodifiableMap(tmp);
	}

	Market market;
	List<Manager> managers;
	
	PlayerBalanceChart<Manager> managersBalanceChart;
	PlayerBalanceChart<Investor> investorsBalanceChart;

	CompanyBalanceChart companyValueChart;


	public MarketWindow(Market market, List<Manager> managers) {
		super("Panic On Wall Street!");
		this.market = market;
		this.managers = managers;
		this.managersBalanceChart = new PlayerBalanceChart<>();
		this.investorsBalanceChart = new PlayerBalanceChart<>();
		this.companyValueChart = new CompanyBalanceChart();
		
		setSize(900, 600);
		setPreferredSize(new Dimension(900, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
	}

	public void draw() {

		// JScrollPane scrPane = new JScrollPane(container);
		//setVisible(false);
		
		JPanel container = new JPanel();

		JPanel window = new JPanel();
		
		BorderLayout borderLayout = new BorderLayout();
		
		

		window.setLayout(borderLayout);
		

		GridLayout mainGrid = new GridLayout(2, 2);
		container.setLayout(mainGrid);

		container.add(drawFluctuations());
		container.add(drawManagers());
		container.add(drawPlayersChart());
		container.add(drawCompaniesChart());
		
		
		JPanel header = new JPanel();
		JLabel roundNumber = new JLabel("Round " + Integer.toString(market.getRound()));
		roundNumber.setVerticalAlignment(JLabel.CENTER);
		header.add(roundNumber);
		
		window.add(header, BorderLayout.PAGE_START);
		window.add(container,BorderLayout.CENTER);

		//mainGrid.addLayoutComponent("something", new Label("yyyy"));
		pack();

		setContentPane(window);

		//setVisible(true);
		//setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	JTabbedPane drawPlayersChart() {
		JTabbedPane tabbedPane = new JTabbedPane();
		
		JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());
		ChartPanel CP = new ChartPanel(managersBalanceChart.get("Managers balance along time"));
		panel1.add(CP, BorderLayout.CENTER);
		tabbedPane.addTab("Managers", null, panel1);

		JComponent panel2 = new JPanel();
		panel2.setLayout(new BorderLayout());
		tabbedPane.addTab("Investors", null, panel2);
		
		ChartPanel CP2 = new ChartPanel(investorsBalanceChart.get("Investors balance along time"));
		panel2.add(CP2, BorderLayout.CENTER);
		
		
		/*
		JComponent panel3 = new JPanel();
		tabbedPane.addTab("Investors", null, panel3);
*/

		return tabbedPane;

	}

	
	JPanel drawCompaniesChart() {
		
		JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());
		ChartPanel CP = new ChartPanel(companyValueChart.get());
		panel1.add(CP, BorderLayout.CENTER);
		return panel1;

	}

	
	
	
	
	JPanel drawFluctuations() {
		JPanel panel = new JPanel(new GridLayout(market.fluctuations.size(), 1, 0, 0));
		panel.setMaximumSize(new Dimension(450, 600));
		panel.setBorder(BorderFactory.createLineBorder(Color.black));

		for (Market.Color color : market.fluctuations.keySet()) {
			panel.add(drawFluctuation(color, market.fluctuations.get(color)));
		}

		return panel;
	}

	JPanel drawFluctuation(Market.Color color, Fluctuation fluctuation) {
		JPanel fPanel = new JPanel(new GridLayout(1, fluctuation.dice.length + 1));
		fPanel.setBackground(ColorMap.get(color));

		fPanel.add(drawFluctuationDiceCell(fluctuation));
		for (int i = 0; i < fluctuation.values.length; i++) {
			fPanel.add(drawFluctuationValueCell(fluctuation, i));
		}

		return fPanel;
	}

	JPanel drawFluctuationDiceCell(Fluctuation fluctuation) {
		JPanel fcPanel = new JPanel(new GridLayout(2, (int) Math.ceil(fluctuation.dice.length), 0, 0));
		fcPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		fcPanel.setBackground(null);
		for (int i = 0; i < fluctuation.dice.length; i++) {
			JPanel wrapper = new JPanel(new GridBagLayout());
			wrapper.setBorder(BorderFactory.createLineBorder(Color.black));

			JLabel label = new JLabel(Integer.toString(fluctuation.dice[i]));
			label.setVerticalAlignment(JLabel.CENTER);
			label.setFont(label.getFont().deriveFont(16.0f));
			wrapper.setBackground(Color.WHITE);
			if (fluctuation.currentDiceIndex == i) {
				wrapper.setBackground(Color.BLACK);
				label.setForeground(Color.WHITE);
			}
			wrapper.add(label);
			fcPanel.add(wrapper);
		}

		/*
		 * if (fluctuation.currentDiceIndex >= 0) { JLabel label = new
		 * JLabel(Integer.toString(fluctuation.dice[fluctuation.currentDiceIndex
		 * ])); label.setVerticalAlignment(JLabel.CENTER);
		 * label.setFont(label.getFont().deriveFont(18.0f)); fcPanel.add(label);
		 * }
		 */
		return fcPanel;
	}

	JPanel drawFluctuationValueCell(Fluctuation fluctuation, int index) {
		JPanel fcPanel = new JPanel(new GridBagLayout());
		fcPanel.setBackground(null);
		JLabel label = new JLabel(Integer.toString(fluctuation.values[index]));
		label.setVerticalAlignment(JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(18.0f));
		if (fluctuation.currentPosition == index) {
			fcPanel.setBackground(Color.BLACK);
			label.setForeground(Color.WHITE);
			// Font font = label.getFont();
			// Map attributes = font.getAttributes();
			// attributes.put(TextAttribute.UNDERLINE,
			// TextAttribute.UNDERLINE_ON);
			// label.setFont(font.deriveFont(attributes));
		}
		fcPanel.add(label);
		return fcPanel;
	}

	JScrollPane drawManagers() {
		JPanel panel = new JPanel();
		JScrollPane scrPane = new JScrollPane(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createLineBorder(Color.black));

		for (Manager manager : managers) {
			panel.add(drawCompanies(manager));
		}

		return scrPane;
	}

	JPanel drawCompanies(Manager manager) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

		for (Company company : manager.companies) {
			panel.add(drawCompany(company));
		}

		return panel;
	}

	JPanel drawCompany(Company company) {
		JPanel panel = new JPanel(new GridLayout(2, 2));
		panel.setMaximumSize(new Dimension(200, 250));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel img = new JPanel(new GridLayout(2, 1));
		img.setMaximumSize(new Dimension(75, 100));

		img.setBackground(ColorMap.get(company.color));
		if (company.owner != null && company.owner.getComponentIdentifier() != null) {

			JLabel oLabel1 = new JLabel(company.name);
			JLabel oLabel2 = new JLabel(company.owner.getComponentIdentifier().getLocalName());
			// oLabel1.setFont(oLabel1.getFont().deriveFont(18.0f));
			img.add(oLabel1);
			img.add(oLabel2);
		}
		panel.add(img);

		JPanel x2 = new JPanel();
		x2.setMaximumSize(new Dimension(75, 100));

		x2.setBackground(ColorMap.get(company.color));
		if (company.x2) {
			JLabel x2Label = new JLabel("X2");
			x2Label.setVerticalAlignment(JLabel.CENTER);
			x2Label.setFont(x2Label.getFont().deriveFont(18.0f));
			x2.add(x2Label);
		}
		panel.add(x2);

		JPanel status = new JPanel();
		status.setMaximumSize(new Dimension(75, 100));
		status.setLayout(new BoxLayout(status, BoxLayout.PAGE_AXIS));
		status.setBackground(Color.WHITE);
		if (company.currentInvestor != null) {
			JLabel sLabel = new JLabel(company.currentInvestor.getComponentIdentifier().getLocalName());
			sLabel.setFont(sLabel.getFont().deriveFont(14.0f));
			status.add(sLabel);
		}

		if (company.closed) {
			JLabel lLabel = new JLabel("CLOSED");
			lLabel.setFont(lLabel.getFont().deriveFont(18.0f));
			status.add(lLabel);
		}
		panel.add(status);

		JPanel value = new JPanel();
		value.setMaximumSize(new Dimension(75, 100));
		value.setBackground(Color.WHITE);
		if (company.currentInvestor != null) {
			JLabel vLabel = new JLabel(Integer.toString(company.currentOffer));
			vLabel.setVerticalAlignment(JLabel.CENTER);
			vLabel.setFont(vLabel.getFont().deriveFont(32.0f));
			value.add(vLabel);
		}
		panel.add(value);

		return panel;
	}

	/*
	 * public static void main(String[] args) throws InterruptedException {
	 * Market market = new Market(); Deck deck = new Deck(); ArrayList<Manager>
	 * managers = new ArrayList<>(); managers.add(new Manager(null,
	 * deck.fetchCompanies(2))); managers.add(new Manager(null,
	 * deck.fetchCompanies(2))); managers.add(new Manager(null,
	 * deck.fetchCompanies(3))); managers.add(new Manager(null,
	 * deck.fetchCompanies(4))); managers.add(new Manager(null,
	 * deck.fetchCompanies(1)));
	 * 
	 * MarketWindow window = new MarketWindow(market, managers); window.draw();
	 * Thread.sleep(1000);
	 * 
	 * market.rollTheDices(); window.draw();
	 * 
	 * }
	 */
	public void setManagers(List<Manager> managers) {
		this.managers = managers;
	}
	
	public void storeCompanies(List<Company> companies) {
		this.companyValueChart.storeData(companies);
	}
	

	public void storePlayersBalance(List<Manager> list, List<Investor> list2) {
		this.managersBalanceChart.storeData(list);
		this.investorsBalanceChart.storeData(list2);
		
	}

	public void storeState(WallStreetAgent.GameState gameState) {
		this.managersBalanceChart.storeState(gameState);
		this.investorsBalanceChart.storeState(gameState);
		this.companyValueChart.storeState(gameState);
		
	}

}
