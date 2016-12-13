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

public class CompanyBalanceChart extends AbstractChart {
	JFreeChart xylineChart;
	private Map<Integer, XYSeries> companySeries;

	CompanyBalanceChart() {
		super();
		
		companySeries = new HashMap<>();

	}

	public XYDataset storeData(List<Company> companies) {
		double elapsedTime = (System.currentTimeMillis() - baseTime) / 1000;
		for (Company company : companies) {
			if (!companySeries.containsKey(company.id)) {
				
				companySeries.put(company.id, new XYSeries(company.name) );
				dataset.addSeries(companySeries.get(company.id));
			}
			companySeries.get(company.id).add(elapsedTime, company.currentOffer);
		}

		return dataset;
	}

	JFreeChart get() {
		return super.get("Company public value", "Time (s)", "Value ($)");
	}
}
