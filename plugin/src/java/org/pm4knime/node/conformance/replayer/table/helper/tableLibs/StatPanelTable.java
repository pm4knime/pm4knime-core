package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;

public class StatPanelTable extends JPanel {
	private static final long serialVersionUID = 7406115693401336511L;

	/**
	 * INFO PROVIDER
	 */
	private CoreInfoProviderTable infoProvider;

	/**
	 * GUI ELEMENTS
	 */
	// info table
	protected ProMTable table;

	// selected transition in the projected visualization
	protected JComboBox selection;

	// statistic content
	protected DefaultTableModel tableModel;

	// static content for GUI of statistic panel
	protected Object[] columnIdentifier;
	protected Vector<String> columnIdentifierV4;

	// store which place also include in marking in which 
	// move on log occur
	protected boolean[] involvedPlaceFlags;

	public StatPanelTable(CoreInfoProviderTable infoProvider) {

		this.infoProvider = infoProvider;
		this.involvedPlaceFlags = new boolean[infoProvider.getNumPlaces()];
		Arrays.fill(involvedPlaceFlags, false);

		SlickerFactory factory = SlickerFactory.instance();

		columnIdentifier = new Object[] { "Property", "Value" };
		columnIdentifierV4 = new Vector<String>(4);
		columnIdentifierV4.add("Marking");
		columnIdentifierV4.add("Move on Log");
		columnIdentifierV4.add("# (Freq)");
		columnIdentifierV4.add("#Traces");

		tableModel = new DefaultTableModel() {
			private static final long serialVersionUID = -4303950078200984098L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new ProMTable(tableModel);

		Object[] items = createComboItems(infoProvider);

		selection = factory.createComboBox(items);
		selection.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				updateStats();
			}

		});
		selection.setSelectedIndex(0);

		// add GUI
		double[][] size = new double[][] { { 120, 400 }, { 30, TableLayout.FILL, 30 } };
		setLayout(new TableLayout(size));
		add(factory.createLabel("Selected elements*"), "0,0,r,c");
		add(selection, "1,0");
		add(table, "0,1,1,1");
		add(new JLabel("  * Click a place/transition on the projected model to see its stats, or use combobox"),
				"0,2,1,2");
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		showAllStats();
	}

	protected Object[] createComboItems(CoreInfoProviderTable infoProvider) {
		Object[] items = new Object[infoProvider.getNumPlaces() + infoProvider.getNumTrans() + 1];
		items[0] = "All elements, aggregated";
		int counter = 1;
		for (Transition t : infoProvider.getTransArray()) {
			items[counter++] = t;
		}
		for (Place p : infoProvider.getPlaceArray()) {
			items[counter++] = p.getLabel();
		}
		return items;
	}

	protected void updateStats() {
		if (selection.getSelectedIndex() == 0) {
			showAllStats();
		} else {
			// check if its trans or places
			if (selection.getSelectedIndex() <= getInfoProvider().getNumTrans()) {
				showTransStats(selection.getSelectedIndex() - 1);
			} else {
				showPlaceStats(selection.getSelectedIndex() - 1);
			}
		}
	}

	protected CoreInfoProviderTable getInfoProvider() {
		return infoProvider;
	}

	protected void showTransStats(int selectedIndex) {
		// reset involved place flags
		Arrays.fill(involvedPlaceFlags, false);

		int[] infoNumber = getInfoProvider().getInfoNode(selectedIndex);

		Object[][] info; // result

		info = new Object[6][2];
		info[0] = new Object[] { "#Move log+model (total)", infoNumber[0] };
		info[1] = new Object[] { "#Move log+model (in 100% fitting traces)", infoNumber[1] };
		info[2] = new Object[] { "#Traces where move log+model occur", infoNumber[2] };
		info[3] = new Object[] { ' ', ' ' };
		info[4] = new Object[] { "#Move model only (in all traces)", infoNumber[3] };
		info[5] = new Object[] { "#Traces where move model only occur", infoNumber[4] };

		tableModel.setDataVector(info, columnIdentifier);
		table.doLayout();
	}

	protected void showPlaceStats(int selectedIndex) {
		// reset involved place flags
		Arrays.fill(involvedPlaceFlags, false);

		// marking ids
		int[] infoNumber = getInfoProvider().getInfoNode(selectedIndex);

		// for each marking, obtain stats
		Place[] places = getInfoProvider().getPlaceArray();
		String[] evClass = getInfoProvider().getEvClassArray();

		Vector<Vector<Object>> dataVector = new Vector<Vector<Object>>();

		for (int i : infoNumber) {
			String marking = "[";
			String limiter = "";

			// info marking : places | total freq ev class | total unique trace
			int[] infoMarking = getInfoProvider().getInfoMarking(i);
			for (int j = 0; j < getInfoProvider().getNumPlaces(); j++) {
				// all places
				if (infoMarking[j] > 0) {
					marking += limiter + places[j].getLabel();
					limiter = ",";
					involvedPlaceFlags[j] = true;
				}
			}
			marking += "]";

			boolean markingPrinted = false;
			for (int j = 0; j < evClass.length; j++) {
				if (infoMarking[places.length + j] > 0) {
					Vector<Object> rowVector = new Vector<Object>(4);
					if (!markingPrinted) {
						markingPrinted = true;
						rowVector.add(marking);
					} else {
						rowVector.add(' ');
					}
					rowVector.add(evClass[j]);
					rowVector.add(infoMarking[places.length + j]);
					rowVector.add(infoMarking[places.length + evClass.length + j]);
					dataVector.add(rowVector);
				}
			}

		}

		tableModel.setDataVector(dataVector, columnIdentifierV4);
		table.doLayout();
	}

	protected void showAllStats() {
		int[] infoNumber = infoProvider.getAllStats();

		// create table with general information
		Object[][] info = new Object[5][2];
		int propCounter = 0;
		info[propCounter++] = new Object[] { "#Move log+model", infoNumber[0] };
		info[propCounter++] = new Object[] { "#Move model only", infoNumber[1] };
		info[propCounter++] = new Object[] { "#Move log only", infoNumber[2] };
		info[propCounter++] = new Object[] { "#Traces", infoNumber[3] };
		info[propCounter++] = new Object[] { "#Unreliable Traces", infoNumber[4] };

		tableModel.setDataVector(info, columnIdentifier);
		table.doLayout();
	}

	/**
	 * Show stats for a transition, assuming the transition exists in
	 * infoProvider
	 * 
	 * @param t
	 */
	public void setTransition(Transition t) {
		selection.setSelectedIndex(getInfoProvider().getIndexOf(t) + 1);
	}

	public void setPlace(Place p) {
		selection.setSelectedIndex(getInfoProvider().getPlaceIndexOf(p) + getInfoProvider().getNumTrans() + 1);
	}

	public boolean[] getInvolvedPlaces() {
		return involvedPlaceFlags;
	}

	public void setInfoProvider(CoreInfoProviderTable infoProvider) {
		this.infoProvider = infoProvider;
		updateStats();
	}

}
