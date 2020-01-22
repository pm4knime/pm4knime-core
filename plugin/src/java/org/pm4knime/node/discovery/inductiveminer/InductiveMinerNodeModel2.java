package org.pm4knime.node.discovery.inductiveminer;

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
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.defaultnode.DefaultMinerNodeModel;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersEKS;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMflc;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.processtree.ProcessTree;

public class InductiveMinerNodeModel2 extends DefaultMinerNodeModel {
	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(InductiveMinerNodeModel2.class);

	public static final String[] defaultType = { "Inductive Miner", //
			"Inductive Miner - Infrequent", //
			"Inductive Miner - Incompleteness", //
			"Inductive Miner - exhaustive K-successor", //
			"Inductive Miner - Life cycle" };

	public static final String CFG_KEY_METHOD_TYPE = "Method";
	public static final String CFG_KEY_NOISE_THRESHOLD = "Noise Threshold";

	private SettingsModelString m_type = new SettingsModelString(InductiveMinerNodeModel.CFGKEY_METHOD_TYPE,
			defaultType[1]);
	private SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(
			InductiveMinerNodeModel.CFGKEY_NOISE_THRESHOLD, 0.0, 0, 1.0);

	protected InductiveMinerNodeModel2() {
		super(new PortType[] { XLogPortObject.TYPE }, new PortType[] { ProcessTreePortObject.TYPE });
	}

	@Override
	protected PortObjectSpec[] configureOutSpec(XLogPortObjectSpec logSpec) {
		// TODO Auto-generated method stub
		ProcessTreePortObjectSpec ptSpec = new ProcessTreePortObjectSpec();
		return new PortObjectSpec[] { ptSpec };
	}
	

	@Override
	protected PortObject mine(XLog log) throws Exception {
		// TODO the most important part, however, should we set the classifier
		// as one parameter, and no need to explicitly infer the classifier from it ??
		// Now, it is just fine
		// the same effort to use it
		logger.info("Begin: Inductive Miner");

		ProcessTree pt = IMProcessTree.mineProcessTree(log, createParameters());

		ProcessTreePortObject pnPO = new ProcessTreePortObject(pt);
		logger.info("End: Inductive Miner");
		return pnPO;
	}

	private MiningParameters createParameters() throws InvalidSettingsException {
		MiningParameters param;

		if (m_type.getStringValue().equals(defaultType[0]))
			param = new MiningParametersIM();
		else if (m_type.getStringValue().equals(defaultType[1]))
			param = new MiningParametersIMf();
		else if (m_type.getStringValue().equals(defaultType[2]))
			param = new MiningParametersIMf();
		else if (m_type.getStringValue().equals(defaultType[3]))
			param = new MiningParametersEKS();
		else if (m_type.getStringValue().equals(defaultType[4]))
			param = new MiningParametersIMflc();
		else
			throw new InvalidSettingsException("unknown inductive miner type " + m_type.getStringValue());

		param.setNoiseThreshold((float) m_noiseThreshold.getDoubleValue());

		XEventClassifier classifier = getEventClassifier();
		param.setClassifier(classifier);

		param.getLifeCycleClassifier();
		return param;
	}

	@Override
	protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		m_type.saveSettingsTo(settings);
//		 noiseThreshold, we need to save it. Only difference is that if it can be used in later process. 
//		 so no need to check if it is enabled or not. We just let it be 
//		 if(m_noiseThreshold.isEnabled())
		m_noiseThreshold.saveSettingsTo(settings);
	}

	@Override
	protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_type.validateSettings(settings);
		String tmp = settings.getString(m_type.getKey());

		if (tmp.equals(defaultType[0])) {
			m_noiseThreshold.setEnabled(false);
		} else {
			m_noiseThreshold.setEnabled(true);
			m_noiseThreshold.validateSettings(settings);
		}
	}

	@Override
	protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_type.loadSettingsFrom(settings);
		m_noiseThreshold.loadSettingsFrom(settings);
	}

	
}
