package org.pm4knime.node.discovery.dfgminer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
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
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.directlyfollowsmodel.DirectlyFollowsModel;
import org.processmining.plugins.directlyfollowsmodel.mining.DFMMiner;
import org.processmining.plugins.directlyfollowsmodel.mining.DFMMiningParameters;
import org.processmining.plugins.directlyfollowsmodel.mining.DFMMiningParametersAbstract;
import org.processmining.plugins.directlyfollowsmodel.mining.variants.DFMMiningParametersDefault;

/**
 * <code>NodeModel</code> for the "DFMMiner" node.
 * @reference code : org.processmining.plugins.directlyfollowsmodel.mining.plugins.DirectlyFollowsModelMinerPlugin
 * @author Kefang Ding
 */
public class DFMMinerNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger
            .getLogger(DFMMinerNodeModel.class);
	
	// one parameter is the setnoise, one is the classifier, those two like inductive miner
	public static final String CFG_NOISE_THRESHOLD_KEY = "NoiseThreshold";
	public static final String CFG_CLASSIFIER_KEY = "Classifier";
	public static final String CFG_LCC_KEY = "LifeCycleClassifier";
	static List<XEventClassifier> classifierList = XLogUtil.getECList();
	static List<XEventClassifier> lcClassifierList = XLogUtil.getDefaultLifeCycleClassifier();
	
	private SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(DFMMinerNodeModel.CFG_NOISE_THRESHOLD_KEY, 0.8, 0, 1.0);
	private SettingsModelString m_clf = new SettingsModelString(DFMMinerNodeModel.CFG_CLASSIFIER_KEY, "");
	private SettingsModelString m_lcClf = new SettingsModelString(DFMMinerNodeModel.CFG_LCC_KEY, "");
	
    /**
     * Constructor for the node model.
     */
    protected DFMMinerNodeModel() {
        // TODO: input is an event log PortObject, the output is a DFMPortObject
        super(new PortType[] {XLogPortObject.TYPE}, new PortType[] {DFMPortObject.TYPE} );
    }

    
	/**
     * {@inheritDoc} It accepts an event log as input and creates a Directly-follows model as output.
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Begin:  DFM Miner");
    	
    	XLogPortObject logPortObject = null ;
    	for(PortObject obj: inData)
        	if(obj instanceof XLogPortObject) {
        		logPortObject = (XLogPortObject)obj;
        		break;
        	}
        
    	XLog log = logPortObject.getLog();
    	// there are two parameters here, we can use one get function 
    	// but first try the default setting, later to add the dialog
    	DFMMiningParameters params = createParameter();
    	DirectlyFollowsModel dfm = DFMMiner.mine(log, params, null);
    
    	DFMPortObject dfmPO = new DFMPortObject(dfm);
    	
    	return new PortObject[]{dfmPO};
    }

    
    private DFMMiningParameters createParameter() {
		// TODO Auto-generated method stub
    	DFMMiningParametersAbstract params =  new DFMMiningParametersDefault();
    	params.setNoiseThreshold(m_noiseThreshold.getDoubleValue());
    	XEventClassifier clf = XLogUtil.getXEventClassifier(m_clf.getStringValue(), classifierList);
    	params.setClassifier(clf);
    	
    	XEventClassifier lcclf = XLogUtil.getXEventClassifier(m_lcClf.getStringValue(), lcClassifierList);
    	params.setLifeCycleClassifier( (XLifeCycleClassifier) lcclf);
    	
		return params;
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
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	
    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
        return new PortObjectSpec[]{new DFMPortObjectSpec()};
    }

    private static Collection<XEventClassifier> setDefaultClassifier(){
    	Collection<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();
    	// what happens if we only use this one classifier?? How about the logSpec introduced before??
    	// we can also use the information to classify the event and get the directly-follow relation..
    	// However, the current situation is quite ok. Then save it. 
    	// but it is not consistent with other miner, change it to use XLogUtil. 
		classifiers.add(new XEventNameClassifier());
		return classifiers;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_noiseThreshold.saveSettingsTo(settings);
    	m_clf.saveSettingsTo(settings);
    	m_lcClf.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_noiseThreshold.loadSettingsFrom(settings);
    	m_clf.loadSettingsFrom(settings);
    	m_lcClf.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

