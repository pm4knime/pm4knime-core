package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.pnalignanalysis.visualization.projection.AlignmentFilterPanel;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerScrollBarUI;

import info.clearthought.layout.TableLayout;

public class AlignmentFilterPanelTable extends JPanel {

		private static final long serialVersionUID = -801212812906789799L;

		private JCheckBox[] moveModelOnly;
		private JCheckBox[] moveLogModel;
		private JCheckBox[] moveLogOnly;

		public AlignmentFilterPanelTable(final PNLogReplayProjectedVisPanelTable mainPanel, Transition[] transArray, String[] acArray) {
			super();
			int standardWidth = 380;
			setPreferredSize(new Dimension(standardWidth, 500));
			setMinimumSize(new Dimension(standardWidth, 500));
			setMaximumSize(new Dimension(standardWidth, 500));
			
			SlickerFactory factory = SlickerFactory.instance();		
			
			// move on model, and move on log+model
			JPanel moveOnModelPanel = new JPanel();
			moveOnModelPanel.setLayout(new BoxLayout(moveOnModelPanel, BoxLayout.Y_AXIS));
			moveOnModelPanel.setBackground(Color.WHITE);
			
			moveModelOnly = new JCheckBox[transArray.length];
			
			final JCheckBox checkAllBoxMoveModel = factory.createCheckBox("Select/deselect all", true);
			moveOnModelPanel.add(checkAllBoxMoveModel);
			
			JPanel moveLogModelPanel = new JPanel();
			moveLogModelPanel.setLayout(new BoxLayout(moveLogModelPanel, BoxLayout.Y_AXIS));
			moveLogModelPanel.setBackground(Color.WHITE);


			moveLogModel = new JCheckBox[transArray.length];
			final JCheckBox checkAllBoxLogModel = factory.createCheckBox("Select/deselect all", true);
			moveLogModelPanel.add(checkAllBoxLogModel);

			for (int i=0; i < transArray.length; i++) {
				// move model only
				JCheckBox checkBox = factory.createCheckBox(transArray[i].getLabel(), true);
				moveOnModelPanel.add(checkBox);
				moveModelOnly[i] = checkBox;

				// move sync
				JCheckBox checkBoxLogModel = factory.createCheckBox(transArray[i].getLabel(), true);
				moveLogModelPanel.add(checkBoxLogModel);
				moveLogModel[i] = checkBoxLogModel;
			}

			checkAllBoxMoveModel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (JCheckBox key : moveModelOnly) {
						key.setSelected(checkAllBoxMoveModel.isSelected());
					}
				}
			});

			checkAllBoxLogModel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (JCheckBox key : moveLogModel) {
						key.setSelected(checkAllBoxLogModel.isSelected());
					}
				}
			});

			// move log only
			JPanel moveOnLogPanel = new JPanel();
			moveOnLogPanel.setLayout(new BoxLayout(moveOnLogPanel, BoxLayout.Y_AXIS));
			moveOnLogPanel.setBackground(Color.WHITE);
			
			moveLogOnly = new JCheckBox[acArray.length];
			final JCheckBox checkAllBoxMoveLog = factory.createCheckBox("Select/deselect all", true);
			moveOnLogPanel.add(checkAllBoxMoveLog);

			for (int i=0; i < acArray.length; i++) {
				// move on log only
				JCheckBox checkBox = factory.createCheckBox(acArray[i].toString(), true);
				moveOnLogPanel.add(checkBox);
				moveLogOnly[i] = checkBox;
			}

			checkAllBoxMoveLog.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (JCheckBox key : moveLogOnly) {
						key.setSelected(checkAllBoxMoveLog.isSelected());
					}
				}
			});

			JButton filterBtn = factory.createButton("Filter out alignment without these movements");
			filterBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mainPanel.filterAlignment(getCheckBoxSelection(moveLogModel), getCheckBoxSelection(moveModelOnly),
							getCheckBoxSelection(moveLogOnly));
				}
			});
			
			JButton selectInverse = factory.createButton("Inverse");
			selectInverse.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					for (JCheckBox key : moveModelOnly) {
						key.setSelected(!key.isSelected());
					}
					for (JCheckBox key : moveLogModel) {
						key.setSelected(!key.isSelected());
					}
					for (JCheckBox key : moveLogOnly) {
						key.setSelected(!key.isSelected());
					}
					checkAllBoxMoveModel.setSelected(false);
					checkAllBoxMoveLog.setSelected(false);
					checkAllBoxLogModel.setSelected(false);
				}			
			});
			
			JButton selectAll = factory.createButton("All");
			selectAll.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					for (JCheckBox key : moveModelOnly) {
						key.setSelected(true);
					}
					for (JCheckBox key : moveLogModel) {
						key.setSelected(true);
					}
					for (JCheckBox key : moveLogOnly) {
						key.setSelected(true);
					}
					checkAllBoxMoveModel.setSelected(true);
					checkAllBoxMoveLog.setSelected(true);
					checkAllBoxLogModel.setSelected(true);
				}
				
			});
			
			JButton selectNone = factory.createButton("None");
			selectNone.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					for (JCheckBox key : moveModelOnly) {
						key.setSelected(false);
					}
					for (JCheckBox key : moveLogModel) {
						key.setSelected(false);
					}
					for (JCheckBox key : moveLogOnly) {
						key.setSelected(false);
					}
					checkAllBoxMoveModel.setSelected(false);
					checkAllBoxMoveLog.setSelected(false);
					checkAllBoxLogModel.setSelected(false);
				}
				
			});
			
			// add all components
			double size[][] = new double[][] { { 80, 100, 100, 100 },
					{ 30, 30, 20, 120, 5, 20, 120, 5, 20, 120 } };
			setLayout(new TableLayout(size));
			add(filterBtn, "0,0,3,0");
			add(factory.createLabel("Selection"), "0,1");
			add(selectAll, "1,1");
			add(selectNone, "2,1");
			add(selectInverse, "3,1");
			
			
			add(factory.createLabel("Move Synchronous"), "0,2,3,2");
			add(createProMScrollPane(moveLogModelPanel), "0,3,3,3");
			add(factory.createLabel("Move Model only"), "0,5,3,5");
			add(createProMScrollPane(moveOnModelPanel), "0,6,3,6");
			
			add(factory.createLabel("Move Log only"), "0,8,3,8");
			add(createProMScrollPane(moveOnLogPanel), "0,9,3,9");
			
			setBackground(new Color(202, 202, 202));
		}

		protected JScrollPane createProMScrollPane(JComponent component){
			JScrollPane scroller = new JScrollPane(component);
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

			setBackground(new Color(202, 202, 202));
			return scroller;
		}
		
		protected boolean[] getCheckBoxSelection(JCheckBox[] array) {
			boolean[] res = new boolean[array.length];
			for (int i=0; i < array.length; i++){
				res[i] = array[i].isSelected();
			}
			return res;
		}
	}
