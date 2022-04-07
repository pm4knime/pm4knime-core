package org.pm4knime.node.discovery.dfgminer.dfgTableMiner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.util.defaultnode.DefaultMinerNodeDialog;

/**
 * This is an example implementation of the node dialog of the
 * "DfgMinerTable" node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}. In general, one can create an
 * arbitrary complex dialog using Java Swing.
 * 
 * @author 
 */
public class DfgMinerTableNodeDialog extends DefaultMinerNodeDialog {


    /**
     * New pane for configuring the DfgMiner node.
     */
	private SettingsModelDoubleBounded m_noiseThreshold = null;	
	SettingsModelString m_variant;

	@Override
	public void init() {
		
		m_variant = new SettingsModelString(DfgMinerTableNodeModel.CFG_VARIANT_KEY, DfgMinerTableNodeModel.CFG_VARIANT_VALUES[0]);
		// the variants values are fixed
		DialogComponentStringSelection variantComp = new DialogComponentStringSelection(m_variant, "Variant",
				DfgMinerTableNodeModel.CFG_VARIANT_VALUES);
    	addDialogComponent(variantComp);
    	
	}
    
    
    
	@Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			
			classifierSet.loadSettingsFrom(settings);
    		List<String> configClassifierSet = Arrays.asList(classifierSet.getStringArrayValue());
    		System.out.println("");
    		DataTableSpec logSpec = (DataTableSpec) specs[0];
			List<String> specClassifierSet = new ArrayList<String>();
			specClassifierSet.addAll(Arrays.asList(logSpec.getColumnNames()));
			specClassifierSet.addAll(DfgMinerTableNodeModel.sClfNames);
			
			if(! configClassifierSet.containsAll(specClassifierSet) 
		    		 ||	!specClassifierSet.containsAll(configClassifierSet)){
				
				classifierSet.setStringArrayValue(specClassifierSet.toArray(new String[0]));
			}
			
			List<String> classAsList = Arrays.asList(logSpec.getColumnNames());
			classifierComp.replaceListItems(classAsList, classAsList.get(0));
			m_classifier.loadSettingsFrom(settings);
			
		} catch (NullPointerException | InvalidSettingsException e ) {
			// TODO Auto-generated catch block
			throw new NotConfigurableException("Please make sure the connected event log in excution state");
			
		}
	}
}

