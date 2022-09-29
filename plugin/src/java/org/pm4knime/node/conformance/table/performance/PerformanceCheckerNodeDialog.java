package org.pm4knime.node.conformance.table.performance;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.SMAlignmentReplayParameterTable;
import org.pm4knime.portobject.RepResultPortObjectSpecTable;
import org.pm4knime.settingsmodel.SMPerformanceParameter;
import org.pm4knime.util.XLogSpecUtil;

/**
 * <code>NodeDialog</code> for the "PerformanceChecker" node. There are 7
 * configuration panels in ProM for Performance Checker. We ignore -- event name
 * pattern assignment, use only single activity at first -- transition pattern
 * assignment -- pairwise transition occurrence
 * 
 * Use panels: -- choose algorithm -- set move costs table, first just use
 * default nodes -- choose timestamp attribute -- choose the comparison strategy
 * for syn moves
 * 
 * To create those tables, at first, put them together, then split them later...
 * Make the tables with different sizes and also its own scrollpane
 * 
 * 
 * TODO 1: unified Performance Checker here.. Without CT. TODO 2: extend
 * conformance checking parameters but with more attributes. Only define them
 * into two parts, one is with CT, one is without CT. TODO 4: change the dialog
 * panels there..
 * 
 * One question to think:: if we assign them the same name, what if there are
 * more nodes there, how to make sure that they are different storage??
 * 
 * @author Kefang Ding
 */
public class PerformanceCheckerNodeDialog extends DefaultNodeSettingsPane {
	protected JPanel m_compositePanel;
	SMPerformanceParameter m_parameter;
	DialogComponentStringSelection m_timestampComp;

	/**
	 * New pane for configuring the PerformanceChecker node.
	 */
	protected PerformanceCheckerNodeDialog() {

		m_parameter = new SMPerformanceParameter("Performance Parameter");

		// add additional items to m_compositePanel panel
		SMPerformanceParameter tmp = (SMPerformanceParameter) m_parameter;
		// 1. syn move strategy
		DialogComponentBoolean m_withSynMoveComp = new DialogComponentBoolean(tmp.isMWithSynMove(),
				SMPerformanceParameter.CKF_KEY_WITH_SYN_MOVE);
		addDialogComponent(m_withSynMoveComp);
		// 2. the attributes of time stamp...
		m_timestampComp = new DialogComponentStringSelection(tmp.getMTimeStamp(), SMPerformanceParameter.CKF_KEY_TIMESTAMP,
				new String[] { "" });
		addDialogComponent(m_timestampComp);

		DialogComponentBoolean m_withUnreliableResultComp = new DialogComponentBoolean(tmp.isMWithUnreliableResult(),
				SMPerformanceParameter.CKF_KEY_WITH_UNRELIABLE_RESULT);
		addDialogComponent(m_withUnreliableResultComp);
	}

	/**
	 * to get the attributes from event log, especially the time stamp information.
	 * we need PortObject.
	 * 
	 */
	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
			throws NotConfigurableException {

		if (!(specs[0] instanceof RepResultPortObjectSpecTable))
			throw new NotConfigurableException("Input is not a valid replayer log!");

		RepResultPortObjectSpecTable repResultPOSpec = (RepResultPortObjectSpecTable) specs[0];

		// if we have the classifier set from the parameter, we can't figure out the key
		// for time stamp.
		// to solve it, two ways: 1. follow the old method
		// 2. save the class name information for event classifiers too.
		// to save the space, we can concatenate the class name and its value together,
		// we we use it, we split the name and class name. we can judge the class, only according its attribute name
		// so that means, if we want to set the map to str... 
		// we depend on the SpecObject to provide us the right value but now it doesn't 
		
		// we will expect we have the spec totally loaded here!! 
		SMAlignmentReplayParameterTable specParameter = repResultPOSpec.getMParameter();

		List<String> tsAttrNameList = new ArrayList<String>();
		if(specParameter.getClassifierSet() != null) {
			for (String clfPlusClass : specParameter.getClassifierSet().getStringArrayValue()) {
				String[] clfPlusClassArray = clfPlusClass.split(XLogSpecUtil.CFG_KEY_CLASSIFIER_SEPARATOR);
//				if (clfPlusClassArray[1].equals(XAttributeTimestampImpl.class.toString()))
//					
				tsAttrNameList.add(clfPlusClassArray[0]);
			}
		}
	    
		if (!tsAttrNameList.contains(m_parameter.getMTimeStamp().getStringValue())) {
			m_timestampComp.replaceListItems(tsAttrNameList, tsAttrNameList.get(0));
			m_parameter.getMTimeStamp().setStringValue(tsAttrNameList.get(0));
			m_parameter.setClassifierSet(specParameter.getClassifierSet().getStringArrayValue());
		}

	}
	
	@Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        assert settings != null;
        
        m_parameter.saveSettingsTo(settings);
    }

}
