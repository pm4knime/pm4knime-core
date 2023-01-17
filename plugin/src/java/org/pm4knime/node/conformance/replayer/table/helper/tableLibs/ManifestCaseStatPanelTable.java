package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.Dimension;
import java.text.NumberFormat;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.plugins.manifestanalysis.visualization.performance.TimeFormatter;

public class ManifestCaseStatPanelTable <N extends ManifestTable, C extends PerfCounterTable> extends JPanel {
	private static final long serialVersionUID = -4288145140047400367L;

	/**
	 * GUI ELEMENTS
	 */
	// info table
	private ProMTable table;

	// statistic content
	private DefaultTableModel tableModel;

	// static content for GUI of statistic panel
	private Object[] columnIdentifier;

	public ManifestCaseStatPanelTable(C provider) {
		columnIdentifier = new Object[] { "Case Property", "Value" };

		tableModel = new DefaultTableModel() {
			private static final long serialVersionUID = -4303950078200984098L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new ProMTable(tableModel);
		Dimension prefDimension = new Dimension(450,200);
		table.setPreferredSize(prefDimension);
		table.setMinimumSize(prefDimension);
		table.setMaximumSize(prefDimension);

		// add GUI
		add(table);
		showAllStats(provider);
		
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
	}

	private void showAllStats(C infoProvider) {
		// format 
		NumberFormat nfDouble = NumberFormat.getInstance();
		nfDouble.setMinimumFractionDigits(2);
		nfDouble.setMaximumFractionDigits(2);
		
		// create table with general information
		Object[][] info = new Object[9][3];
		int propCounter = 0;
		info[propCounter++] = new Object[] { "#Cases", infoProvider.getCaseTotalFreq() > 0 ? infoProvider.getCaseTotalFreq() : "-"};
		info[propCounter++] = new Object[] { "#Perfectly-fitting cases", infoProvider.getCaseTotalFreq() > 0 ? infoProvider.getCaseTotalFreq() - infoProvider.getCaseNonFittingFreq() : "-"};
		info[propCounter++] = new Object[] { "#Non-fitting cases", infoProvider.getCaseTotalFreq() > 0 ? infoProvider.getCaseNonFittingFreq() : "-"};
		info[propCounter++] = new Object[] { "#Properly started cases", infoProvider.getCaseTotalFreq() > 0 ? infoProvider.getCaseProperlyStartedFreq() : "-"};
		info[propCounter++] = new Object[] { "Case Throughput time (avg)", infoProvider.getCaseTotalFreq() > 0 ? TimeFormatter.formatTime(infoProvider.getCaseThroughputAvg(), nfDouble) : "-"};
		info[propCounter++] = new Object[] { "Case Throughput time (min)", infoProvider.getCaseTotalFreq() > 0 ? TimeFormatter.formatTime(infoProvider.getCaseThroughputMin(), nfDouble) : "-"};
		info[propCounter++] = new Object[] { "Case Throughput time (max)", infoProvider.getCaseTotalFreq() > 0 ? TimeFormatter.formatTime(infoProvider.getCaseThroughputMax(), nfDouble) : "-" };
		info[propCounter++] = new Object[] { "Case Throughput time (std. dev)", infoProvider.getCaseTotalFreq() > 0 ? TimeFormatter.formatTime(infoProvider.getCaseThroughputStdDev(), nfDouble) : "-" };
		info[propCounter++] = new Object[] { "Observation period", infoProvider.getCaseTotalFreq() > 0 ? TimeFormatter.formatTime(infoProvider.getCasePeriod(), nfDouble) : "-" };
		
		tableModel.setDataVector(info, columnIdentifier);
		table.doLayout();
	}

	public void setInfoProvider(C infoProvider) {
		showAllStats(infoProvider);
	}

}
