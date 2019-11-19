package org.pm4knime.node.conversion.table2log;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "CVS2XLogConverter" node.
 * 
 * @author Kefang Ding
 */
public class Table2XLogConverterNodeDialog extends DefaultNodeSettingsPane {

	// here we have optional item, but now just this two
	private List<String> m_possibleColumns ;
	private SettingsModelString m_caseID, m_eventID ;
	
	private SettingsModelString m_startTime, m_completeTime;
	private SettingsModelString m_sFormat, m_cFormat;
	private SettingsModelBoolean m_withSTime;
	private SettingsModelFilterString m_traceAttrSet , m_eventAttrSet;
	Table2XLogConfigModel config;
	
	DialogComponentStringSelection caseIDComp, eventIDComp, sTimeComp, cTimeComp; 
	DialogComponentBoolean withSTimeComp;
	DialogComponentStringSelection sFormatComp, cFormatComp;
	DialogComponentColumnFilter m_traceAttrFilterComp, m_eventAttrFilterComp;
    /**
     * New pane for configuring the CVS2XLogConverter node.
     */
    protected Table2XLogConverterNodeDialog() {
    	// here to initialise the option
    	m_possibleColumns = new ArrayList<String>();
    	m_possibleColumns.add("");
    	// all the this options, how to make them as optional choice?? 
    	// if we have optional choice, it means we can use the default values?? or just guess values there
    	// if there is no start time or complete time, how to set them there is not chosen then??
    	
    	// another panel is about the advanced configuration here
    	config = new Table2XLogConfigModel();
    	// config = CSV2XLogConverterNodeModel.m_config;
    	// add caseID choose to the panel
    	m_caseID = config.getMCaseID();
    	caseIDComp = new DialogComponentStringSelection(m_caseID, 
    			"Choose CaseID Column", m_possibleColumns);
    	addDialogComponent(caseIDComp);
    	
    	// add eventID choose to the panel
    	m_eventID = config.getMEventID();
    	eventIDComp = new DialogComponentStringSelection(m_eventID, 
    			"Choose EventID Column", m_possibleColumns);
    	addDialogComponent(eventIDComp);
    	
    	
        this.setHorizontalPlacement(true);
        // complete time choose 
    	m_completeTime = config.getMCompleteTime();
    	cTimeComp = new DialogComponentStringSelection(m_completeTime, 
    			"Choose Complete Time Column", m_possibleColumns);
    	addDialogComponent(cTimeComp);
    	// set the format to change string to date format
    	m_cFormat = config.getMCFormat();
    	cFormatComp = new DialogComponentStringSelection(m_cFormat, "Date format: ",
    			Table2XLogConfigModel.createPredefinedFormats(), true);
    	addDialogComponent(cFormatComp);
    	this.setHorizontalPlacement(false);
    		
    	// start time choice but as an option
    	m_withSTime = config.getMWithSTime();
    	withSTimeComp = new DialogComponentBoolean(m_withSTime, "With Start Time");
    	addDialogComponent(withSTimeComp);
    	
    	
    	this.setHorizontalPlacement(true);
    	m_startTime = config.getMStartTime();
    	m_startTime.setEnabled(m_withSTime.getBooleanValue());
    	sTimeComp = new DialogComponentStringSelection(m_startTime, 
    			"Choose Start Time Column", m_possibleColumns);
    	
    	addDialogComponent(sTimeComp);
    	// set the format to change string to date format
    	m_sFormat = config.getMSFormat();
    	
        sFormatComp = new DialogComponentStringSelection(m_sFormat, "Date format: ",
        		Table2XLogConfigModel.createPredefinedFormats(), true);
        m_sFormat.setEnabled(m_withSTime.getBooleanValue());
        addDialogComponent(sFormatComp);
        this.setHorizontalPlacement(false);
        
    	
    	// here we set another panel to set attributes for trace or events
        createNewTab("Choose attributes set");
        createNewGroup("Choose Columns as Trace Attributes: ");
        m_traceAttrSet = config.getMTraceAttrSet();
        m_traceAttrFilterComp = new DialogComponentColumnFilter(m_traceAttrSet,0, true);
        addDialogComponent(m_traceAttrFilterComp);
        createNewGroup("Choose Columns as Event Attributes: ");
        m_eventAttrSet = config.getMEventAttrSet();
        m_eventAttrFilterComp = new DialogComponentColumnFilter(m_eventAttrSet,0, true);
        addDialogComponent(m_eventAttrFilterComp);

        
        ExpertConfigPanel ecPanel = new ExpertConfigPanel();
    	ecPanel.setConversionConfig(config);
    	addTab("Expert Choice", ecPanel);
    	
    	m_withSTime.addChangeListener(new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
			// TODO Auto-generated method stub
			m_startTime.setEnabled(m_withSTime.getBooleanValue());
			m_sFormat.setEnabled(m_withSTime.getBooleanValue());
			
		}
  		
  	});
    }
    
    /**
     * Override hook to load additional settings when all input ports are
     * data ports.
     * Because in <code>DefaultNodeSettingsPane</code>, it sets the <code>loadSettingsFrom</code>
     * final. Therefore, we can't override this method and can only interact with dialog with 
     * <code>loadAdditionalSettingsFrom</code>. 
     * 
     * Here we get the values of possible columns and then use it to configure the Options.
     * @param settings The settings of the node
     * @param specs The <code>DataTableSpec</code> of the input tables.
     * @throws NotConfigurableException If not configurable
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
    	DataTableSpec spec = (DataTableSpec) specs[0];
    	m_possibleColumns.clear();
    	for(String colName : spec.getColumnNames())
    		m_possibleColumns.add(colName) ;
    	
    	// here we can set the values for config from the eventID values there.
    	caseIDComp.replaceListItems(m_possibleColumns, m_caseID.getStringValue());
    	eventIDComp.replaceListItems(m_possibleColumns, m_eventID.getStringValue());
    	sTimeComp.replaceListItems(m_possibleColumns, m_startTime.getStringValue());
    	cTimeComp.replaceListItems(m_possibleColumns, m_completeTime.getStringValue());
    	
    	m_traceAttrSet.setIncludeList(m_possibleColumns);
    	m_eventAttrSet.setIncludeList(m_possibleColumns);
    	
		try {
			// separate reason is that I want to simplify the codes for other item;
			// but one thing is that we summary some model values into config
			// make it work at first, then improve it later

			config.loadSettings(settings);
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
    
    /**
     * Override this method to  
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
    	// here we assign the m_possibleColumns values for option?? Not really, let us check
    	// System.out.println("possible columns length: " + m_possibleColumns.size());
    	// how to save the settings to the whole config?? we create the ConfigModel, 
    	// but in default, how to do it??
    	config.saveSettings(settings);
    }
    
	
}

