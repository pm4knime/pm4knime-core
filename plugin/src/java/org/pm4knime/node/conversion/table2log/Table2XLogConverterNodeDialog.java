package org.pm4knime.node.conversion.table2log;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.settingsmodel.SMTable2XLogConfig;

/**
 * <code>NodeDialog</code> for the "CVS2XLogConverter" node.
 * 
 * @author Kefang Ding
 * @Modification 13 Dec 2019, changes include :
 *  -- dispose the attributes choices panel, but need to distinguish the trace attributes and event attributes?? 
 *  That's why I have added all the attributes there!! to choose. 
 *  ===> final decision: no need to change this panel
 *  -- set specific attributes for the event log!! 
 *    ++ caseID column, we need to define it. Else, I'd like to say no... there is always the attributes called concept:name
 *    which is used to show the event logs there
 *    ++ with lifecycle:transition
 *      -- yes, choose the column for value and the time stamp for it!!  Automatically to add it in the model.
 *      -- no, choose the complete time stamp but without assign the lifecycle: transition.
 *  Choose with lifecycle, then automatically the column choice is enabled, after this, choose the timestamp and its format!!
 *  
 *  // do two line, one to choose if with time stamp, the other for timestamp
 */
public class Table2XLogConverterNodeDialog extends DefaultNodeSettingsPane {

	// here we have optional item, but now just this two
	private List<String> m_possibleColumns ;
	private SettingsModelString m_caseID,  m_eventClass;
	
	private SettingsModelString m_lifecycleName, m_timeStamp,  m_tsFormat;
	private SettingsModelFilterString m_traceAttrSet , m_eventAttrSet;
	SMTable2XLogConfig config;
	
	DialogComponentStringSelection caseIDComp, eventClassComp, timeStampComp, lifecycleNameComp; 
	DialogComponentBoolean withLifecycleComp;
	DialogComponentString tsFormatComp;
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
    	config = new SMTable2XLogConfig(Table2XLogConverterNodeModel.CFG_KEY_CONFIG);
    	// add caseID choose to the panel
    	m_caseID = config.getMCaseID();
    	caseIDComp = new DialogComponentStringSelection(m_caseID, 
    			"Choose CaseID Column", m_possibleColumns);
    	addDialogComponent(caseIDComp);
    	
    	
    	// add eventID choose to the panel
    	m_eventClass = config.getMEventClass();
    	eventClassComp = new DialogComponentStringSelection(m_eventClass, 
    			"Choose Eventclass Column", m_possibleColumns);
    	addDialogComponent(eventClassComp);
    	
    	
    	// @modify 16 Dec 2019: to add one option to choose if there is lifecycle attribute enable with checkbox
    	// if the checkbox is enabled, then choose one column to set the lifecycle transition
    	// after this, choose the columns for time annd format
    	// add life cycle component choice
    	// add eventID choose to the panel
    	// optional choices for them. They can be empty there
    	m_lifecycleName = config.getMLifecycle();
    	lifecycleNameComp = new DialogComponentStringSelection(m_lifecycleName, "Life Cycle", m_possibleColumns);
    	addDialogComponent(lifecycleNameComp);
        
        // in default, it is the complete time, but we don't assign it there until we have the clear lifecycle choose
    	m_timeStamp = config.getMTimeStamp();
    	timeStampComp = new DialogComponentStringSelection(m_timeStamp, 
    			"Choose Time Stamp Column", m_possibleColumns);
    	
    	addDialogComponent(timeStampComp);
    	// set the format to change string to date format
    	m_tsFormat = config.getMTSFormat();
        tsFormatComp = new DialogComponentString(m_tsFormat, "Date format: ");
        addDialogComponent(tsFormatComp);
        
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
    	// If at first, we already add the settings here, we need to load the saved value at first, and then check the values there
    	// check if it is after reset, or it is the first time to import into the workflow??
    	
    	
    	DataTableSpec spec = (DataTableSpec) specs[0];
    	m_possibleColumns.clear();
    	for(String colName : spec.getColumnNames())
    		m_possibleColumns.add(colName) ;
    	
    	// here we can set the values for config from the eventID values there.
    	caseIDComp.replaceListItems(m_possibleColumns, m_caseID.getStringValue());
    	eventClassComp.replaceListItems(m_possibleColumns, m_eventClass.getStringValue());
    	timeStampComp.replaceListItems(m_possibleColumns, m_timeStamp.getStringValue());
    	
    	m_traceAttrSet.setIncludeList(m_possibleColumns);
    	m_eventAttrSet.setIncludeList(m_possibleColumns);
    	
    	
    	// add empty for their choices!!
    	m_possibleColumns.add(Table2XLogConfigModel.CFG_NO_OPTION);
//    	eventIDComp.replaceListItems(m_possibleColumns, m_eventID.getStringValue());
    	lifecycleNameComp.replaceListItems(m_possibleColumns, m_lifecycleName.getStringValue());
    	
		try {
			// separate reason is that I want to simplify the codes for other item;
			// but one thing is that we summary some model values into config
			// make it work at first, then improve it later
			config.loadSettingsFrom(settings);
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
    	config.saveSettingsTo(settings);
    }
    
	
}

