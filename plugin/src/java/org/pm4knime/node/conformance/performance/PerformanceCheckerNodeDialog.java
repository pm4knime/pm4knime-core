package org.pm4knime.node.conformance.performance;

import java.util.List;

import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.port.PortObject;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.settingsmodel.SMPerformanceParameter;
import org.pm4knime.util.XLogUtil;

/**
 * <code>NodeDialog</code> for the "PerformanceChecker" node.
 * There are 7 configuration panels in ProM for Performance Checker. 
 * We ignore 
 * -- event name pattern assignment, use only single activity at first
 * -- transition pattern assignment
 * -- pairwise transition occurrence 
 * 
 * Use panels:
 * -- choose algorithm
 * -- set move costs table, first just use default nodes
 * -- choose timestamp attribute
 * -- choose the comparison strategy for syn moves
 * 
 * To create those tables, at first, put them together, then split them later... 
 *  Make the tables with different sizes and also its own scrollpane 
 * 
 * 
 * TODO 1: unified Performance Checker here.. Without CT. 
 * TODO 2: extend conformance checking parameters but with more attributes. Only define them into two parts,
 * one is with CT, one is without CT. 
 * TODO 4: change the dialog panels there..
 * 
 * One question to think:: if we assign them the same name, what if there are more nodes there,
 * how to make sure that they are different storage??
 * @author Kefang Ding
 */
public class PerformanceCheckerNodeDialog extends DataAwareNodeDialogPane {
	protected JPanel m_compositePanel;
	SMPerformanceParameter m_parameter;
	DialogComponentStringSelection m_timestampComp ;
    /**
     * New pane for configuring the PerformanceChecker node.
     */
    protected PerformanceCheckerNodeDialog() {
    	super();
    	m_parameter = new SMPerformanceParameter("Performance Parameter");
    	
    	// add additional items to m_compositePanel panel
    	SMPerformanceParameter tmp = (SMPerformanceParameter) m_parameter;
    	// 1. syn move strategy
    	DialogComponentBoolean m_withSynMoveComp = new DialogComponentBoolean(tmp.isMWithSynMove(), tmp.CKF_KEY_WITH_SYN_MOVE);
    	addDialogComponent(m_withSynMoveComp);
    	// 2. the attributes of time stamp... 
    	m_timestampComp = new DialogComponentStringSelection(tmp.getMTimeStamp(),
				tmp.CKF_KEY_TIMESTAMP, new String[]{""} ) ;
		addDialogComponent(m_timestampComp);
		
		DialogComponentBoolean m_withUnreliableResultComp = new DialogComponentBoolean(tmp.isMWithUnreliableResult(), tmp.CKF_KEY_WITH_UNRELIABLE_RESULT);
    	addDialogComponent(m_withUnreliableResultComp);
    	
    }
    
    protected void addDialogComponent(final DialogComponent diaC) {
		// TODO Auto-generated method stub
		m_compositePanel.add(diaC.getComponentPanel());
	}

    
    /**
     * to get the attributes from event log, especially the time stamp information. we need PortObject.
     * 
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObject[] input) throws NotConfigurableException {
    	
			if (!(input[0] instanceof RepResultPortObject))
				throw new NotConfigurableException("Input is not a valid replayer log!");
			
			// TODO : check the type if it is mainfest replayer result
			RepResultPortObject repResultPO = (RepResultPortObject) input[0];
			
			XLog log = repResultPO.getLog();
			
			// get the attributes available for the time stamp from event log
			List<String> tsAttrNameList = XLogUtil.getTSAttrNames(log);
			
			m_timestampComp.replaceListItems(tsAttrNameList, tsAttrNameList.get(0));
		
			// do we need to repaint all the composite , or only the one corresponding to this attributes?
			// when it is the first time to initialize parameter, we need the input PortObject
    		// when it is the second time to use this, the already existing values should be there
			try {
				m_parameter.loadSettingsFrom(settings);
			} catch (InvalidSettingsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			m_compositePanel.repaint();
		
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_parameter.saveSettingsTo(settings);
	}


}

