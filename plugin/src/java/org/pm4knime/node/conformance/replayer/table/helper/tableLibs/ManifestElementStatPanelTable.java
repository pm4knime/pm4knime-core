package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.manifestanalysis.visualization.performance.IPerfCounter;
import org.processmining.plugins.manifestanalysis.visualization.performance.TimeFormatter;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerScrollBarUI;

import info.clearthought.layout.TableLayout;

public class ManifestElementStatPanelTable<N extends ManifestEvClassPatternTable, C extends PerfCounterTable> extends JPanel {

	private static final long serialVersionUID = 3644955575441731960L;

	/**
	 * INFO PROVIDER
	 */
	private C infoProvider;
	private N manifest;

	/**
	 * GUI ELEMENTS
	 */
	// info table
	// private ProMTable table;
	private JPanel mainInfo;
	private JScrollPane scrollPane;

	// selected element in the projected visualization
	private JComboBox selection;

	// static content for GUI of statistic panel
	private Object[] columnIdentifier;
	private SlickerFactory factory;
	private NumberFormat nfDouble;
	private NumberFormat nfInteger;

	public ManifestElementStatPanelTable(N manifest, C infoProvider) {
		// import information
		this.infoProvider = infoProvider;
		this.manifest= manifest;

		// initialize utility classes
		nfDouble = NumberFormat.getInstance();
		nfDouble.setMinimumFractionDigits(2);
		nfDouble.setMaximumFractionDigits(2);

		nfInteger = NumberFormat.getInstance();
		nfInteger.setMinimumFractionDigits(0);
		nfInteger.setMaximumFractionDigits(0);

		factory = SlickerFactory.instance();

		columnIdentifier = new Object[] { "Property", "Min.", "Max.", "Avg.", "Std. Dev", "Freq." };

		Object[] items = new Object[infoProvider.getNumPlaces() + infoProvider.getNumTrans()];
		int counter = 0;
		for (Transition t : infoProvider.getTransArray()) {
			items[counter++] = t;
		}
		for (Place p : infoProvider.getPlaceArray()) {
			items[counter++] = p.getLabel();
		}

		selection = factory.createComboBox(items);
		selection.setSelectedIndex(0);
		selection.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				updateStats();
			}

		});

		// main panel
		mainInfo = new JPanel();
		mainInfo.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		mainInfo.setLayout(new BoxLayout(mainInfo, BoxLayout.Y_AXIS));
		scrollPane = new JScrollPane(mainInfo);
		scrollPane.setOpaque(true);
		scrollPane.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		scrollPane.getViewport().setOpaque(true);
		scrollPane.getViewport().setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

		JScrollBar hBar = scrollPane.getHorizontalScrollBar();
		hBar.setUI(new SlickerScrollBarUI(hBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
				WidgetColors.COLOR_NON_FOCUS, 4, 12));
		hBar.setOpaque(true);
		hBar.setBackground(WidgetColors.PROPERTIES_BACKGROUND);

		JScrollBar vBar = scrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
				WidgetColors.COLOR_NON_FOCUS, 4, 12));
		vBar.setOpaque(true);
		vBar.setBackground(WidgetColors.PROPERTIES_BACKGROUND);

		// add GUI
		double[][] size = new double[][] { { 120, 400 }, { 30, TableLayout.FILL, 30 } };
		setLayout(new TableLayout(size));
		add(factory.createLabel("Selected elements*"), "0,0,r,c");
		add(selection, "1,0");
		add(scrollPane, "0,1,1,1");
		add(new JLabel("  * Click a place/transition on the projected model to see its stats, or use combobox"),
				"0,2,1,2");

		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		updateStats();
	}

	protected void updateStats() {
		// check if its trans or places
		if (selection.getSelectedIndex() < getInfoProvider().getNumTrans()) {
			showTransStats(selection.getSelectedIndex());
		} else {
			showPlaceStats(selection.getSelectedIndex() - getInfoProvider().getNumTrans());
		}
	}

	private void showPlaceStats(int encodedPlaceID) {
		// get all patterns associated with the encodedTransID
		double[] infoNumber = infoProvider.getPlaceStats(encodedPlaceID);

		// create table with general information
		mainInfo.removeAll();
		int counterInfoNumber = 0;
		while (counterInfoNumber < infoNumber.length) {
			mainInfo.add(Box.createRigidArea(new Dimension(0, 10)));

			DefaultTableModel tableModel = new DefaultTableModel() {
				private static final long serialVersionUID = -4303950078200984098L;

				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			ProMTable table = new ProMTable(tableModel);

			Object[][] info = new Object[5][6];
			int i = 0;
			info[i++] = new Object[] { "Waiting time",
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					nfInteger.format(infoNumber[counterInfoNumber++]) };
			info[i++] = new Object[] { "Synchronization time",
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					nfInteger.format(infoNumber[counterInfoNumber++])};
			info[i++] = new Object[] { "Sojourn time",
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
					nfInteger.format(infoNumber[counterInfoNumber++])};
			tableModel.setDataVector(info, columnIdentifier);

			mainInfo.add(table);
			table.setPreferredSize(new Dimension(300, 125));
			table.doLayout();
		}
		this.selection.setSelectedIndex(encodedPlaceID + infoProvider.getNumTrans());

		mainInfo.revalidate();
		scrollPane.repaint();
		scrollPane.revalidate();
		revalidate();
	}

	private void showTransStats(int encodedTransID) {
		// get all patterns associated with the encodedTransID
		double[] infoNumber = infoProvider.getTransStats(manifest, encodedTransID);

		// create table with general information
		mainInfo.removeAll();
		if (infoNumber != null) {
			int counterInfoNumber = IPerfCounter.MULTIPLIER;
			while (counterInfoNumber < infoNumber.length) {
				mainInfo.add(Box.createRigidArea(new Dimension(0, 10)));
				mainInfo.add(factory.createLabel("Pattern : "
						+ infoProvider.getPatternString(manifest, (short) infoNumber[counterInfoNumber++])));

				DefaultTableModel tableModel = new DefaultTableModel() {
					private static final long serialVersionUID = -4303950078200984098L;

					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};
				ProMTable table = new ProMTable(tableModel);

				Object[][] info = new Object[7][6];
				int i = 0;
				info[i++] = new Object[] { "Throughput time",
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						nfInteger.format(infoNumber[counterInfoNumber + 8]) };
				info[i++] = new Object[] { "Waiting time",
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						nfInteger.format(infoNumber[counterInfoNumber + 5]) };
				info[i++] = new Object[] { "Sojourn time",
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						TimeFormatter.formatTime(infoNumber[counterInfoNumber++], nfDouble),
						nfInteger.format(infoNumber[counterInfoNumber + 2])};
				counterInfoNumber+= 3;
				info[i++] = new Object[] { "#Unique cases (throughput)", nfInteger.format(infoNumber[counterInfoNumber++]), "", "",
				"" };
				tableModel.setDataVector(info, columnIdentifier);

				mainInfo.add(table);
				table.setPreferredSize(new Dimension(300, 125));
				table.doLayout();
			}
		}
		
		// add info about move on model
		mainInfo.add(Box.createRigidArea(new Dimension(0, 10)));
		
		JLabel moveModelLbl = factory.createLabel("#Move on model : "
				+ infoProvider.getMoveModelOfTrans(encodedTransID));
		moveModelLbl.setAlignmentX(LEFT_ALIGNMENT);
		moveModelLbl.setAlignmentY(BOTTOM_ALIGNMENT);
		mainInfo.add(moveModelLbl);
		mainInfo.setAlignmentX(LEFT_ALIGNMENT);
		
		mainInfo.add(factory.createLabel("#Unique cases where move model occur: "
				+ infoProvider.getUniqueCaseMoveModelOfTrans(encodedTransID)));
		mainInfo.revalidate();
		scrollPane.repaint();
		scrollPane.revalidate();
		revalidate();

		this.selection.setSelectedIndex(encodedTransID);
	}

	private C getInfoProvider() {
		return this.infoProvider;
	}

	public void setTransition(Transition trans) {
		showTransStats(infoProvider.getEncOfTrans(trans));
	}

	public void setPlace(Place place) {
		showPlaceStats(infoProvider.getEncOfPlace(place));
	}
}
