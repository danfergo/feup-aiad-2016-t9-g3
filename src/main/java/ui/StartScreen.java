package ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.GridLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.event.ActionEvent;
//import com.jgoodies.forms.layout.FormLayout;
//import com.jgoodies.forms.layout.ColumnSpec;
//import com.jgoodies.forms.layout.RowSpec;
//import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.FlowLayout;

public class StartScreen extends JFrame implements ActionListener{


	private int nump;
	private HashMap<String, Integer> managers;
	private HashMap<String, Integer> investors;
	private ArrayList<String> player_names;
	private boolean done;
	
	private void loadPlayerNames() {
		
		player_names = new ArrayList<>();
		
		try(BufferedReader br = new BufferedReader(new FileReader("names.txt"))) {
		    String line = br.readLine();

		    while (line != null) {
		    	player_names.add(line);
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

	public int getNump() {
		return nump;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		StartScreen window = new StartScreen();
		window.draw();
	}


	public StartScreen() {
		this.nump = 5;
		loadPlayerNames();
		done = false;
		
		
	}




	public boolean isDone() {
		return done;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void draw() {
		
		
		managers = new HashMap<>();
		investors = new HashMap<>();
	
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JLabel Title = new JLabel("Panic on Wall Street");
		Title.setFont(new Font("Arial Black", Font.PLAIN, 20));
		Title.setForeground(Color.RED);
		Title.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(Title, BorderLayout.NORTH);

		JPanel MainPan = new JPanel();
		getContentPane().add(MainPan, BorderLayout.CENTER);
		MainPan.setLayout(new BorderLayout(0, 0));



		JPanel NumPlayers = new JPanel();
		MainPan.add(NumPlayers, BorderLayout.NORTH);

		JLabel numPlayersLabel = new JLabel("Number of Players");
		NumPlayers.add(numPlayersLabel);
		JComboBox numPlayersCB = new JComboBox();

		numPlayersCB.setModel(new DefaultComboBoxModel(new String[] {"5", "6", "7", "8", "9", "10", "11"}));
		NumPlayers.add(numPlayersCB);
		numPlayersCB.setToolTipText("");

		JPanel panel_2 = new JPanel();
		MainPan.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new GridLayout(0, 2, 0, 0));
		
		//MANAGERS
		JPanel ManagerPan = new JPanel();
		panel_2.add(ManagerPan);
		ManagerPan.setLayout(new BorderLayout(0, 0));

		JLabel ManagerGroupLabel = new JLabel("Managers");
		ManagerGroupLabel.setForeground(Color.GREEN);
		ManagerPan.add(ManagerGroupLabel, BorderLayout.NORTH);
		ManagerGroupLabel.setHorizontalAlignment(SwingConstants.CENTER);
		ManagerGroupLabel.setVerticalAlignment(SwingConstants.TOP);

		JPanel ManagerInsidePanel = new JPanel();
		ManagerPan.add(ManagerInsidePanel, BorderLayout.CENTER);
		ManagerInsidePanel.setLayout(new GridLayout(0, 1, 0, 0));

		for(int i = 0; i < nump/2; i++) {
			
			String playername = player_names.get(i);
			JPanel panel_1 = addPlayer(playername,"Manager");
			ManagerInsidePanel.add(panel_1);
			managers.put(playername, 0);
		}
		
		//INVESTORS
		JPanel InvestorPan = new JPanel();
		panel_2.add(InvestorPan);
		InvestorPan.setLayout(new BorderLayout(0, 0));

		JLabel InvestorGroupLabel = new JLabel("Investor");
		InvestorGroupLabel.setForeground(Color.CYAN);
		InvestorPan.add(InvestorGroupLabel, BorderLayout.NORTH);
		InvestorGroupLabel.setHorizontalAlignment(SwingConstants.CENTER);
		InvestorGroupLabel.setVerticalAlignment(SwingConstants.TOP);

		JPanel InvestorInsidePanel = new JPanel();
		InvestorPan.add(InvestorInsidePanel, BorderLayout.CENTER);
		InvestorInsidePanel.setLayout(new GridLayout(0, 1, 0, 0));

		for(int i = nump/2; i < nump; i++) {
			
			String playername = player_names.get(i);
			JPanel panel_1 = addPlayer(playername,"Investor");
			InvestorInsidePanel.add(panel_1);
			investors.put(playername, 0);
		}
		
		

		numPlayersCB.setActionCommand("numPlayers");
		numPlayersCB.addActionListener(this);

		JButton btnNewButton = new JButton("Start");
		btnNewButton.setActionCommand("button");
		btnNewButton.addActionListener(this);
		getContentPane().add(btnNewButton, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}
	
	private JPanel addPlayer(String player,String type) {
		JPanel panel = new JPanel();
		JLabel lblNewLabel = new JLabel(player);
		panel.add(lblNewLabel);

		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Stupid", "Inteligent"}));
		comboBox.setActionCommand(player + " " + type);
		comboBox.addActionListener(this);
		panel.add(comboBox);
		
		
		
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("numPlayers")) {
			JComboBox cb = (JComboBox)e.getSource();
			String numplayers = (String)cb.getSelectedItem();
			int np = Integer.parseInt(numplayers);
			this.nump = np;
			this.getContentPane().removeAll();
			draw();

		}
		else if(e.getActionCommand().equals("button")) {
			this.done = true;
			setVisible(false);
			//dispose();

		}
		else {
			JComboBox cb = (JComboBox)e.getSource();
			String[] ss = cb.getActionCommand().split(" ");
			String type = (String)cb.getSelectedItem();
			int intType = 0;
			if(type.equals("Stupid")){
				intType = 0;
				
			}
			else if(type.equals("Inteligent")){
				intType = 1;
			}
			if(ss[1].equals("Manager"))
				managers.put(ss[0], intType);
			else if(ss[1].equals("Investor"))
				investors.put(ss[0], intType);
			
		}

	}

	public HashMap<String, Integer> getManagers() {
		return managers;
	}

	public HashMap<String, Integer> getInvestors() {
		return investors;
	}
}
