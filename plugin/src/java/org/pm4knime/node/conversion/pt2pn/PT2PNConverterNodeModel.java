package org.pm4knime.node.conversion.pt2pn;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.js.core.node.AbstractSVGWizardNodeModel;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.AbstractDotPanelPortObject;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.processtree.ProcessTree;

/**
 * <code>NodeModel</code> for the "PT2PNConverter" node. It converts a process tree into Petri net.
 * Since the conversion is guaranteed to work, so no need of NodeDialog. 
 *
 * @author Kefang Ding
 */
public class PT2PNConverterNodeModel extends AbstractSVGWizardNodeModel<JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {
	// the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(PT2PNConverterNodeModel.class);
	protected PortObject pnPO;
	protected ProcessTreePortObject ptPO;
    /**
     * Constructor for the node model.
     */
    protected PT2PNConverterNodeModel() {
    	super(new PortType[] { ProcessTreePortObject.TYPE },
				new PortType[] { PetriNetPortObject.TYPE }, "Petri Net JS View");
    }

    
    @Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{pnPO};
    }
	
	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		
    	ptPO = (ProcessTreePortObject) inObjects[0];
    	ProcessTree tree = ptPO.getTree();

    	ProcessTree2Petrinet.PetrinetWithMarkings pn = ProcessTree2Petrinet.convert(tree, false);

		AcceptingPetriNet anet = AcceptingPetriNetFactory.createAcceptingPetriNet(pn.petrinet, pn.initialMarking,
				pn.finalMarking);

		pnPO = new PetriNetPortObject(anet);
        
		
		final String dotstr;
		JSGraphVizViewRepresentation representation = getViewRepresentation();

		synchronized (getLock()) {
			AbstractDotPanelPortObject port_obj = (AbstractDotPanelPortObject) pnPO;
			Dot dot =  port_obj.getDotPanel().getDot();
			dotstr = dot.toString();
		}
		representation.setDotstr(dotstr);

	}
    
   

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	if(!inSpecs[0].getClass().equals(ProcessTreePortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid process tree!");
    	
    	ProcessTreePortObjectSpec logSpec = (ProcessTreePortObjectSpec) inSpecs[0];
		
		return configureOutSpec(logSpec);
    }

    
    protected PortObjectSpec[] configureOutSpec(ProcessTreePortObjectSpec logSpec) {

        return new PortObjectSpec[]{new PetriNetPortObjectSpec()};
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
    
    @Override
	protected void performReset() {
	}

	@Override
	protected void useCurrentValueAsDefault() {
	}

	
	@Override
    protected boolean generateImage() {
        return false;
    }
	
	
	@Override
	public JSGraphVizViewRepresentation createEmptyViewRepresentation() {
		return new JSGraphVizViewRepresentation();
	}

	@Override
	public JSGraphVizViewValue createEmptyViewValue() {
		return new JSGraphVizViewValue();
	}
	
	@Override
	public boolean isHideInWizard() {
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public ValidationError validateViewValue(JSGraphVizViewValue viewContent) {
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}
	
	
	@Override
	public String getJavascriptObjectID() {
		return "org.pm4knime.node.visualizations.jsgraphviz.component";
	}


	@Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] {ptPO};
	}


	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		ptPO = (ProcessTreePortObject) portObjects[0];
		
	}
	
}

