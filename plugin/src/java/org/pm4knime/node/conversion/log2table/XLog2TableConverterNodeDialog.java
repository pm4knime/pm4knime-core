package org.pm4knime.node.conversion.log2table;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.ui.DialogComponentAttributesFilter;

/**
 * <code>NodeDialog</code> for the "Xlog2CSVConverter" node.
 * After adding the log attributes as XLogPortObjectSpec, we can choose the attributes to convert into data table.
 * 
 *  Using the column selection here. But one question here is that, if we use the DefaultNodeSettingsPane, 
 *  for the DialogComponentColumnFilter, it demands the DataTabelSpec, which we can't give it. 
 *  So we need to change the strategy to create another component here. 
 *  
 * @author Kefang
 */
public class XLog2TableConverterNodeDialog extends DefaultNodeSettingsPane {

	private SettingsModelFilterString m_traceAttrSet, m_eventAttrSet;
    /**
     * New pane for configuring the Xlog2TableConverter node.
     */
    protected XLog2TableConverterNodeDialog() {
    	// we should get the attribute set from the spec
    	createNewGroup("Choose trace attributes");
    	m_traceAttrSet = new SettingsModelFilterString(XLogSpecUtil.CFG_KEY_TRACE_ATTRSET, new String[]{}, new String[]{}, false );
    	DialogComponentAttributesFilter m_traceAttrFilterComp = new DialogComponentAttributesFilter(m_traceAttrSet, true);
        addDialogComponent(m_traceAttrFilterComp);
        
        createNewGroup("Choose event Attributes");
    	m_eventAttrSet = new SettingsModelFilterString(XLogSpecUtil.CFG_KEY_EVENT_ATTRSET, new String[]{}, new String[]{}, false );
    	DialogComponentAttributesFilter m_eventAttrFilterComp = new DialogComponentAttributesFilter(m_eventAttrSet, true);
    	addDialogComponent(m_eventAttrFilterComp);
    }
    
    /**
     * here we want to use the spec available to check 
     */
    
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
    	// reset the m_traceAttrSet and other parts
    	// check the specs is log
    	// TODO : how to load the values from the saved workflow but not from the specs!
    	// if they haven't changed the spec the connection, we should load the old configuration, else
    	// we need to load the new connection there.  But how to do this?? 
    	// how to assign the dialog values??
    	
    	// we can check the m_traceAttrSet situations. 
    	// in default, it can load the values from save and other stuff, but when it is 
    	// initialized, there is no values here, so we get values from PortObjectSpec
    	super.loadAdditionalSettingsFrom(settings, specs);
//    	if(m_traceAttrSet.getIncludeList().isEmpty() && m_traceAttrSet.getExcludeList().isEmpty())
//    		if(m_eventAttrSet.getIncludeList().isEmpty() && m_eventAttrSet.getExcludeList().isEmpty()) {
//    	

    	
		    	if(!(specs[0] instanceof XLogPortObjectSpec))
		    		throw new NotConfigurableException("the spec does not have the right type");
		    	XLogPortObjectSpec logSpec = (XLogPortObjectSpec) specs[0];
		    	
		    	// define one condition that we use the old settings, not the initialisation. 
		    	// how to know the Specs has changed before?? 
		    	// 
		    	
		    	m_traceAttrSet.setIncludeList(logSpec.getGTraceAttrMap().keySet());
		    	m_traceAttrSet.setExcludeList(new String[0]);
		    	
		    	m_eventAttrSet.setIncludeList(logSpec.getGEventAttrMap().keySet());
		    	m_eventAttrSet.setExcludeList(new String[0]);
//    		}

    }
}

