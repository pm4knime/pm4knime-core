package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.plugins.manifestanalysis.visualization.performance.CaseFilterPanelPerf;
import org.processmining.plugins.manifestanalysis.visualization.performance.IPerfCounter;
import org.processmining.plugins.manifestanalysis.visualization.performance.ManifestPerfPanel;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerScrollBarUI;

import gnu.trove.list.array.TIntArrayList;
import info.clearthought.layout.TableLayout;

public class CaseFilterPanelPerfTable<N extends ManifestEvClassPatternTable, C extends PerfCounterTable> extends JPanel {
	private static final long serialVersionUID = -3883326322705773889L;
	private JList caseList;
	private DefaultListModel listModel;

	private JScrollPane scroller;

	public CaseFilterPanelPerfTable(final ManifestPerfPanelTable<N, C> mainPanel, List<String> caseLabels,
			final boolean[] caseReliability, boolean showUnreliableCases) {

		SlickerFactory factory = SlickerFactory.instance();
		listModel = new DefaultListModel();
		for (String caseLabel : caseLabels) {
			listModel.addElement(caseLabel);
		}

		JButton selectAll = factory.createButton("Select all");
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAllIndices();
			}
		});

		JButton deselectAll = factory.createButton("Deselect all");
		deselectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				caseList.getSelectionModel().clearSelection(); // not selecting anything
			}
		});

		JButton inverseSelection = factory.createButton("Inverse");
		inverseSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int size = listModel.size();
				int[] currSelectedIndices = caseList.getSelectedIndices();
				int[] toBeSelected = new int[size - currSelectedIndices.length];

				int ptrCurrSelectedIndices = 0;
				int ptrToBeSelected = 0;

				int i = 0;
				while (ptrCurrSelectedIndices < currSelectedIndices.length) {
					if (currSelectedIndices[ptrCurrSelectedIndices] != i) {
						toBeSelected[ptrToBeSelected++] = i;
					} else {
						ptrCurrSelectedIndices++;
					}
					i++;
				}
				while (ptrToBeSelected < toBeSelected.length) {
					toBeSelected[ptrToBeSelected++] = i++;
				}
				caseList.setSelectedIndices(toBeSelected);
			}
		});

		JButton filter = factory.createButton("Filter");
		filter.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int[] selectedIndices = caseList.getSelectedIndices();

				Set<Integer> setSelectedIndices = new HashSet<Integer>(selectedIndices.length);
				for (int i : selectedIndices) {
					setSelectedIndices.add(i);
				}
				mainPanel.filterAlignmentPreserveIndex(mainPanel.getViewSpecificAttributeMap(), setSelectedIndices);
			}
		});

		final ProMTextField query = new ProMTextField("<minimize inspector,type case id here>");
		query.setEnabled(true);
		query.setEditable(true);

		JButton searchBtn = factory.createButton("Goto Case ID");
		searchBtn.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				if (!query.getText().trim().equals("")) {
					int index = 0;
					for (Enumeration<Object> enumer = (Enumeration<Object>) listModel.elements(); enumer
							.hasMoreElements();) {
						if (enumer.nextElement().toString().startsWith(query.getText().trim())) {
							setPosition((index - 1) / (listModel.size() + 0.333));
						}
						index++;
					}
				}
			}
		});
		/**
		 * beautify scrolls for list
		 */
		caseList = new JList(listModel);

		scroller = new JScrollPane(caseList);
		scroller.setOpaque(false);
		scroller.setBorder(BorderFactory.createEmptyBorder());
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JScrollBar vBar = scroller.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
				WidgetColors.COLOR_NON_FOCUS, 4, 12));
		vBar.setOpaque(true);
		vBar.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		vBar = scroller.getHorizontalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
				WidgetColors.COLOR_NON_FOCUS, 4, 12));
		vBar.setOpaque(true);
		vBar.setBackground(WidgetColors.PROPERTIES_BACKGROUND);

		// select reliable
		final JButton selectUnreliableCase = factory.createButton("Include");
		selectUnreliableCase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((caseReliability != null) && (caseReliability.length > 0)) {
					TIntArrayList finallySelected = new TIntArrayList(caseList.getSelectedIndices());

					for (int i = 0; i < caseReliability.length; i++) {
						if (!caseReliability[i]) {
							finallySelected.add(i);
						}
					}
					caseList.setSelectedIndices(finallySelected.toArray());
				}
			}
		});

		final JButton deselectUnreliableCase = factory.createButton("Exclude");
		deselectUnreliableCase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TIntArrayList finallySelected = new TIntArrayList(caseList.getSelectedIndices());

				for (int i = 0; i < caseReliability.length; i++) {
					if (!caseReliability[i]) {
						finallySelected.remove(i);
					}
				}

				caseList.setSelectedIndices(finallySelected.toArray());

			}

		});

		setBackground(new Color(202, 202, 202));

		double[][] size = new double[][] { { 150, 150, 150 }, { 25, TableLayout.FILL, 25, 25, 30 } };
		setLayout(new TableLayout(size));

		add(searchBtn, "0,0");
		add(query, "1,0,2,0");
		add(scroller, "0,1,2,1");
		add(selectAll, "0,2");
		add(deselectAll, "1,2");
		add(inverseSelection, "2,2");

		add(factory.createLabel("Include unreliable results?"), "0,3");
		add(selectUnreliableCase, "1,3");
		add(deselectUnreliableCase, "2,3");

		add(filter, "0,4,2,4");

		// initially, select all cases
		if (showUnreliableCases) {
			selectAllIndices();
		} else {
			selectCases(caseReliability);
		}
	}

	private void selectAllIndices() {
		int[] indices = new int[listModel.size()];
		for (int i = 0; i < listModel.size(); i++) {
			indices[i] = i;
		}
		caseList.setSelectedIndices(indices);
	}

	public void setPosition(double ratio) {
		if ((ratio < 0) || (ratio > 1)) {
			JScrollBar bar = scroller.getVerticalScrollBar();
			bar.setValue(bar.getMinimum());

			bar = scroller.getHorizontalScrollBar();
			bar.setValue(bar.getMinimum());
		} else {
			JScrollBar bar = scroller.getVerticalScrollBar();
			bar.setValue(((int) Math.floor((bar.getMaximum() - bar.getMinimum()) * ratio)) + bar.getMinimum());

			bar = scroller.getHorizontalScrollBar();
			bar.setValue(((int) Math.floor((bar.getMaximum() - bar.getMinimum()) * ratio)) + bar.getMinimum());
		}
	}

	/**
	 * get selected index
	 * 
	 * @return
	 */
	public Set<Integer> getSelectedIndex() {
		int[] selectedIndices = caseList.getSelectedIndices();
		HashSet<Integer> res = new HashSet<Integer>(selectedIndices.length);
		for (int i : selectedIndices) {
			res.add(i);
		}
		return res;
	}

	public void selectCases(boolean[] filter) {
		TIntArrayList arrList = new TIntArrayList(filter.length / 8);
		for (int i = 0; i < filter.length; i++) {
			if (filter[i]) {
				arrList.add(i);
			}
		}
		caseList.setSelectedIndices(arrList.toArray());
	}

}
