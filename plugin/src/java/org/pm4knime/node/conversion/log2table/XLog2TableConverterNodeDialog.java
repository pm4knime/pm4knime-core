package org.pm4knime.node.conversion.log2table;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    	
    		// if we only check the include lists here;
    		// when the new spec includes more values, it can't add the new values into the 
    	// right place here. to make sure that they are from the same values, 
    	// we check the both sides. get all values from the traceAttrSet, compare '
    	// if they are the same, if they are not, then change the traceAttrSet to the new values here
	    	if(!(specs[0] instanceof XLogPortObjectSpec))
	    		throw new NotConfigurableException("the spec does not have the right type");
	    	XLogPortObjectSpec logSpec = (XLogPortObjectSpec) specs[0];
	    	
	    	List<String> configTraceAttrColumns = new ArrayList<String>(m_traceAttrSet.getIncludeList());
	    	configTraceAttrColumns.addAll(m_traceAttrSet.getExcludeList());
	    	Set<String> specTraceColumns = logSpec.getGTraceAttrMap().keySet();
	    	
	    	if(!specTraceColumns.containsAll(configTraceAttrColumns) 
	    		|| !configTraceAttrColumns.containsAll(specTraceColumns)) {
	    		// here how to know if it has some changes, we have the excluded list there
	    		m_traceAttrSet.setIncludeList(specTraceColumns);
		    	m_traceAttrSet.setExcludeList(new String[0]);
		    	
	    	}
	    	
	    	List<String> configEventAttrColumns = new ArrayList<String>(m_eventAttrSet.getIncludeList());
	    	configEventAttrColumns.addAll(m_eventAttrSet.getExcludeList());
	    	Set<String> specEventColumns = logSpec.getGEventAttrMap().keySet();
	    	
	    	
	    	if(!specEventColumns.containsAll(configEventAttrColumns) 
		    		|| !configEventAttrColumns.containsAll(specEventColumns)) {
		    	m_eventAttrSet.setIncludeList(specEventColumns);
		    	m_eventAttrSet.setExcludeList(new String[0]);
	    	}
	    
    }
}

