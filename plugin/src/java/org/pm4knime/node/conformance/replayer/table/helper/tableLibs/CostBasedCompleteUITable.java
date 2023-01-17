package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;



public class CostBasedCompleteUITable extends JComponent {
	private static final long serialVersionUID = -1764028439791226196L;

	// default value 
	private static final int DEFCOSTMOVEONLOG = 1;
	private static final int DEFCOSTMOVEONMODEL = 1;
	private static final int MAXLIMMAXNUMINSTANCES = 10001;
	private static final int DEFLIMMAXNUMINSTANCES = 2000;

	// parameter-related GUI
	private NiceIntegerSlider limExpInstances;
	private Map<Transition, Integer> mapTrans2RowIndex = new HashMap<Transition, Integer>();
	private DefaultTableModel tableModel = null;
	private ProMTable promTable;

	private Map<String, Integer> mapXEvClass2RowIndex = new HashMap<String, Integer>();
	private DefaultTableModel evClassTableModel = null;
	private ProMTable promEvClassTable;

	private Map<Transition, Integer> mapSync2RowIndex = new HashMap<Transition, Integer>();
	private DefaultTableModel syncModel = null;
	private ProMTable promSyncTable;

	private JCheckBox cbUsePartialOrderedEvents = null;

	private final TableLayout tl;

	protected CostBasedCompleteUITable(double[][] size) {
		tl = new TableLayout(size);
		setLayout(tl);

	}

	public CostBasedCompleteUITable(Collection<Transition> transCol, Collection<String> evClassCol) {
		this(transCol, evClassCol, null, null, null);
	}

	public CostBasedCompleteUITable(Collection<Transition> transCol, Collection<String> evClassCol,
			Map<Transition, Integer> defMoveModelCost, Map<Transition, Integer> defSyncCost,
			Map<String, Integer> defMoveLogCost) {
		this(new double[][] {
				{ TableLayoutConstants.FILL },
				{ 80, 40, TableLayoutConstants.FILL, 35, TableLayoutConstants.FILL, 35, TableLayoutConstants.FILL, 35,
						30 } });

		SlickerFactory slickerFactoryInstance = SlickerFactory.instance();
		setTitle(
				slickerFactoryInstance,
				"<html><h1>Set parameters</h1><p>Double click costs on table to change their values. Use only non-negative integers.</p></html>");

		setUpStateSlider(slickerFactoryInstance, 1);
		setupUI(transCol, evClassCol, defMoveModelCost, defSyncCost, defMoveLogCost, slickerFactoryInstance, 2);

		setupPOCheckbox(slickerFactoryInstance, 8);

	}

	protected void setupPOCheckbox(SlickerFactory slickerFactoryInstance, int row) {
		cbUsePartialOrderedEvents = slickerFactoryInstance.createCheckBox(
				"Treat events with same timestamps as partially ordered events", false);

		add(cbUsePartialOrderedEvents, "0, " + row + ", l, t");
	}

	protected void setTitle(SlickerFactory slickerFactoryInstance, String title) {
		add(slickerFactoryInstance.createLabel(title), "0, 0, l, t");
	}

	protected void setUpStateSlider(SlickerFactory slickerFactoryInstance, int row) {
		// max instance
		limExpInstances = slickerFactoryInstance.createNiceIntegerSlider(
				"<html><h4># Maximum explored states (in hundreds). Set max for unlimited.</h4></html>", 1,
				MAXLIMMAXNUMINSTANCES, DEFLIMMAXNUMINSTANCES, Orientation.HORIZONTAL);
		limExpInstances.setPreferredSize(new Dimension(700, 20));
		limExpInstances.setMaximumSize(new Dimension(700, 20));
		limExpInstances.setMinimumSize(new Dimension(700, 20));

		add(limExpInstances, "0, " + (row) + ", c, t");

	}

	protected void setupUI(Collection<Transition> transCol, Collection<String> evClassCol,
			Map<Transition, Integer> defMoveModelCost, Map<Transition, Integer> defSyncCost,
			Map<String, Integer> defMoveLogCost, SlickerFactory slickerFactoryInstance, int startAtRow) {
		// label
		int r = startAtRow;

		populateMoveOnModelPanel(transCol, defMoveModelCost, (r++));
		populateSetAllButton(String.valueOf(DEFCOSTMOVEONMODEL), tableModel, "0, " + (r++) + ", c, t");

		populateMoveOnLogPanel(evClassCol, defMoveLogCost, r++);
		populateSetAllButton(String.valueOf(DEFCOSTMOVEONLOG), evClassTableModel, "0, " + (r++) + ", c, t");

		populateMoveSyncPanel(transCol, defSyncCost, r++);
		populateSetAllButton("0", syncModel, "0, " + (r++) + ", c, t");

	}

	private void populateMoveSyncPanel(Collection<Transition> transitions, Map<Transition, Integer> defaultCost, int row) {
		// create table to map move on model cost
		List<Transition> sortedTransitions = new ArrayList<Transition>(transitions);
		Collections.sort(sortedTransitions, new Comparator<Transition>() {
			public int compare(Transition t1, Transition t2) {
				return t1.getLabel().compareTo(t2.getLabel());
			}
		});
		Object[][] tableContent = new Object[transitions.size()][2];
		int rowCounter = 0;
		for (Transition trans : sortedTransitions) {
			if (!trans.isInvisible()) {
				if (defaultCost != null) {
					tableContent[rowCounter] = new Object[] { trans.getLabel(),
							defaultCost.get(trans) == null ? 0 : defaultCost.get(trans) };
				} else {
					tableContent[rowCounter] = new Object[] { trans.getLabel(), 0 };
				}
				mapSync2RowIndex.put(trans, rowCounter);
				rowCounter++;
			}
		}
		syncModel = new DefaultTableModel(tableContent, new Object[] { "Transition", "Move Synchronous Cost" }) {
			private static final long serialVersionUID = -6019224467802441949L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return (column != 0);
			};
		};
		promSyncTable = new ProMTable(syncModel);
		promSyncTable.setPreferredSize(new Dimension(700, 200));
		promSyncTable.setMinimumSize(new Dimension(700, 200));
		add(promSyncTable, "0, " + row + ", c, t");
	}

	private void populateMoveOnModelPanel(Collection<Transition> transitions, Map<Transition, Integer> defaultCost,
			int row) {
		// create table to map move on model cost
		List<Transition> sortedTransitions = new ArrayList<Transition>(transitions);
		Collections.sort(sortedTransitions, new Comparator<Transition>() {
			public int compare(Transition t1, Transition t2) {
				return t1.getLabel().compareTo(t2.getLabel());
			}
		});
		Object[][] tableContent = new Object[transitions.size()][2];
		int rowCounter = 0;
		for (Transition trans : sortedTransitions) {
			if (defaultCost != null) {
				tableContent[rowCounter] = new Object[] {
						trans.getLabel(),
						defaultCost.get(trans) == null ? (trans.isInvisible() ? 0 : DEFCOSTMOVEONMODEL) : defaultCost
								.get(trans) };
			} else {
				tableContent[rowCounter] = new Object[] { trans.getLabel(),
						trans.isInvisible() ? 0 : DEFCOSTMOVEONMODEL };
			}
			mapTrans2RowIndex.put(trans, rowCounter);
			rowCounter++;
		}
		tableModel = new DefaultTableModel(tableContent, new Object[] { "Transition", "Move on Model Cost" }) {
			private static final long serialVersionUID = -6019224467802441949L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return (column != 0);
			};
		};
		promTable = new ProMTable(tableModel);
		promTable.setPreferredSize(new Dimension(700, 200));
		promTable.setMinimumSize(new Dimension(700, 200));
		add(promTable, "0, " + row + ", c, t");
	}

	protected JButton populateSetAllButton(String defaultCost, final DefaultTableModel tableModel, String addLocation) {
		SlickerFactory factory = SlickerFactory.instance();

		final ProMTextField textField = new ProMTextField(defaultCost);
		textField.setMaximumSize(new Dimension(70, 20));
		//		textField.setMinimumSize(new Dimension(70,25));
		textField.setPreferredSize(new Dimension(70, 20));
		JButton setButton = factory.createButton("Set");
		setButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					int cost = Integer.parseInt(textField.getText().trim());
					if (cost >= 0) {
						for (int i = 0; i < tableModel.getRowCount(); i++) {
							tableModel.setValueAt(cost, i, 1);
						}
					}
				} catch (Exception exc) {
					// no action is performed
				}
			}
		});

		JPanel bgPanel = new JPanel();
		bgPanel.setBackground(new Color(150, 150, 150));
		bgPanel.add(factory.createLabel("Set all costs above to "));
		bgPanel.add(textField);
		bgPanel.add(setButton);
		add(bgPanel, addLocation);

		return setButton;
	}

	/**
	 * Generate event class move on log panel
	 * 
	 * @param eventClassesName
	 */
	private void populateMoveOnLogPanel(Collection<String> eventClassesName,
			Map<String, Integer> defaultCost, int row) {
		// move on log cost (determined by the selection of event class in mapping)
		evClassTableModel = new DefaultTableModel() {
			private static final long serialVersionUID = 5238526181467190856L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return (column != 0);
			};
		};

		mapXEvClass2RowIndex.clear();

		// move on log cost
		List<String> sortedEventClasses = new ArrayList<String>(eventClassesName);
		Collections.sort(sortedEventClasses, new Comparator<String>() {
			public int compare(String t1, String t2) {
				return t1.compareTo(t2);
			}
		});
		Object[][] evClassTableContent = new Object[eventClassesName.size()][2];
		int evClassRowCounter = 0;
		for (String evClass : sortedEventClasses) {
			if (defaultCost != null) {
				evClassTableContent[evClassRowCounter] = new Object[] { evClass,
						defaultCost.get(evClass) == null ? DEFCOSTMOVEONLOG : defaultCost.get(evClass) };
			} else {
				evClassTableContent[evClassRowCounter] = new Object[] { evClass, DEFCOSTMOVEONLOG };
			}
			mapXEvClass2RowIndex.put(evClass, evClassRowCounter);
			evClassRowCounter++;
		}
		evClassTableModel.setDataVector(evClassTableContent, new Object[] { "Event Class", "Move on Log Cost" });

		promEvClassTable = new ProMTable(evClassTableModel);
		promEvClassTable.setPreferredSize(new Dimension(700, 200));
		promEvClassTable.setMinimumSize(new Dimension(700, 200));
		add(promEvClassTable, "0, " + row + ", c, t");
	}

	/**
	 * Get map from event class to cost of move on log
	 * 
	 * @return
	 */
	public Map<String, Integer> getMapEvClassToCost() {
		Map<String, Integer> mapEvClass2Cost = new HashMap<String, Integer>();
		for (String evClass : mapXEvClass2RowIndex.keySet()) {
			int index = mapXEvClass2RowIndex.get(evClass);
			if (evClassTableModel.getValueAt(index, 1) instanceof Integer) {
				mapEvClass2Cost.put(evClass, (Integer) evClassTableModel.getValueAt(index, 1));
			} else {
				try {
					mapEvClass2Cost.put(evClass,
							Integer.parseInt(evClassTableModel.getValueAt(index, 1).toString().trim()));
				} catch (Exception exc) {
					mapEvClass2Cost.put(evClass, DEFCOSTMOVEONLOG);
				}

			}
		}
		return mapEvClass2Cost;
	}

	/**
	 * get penalty when move on model is performed
	 * 
	 * @return
	 */
	public Map<Transition, Integer> getTransitionWeight() {
		Map<Transition, Integer> costs = new HashMap<Transition, Integer>();
		for (Transition trans : mapTrans2RowIndex.keySet()) {
			int index = mapTrans2RowIndex.get(trans);
			if (tableModel.getValueAt(index, 1) instanceof Integer) {
				costs.put(trans, (Integer) tableModel.getValueAt(index, 1));
			} else { // instance of other
				try {
					costs.put(trans, Integer.parseInt(tableModel.getValueAt(index, 1).toString().trim()));
				} catch (Exception exc) {
					costs.put(trans, DEFCOSTMOVEONMODEL);
				}

			}
		}
		return costs;
	}

	/**
	 * get cost of doing synchronous moves
	 * 
	 * @return
	 */
	public Map<Transition, Integer> getSyncCost() {
		Map<Transition, Integer> costs = new HashMap<Transition, Integer>(1);
		for (Entry<Transition, Integer> entry : mapSync2RowIndex.entrySet()) {
			int index = entry.getValue();
			if (syncModel.getValueAt(index, 1) instanceof Integer) {
				costs.put(entry.getKey(), (Integer) syncModel.getValueAt(index, 1));
			} else { // instance of other
				try {
					costs.put(entry.getKey(), Integer.parseInt(syncModel.getValueAt(index, 1).toString().trim()));
				} catch (Exception exc) {
					costs.put(entry.getKey(), 0);
				}

			}
		}
		return costs;
	}

	/**
	 * get maximum number of explored states before stop exploration
	 * 
	 * @return
	 */
	public Integer getMaxNumOfStates() {
		return limExpInstances == null || limExpInstances.getValue() == MAXLIMMAXNUMINSTANCES ? Integer.MAX_VALUE
				: limExpInstances.getValue() * 100;
	}

	/**
	 * True if events with same timestamps are treated as partially ordered
	 * events
	 * 
	 * @return
	 */
	public boolean isUsePartialOrderedEvents() {
		return cbUsePartialOrderedEvents.isSelected();
	}
}
