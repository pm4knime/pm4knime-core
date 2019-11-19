package org.pm4knime.node.conversion.log2table;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogSpecUtil;

/**
 * <code>NodeDialog</code> for the "Xlog2CSVConverter" node.
 * After adding the log attributes as XLogPortObjectSpec, we can choose the attributes to convert into data table.
 * 
 *  Using the column selection here
 * @author Kefang
 */
public class XLog2TableConverterNodeDialog extends DefaultNodeSettingsPane {

	private SettingsModelFilterString m_traceAttrSet, m_eventAttrSet;
    /**
     * New pane for configuring the Xlog2CSVConverter node.
     */
    protected XLog2TableConverterNodeDialog() {
    	// we should get the attribute set from the spec
    	createNewTab("Choose trace attributes");
    	m_traceAttrSet = new SettingsModelFilterString(XLogSpecUtil.CFG_KEY_TRACE_ATTRSET, new String[]{}, new String[]{}, false );
    	DialogComponentColumnFilter m_traceAttrFilterComp = new DialogComponentColumnFilter(m_traceAttrSet,0, true);
        addDialogComponent(m_traceAttrFilterComp);
        
        createNewGroup("Choose event Attributes");
    	m_eventAttrSet = new SettingsModelFilterString(XLogSpecUtil.CFG_KEY_EVENT_ATTRSET, new String[]{}, new String[]{}, false );
    	DialogComponentColumnFilter m_eventAttrFilterComp = new DialogComponentColumnFilter(m_eventAttrSet,0, true);
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
    	
    	// we can check the m_traceAttrSet situations. 
    	// in default, it can load the values from save and other stuff, but when it is 
    	// initialized, there is no values here, so we get values from PortObjectSpec
    	if(m_traceAttrSet.getIncludeList().isEmpty() && m_traceAttrSet.getExcludeList().isEmpty())
    		if(m_eventAttrSet.getIncludeList().isEmpty() && m_eventAttrSet.getExcludeList().isEmpty()) {
    	
		    	if(!(specs[0] instanceof XLogPortObjectSpec))
		    		throw new NotConfigurableException("the spec does not have the right type");
		    	XLogPortObjectSpec logSpec = (XLogPortObjectSpec) specs[0];
		    	
		    	m_traceAttrSet.setIncludeList(logSpec.getGTraceAttrMap().keySet());
		    	m_traceAttrSet.getExcludeList().clear();
		    	
		    	m_eventAttrSet.setIncludeList(logSpec.getGEventAttrMap().keySet());
		    	m_traceAttrSet.getExcludeList().clear();
    		}
    }
}

