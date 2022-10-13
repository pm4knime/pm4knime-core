package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.processmining.framework.util.ui.widgets.WidgetColors;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ShowHideMovementPanelTable extends JPanel{
	private static final long serialVersionUID = -801212812906789799L;
	private final JCheckBox logModel;
	private final JCheckBox moveModel;
	private final PNLogReplayProjectedVisPanelTable mainPanel; 
	
	public ShowHideMovementPanelTable(PNLogReplayProjectedVisPanelTable mainPanel){

		this.mainPanel = mainPanel;
		
		SlickerFactory factory = SlickerFactory.instance();
		logModel = factory.createCheckBox("Show log+model moves", true);
		moveModel = factory.createCheckBox("Show model only moves", true);
		
		DefaultAction defaultAction = new DefaultAction();
		logModel.addActionListener(defaultAction);
		moveModel.addActionListener(defaultAction);
		
		// add checkbox
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		logModel.setAlignmentX(Component.LEFT_ALIGNMENT);
		moveModel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(logModel);
		add(moveModel);
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
	}
	
	class DefaultAction implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			mainPanel.constructVisualization(mainPanel.getViewSpecificAttributeMap(), logModel.isSelected(), moveModel.isSelected());
			mainPanel.repaint();
		}
	}
}
