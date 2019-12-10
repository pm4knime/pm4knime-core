package org.pm4knime.node.discovery.inductiveminer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersEKS;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMflc;
import org.processmining.plugins.InductiveMiner.plugins.IM;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of InductiveMiner.
 * use the inductive miner to do process discovery
 *
 * @author KFDing
 */
public class InductiveMinerNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(InductiveMinerNodeModel.class);
  
	public static final String[] defaultType = {
			"Inductive Miner", //
			"Inductive Miner - Infrequent", //
			"Inductive Miner - Incompleteness", //
			"Inductive Miner - exhaustive K-successor", //
			"Inductive Miner - Life cycle"
	};

	public static final String CFGKEY_METHOD_TYPE = "InductiveMinerMethod";

	public static final String CFGKEY_NOISE_THRESHOLD = "NoiseThreshold";
	public static final String CFGKEY_CLASSIFIER = "Classifier";
	
	private static boolean withNoiseThreshold = false;

	private SettingsModelString m_type =  new SettingsModelString(InductiveMinerNodeModel.CFGKEY_METHOD_TYPE, defaultType[0]);

	private SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(InductiveMinerNodeModel.CFGKEY_NOISE_THRESHOLD, 0.0, 0, 1.0);
	
	
	// for classifer, only event name is possible now to choose and show
	
	// if we want it simpler solution, just set the event name as the default one...
	// if we want to update on them, we need to get the event log values from this
	// simple solution here, at first!! 
	private static Collection<XEventClassifier> classifiers = setDefaultClassifier();
	public static List<String> defaultClassifer = setClassifierNames(classifiers);
	
	private SettingsModelString m_classifier = new SettingsModelString(InductiveMinerNodeModel.CFGKEY_CLASSIFIER, "");
	
    /**
     * Constructor for the node model.
     */
    protected InductiveMinerNodeModel() {
    	super(new PortType[] { XLogPortObject.TYPE },
				new PortType[] { PetriNetPortObject.TYPE });
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects,
            final ExecutionContext exec) throws Exception {

        logger.info("Begin: Inductive Miner");
        
        XLogPortObject logPO = null;
        // get the log from inData
        for(PortObject obj: inObjects)
        	if(obj instanceof XLogPortObject) {
        		logPO = (XLogPortObject)obj;
        		break;
        	}
        
        XLog log = logPO.getLog();
        
        PluginContext context =  PM4KNIMEGlobalContext.instance().getPluginContext(); //.getFutureResultAwarePluginContext(IM.class);
        
        Object[] result = IM.minePetriNet(context, log, createParameters());
        AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) result[0], (Marking) result[1],  (Marking) result[2]);
        
        PetriNetPortObject pnPO = new PetriNetPortObject(anet);
        
        logger.info("End: Inductive Miner");
        return new PortObject[] { pnPO};
    }

    private  MiningParameters createParameters() throws InvalidSettingsException {
    	MiningParameters param;
    	
    	if(m_type.getStringValue().equals(defaultType[0]))
        	param = new MiningParametersIM();
        else if(m_type.getStringValue().equals(defaultType[1]))
        	param = new MiningParametersIMf();
        else if(m_type.getStringValue().equals(defaultType[2]))
        	param = new MiningParametersIMf();
        else if(m_type.getStringValue().equals(defaultType[3]))
        	param = new MiningParametersEKS();
        else if(m_type.getStringValue().equals(defaultType[4]))
        	param = new MiningParametersIMflc();
        else 
        	throw new InvalidSettingsException("unknown inductive miner type "+ m_type.getStringValue());
    	
    	
        param.setNoiseThreshold((float) m_noiseThreshold.getDoubleValue());
        // we need to give one of the classifier to have it
        // we need to get the classifer for use?? The classifier is extracted from the event log
        // so check the event log and find out the real classifier !!
        
        XEventClassifier classifier = mapClassifier(m_classifier.getStringValue());
        param.setClassifier(classifier);
    	
        return param;
    } 
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Code executed on reset.
    }
    
    private XEventClassifier mapClassifier(String name) {
    	for(XEventClassifier clf: classifiers) {
			if(clf.name().equals(name))
				return clf;
		}
    	return null;
    }
    
    // here we collect all the classifiers, the classifier should be changed due to the different event log
    // it should be object based, shouldn't be static to class!!! 
    private static Collection<XEventClassifier> setDefaultClassifier(){
    	Collection<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();
    	// we should be able to get the classifiers from the event log and save it in PortObjecSpec. 
    	// choose it and do it now!! 
		classifiers.add(new XEventNameClassifier());
		return classifiers;
    }
    
    public static List<String> setClassifierNames(Collection<XEventClassifier> classList){
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
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	
    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	PetriNetPortObjectSpec pnSpec = new PetriNetPortObjectSpec();
        return new PortObjectSpec[]{pnSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_type.saveSettingsTo(settings);
       if(isWithNoiseThreshold())
    	   m_noiseThreshold.saveSettingsTo(settings);
    }

   
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
          m_type.loadSettingsFrom(settings);
//          setWithNoiseThreshold();
          if(isWithNoiseThreshold())
        	  m_noiseThreshold.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
       m_type.validateSettings(settings);
       setWithNoiseThreshold();
       if(isWithNoiseThreshold())
    	   m_noiseThreshold.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

    }

	public  boolean isWithNoiseThreshold() {
		
		return withNoiseThreshold;
	}

	public void setWithNoiseThreshold() {
		if(m_type.getStringValue().equals(defaultType[0])) {
			withNoiseThreshold = false;
//			m_noiseThreshold = new SettingsModelDoubleBounded(CFGKEY_NOISE_THRESHOLD, 0.2, 0, 1.0);
		}else
			withNoiseThreshold = true;
	}

}

