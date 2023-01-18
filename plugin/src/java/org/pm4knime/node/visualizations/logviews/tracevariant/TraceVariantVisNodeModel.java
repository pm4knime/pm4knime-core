package org.pm4knime.node.visualizations.logviews.tracevariant;

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
import org.processmining.plugins.graphviz.dot.Dot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;


public class TraceVariantVisNodeModel extends AbstractWizardNodeModel<TraceVariantVisViewRepresentation, TraceVariantVisViewValue> implements PortObjectHolder {

	// Input and output port types
	private static final PortType[] IN_TYPES = {BufferedDataTable.TYPE};
	private static final PortType[] OUT_TYPES = {};
	
	public static final String KEY_TRACE_CLASSIFIER = "Trace Classifier";
	public static final String KEY_EVENT_CLASSIFIER = "Event Classifier";
	public static final String KEY_CLASSIFIER_SET = "Classifier Set";
	
	protected String t_classifier;
	protected String e_classifier;
	
	protected BufferedDataTable table;

	public TraceVariantVisNodeModel() {
		super(IN_TYPES, OUT_TYPES, "Trace Variant Explorer");
	}

	@Override
	public TraceVariantVisViewRepresentation createEmptyViewRepresentation() {
		return new TraceVariantVisViewRepresentation();
	}

	@Override
	public TraceVariantVisViewValue createEmptyViewValue() {
		return new TraceVariantVisViewValue();
	}

	@Override
	public String getJavascriptObjectID() {
		return "org.pm4knime.node.visualizations.logviews.tracevariant.component";
	}

	@Override
	public boolean isHideInWizard() {
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public ValidationError validateViewValue(TraceVariantVisViewValue viewContent) {
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { };
	}

	@Override
	protected PortObject[] performExecute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		table = (BufferedDataTable)inObjects[0];
		TraceVariantVisViewRepresentation representation = getViewRepresentation();
		
		String[] columns = table.getDataTableSpec().getColumnNames();
		String [] data = new String[columns.length+2];
		data[0] = Long.toString(table.size());
		data[1] = Long.toString(columns.length);
		for (int i=0; i<columns.length; i++) {
			data[i+2] = columns[i];
		}
		representation.setData(data);
		
		return new PortObject[] { };
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
		return new PortObject[] {table};
	}

	
	public void setInternalPortObjects(PortObject[] portObjects) {
		table = (BufferedDataTable) portObjects[0];
	}

	
	public String getEventClassifier() {
		return e_classifier;		
	}
	
	
	public String getTraceClassifier() {
		return t_classifier;		
	}
	
	public void setTraceClassifier(String c) {
		t_classifier = c;
	}


	public void setEventClassifier(String c) {
		e_classifier = c;
	}
	
	
}