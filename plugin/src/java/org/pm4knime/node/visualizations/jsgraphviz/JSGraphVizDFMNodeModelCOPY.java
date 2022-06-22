package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.discovery.dfgminer.DFM2PMNodeModel;
import org.pm4knime.portobject.DFMJSPortObject;
import org.pm4knime.portobject.DFMPortObject;
import org.pm4knime.portobject.DFMPortObjectSpec;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;



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
public class JSGraphVizDFMNodeModelCOPY extends DefaultNodeModel implements PortObjectHolder {
	private Dfg dfg;
	private static final NodeLogger logger = NodeLogger
            .getLogger(DFM2PMNodeModel.class);


	private static final PortType[] IN_TYPES = {DFMPortObject.TYPE};
	private static final PortType[] OUT_TYPES = {DFMJSPortObject.TYPE};
	
	public static final String CFG_KEY_CLASSIFIER = "Event Classifier";
	public static final String CFG_KEY_CLASSIFIER_SET = "Event Classifier Set";
    protected SettingsModelString m_classifier =  new SettingsModelString(CFG_KEY_CLASSIFIER, "");
	SettingsModelStringArray classifierSet = new SettingsModelStringArray(CFG_KEY_CLASSIFIER_SET, 
			new String[] {""}) ;
	protected DFMPortObject dfmPO = null;

	public JSGraphVizDFMNodeModelCOPY() {
		super(IN_TYPES, OUT_TYPES);
	}


    
	@Override
	protected PortObject[] execute(final PortObject[] inObjects,
	            final ExecutionContext exec) throws Exception {
		dfmPO = (DFMPortObject) inObjects[0];
		PortObject newPO = new DFMJSPortObject(dfmPO.getDfm());
		return new PortObject[] {newPO};
	}
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(DFMPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a causal graph!");		
		DFMPortObjectSpec logSpec = (DFMPortObjectSpec) inSpecs[0];
		
		return configureOutSpec(logSpec);
	}
	
	protected PortObjectSpec[] configureOutSpec(DFMPortObjectSpec spec) {

        return new PortObjectSpec[]{new DFMPortObjectSpec()};
    }
	
	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_classifier.saveSettingsTo(settings);
		classifierSet.saveSettingsTo(settings);
	}
	
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_classifier.validateSettings(settings);
	}
	
	
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_classifier.loadSettingsFrom(settings);
		classifierSet.loadSettingsFrom(settings);
	}



	public PortObject[] getInternalPortObjects() {
		return new PortObject[] {dfmPO};
	}

	public void setInternalPortObjects(PortObject[] portObjects) {
		dfmPO = (DFMPortObject) portObjects[0];
	}

}