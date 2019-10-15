package org.pm4knime.node.discovery.dfgminer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
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
import org.pm4knime.node.discovery.heuritsicsminer.HeuristicsMinerNodeModel;
import org.pm4knime.node.discovery.inductiveminer.InductiveMinerNodeModel;
import org.pm4knime.portobject.DFMPortObject;
import org.pm4knime.portobject.DFMPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycleClassifier;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.directlyfollowsmodel.DirectlyFollowsModel;
import org.processmining.plugins.directlyfollowsmodel.mining.DFMMiner;
import org.processmining.plugins.directlyfollowsmodel.mining.DFMMiningParameters;
import org.processmining.plugins.directlyfollowsmodel.mining.DFMMiningParametersAbstract;
import org.processmining.plugins.directlyfollowsmodel.mining.variants.DFMMiningParametersDefault;

/**
 * <code>NodeModel</code> for the "DFMMiner" node.
 * 
 * @author Kefang Ding
 */
public class DFMMinerNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger
            .getLogger(DFMMinerNodeModel.class);
	
	// one parameter is the setnoise, one is the classifier, those two like inductive miner
	public static final String CFG_NOISE_THRESHOLD_KEY = "NoiseThreshold";
	public static final String CFG_CLASSIFIER_KEY = "Classifier";
	public static final String CFG_LCC_KEY = "LifeCycleClassifier";
	private static Collection<XEventClassifier> clfs = setDefaultClassifier();
	private static Collection<XEventClassifier> lcClfs = setDefaultLCClassifier();
	public static List<String> defaultClfNames = setClassifierNames(clfs);
	public static List<String> defaultLcClfNames = setClassifierNames(lcClfs);
	
		
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
    	XEventClassifier clf = mapClassifier(m_clf.getStringValue(), clfs);
    	params.setClassifier(clf);
    	
    	XLifeCycleClassifier lcclf = (XLifeCycleClassifier) mapClassifier(m_lcClf.getStringValue(), lcClfs);
    	params.setLifeCycleClassifier( lcclf);
    	
		return params;
	}

    private XEventClassifier mapClassifier(String name, Collection<XEventClassifier> clfs) {
    	for(XEventClassifier clf: clfs) {
			if(clf.name().equals(name))
				return clf;
		}
    	return null;
    }

    private static List<String> setClassifierNames(Collection<XEventClassifier> classList){
    	List<String> nameList = new ArrayList();
    	
		for(XEventClassifier clf: classList) {
			nameList.add(clf.name());
		}
	
		return nameList;
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
		classifiers.add(new XEventNameClassifier());
		return classifiers;
    }
    
    private static Collection<XEventClassifier> setDefaultLCClassifier() {
		// TODO Auto-generated method stub
    	Collection<XEventClassifier> classifiers = new ArrayList<>();
		classifiers.add(new LifeCycleClassifier());
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

