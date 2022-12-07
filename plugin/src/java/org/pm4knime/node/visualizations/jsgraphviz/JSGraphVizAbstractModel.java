package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.js.core.node.AbstractSVGWizardNodeModel;
import org.processmining.plugins.graphviz.dot.Dot;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.base.data.xml.SvgCell;
import org.pm4knime.portobject.AbstractDotPanelPortObject;


public class JSGraphVizAbstractModel extends AbstractSVGWizardNodeModel<JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {

	private static PortType[] OUT_TYPES = {ImagePortObject.TYPE};
	private static PortType IN_TYPE;
	AbstractDotPanelPortObject port_obj;
	

	public JSGraphVizAbstractModel(PortType[] in_types, String view_name) {
		super(in_types, OUT_TYPES, view_name);
		IN_TYPE = in_types[0];
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

		PortObjectSpec imageSpec = new ImagePortObjectSpec(SvgCell.TYPE);
        
        return new PortObjectSpec[]{imageSpec};
	}

	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		final String dotstr;
		
		JSGraphVizViewRepresentation representation = getViewRepresentation();

		synchronized (getLock()) {
			
			port_obj = (AbstractDotPanelPortObject) inObjects[0];
			//System.out.println(processtree.getSummary());
			Dot dot =  port_obj.getDotPanel().getDot();
			dotstr = dot.toString();

		}
		
		representation.setDotstr(dotstr);
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
		return new PortObject[] {port_obj};
	}

	
	public void setInternalPortObjects(PortObject[] portObjects) {
		port_obj = (AbstractDotPanelPortObject) portObjects[0];
	}
	
	@Override
    protected boolean generateImage() {
        return true;
    }
	
	@Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{svgImageFromView};
    }
	
}
