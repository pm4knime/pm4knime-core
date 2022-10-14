package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.plugins.manifestanalysis.visualization.performance.IPerfCounter;
import org.processmining.plugins.manifestanalysis.visualization.performance.ManifestPerfPanel;
import org.processmining.plugins.manifestanalysis.visualization.performance.PerformanceConstants;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class PerformanceFilterPanelTable<N extends Manifest, C extends IPerfCounter<N>> extends JPanel {
	private static final long serialVersionUID = -4864946337438444991L;

	@SuppressWarnings("unused")
	private PerformanceFilterPanelTable() {
	};

	private NiceDoubleSlider minSlider;
	private NiceDoubleSlider maxSlider;
	private JPanel histogramPanel;
	private JButton filter;

	public PerformanceFilterPanelTable(final ManifestPerfPanelTable mainPanel, final ManifestEvClassPatternTable manifest, final PerfCounterTable provider) {
		final SlickerFactory factory = SlickerFactory.instance();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// throughput time of cases
		histogramPanel = new JPanel();
		histogramPanel.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		add(histogramPanel);
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		initiateAll(mainPanel, manifest, provider, factory);
	}

	private void initiateAll(final ManifestPerfPanelTable mainPanel, ManifestEvClassPatternTable manifest, PerfCounterTable provider,
			SlickerFactory factory) {
		invalidate();
		long min = Long.MAX_VALUE;
		long max = -Long.MAX_VALUE;

		final long[] temp = provider.getCaseThroughputTime();
		for (long throughputTime : temp) {
			if (throughputTime > max) {
				max = throughputTime;
			}
			if (throughputTime < min) {
				min = throughputTime;
			}
		}

		// choose the appropriate time unit
		final String timeUnit;
		final double divider;
		if (min < 1000L) {
			timeUnit = "ms";
			divider = 1;
		} else if (min < 60 * 1000L) {
			timeUnit = "seconds";
			divider = 1000;
		} else if (min < 60 * 60 * 1000L) {
			timeUnit = "minutes";
			divider = 1000 * 60;
		} else if (min < 24 * 60 * 60 * 1000L) {
			timeUnit = "hours";
			divider = 1000 * 60 * 60;
		} else if (min < 30 * 24 * 60 * 60 * 1000L) {
			timeUnit = "days";
			divider = 1000 * 60 * 60 * 24;
		} else if (min < 12 * 30 * 24 * 60 * 60 * 1000L) {
			timeUnit = "months";
			divider = 1000 * 60 * 60 * 24 * 30;
		} else {
			timeUnit = "years";
			divider = 1000 * 60 * 60 * 24 * 30 * 12;
		}

		double[] throughputTimes = new double[temp.length];
		for (int i = 0; i < temp.length; i++) {
			throughputTimes[i] = temp[i] / divider;
		}
		initHistogram("Case throughput time (" + timeUnit + ")", throughputTimes);

		double minDivided = min / divider;
		double maxDivided = max / divider;
		minSlider = factory.createNiceDoubleSlider("Min. value (" + timeUnit + ")", minDivided, maxDivided, minDivided,
				Orientation.HORIZONTAL);
		maxSlider = factory.createNiceDoubleSlider("Max. value (" + timeUnit + ")", minDivided, maxDivided, maxDivided,
				Orientation.HORIZONTAL);
		filter = factory.createButton("Filter out cases outside of range");

		filter.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Set<Integer> setSelectedIndices = new HashSet<Integer>();
				for (int i = 0; i < temp.length; i++) {
					if ((temp[i] >= minSlider.getValue() * divider) && (temp[i] <= maxSlider.getValue() * divider)) {
						setSelectedIndices.add(i);
					}
				}
				mainPanel.filterAlignmentPreserveIndex(mainPanel.getViewSpecificAttributeMap(), setSelectedIndices);
			}
		});

		add(minSlider);
		add(maxSlider);
		add(filter);

		revalidate();
		repaint();
	}

	public void initHistogram(String selectedItem, double[] values) {
		histogramPanel.invalidate();

		int number = 100;

		HistogramDataset dataset = new HistogramDataset();
		dataset.setType(HistogramType.FREQUENCY);
		dataset.addSeries(selectedItem, values, number);
		String plotTitle = "Value Distribution for Unfiltered Alignments";
		String xaxis = selectedItem;
		String yaxis = "Number of Cases";
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		boolean show = false;
		boolean toolTips = false;
		boolean urls = false;
		JFreeChart chart = ChartFactory.createHistogram(plotTitle, xaxis, yaxis, dataset, orientation, show, toolTips,
				urls);
		chart.setBackgroundPaint(WidgetColors.PROPERTIES_BACKGROUND);
		chart.getPlot().setBackgroundPaint(Color.BLACK);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYBarRenderer.setDefaultBarPainter(new StandardXYBarPainter());
		XYBarRenderer.setDefaultShadowsVisible(false);
		XYBarRenderer renderer = new XYBarRenderer();
		renderer.setShadowVisible(false);
		renderer.setBaseFillPaint(Color.WHITE);
		renderer.setSeriesPaint(0, PerformanceConstants.BAD);
		plot.setRenderer(renderer);

		BufferedImage image = chart.createBufferedImage(500, 300);
		JLabel lblChart = new JLabel();
		lblChart.setIcon(new ImageIcon(image));

		lblChart.setBackground(WidgetColors.PROPERTIES_BACKGROUND);

		histogramPanel.removeAll();
		histogramPanel.add(lblChart);
		histogramPanel.revalidate();
	}
}
