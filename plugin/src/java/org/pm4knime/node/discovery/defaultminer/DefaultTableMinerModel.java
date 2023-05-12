package org.pm4knime.node.discovery.defaultminer;

import org.knime.core.data.DataTableSpec;
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
import org.knime.js.core.node.AbstractSVGWizardNodeModel;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.AbstractDotPanelPortObject;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.plugins.graphviz.dot.Dot;

//public abstract class DefaultTableMinerModel extends DefaultNodeModel implements PortObjectHolder {
public abstract class DefaultTableMinerModel extends AbstractSVGWizardNodeModel<JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {

	protected DefaultTableMinerModel(PortType[] inPortTypes, PortType[] outPortTypes, String view_name) {
		super(inPortTypes, outPortTypes, view_name);
	}
	
	
	public static final String KEY_TRACE_CLASSIFIER = "Trace Classifier";
	public static final String KEY_EVENT_CLASSIFIER = "Event Classifier";
	public static final String KEY_CLASSIFIER_SET = "Classifier Set";
	
	protected String t_classifier;
	protected String e_classifier;
	
	protected BufferedDataTable logPO;
	protected PortObject pmPO;
	
	
	@Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{pmPO};
    }
	
	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		
		logPO = (BufferedDataTable)inObjects[0];
		pmPO = mine(logPO, exec);
		
		final String dotstr;
		JSGraphVizViewRepresentation representation = getViewRepresentation();

		synchronized (getLock()) {
			AbstractDotPanelPortObject port_obj = (AbstractDotPanelPortObject) pmPO;
			Dot dot =  port_obj.getDotPanel().getDot();
			dotstr = dot.toString();
		}
		representation.setDotstr(dotstr);

	}
	
	
	protected abstract PortObject mine(BufferedDataTable log, final ExecutionContext exec) throws Exception; 
	
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(DataTableSpec.class))
			throw new InvalidSettingsException("Input is not a valid Table!");
		DataTableSpec logSpec = (DataTableSpec) inSpecs[0];
		if(e_classifier == null || t_classifier == null)
			throw new InvalidSettingsException("Classifiers are not set!");
		
		return configureOutSpec(logSpec);
	}


	protected abstract PortObjectSpec[] configureOutSpec(DataTableSpec logSpec);	
	
	
	public String getEventClassifier() {
		return e_classifier;		
	}
	
	
	public String getTraceClassifier() {
		return t_classifier;		
	}
	
	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		settings.addString(KEY_TRACE_CLASSIFIER, t_classifier);
		settings.addString(KEY_EVENT_CLASSIFIER, e_classifier);
		saveSpecificSettingsTo(settings);
	}
	
	
	protected abstract void saveSpecificSettingsTo(NodeSettingsWO settings);

	
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		validateSpecificSettings(settings);
	}
	
	
	protected abstract void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException;
	
	
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		t_classifier = settings.getString(KEY_TRACE_CLASSIFIER);
		e_classifier = settings.getString(KEY_EVENT_CLASSIFIER);
		loadSpecificValidatedSettingsFrom(settings);
	}

	
	protected abstract void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException;
	
	
	public PortObject[] getInternalPortObjects() {
		return new PortObject[] {logPO};
	}

	
	public void setInternalPortObjects(PortObject[] portObjects) {
		logPO = (BufferedDataTable) portObjects[0];
	}


	public void setTraceClassifier(String c) {
		t_classifier = c;
	}


	public void setEventClassifier(String c) {
		e_classifier = c;
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
	
}
