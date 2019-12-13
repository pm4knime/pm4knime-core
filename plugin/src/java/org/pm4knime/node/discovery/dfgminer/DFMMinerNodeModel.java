package org.pm4knime.node.discovery.dfgminer;

import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.DFMPortObject;
import org.pm4knime.portobject.DFMPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.defaultnode.DefaultMinerNodeModel;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.directlyfollowsmodel.DirectlyFollowsModel;
import org.processmining.plugins.directlyfollowsmodel.mining.DFMMiner;
import org.processmining.plugins.directlyfollowsmodel.mining.DFMMiningParameters;
import org.processmining.plugins.directlyfollowsmodel.mining.DFMMiningParametersAbstract;
import org.processmining.plugins.directlyfollowsmodel.mining.variants.DFMMiningParametersDefault;

/**
 * <code>NodeModel</code> for the "DFMMiner" node.
 * 
 * @reference code :
 *            org.processmining.plugins.directlyfollowsmodel.mining.plugins.DirectlyFollowsModelMinerPlugin
 * @author Kefang Ding
 */
public class DFMMinerNodeModel extends DefaultMinerNodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(DFMMinerNodeModel.class);

	// one parameter is the setnoise, one is the classifier, those two like
	// inductive miner
	public static final String CFG_NOISE_THRESHOLD_KEY = "Noise threshold";
	public static final String CFG_LCC_KEY = "LifeCycle classifier";
	static List<XEventClassifier> lcClassifierList = XLogUtil.getDefaultLifeCycleClassifier();

	private SettingsModelString m_lcClf = new SettingsModelString(DFMMinerNodeModel.CFG_LCC_KEY, "");
	private SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(
			DFMMinerNodeModel.CFG_NOISE_THRESHOLD_KEY, 0.8, 0, 1.0);

	/**
	 * Constructor for the node model.
	 */
	protected DFMMinerNodeModel() {
		// TODO: input is an event log PortObject, the output is a DFMPortObject
		super(new PortType[] { XLogPortObject.TYPE }, new PortType[] { DFMPortObject.TYPE });
	}

	@Override
	protected PortObject mine(XLog log) throws Exception {
		// TODO Auto-generated method stub
		logger.info("Begin:  DFM Miner");
		DFMMiningParameters params = createParameter();
		DirectlyFollowsModel dfm = DFMMiner.mine(log, params, null);

		DFMPortObject dfmPO = new DFMPortObject(dfm);
		logger.info("End:  DFM Miner");
		return dfmPO;
	}

	private DFMMiningParameters createParameter() {
		// TODO Auto-generated method stub
		DFMMiningParametersAbstract params = new DFMMiningParametersDefault();

		XEventClassifier clf = getEventClassifier();
		params.setClassifier(clf);

		XEventClassifier lcclf = XLogUtil.getXEventClassifier(m_lcClf.getStringValue(), lcClassifierList);
		params.setLifeCycleClassifier((XLifeCycleClassifier) lcclf);

		params.setNoiseThreshold(m_noiseThreshold.getDoubleValue());
		return params;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(XLogPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid Event Log!");

		return new PortObjectSpec[] { new DFMPortObjectSpec() };
	}


	@Override
	protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		m_lcClf.saveSettingsTo(settings);
		m_noiseThreshold.saveSettingsTo(settings);
	}

	@Override
	protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_noiseThreshold.loadSettingsFrom(settings);
		m_lcClf.loadSettingsFrom(settings);
	}

}
