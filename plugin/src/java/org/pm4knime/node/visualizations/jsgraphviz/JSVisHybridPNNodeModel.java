package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.js.core.node.AbstractWizardNodeModel;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.processmining.plugins.graphviz.dot.Dot;


public class JSVisHybridPNNodeModel extends AbstractWizardNodeModel<JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	private static final PortType[] IN_TYPES = {HybridPetriNetPortObject.TYPE};
	private static final PortType[] OUT_TYPES = {};

	public JSVisHybridPNNodeModel() {
		super(IN_TYPES, OUT_TYPES, "JSVisHybridPN");
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
		return new PortObjectSpec[] {};
	}

	@Override
	protected PortObject[] performExecute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		final String dotstr;
		
		JSGraphVizViewRepresentation representation = getViewRepresentation();

		synchronized (getLock()) {
			
			HybridPetriNetPortObject petrinet = (HybridPetriNetPortObject) inObjects[0];
			//System.out.println(processtree.getSummary());
			Dot dot =  petrinet.getDotPanel().getDot();
			dotstr = dot.toString();

		}
		
		representation.setDotstr(dotstr);

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
}
