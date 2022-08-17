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
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.portobject.DfgMsdPortObjectSpec;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.framework.packages.PackageManager.Canceller;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerPlugin;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerWithoutLogPlugin;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;
import org.processmining.plugins.inductiveminer2.withoutlog.variants.MiningParametersIMWithoutLog;
import org.processmining.processtree.ProcessTree;

/**
 * <code>NodeModel</code> for the "InductiveMinerDFGTable" node.
 *
 * @author
 */
public class InductiveMinerDFGTableNodeModel extends DefaultNodeModel {

	private static final NodeLogger logger = NodeLogger.getLogger(InductiveMinerDFGTableNodeModel.class);
	public static final String CFG_VARIANT_KEY = "Variant";
	public static final String[] CFG_VARIANT_VALUES = { "Inductive Miner - directly follows (IMd)",
			"Inductive Miner - infrequent - directly follows (IMfd)",
			"Inductive Miner - incompleteness - directly follows (IMcd)" };

	public static final String CFGKEY_NOISE_THRESHOLD = "Noise Threshold";

	SettingsModelString m_variant = new SettingsModelString(CFG_VARIANT_KEY, "");
	SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(CFGKEY_NOISE_THRESHOLD, 0.8, 0, 1.0);

	/**
	 * Constructor for the node model.
	 */
	protected InductiveMinerDFGTableNodeModel() {

		super(new PortType[] { DfgMsdPortObject.TYPE }, new PortType[] { ProcessTreePortObject.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
		logger.info("Begin:  Inductive miner Miner");

		DfgMsdPortObject dfgMsdPO = (DfgMsdPortObject) inData[0];

		DfgMsd dfmMsd = dfgMsdPO.getDfgMsd();

		MiningParametersIMWithoutLog params = new MiningParametersIMWithoutLog();
		params.setNoiseThreshold((float) m_noiseThreshold.getDoubleValue());

		EfficientTree ptEff = InductiveMinerWithoutLogPlugin.mineTree(dfmMsd, new MiningParametersIMWithoutLog(),
				new Canceller() {
					public boolean isCancelled() {
						try {
							checkCanceled(exec);
						} catch (final CanceledExecutionException ce) {
							return true;
						}
						return false;
					}
				});

		ProcessTree tree = EfficientTree2processTree.convert(ptEff);

		checkCanceled(exec);
		ProcessTreePortObject petriObj = new ProcessTreePortObject(tree);
		logger.info("End:  Inductive Miner");
		return new PortObject[] { petriObj };
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
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(DfgMsdPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid DfgMsd  model!");

		ProcessTreePortObjectSpec ptPOSpec = new ProcessTreePortObjectSpec();

		return new ProcessTreePortObjectSpec[] { ptPOSpec };
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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO: generated method stub
	}

}
