package org.pm4knime.node.discovery.dfgminer.knimeTable;

import java.io.File;
import java.io.IOException;


import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.js.core.node.AbstractSVGWizardNodeModel;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.AbstractDotPanelPortObject;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.portobject.DfgMsdPortObjectSpec;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.processmining.framework.packages.PackageManager.Canceller;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerWithoutLogPlugin;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;
import org.processmining.plugins.inductiveminer2.withoutlog.variants.MiningParametersIMWithoutLog;
import org.processmining.processtree.ProcessTree;

/**
 * <code>NodeModel</code> for the "InductiveMinerDFGTable" node.
 *
 * @author
 */
public class InductiveMinerDFGTableNodeModel extends AbstractSVGWizardNodeModel<JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {

	private static final NodeLogger logger = NodeLogger.getLogger(InductiveMinerDFGTableNodeModel.class);
	public static final String[] CFG_VARIANT_VALUES = { "Inductive Miner - directly follows (IMd)",
			"Inductive Miner - infrequent - directly follows (IMfd)",
			"Inductive Miner - incompleteness - directly follows (IMcd)" };

	public static final String CFGKEY_NOISE_THRESHOLD = "Noise Threshold";

	SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(CFGKEY_NOISE_THRESHOLD, 0.8, 0, 1.0);
	protected ProcessTreePortObject ptpo;
	protected DfgMsdPortObject dfgMsdPO;

	/**
	 * Constructor for the node model.
	 */
	protected InductiveMinerDFGTableNodeModel() {

		super(new PortType[] { DfgMsdPortObject.TYPE }, new PortType[] { ProcessTreePortObject.TYPE }, "Process Tree JS View");
	}

	
	@Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{ptpo};
    }
	
	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		
		logger.info("Begin:  Inductive miner Miner");

		dfgMsdPO = (DfgMsdPortObject) inObjects[0];

		DfgMsd dfmMsd = dfgMsdPO.getDfgMsd();

		MiningParametersIMWithoutLog params = new MiningParametersIMWithoutLog();
		params.setNoiseThreshold((float) m_noiseThreshold.getDoubleValue());

		EfficientTree ptEff = InductiveMinerWithoutLogPlugin.mineTree(dfmMsd, new MiningParametersIMWithoutLog(),
				new Canceller() {
					public boolean isCancelled() {
						try {
							exec.checkCanceled();
						} catch (final CanceledExecutionException ce) {
							return true;
						}
						return false;
					}
				});

		ProcessTree tree = EfficientTree2processTree.convert(ptEff);

		ptpo = new ProcessTreePortObject(tree);
		logger.info("End:  Inductive Miner");
		
		final String dotstr;
		JSGraphVizViewRepresentation representation = getViewRepresentation();

		synchronized (getLock()) {
			AbstractDotPanelPortObject port_obj = (AbstractDotPanelPortObject) ptpo;
			Dot dot =  port_obj.getDotPanel().getDot();
			dotstr = dot.toString();
		}
		representation.setDotstr(dotstr);

	}



	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(DfgMsdPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid DFG model!");

		DfgMsdPortObjectSpec dfgSpec = new DfgMsdPortObjectSpec();

		return new PortObjectSpec[]{new ProcessTreePortObjectSpec()};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_noiseThreshold.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_noiseThreshold.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
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


	public PortObject[] getInternalPortObjects() {
		return new PortObject[] {dfgMsdPO};
	}

	public void setInternalPortObjects(PortObject[] portObjects) {
		dfgMsdPO = (DfgMsdPortObject) portObjects[0];
	}

}
