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
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import agents.WallStreetAgent;
import classes.Investor;
import classes.Market;
import classes.Player;

public class PlayerBalanceChart<T extends Player> extends AbstractChart {

	private Map<Player, XYSeries> playersSeries;

	PlayerBalanceChart() {
		playersSeries = new HashMap<>();
	}

	public XYDataset storeData(List<T> entity) {
		double elapsedTime = (System.currentTimeMillis() - baseTime) / 1000;
		for (T player : entity) {
			if (!playersSeries.containsKey(player)) {

				XYSeries series = playersSeries.put(player,
						new XYSeries(player.toString()));
				dataset.addSeries(playersSeries.get(player));
			}
			playersSeries.get(player).add(elapsedTime, player.balance);
		}

		return dataset;
	}

	JFreeChart get(String title) {
		return super.get(title, "Time (s)", "Balance ($)");
	}

}
