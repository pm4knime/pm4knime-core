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
	DialogComponentAttributesFilter m_traceAttrFilterComp, m_eventAttrFilterComp;
	private SettingsModelFilterString m_traceAttrSet, m_eventAttrSet;
    /**
     * New pane for configuring the Xlog2TableConverter node.
     */
    protected XLog2TableConverterNodeDialog() {
    	// we should get the attribute set from the spec
    	createNewGroup("Choose trace attributes");
    	m_traceAttrSet = new SettingsModelFilterString(XLogSpecUtil.CFG_KEY_TRACE_ATTRSET, new String[]{}, new String[]{}, false );
    	m_traceAttrFilterComp = new DialogComponentAttributesFilter(m_traceAttrSet, true);
        addDialogComponent(m_traceAttrFilterComp);
        
        createNewGroup("Choose event Attributes");
    	m_eventAttrSet = new SettingsModelFilterString(XLogSpecUtil.CFG_KEY_EVENT_ATTRSET, new String[]{}, new String[]{}, false );
    	m_eventAttrFilterComp = new DialogComponentAttributesFilter(m_eventAttrSet, true);
    	addDialogComponent(m_eventAttrFilterComp);
    }
    
    /**
     * here we want to use the spec available to check 
     */
    
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
    	
    	
	    	if(!(specs[0] instanceof XLogPortObjectSpec))
	    		throw new NotConfigurableException("the spec does not have the right type");
	    	XLogPortObjectSpec logSpec = (XLogPortObjectSpec) specs[0];
	    
	    	if(!logSpec.getGTraceAttrMap().keySet().containsAll(m_traceAttrSet.getIncludeList())) {
	    		// here how to know if it has some changes, we have the excluded list there
	    		m_traceAttrSet.setIncludeList(logSpec.getGTraceAttrMap().keySet());
		    	m_traceAttrSet.setExcludeList(new String[0]);
		    	
	    	}
	    	
	    	if(!logSpec.getGEventAttrMap().keySet().containsAll(m_traceAttrSet.getIncludeList())) {

		    	m_eventAttrSet.setIncludeList(logSpec.getGEventAttrMap().keySet());
		    	m_eventAttrSet.setExcludeList(new String[0]);
	    	}
	    
    }
}

