package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.node.discovery.alpha.AlphaMinerNodeModel;
import org.processmining.alphaminer.parameters.AlphaVersion;

/**
 * <code>NodeDialog</code> for the "FilterByFrequency" node.
 * 
 * @author Kefang Ding
 */
public class FilterByFrequencyTableNodeDialog extends DefaultNodeSettingsPane {
	SettingsModelBoolean m_isKeep, m_isForSingleTV;
	SettingsModelDoubleBounded m_threshold ;
	private SettingsModelString m_variantCase, m_variantTime , m_variantActivity;
	
    /**
     * New pane for configuring the FilterByFrequency node.
     */
    protected FilterByFrequencyTableNodeDialog() {
    	
    	createNewGroup("Choose the corresponding columns");
    	String[] variantListCase =  FilterByFrequencyTableNodeModel.variantListCase;
        m_variantCase = new SettingsModelString(FilterByFrequencyTableNodeModel.CFGKEY_VARIANT_CASE, variantListCase[0]);
        addDialogComponent(new DialogComponentStringSelection(m_variantCase,"Case ID", variantListCase));
        
        String[] variantListTime =  FilterByFrequencyTableNodeModel.variantListTime;
        m_variantTime = new SettingsModelString(FilterByFrequencyTableNodeModel.CFGKEY_VARIANT_TIME, variantListTime[0]);
        addDialogComponent(new DialogComponentStringSelection(m_variantTime,"Timestamp", variantListTime));
        
        String[] variantListActivity =  FilterByFrequencyTableNodeModel.variantListActivity;
        m_variantActivity = new SettingsModelString(FilterByFrequencyTableNodeModel.CFGKEY_VARIANT_ACTIVITY, variantListActivity[0]);
        addDialogComponent(new DialogComponentStringSelection(m_variantActivity,"Activity", variantListActivity));
        
        closeCurrentGroup();
    	
    	
    	m_isKeep = new SettingsModelBoolean(FilterByFrequencyTableNodeModel.CFG_ISKEEP, true);
    	DialogComponentBoolean isKeepComp = new DialogComponentBoolean(m_isKeep, FilterByFrequencyTableNodeModel.CFG_ISKEEP);
    	addDialogComponent(isKeepComp);
    	
    	m_isForSingleTV  = new SettingsModelBoolean(FilterByFrequencyTableNodeModel.CFG_ISFOR_SINGLETRACE_VARIANT, true);
    	DialogComponentBoolean isForSingleTVComp = new DialogComponentBoolean(m_isForSingleTV, FilterByFrequencyTableNodeModel.CFG_ISFOR_SINGLETRACE_VARIANT);
    	addDialogComponent(isForSingleTVComp);
    	
    	m_threshold = new SettingsModelDoubleBounded(
    			FilterByFrequencyTableNodeModel.CFG_THRESHOLD, 0.2, 0, Integer.MAX_VALUE);
    	DialogComponentNumberEdit thresholdComp = new DialogComponentNumberEdit(m_threshold,
    			FilterByFrequencyTableNodeModel.CFG_THRESHOLD , 5);
    	addDialogComponent(thresholdComp);
    	/*
    	m_threshold.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if(m_threshold.getDoubleValue() > 1.0) {
					m_threshold.setDoubleValue((int) m_threshold.getDoubleValue());
					thresholdComp.getComponentPanel().repaint();
				}
			}
    		
    	});
    	*/
   }
}

