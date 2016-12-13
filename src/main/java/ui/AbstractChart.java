package ui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;

import agents.WallStreetAgent;
import ui.AbstractChart.Pair;

public abstract class AbstractChart {

	
	static Map<WallStreetAgent.GameState, Color> ColorMap = new HashMap<>();
	static {
		HashMap<WallStreetAgent.GameState, Color> tmp = new HashMap<>();
		tmp.put(WallStreetAgent.GameState.NEGOTIATION, Color.WHITE);
		tmp.put(WallStreetAgent.GameState.EXCHANGING_INCOMES, Color.LIGHT_GRAY);
		tmp.put(WallStreetAgent.GameState.SOLVING_MANAGERS_DEBTS, Color.WHITE);
		tmp.put(WallStreetAgent.GameState.AUCTIONING_NEW_COMPANIES, Color.LIGHT_GRAY);

		ColorMap = (Map<WallStreetAgent.GameState, Color>) Collections.unmodifiableMap(tmp);
	}
	
	protected class Pair{
		double time;
		WallStreetAgent.GameState state;
		
		Pair(double time, WallStreetAgent.GameState state){
			this.state = state;
			this.time = time;
		}
	}
	
	JFreeChart xylineChart;
	protected List<Pair> gameStates;
	protected double baseTime;
	protected XYSeriesCollection dataset;

	AbstractChart(){
		baseTime = System.currentTimeMillis();
		gameStates = new ArrayList<>();
		dataset = new XYSeriesCollection();

	}
	
	void storeState(WallStreetAgent.GameState gameState){
		double elapsedTime = (System.currentTimeMillis() - baseTime) / 1000;
		gameStates.add(new Pair(elapsedTime, gameState));
	}
	
	JFreeChart get(String title, String axisX, String axisY) {
		JFreeChart chart = ChartFactory.createXYLineChart(title, axisX, axisY, dataset,
				PlotOrientation.VERTICAL, true, true, false);
        
		for(int i = 1; i < gameStates.size(); i++){
			Pair startt = gameStates.get(i-1);
			Pair finalt = gameStates.get(i);
			
			
			XYPlot plot = (XYPlot) chart.getPlot();
	        final IntervalMarker target = new IntervalMarker(startt.time, finalt.time);
	        if(startt.state.equals(WallStreetAgent.GameState.NEGOTIATION)){
		        target.setLabel(startt.state.toString().toLowerCase());
		        target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
		        target.setLabelAnchor(RectangleAnchor.CENTER);
	        }

	        target.setPaint(ColorMap.get(startt.state));
	        plot.addDomainMarker(target, Layer.BACKGROUND);
		}
		

        
		return chart;
	}
	
	
}
