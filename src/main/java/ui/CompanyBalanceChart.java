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

import classes.Company;
import classes.Player;

public class CompanyBalanceChart {
	JFreeChart xylineChart;
	double baseTime;

	private XYSeriesCollection dataset;
	private Map<Company, XYSeries> companySeries;

	CompanyBalanceChart() {
		companySeries = new HashMap<>();
		baseTime = System.currentTimeMillis();
		dataset = new XYSeriesCollection();

		final XYSeriesCollection dataset = new XYSeriesCollection();
	}

	public XYDataset storeData(List<Company> companies) {
		double elapsedTime = (System.currentTimeMillis() - baseTime) / 1000;
		for (Company company : companies) {
			if (!companySeries.containsKey(company)) {
				companySeries.put(company, new XYSeries(company.name));
				dataset.addSeries(companySeries.get(company));
			}
			companySeries.get(company).add(elapsedTime, company.currentOffer);
		}

		return dataset;
	}

	JFreeChart get() {
		return ChartFactory.createXYLineChart("Company public value", "Time (s)", "Value ($)", dataset,
				PlotOrientation.VERTICAL, true, true, false);
	}
}
