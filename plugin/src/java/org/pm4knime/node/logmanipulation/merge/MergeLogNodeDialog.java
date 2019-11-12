package org.pm4knime.node.logmanipulation.merge;

import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.ui.DialogComponentAttributesFilter;

/**
 * <code>NodeDialog</code> for the "MergeLog" node.
 * It includes dialog components for parameters: 
 *   <1> if to merge trace with same identifier
 *   	<2> only if the merge is chosen, the next chioces are enabled
 *   		if to merge the event with same label together?? 
 *   			<3> enabled due to the last merge, which event attributes are used?
 *   			choose the first one, or the second one?? How flexible are attributes?? 
 *   Another strategy can be:: 
 *   	-- it keeps some parts out!! If seeing the same identifier, then not use them!! 
 *   	But the different ones are merged together. Even, 
 *   	-- they only merge the traces with the same identifiers. The others are not used
 *   
 *   It might be called event log intersection, but not used so often. 
 *   
 *  treat the trace: 
 *     -- separate, 
 *        trace and event attributes no effect 
 *     -- ignore the repetive ones from the first or second one,
 *     	  trace and event attributes no effect
 *     -- merge the ones with same identifier but with conditions on the trace and event attributes:: 
 *        how to choose the trace and event attributes later?? 
 *        for the same trace attributes, we can also do the three choices but for all attributes!! 
 *        -- if we want to select the attributes for generated trace, we can't do it!! 
 *        But I'd like to say, it is better to provide the attributes list for them to choose 
 *        in the new generated event log, and also the event attributes there!! 
 *        We don't want the duplicated situations!! 
 *        
 * 
 * which event attributes you want to keep in the new merged data?? 
 * One is the trace attributes to choose, 
 * one is the event attributes to choose for the same trace 
 *  
 * It is a fancy feature to add some statistic information before merge?? Like 
 *  -- how many traces with the same identifier?? 
 *  -- how many event attributes are the same?
 *  We can always give the statistical information before merging. With data aware, but also the model information from it..
 *  Optional Step 1: show stat info: traces with same identifier, same trace attributes, same event attributes
 *  
 *  Step 1: create tables to choose trace attributes 
 *  Step 2: accept the parameters. If we choose the complex way, we'd better set a parameter for this. Else,
 *  a simple way is OK.. 
 *  
 *  Step 3 : deal with the action changes in the model.
 * @author Kefang Ding
 */
public class MergeLogNodeDialog extends DataAwareNodeDialogPane {
	private int num = MergeLogNodeModel.CGF_INPUTS_NUM;
	protected JPanel m_compositePanel;
	SettingsModelString m_strategy;
	
	SettingsModelString[] m_traceIDs;
	SettingsModelString[] m_eventIDs;
	SettingsModelFilterString m_traceAttrSet , m_eventAttrSet;
	DialogComponentAttributesFilter m_traceAttrFilterComp, m_eventAttrFilterComp;
	
	private DialogComponentStringSelection[] tIDComps, eIDComps;
    /**
     * New pane for configuring the MergeLog node.
     */
    protected MergeLogNodeDialog() {
    	m_compositePanel = new JPanel();
        m_compositePanel.setLayout(new BoxLayout(m_compositePanel,
                BoxLayout.Y_AXIS));
        super.addTab("Options", m_compositePanel);
        
    	// set parameters
    	m_strategy = new SettingsModelString(MergeLogNodeModel.CFG_KEY_TRACE_STRATEGY, MergeLogNodeModel.CFG_TRACE_STRATEGY[2]);
    	DialogComponentStringSelection strategyComp = new DialogComponentStringSelection(m_strategy,"Choose Merge Strategy" ,MergeLogNodeModel.CFG_TRACE_STRATEGY);
    	m_compositePanel.add(strategyComp.getComponentPanel());
    	
    	// add additional equation to choose the comparison
    	
    	m_traceIDs = new SettingsModelString[num];
    	m_eventIDs = new SettingsModelString[num];
    	tIDComps = new DialogComponentStringSelection[num];
    	eIDComps = new DialogComponentStringSelection[num];
    	for(int i=0; i< num;i++) {
    		Box idChoosePane = createBox(true);
    		m_traceIDs[i] = new SettingsModelString(MergeLogNodeModel.CFG_KEY_CASE_ID[i], "");
    		// create comp for this traeID to make them equal
    		tIDComps[i] = new DialogComponentStringSelection(m_traceIDs[i],"Choose Trace ID for log " + i, new String[]{" "});
    		idChoosePane.add(tIDComps[i].getComponentPanel());
    		
    		m_eventIDs[i] = new SettingsModelString(MergeLogNodeModel.CFG_KEY_EVENT_ID[i], "");	
    		eIDComps[i] = new DialogComponentStringSelection(m_eventIDs[i],"Event ID: ", new String[]{" "});
    		idChoosePane.add(eIDComps[i].getComponentPanel());
    		
    		m_compositePanel.add(idChoosePane);
    	}
    	
    	
    	m_traceAttrSet = new SettingsModelFilterString(MergeLogNodeModel.CFG_KEY_TRACE_ATTRSET, new String[]{}, new String[]{}, true );
    	//m_traceAttrSet.setEnabled(false);
    	m_traceAttrFilterComp = new DialogComponentAttributesFilter(m_traceAttrSet, true);
    	m_compositePanel.add(m_traceAttrFilterComp.getComponentPanel());
    	
    	m_eventAttrSet = new SettingsModelFilterString(MergeLogNodeModel.CFG_KEY_EVENT_ATTRSET, new String[]{}, new String[]{}, true );
    	// m_eventAttrSet.setEnabled(false);
    	// here must put them as the DataCell.class format. They are designed for DataTableSpec.
    	// so for us, we don't has DataTable as input, so there is some problems to updata
    	// but the ways to do this, we can create DataTaleSpecs for the inputs ports
    	m_eventAttrFilterComp = new DialogComponentAttributesFilter(m_eventAttrSet, true);
    	m_compositePanel.add(m_eventAttrFilterComp.getComponentPanel());
    	
    	
    	// we always ignore the repetive caseId from second log
    	m_strategy.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				// TODO if is is merge, then we enable the m_traceAttr and m_eventAttrSet, else disable
				if(m_strategy.getStringValue().equals(MergeLogNodeModel.CFG_TRACE_STRATEGY[2])) {
					// internal trace merge
					m_traceAttrSet.setEnabled(true);	
					m_eventAttrSet.setEnabled(false);
					// m_compositePanel.repaint();
				}else if(m_strategy.getStringValue().equals(MergeLogNodeModel.CFG_TRACE_STRATEGY[3])) {
					// internal event merge
					m_traceAttrSet.setEnabled(true);
					m_eventAttrSet.setEnabled(true);
				}else {
					m_traceAttrSet.setEnabled(false);
					m_eventAttrSet.setEnabled(false);
				}
			}
    		
    	});
    	
    	
    }
    
    protected void addDialogComponent(final DialogComponent diaC) {
		// TODO how to change the horizontal 
    	m_compositePanel.add(diaC.getComponentPanel());
	}

    private Box createBox(final boolean horizontal) {
        final Box box;
        if (horizontal) {
            box = new Box(BoxLayout.X_AXIS);
            box.add(Box.createVerticalGlue());
        } else {
            box = new Box(BoxLayout.Y_AXIS);
            box.add(Box.createHorizontalGlue());
        }
         return box;
    }
    
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_strategy.saveSettingsTo(settings);
		
		
		for(int i=0; i< num; i++) {
    		m_traceIDs[i].saveSettingsTo(settings);
    		m_eventIDs[i].saveSettingsTo(settings);
    	}
		
		m_traceAttrSet.saveSettingsTo(settings);
		m_eventAttrSet.saveSettingsTo(settings);
	}
	
	
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObject[] input) throws NotConfigurableException {
		// if we don't have such values, we need to search for event log. But if we have it. No need?
		
		if(!(input[0] instanceof XLogPortObject) || !(input[1] instanceof XLogPortObject)) {
			// we can get the log object and then set all the choice of them, but at first, we need to put all the stuff here again
			throw new NotConfigurableException("This is not valid event log inputs");
		}
		// get the two logs for merge
		XLog log0 = ((XLogPortObject) input[0]).getLog();
		XLog log1 = ((XLogPortObject) input[1]).getLog();
		XLog[] logs = {log0, log1};
		// do we need to generate the Spec for XLog here in different order??  
		// we need another dialog there to use this
		// get the trace and event attributes list from both logs in tables 
		double percent = 0.2;
		
		List<String> tNameList = new LinkedList();
		
		for(int i = 0; i< num; i++) {
			// TODO: give log also in array, to make codes dense
			List<XAttribute> tAttrList =  XLogUtil.getTAttributes(logs[i], percent);
			
			List<String> tmpNameList = XLogUtil.getAttrNameList(tAttrList);
			
			int ti=0;
			for(; ti< tmpNameList.size(); ti++) {
				tmpNameList.set(ti, MergeLogNodeModel.CFG_ATTRIBUTE_PREFIX + i + tmpNameList.get(ti)); 
			}
			tIDComps[i].replaceListItems(tmpNameList, tmpNameList.get(0));

			tNameList.addAll(tmpNameList);
		}
		// how to make the include and exclude at this step??
		m_traceAttrSet.setNewValues(tNameList, new LinkedList<String>(), true);
    	
		List<String> eNameList = new LinkedList();
		for(int i = 0; i< num; i++) {
			// TODO: give log also in array, to make codes dense
			List<XAttribute> eAttrList = XLogUtil.getEAttributes(logs[i], percent);
			
			List<String> tmpNameList = XLogUtil.getAttrNameList(eAttrList);
			
			int ei=0;
			for(; ei< tmpNameList.size(); ei++) {
				tmpNameList.set(ei, MergeLogNodeModel.CFG_ATTRIBUTE_PREFIX + i + tmpNameList.get(ei)); 
			}
			eIDComps[i].replaceListItems(tmpNameList, tmpNameList.get(0));
			eNameList.addAll(tmpNameList);
		}
		m_eventAttrSet.setNewValues(eNameList, new LinkedList<String>(), true);
		
//		System.out.println(m_eventAttrSet.getIncludeList().size());
//		// filter is wrong which causes the proble not good!!
//		System.out.println(m_eventAttrFilterComp.getValidIncludeColumns().size());
//		System.out.println(m_eventAttrFilterComp.getValidExcludeColumns().size());
		// how to update the values and show in dialog??
		m_compositePanel.updateUI();
		m_compositePanel.repaint();
	}
}

