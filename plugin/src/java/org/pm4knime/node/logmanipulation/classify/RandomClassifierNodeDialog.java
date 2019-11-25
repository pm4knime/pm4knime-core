package org.pm4knime.node.logmanipulation.classify;

import java.util.List;

import javax.swing.JPanel;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObject;
import org.pm4knime.portobject.XLogPortObject;
import org.processmining.incorporatenegativeinformation.dialogs.ui.VariantWholeView;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;
import org.processmining.incorporatenegativeinformation.models.TraceVariant;

/**
 * <code>NodeDialog</code> for the "RandomClassifier" Node.
 * RandomClassifier classifies the event log randomly, and assigns labels to the trace
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 *  
 * @Date: Modification on this node. To allow the panel classify. Not just like this. 
 * -- Add one imported panel to it. 
 * -- needs the event logs as inputs 
 * -- no settings are in need
 * @author Kefang Ding
 */
public class RandomClassifierNodeDialog extends DataAwareNodeDialogPane {
	JPanel controlPanel;
	VariantWholeView vPanel;
    /**
     * New pane for configuring the RandomClassifier node.
     */
    protected RandomClassifierNodeDialog() {
    	controlPanel = new JPanel();
    	addTab("Options", controlPanel);
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObject[] input) throws NotConfigurableException {
		// get the event log and create one panel on it
		XLog log =((XLogPortObject) input[0]).getLog();
		
		List<TraceVariant> variants = EventLogUtilities.getTraceVariants(log);
		XLogInfo info = XLogInfoFactory.createLogInfo(log);
		// how to return the log with labels back?? 
		// after the loadSettingsFrom it, how to give it back?? 
		controlPanel.remove(vPanel);
		vPanel = new VariantWholeView(variants, info);
		controlPanel.add(vPanel);
		
		// if we set it as the execution part, what to do next?? 
		// we remember the logs and the nodes from it ?
	}
}

