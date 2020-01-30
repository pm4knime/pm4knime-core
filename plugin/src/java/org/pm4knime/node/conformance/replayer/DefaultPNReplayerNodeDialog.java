package org.pm4knime.node.conformance.replayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.settingsmodel.SMAlignmentReplayParameter;
import org.pm4knime.util.ReplayerUtil;
import org.pm4knime.util.XLogSpecUtil;

/**
 * <code>NodeDialog</code> for the "PNReplayer" node.
 * 
 * @author 
 */
public class DefaultPNReplayerNodeDialog extends DefaultNodeSettingsPane {

	DialogComponentStringSelection classifierComp ;
	protected SMAlignmentReplayParameter m_parameter;
	String[] strategyList = ReplayerUtil.strategyList;
	
    /**
     * New pane for configuring the PNReplayer node.
     */
    protected DefaultPNReplayerNodeDialog() {
    	
		specialInit();
		
    }
    
    protected void specialInit() {
		// TODO Auto-generated method stub
		m_parameter = new SMAlignmentReplayParameter(DefaultPNReplayerNodeModel.CFG_PARAMETER_NAME);
		commonInitPanel(m_parameter);
		
	}

    protected void commonInitPanel(SMAlignmentReplayParameter parameter) {
		// parameter = new SMAlignmentReplayParameter("Parameter in Tester");
		// we need to assign the classifier names 
    	
    	// modification at 21 Jan 2020. try to get the classifierNames from the configuration 
    	// how to assign values here, directly after this value?? if we use the list, check the later one 
    	// here, some improvements are in need. This is one constructor, we only assigns values once for the
    	// dialog components. But how to refresh their values according to spec?? 
    	
    	// check if it is an empty values here??
		classifierComp = new DialogComponentStringSelection(
				parameter.getMClassifierName(), "Select Classifier Name", 
				new String[ ]{""});
		
		addDialogComponent(classifierComp);

		parameter.getMStrategy().setStringValue(strategyList[0]);
		DialogComponentStringSelection m_strategyComp = new DialogComponentStringSelection(parameter.getMStrategy(),
				"Select Replay Strategy", strategyList);
		addDialogComponent(m_strategyComp);

		// change it to horizontal
		setHorizontalPlacement(true);
		DialogComponentNumberEdit[] defaultCostComps = new DialogComponentNumberEdit[SMAlignmentReplayParameter.CFG_COST_TYPE_NUM];
		for (int i = 0; i < SMAlignmentReplayParameter.CFG_COST_TYPE_NUM; i++) {
			defaultCostComps[i] = new DialogComponentNumberEdit(parameter.getMDefaultCosts()[i],
					SMAlignmentReplayParameter.CFG_MCOST_KEY[i], 5);
			addDialogComponent(defaultCostComps[i]);
		}
		
	}
    
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
    	
    	try {
    		// because it is not stored in the settings, so we should load the settings
    		
    		m_parameter.loadSettingsFrom(settings);
    		List<String> configClassifierSet = Arrays.asList(m_parameter.getClassifierSet().getStringArrayValue());
    		
    		XLogPortObjectSpec logSpec = (XLogPortObjectSpec) specs[0];
    		// with class name for each classifier here
			List<String> specClassifierSet = new ArrayList<String>(
					XLogSpecUtil.getClassifierWithClsList(logSpec.getClassifiersMap()));
			
			
			if(! configClassifierSet.containsAll(specClassifierSet) 
		    		 ||	!specClassifierSet.containsAll(configClassifierSet)){
				m_parameter.getClassifierSet().setStringArrayValue(specClassifierSet.toArray(new String[0]));
			}
			// one invalid settings is from the classifierComp, no matter where it is, it needs update 
			// if we include class into the classifierSet, to show it into the classifierComp,
			// we still use the event attribute classifier, before this, we need to change the showing of 
			// classifierComp
			classifierComp.replaceListItems(logSpec.getClassifiersMap().keySet(), 
					logSpec.getClassifiersMap().keySet().iterator().next());
			// this is also not the right way to do this.. 
			// comp saves its separate settings there?? But if we only use settings but separate its saving..
			// could we do this? Comp is already saved into the setting. We can do it again, to save it again
			// but make sure that there is no underconfig??
			m_parameter.getMClassifierName().loadSettingsFrom(settings);
			
		} catch (InvalidSettingsException | NullPointerException e ) {
			// TODO Auto-generated catch block
			throw new NotConfigurableException("Please make sure the connected event log in eexcution state");
			
		}
    	
    }
    
    
    
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        assert settings != null;
        
        m_parameter.saveSettingsTo(settings);
    }
}

