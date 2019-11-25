package org.pm4knime.node.logmanipulation.classify;


import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.processmining.incorporatenegativeinformation.dialogs.ui.VariantWholeView;
import org.processmining.incorporatenegativeinformation.help.Configuration;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;
import org.processmining.incorporatenegativeinformation.models.TraceVariant;

/**
 * This is the model implementation of RandomClassifier.
 * RandomClassifier classifies the event log randomly, and assigns labels to the trace
 *
 * @author Kefang Ding
 */
public class RandomClassifierNodeModel extends NodeModel {
	
    /**
     * Constructor for the node model.
     */
    protected RandomClassifierNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { XLogPortObject.TYPE },
				new PortType[] { XLogPortObject.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: assign the labels to event log according to the overlap rate and positive rate
    	if(! (inData[0] instanceof XLogPortObject))
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	XLogPortObject logPortObject = (XLogPortObject) inData[0];
    	XLog log = logPortObject.getLog();
    	if(log.isEmpty()) {
    		throw new InvalidSettingsException("This event log is empty, reset a new event log");
    	}
    	// how to add the attributes to new event log?? We can do it later.
    	XFactory factory = XFactoryRegistry.instance().currentDefault();
    	XLog label_log = (XLog) log.clone();
		XLogInfo info = XLogInfoFactory.createLogInfo(label_log);
		
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean(Configuration.POS_LABEL, false, null));
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean(Configuration.FIT_LABEL, false, null));
		
		List<TraceVariant> variants = EventLogUtilities.getTraceVariants(label_log);
		VariantWholeView view = new VariantWholeView(variants, info);
		view.setSize(1000, 1000);
		
		// how to show this out?? This is one problem!! How to 
		int n = JOptionPane.showConfirmDialog(null, view, "Classify Variants", JOptionPane.YES_NO_OPTION);
		// if it is cancelled, what to do ?? we return back the old xlog and give it back
		if(n== JOptionPane.YES_OPTION)
			return new PortObject[]{new XLogPortObject(label_log)};
		else
			return  new PortObject[]{logPortObject};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	
        // TODO: check if the inport object is XLOgPortObject
    	if(! (inSpecs[0] instanceof XLogPortObjectSpec))
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
        return new PortObjectSpec[]{new XLogPortObjectSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
       
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

