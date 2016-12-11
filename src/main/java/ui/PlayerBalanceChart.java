package ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import classes.Player;

public class PlayerBalanceChart {

	JFreeChart xylineChart;
	double baseTime;

	private XYSeriesCollection dataset;
	private Map<Player, XYSeries> playersSeries;

	PlayerBalanceChart() {
		playersSeries = new HashMap<>();
		baseTime = System.currentTimeMillis();
		dataset = new XYSeriesCollection();

		final XYSeriesCollection dataset = new XYSeriesCollection();
	}

	public XYDataset storeData(List<Player> players) {
		double elapsedTime = (System.currentTimeMillis() - baseTime) / 1000;
		for (Player player : players) {
			if (!playersSeries.containsKey(player)) {
				XYSeries series = playersSeries.put(player,
						new XYSeries(player.getComponentIdentifier().getLocalName()));
				dataset.addSeries(playersSeries.get(player));
			}
			playersSeries.get(player).add(elapsedTime, player.balance);
		}

		return dataset;
	}

	JFreeChart get() {
		return ChartFactory.createXYLineChart("Players balance along time", "Time (s)", "Balance ($)", dataset,
				PlotOrientation.VERTICAL, true, true, false);
	}

}
