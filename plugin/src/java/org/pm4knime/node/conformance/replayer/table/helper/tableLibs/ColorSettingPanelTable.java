package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ManifestPerfPanelTable.GraphHighlightObject;
import org.processmining.framework.util.ui.widgets.WidgetColors;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;

public class ColorSettingPanelTable<N extends ManifestTable, C extends PerfCounterTable> extends JPanel {
	private static final long serialVersionUID = -154023208885889250L;

	/**
	 * Pointer to parent
	 */
	private JComboBox transitionColor;
	private JComboBox transitionSize;
	private JComboBox transitionBorderwidth;

	private JComboBox placeColor;
	private JComboBox placeSize;
	private JComboBox placeBorderwidth;

	private JComboBox arcColor;
	private JComboBox arcWidth;

	public ColorSettingPanelTable(final ManifestPerfPanelTable manifestPerfPanel) {
		// transition stats
		Object[] transStats = new Object[] { ManifestPerfPanelTable.NONE, 
				ManifestPerfPanelTable.TRANS_THROUGHPUT_TIME_MIN,
				ManifestPerfPanelTable.TRANS_THROUGHPUT_TIME_MAX, 
				ManifestPerfPanelTable.TRANS_THROUGHPUT_TIME_AVG,
				//ManifestPerfPanelTable.TRANS_THROUGHPUT_TIME_STDDEV, 
				ManifestPerfPanelTable.TRANS_WAITING_TIME_MIN, 
				ManifestPerfPanelTable.TRANS_WAITING_TIME_MAX, 
				ManifestPerfPanelTable.TRANS_WAITING_TIME_AVG, 
				//ManifestPerfPanelTable.TRANS_WAITING_TIME_STDDEV,
				ManifestPerfPanelTable.TRANS_SOJOURN_TIME_MIN, 
				ManifestPerfPanelTable.TRANS_SOJOURN_TIME_MAX, 
				ManifestPerfPanelTable.TRANS_SOJOURN_TIME_AVG, 
				//ManifestPerfPanelTable.TRANS_SOJOURN_TIME_STDDEV, 
				ManifestPerfPanelTable.TRANS_FREQUENCY, 
				//ManifestPerfPanelTable.TRANS_UNIQUECASES 
				};

		// place related stats
		Object[] placeStats = new Object[] {ManifestPerfPanelTable.NONE, 
				ManifestPerfPanelTable.PLACE_WAITING_TIME_MIN,
				ManifestPerfPanelTable.PLACE_WAITING_TIME_MAX, 
				ManifestPerfPanelTable.PLACE_WAITING_TIME_AVG, 
				ManifestPerfPanelTable.PLACE_WAITING_TIME_STDDEV,
				ManifestPerfPanelTable.PLACE_SYNC_TIME_MIN, 
				ManifestPerfPanelTable.PLACE_SYNC_TIME_MAX, 
				ManifestPerfPanelTable.PLACE_SYNC_TIME_AVG, 
				ManifestPerfPanelTable.PLACE_SYNC_TIME_STDDEV, 
				ManifestPerfPanelTable.PLACE_SOJOURN_TIME_MIN, 
				ManifestPerfPanelTable.PLACE_SOJOURN_TIME_MAX,
				ManifestPerfPanelTable.PLACE_SOJOURN_TIME_AVG, 
				ManifestPerfPanelTable.PLACE_SOJOURN_TIME_STDDEV, 
				ManifestPerfPanelTable.PLACE_FREQUENCY 
				};

		// arc related stats
		Object[] arcStats = new Object[] { ManifestPerfPanelTable.NONE, ManifestPerfPanelTable.ARC_FREQUENCY };

		SlickerFactory factory = SlickerFactory.instance();
		
		transitionColor = factory.createComboBox(transStats);
		transitionColor.setSelectedItem(ManifestPerfPanelTable.TRANS_SOJOURN_TIME_AVG);
		transitionColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(manifestPerfPanel.getViewSpecificAttributeMap(), GraphHighlightObject.TRANSITIONCOLOR, transitionColor.getSelectedItem().toString());
			}
		});
		
		transitionSize = factory.createComboBox(transStats);
		transitionSize.setSelectedItem(ManifestPerfPanelTable.NONE);
		transitionSize.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(manifestPerfPanel.getViewSpecificAttributeMap(),GraphHighlightObject.TRANSITIONSIZE, transitionSize.getSelectedItem().toString());
			}
		});
		
		transitionBorderwidth = factory.createComboBox(transStats);
		transitionBorderwidth.setSelectedItem(ManifestPerfPanelTable.TRANS_FREQUENCY);
		transitionBorderwidth.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(manifestPerfPanel.getViewSpecificAttributeMap(),GraphHighlightObject.TRANSITIONWIDTH, transitionBorderwidth.getSelectedItem().toString());
			}
		});

		placeColor = factory.createComboBox(placeStats);
		placeColor.setSelectedItem(ManifestPerfPanelTable.PLACE_WAITING_TIME_AVG);
		placeColor.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(manifestPerfPanel.getViewSpecificAttributeMap(),GraphHighlightObject.PLACECOLOR, placeColor.getSelectedItem().toString());
			}
		});
		
		placeSize = factory.createComboBox(placeStats);
		placeSize.setSelectedItem(ManifestPerfPanelTable.NONE);
		placeSize.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(manifestPerfPanel.getViewSpecificAttributeMap(),GraphHighlightObject.PLACESIZE, placeSize.getSelectedItem().toString());
			}
		});
		
		placeBorderwidth = factory.createComboBox(placeStats);
		placeBorderwidth.setSelectedItem(ManifestPerfPanelTable.PLACE_FREQUENCY);
		placeBorderwidth.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(manifestPerfPanel.getViewSpecificAttributeMap(),GraphHighlightObject.PLACEWIDTH, placeBorderwidth.getSelectedItem().toString());
			}
		});

		arcColor = factory.createComboBox(arcStats);
		arcColor.setSelectedItem(ManifestPerfPanelTable.ARC_FREQUENCY);
		arcColor.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(manifestPerfPanel.getViewSpecificAttributeMap(),GraphHighlightObject.ARCCOLOR, arcColor.getSelectedItem().toString());
			}
		});
		
		arcWidth = factory.createComboBox(arcStats);
		arcWidth.setSelectedItem(ManifestPerfPanelTable.ARC_FREQUENCY);
		arcWidth.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(manifestPerfPanel.getViewSpecificAttributeMap(),GraphHighlightObject.ARCWIDTH, arcWidth.getSelectedItem().toString());
			}
		});

		double[][] size = new double[][] { { .4, .5 }, { 25, 25, 25, 25, 25, 25, 25, 25 } };
		setLayout(new TableLayout(size));
		add(factory.createLabel("Transition Color"), "0,0");
		add(transitionColor, "1,0");
		add(factory.createLabel("Transition Size"), "0,1");
		add(transitionSize, "1,1");
		add(factory.createLabel("Transition Border Width"), "0,2");
		add(transitionBorderwidth, "1,2");
		add(factory.createLabel("Place Color"), "0,3");
		add(placeColor, "1,3");
		add(factory.createLabel("Place Size"), "0,4");
		add(placeSize, "1,4");
		add(factory.createLabel("Place Border Width"), "0,5");
		add(placeBorderwidth, "1,5");
		add(factory.createLabel("Arc color"), "0,6");
		add(arcColor, "1,6");
		add(factory.createLabel("Arc Width"), "0,7");
		add(arcWidth, "1,7");
		
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
	}
}