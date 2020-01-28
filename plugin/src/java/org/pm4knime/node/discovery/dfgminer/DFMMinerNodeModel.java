package org.pm4knime.node.discovery.dfgminer;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.DFMPortObject;
import org.pm4knime.portobject.DFMPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.defaultnode.DefaultMinerNodeModel;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoStartEndComplete;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycleClassifier;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.inductiveminer2.helperclasses.XLifeCycleClassifierIgnore;

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
	public static final String CFG_VARIANT_KEY= "Variant";
	public static final String[] CFG_VARIANT_VALUES = {"use event classes - (IMd, IMfd, IMcd)", 
			"use event classes and life cycles - (IMdlc, IMfdlc, IMcdlc)", "use event classes but mind partial traces - (IMpt)"};
	
	SettingsModelString m_variant = new SettingsModelString(DFMMinerNodeModel.CFG_VARIANT_KEY, "");
	public static List<String> sClfNames; 
	public static List<XEventClassifier> sClassifierList ;
	static {
		sClassifierList = new ArrayList<XEventClassifier>();
		XEventNameClassifier eventNameClf = new XEventNameClassifier();
		XEventAndClassifier lfAndEventClf  = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		sClassifierList.add(eventNameClf);
		sClassifierList.add(lfAndEventClf);
		
		sClfNames = new ArrayList<String>();
		sClfNames.add(eventNameClf.name());
		sClfNames.add(lfAndEventClf.name());
		
	}
	
	/**
	 * Constructor for the node model.
	 */
	protected DFMMinerNodeModel() {
		// TODO: input is an event log PortObject, the output is a DFMPortObject
		super(new PortType[] { XLogPortObject.TYPE }, new PortType[] { DFMPortObject.TYPE });
		
	}

	@Override
	protected PortObject mine(XLog log, final ExecutionContext exec) throws Exception {
		// TODO Auto-generated method stub
		logger.info("Begin:  DFM Miner");
		
		XEventClassifier activityClassifier = getEventClassifier();
		XLifeCycleClassifier lifeCycleClassifier = getLifeCycleClassifier();
		// according to the variant values, there is different implementation
		IMLog2IMLogInfo logInfo ;
		if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[0])) {
			logInfo = new IMLog2IMLogInfoDefault();
		}else if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[1])) {
			logInfo = new IMLog2IMLogInfoLifeCycle();
		}else if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[2])) {
			logInfo = new IMLog2IMLogInfoStartEndComplete();
		}else {
			throw new Exception("not found variant type");
		}
		
		
		IMLog IM_log = new IMLogImpl(log, activityClassifier, lifeCycleClassifier);
		Dfg dfg = logInfo.createLogInfo(IM_log).getDfg();
		
		DFMPortObject dfmPO = new DFMPortObject(dfg);
		logger.info("End:  DFM Miner");
		return dfmPO;
	}

	private XLifeCycleClassifier getLifeCycleClassifier() {
		// set one option to use the life cycle here
		// one option is that we check the life cycle here. 
		// m_classifier is not the decision factor
		if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[1])) 
			return new LifeCycleClassifier();
		else {
			XLifeCycleClassifier defaultLifeCycleClassifier = new XLifeCycleClassifierIgnore();
			return defaultLifeCycleClassifier;
		}
	}
	
	@Override
	public XEventClassifier getEventClassifier() {
		XEventClassifier nClf = super.getEventClassifier();
		if(nClf != null)
			return nClf;
		else {
			// consider the specific classifier and add them here
			for(XEventClassifier clf: sClassifierList) {
				if(clf.name().equals(m_classifier.getStringValue()))
					return clf;
			}
		}
		return null;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configureOutSpec(XLogPortObjectSpec logSpec) {
		return new PortObjectSpec[] { new DFMPortObjectSpec() };
	}


	@Override
	protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		m_variant.saveSettingsTo(settings);
	}

	@Override
	protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_variant.validateSettings(settings);
	}

	@Override
	protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_variant.loadSettingsFrom(settings);
	}

}
