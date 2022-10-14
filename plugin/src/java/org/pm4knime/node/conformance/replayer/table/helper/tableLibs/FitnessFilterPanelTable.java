package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import org.processmining.plugins.manifestanalysis.visualization.performance.PerformanceConstants;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import gnu.trove.list.array.TDoubleArrayList;

public class FitnessFilterPanelTable extends JPanel {
	private static final long serialVersionUID = -7836262838362433895L;

	@SuppressWarnings("unused")
	private FitnessFilterPanelTable() {
	};

	private JComboBox criteria = null;

	private NiceDoubleSlider minSlider;
	private NiceDoubleSlider maxSlider;
	private JPanel histogramPanel;
	private JButton filter;

	public FitnessFilterPanelTable(final PNLogReplayProjectedVisPanelTable mainPanel, final PNRepResult repResult) {
		final SlickerFactory factory = SlickerFactory.instance();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// take one sync rep result as an example
		Set<String> keySet = repResult.iterator().next().getInfo().keySet();
		if (keySet != null) {
			criteria = factory.createComboBox(keySet.toArray());
			JPanel p = new JPanel();
			p.add(factory.createLabel("Filter criteria"));
			p.add(criteria);
			p.setBackground(WidgetColors.PROPERTIES_BACKGROUND);

			criteria.setPreferredSize(new Dimension(400,30));
			criteria.setSize(300,30);
			criteria.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// re-adjust the slider bound values
					initiateAll(criteria.getSelectedItem(), factory, repResult);
				}

			});
			criteria.setBackground(WidgetColors.PROPERTIES_BACKGROUND);


			histogramPanel = new JPanel();
			histogramPanel.setBackground(WidgetColors.PROPERTIES_BACKGROUND);

			filter = factory.createButton("Filter out alignments outside of range");
			filter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Set<Integer> preservedIndex = new HashSet<Integer>();
					for (SyncReplayResult res : repResult) {
						Double val = res.getInfo().get(criteria.getSelectedItem());

						if ((Double.compare(val, minSlider.getValue()) >= 0)
								&& (Double.compare(val, maxSlider.getValue()) <= 0)) {
							preservedIndex.addAll(res.getTraceIndex());
						}
					}

					mainPanel.filterAlignmentPreserveIndex(preservedIndex);
				}
			});
			filter.setBackground(WidgetColors.PROPERTIES_BACKGROUND);

			add(histogramPanel);
			add(p);
			setBackground(WidgetColors.PROPERTIES_BACKGROUND);

			initiateAll(criteria.getSelectedItem(), factory, repResult);

		} else {
			add(factory.createLabel("No stats is available from replay result"));
		}
	}

	private void initiateAll(Object selectedItem, SlickerFactory factory, PNRepResult repResult) {
		invalidate();
		double min = Double.NaN;
		double max = Double.NaN;

		TDoubleArrayList values = new TDoubleArrayList();
		for (SyncReplayResult res : repResult) {
			Double val = res.getInfo().get(criteria.getSelectedItem());
			if (val != null) {
				if (Double.isNaN(min)) {
					min = val;
				} else {
					if (Double.compare(min, val) > 0) {
						min = val;
					}
				}

				if (Double.isNaN(max)) {
					max = val;
				} else {
					if (Double.compare(max, val) < 0) {
						max = val;
					}
				}
				double[] vals = new double[res.getTraceIndex().size()];
				Arrays.fill(vals, val);
				values.add(vals);
			}
		}

		if (minSlider != null) {
			remove(minSlider);
			remove(maxSlider);
			remove(filter);
		}
		
		minSlider = factory.createNiceDoubleSlider("Min. value", min, max, min, Orientation.HORIZONTAL);
		maxSlider = factory.createNiceDoubleSlider("Max. value", min, max, max, Orientation.HORIZONTAL);

		add(minSlider);
		add(maxSlider);
		add(filter);

		initHistogram(selectedItem.toString(), values.toArray());

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
