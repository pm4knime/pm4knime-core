package org.pm4knime.node.conversion.pt2pn;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.processtree.ProcessTree;

/**
 * <code>NodeModel</code> for the "PT2PNConverter" node. It covverts a process tree into Petri net.
 * Since the conversion is guaranteed to work, so no need of NodeDialog. 
 *
 * @author Kefang Ding
 */
public class PT2PNConverterNodeModel extends NodeModel {
	// the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(PT2PNConverterNodeModel.class);
    /**
     * Constructor for the node model.
     */
    protected PT2PNConverterNodeModel() {
    	super(new PortType[] { ProcessTreePortObject.TYPE },
				new PortType[] { PetriNetPortObject.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects,
            final ExecutionContext exec) throws Exception {
    	// check the input type and convert it
    	logger.info("Begin: Conversion from process tree to Petri net");
    	ProcessTreePortObject ptPO = (ProcessTreePortObject) inObjects[0];
    	ProcessTree tree = ptPO.getTree();
    	
    	ProcessTree2Petrinet.PetrinetWithMarkings pn = ProcessTree2Petrinet.convert(tree, false);

		AcceptingPetriNet anet = AcceptingPetriNetFactory.createAcceptingPetriNet(pn.petrinet, pn.initialMarking,
				pn.finalMarking);

		PetriNetPortObject pnPO = new PetriNetPortObject(anet);
        
    	logger.info("End: Conversion from process tree to Petri net");
        return new PortObject[]{pnPO};
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
    	if(!inSpecs[0].getClass().equals(ProcessTreePortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid process tree!");
    	
        return new PortObjectSpec[]{null};
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
        // TODO: generated method stub
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

