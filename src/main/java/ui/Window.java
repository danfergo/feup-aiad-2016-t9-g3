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
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import classes.Company;
import classes.Deck;
import classes.Manager;
import classes.Market;
import classes.Market.Fluctuation;

public class Window extends JFrame {

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
	ArrayList<Manager> managers;

	Window(Market market, ArrayList<Manager> managers) {
		super("Panic On Wall Street!");
		this.market = market;
		this.managers = managers;
	}

	void draw() {
		setContentPane(new JPanel());

		setSize(900, 600);
		setPreferredSize(new Dimension(900, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GridLayout mainGrid = new GridLayout(1, 2);
		getContentPane().setLayout(mainGrid);

		getContentPane().add(drawFluctuations());
		getContentPane().add(drawManagers());

		mainGrid.addLayoutComponent("something", new Label("yyyy"));
		pack();
		setVisible(true);
	}

	JPanel drawFluctuations() {
		JPanel panel = new JPanel(new GridLayout(market.fluctuations.size(), 1));
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
		JPanel fcPanel = new JPanel(new GridBagLayout());
		fcPanel.setBackground(null);
		if (fluctuation.currentDiceIndex >= 0) {
			JLabel label = new JLabel(Integer.toString(fluctuation.dice[fluctuation.currentDiceIndex]));
			label.setVerticalAlignment(JLabel.CENTER);
			label.setFont(label.getFont().deriveFont(18.0f));
			fcPanel.add(label);
		}
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
			//Font font = label.getFont();
			//Map attributes = font.getAttributes();
			//attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			//label.setFont(font.deriveFont(attributes));
		}
		fcPanel.add(label);
		return fcPanel;
	}

	JPanel drawManagers() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createLineBorder(Color.black));

		for (Manager manager : managers) {
			panel.add(drawCompanies(manager));
		}

		return panel;
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
		panel.setMaximumSize(new Dimension(300, 200));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel img = new JPanel();

		img.setBackground(ColorMap.get(company.color));
		if (company.owner != null && company.owner.getComponentIdentifier() != null) {
			JLabel oLabel = new JLabel(company.owner.getComponentIdentifier().getLocalName());
			img.add(oLabel);
		}
		panel.add(img);

		JPanel x2 = new JPanel();
		x2.setBackground(ColorMap.get(company.color));
		if (company.x2) {
			JLabel x2Label = new JLabel("X2");
			x2Label.setVerticalAlignment(JLabel.CENTER);
			x2Label.setFont(x2Label.getFont().deriveFont(18.0f));
			x2.add(x2Label);
		}
		panel.add(x2);

		JPanel status = new JPanel();
		status.setLayout(new BoxLayout(status, BoxLayout.PAGE_AXIS));
		status.setBackground(Color.WHITE);
		if (company.currentInvestor != null) {
			JLabel sLabel = new JLabel(company.currentInvestor.getComponentIdentifier().getLocalName());
			sLabel.setFont(sLabel.getFont().deriveFont(18.0f));
			status.add(sLabel);
		}

		if (company.closed) {
			JLabel lLabel = new JLabel("CLOSED");
			lLabel.setFont(lLabel.getFont().deriveFont(18.0f));
			status.add(lLabel);
		}
		panel.add(status);

		JPanel value = new JPanel();
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

	public static void main(String[] args) throws InterruptedException {
		Market market = new Market();
		Deck deck = new Deck();
		ArrayList<Manager> managers = new ArrayList<>();
		managers.add(new Manager(null, deck.fetchCompanies(2)));
		managers.add(new Manager(null, deck.fetchCompanies(2)));
		managers.add(new Manager(null, deck.fetchCompanies(3)));
		managers.add(new Manager(null, deck.fetchCompanies(4)));
		managers.add(new Manager(null, deck.fetchCompanies(1)));

		Window window = new Window(market, managers);
		window.draw();
		Thread.sleep(1000);

		market.rollTheDices();
		window.draw();

	}

}