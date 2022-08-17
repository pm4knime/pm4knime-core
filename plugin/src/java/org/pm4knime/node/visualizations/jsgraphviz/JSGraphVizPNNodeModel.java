package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.js.core.node.AbstractWizardNodeModel;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.processmining.plugins.graphviz.dot.Dot;


/**
 * This is an example implementation of the node model of the
 * "JSGraphViz" node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author 
 */
public class JSGraphVizPNNodeModel extends AbstractWizardNodeModel<JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {

	// Input and output port types
	private static final PortType[] IN_TYPES = {PetriNetPortObject.TYPE};
	private static final PortType[] OUT_TYPES = {};
	PetriNetPortObject petrinet;

	public JSGraphVizPNNodeModel() {
		super(IN_TYPES, OUT_TYPES, "JSGraphVizPN");
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
	public String getJavascriptObjectID() {
		return "org.pm4knime.node.visualizations.jsgraphviz.component";
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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(PetriNetPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid Petri net!");
		return new PortObjectSpec[] {};
	}

	@Override
	protected PortObject[] performExecute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		final String dotstr;
		
		JSGraphVizViewRepresentation representation = getViewRepresentation();

		synchronized (getLock()) {
			
			petrinet = (PetriNetPortObject) inObjects[0];
			//System.out.println(processtree.getSummary());
			Dot dot =  petrinet.getDotPanel().getDot();
			dotstr = dot.toString();

		}
		
		representation.setDotstr(dotstr);

		// The FlowVariablePortObject ports are a mockup. They are not actually
		// necessary as the flow
		// variables are shared across the workflow.
		return new PortObject[] {};
	}

	@Override
	protected void performReset() {
	}

	@Override
	protected void useCurrentValueAsDefault() {
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
	}
	
	public PortObject[] getInternalPortObjects() {
		return new PortObject[] {petrinet};
	}

	
	public void setInternalPortObjects(PortObject[] portObjects) {
		petrinet = (PetriNetPortObject) portObjects[0];
	}
}
