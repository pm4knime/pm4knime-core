package org.pm4knime.node.discovery.heuritsicsminer;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.DefaultMinerNodeModel;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.heuristicsnet.miner.heuristics.converter.HeuristicsNetToPetriNetConverter;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.FlexibleHeuristicsMinerPlugin;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;

/**
 * <code>NodeModel</code> for the "HeuristicsMiner" node.
 *
 * @author Kefang Ding
 */
public class HeuristicsMinerNodeModel extends DefaultMinerNodeModel implements PortObjectHolder{
	
	private static final NodeLogger logger = NodeLogger
            .getLogger(HeuristicsMinerNodeModel.class);
	
	// the following are the required parameters configuration
	public static final String CFGKEY_THRESHOLD_R2B = "Threshold: Relative-to-best";
	public static final String CFGKEY_THRESHOLD_DEPENDENCY = "Threshold: Dependency";
	public static final String CFGKEY_THRESHOLD_LENGTH_ONE_LOOP = "Threshold: Length-one-loops";
	public static final String CFGKEY_THRESHOLD_LENGTH_TWO_LOOP = "Threshold: Length-two-loops";
	public static final String CFGKEY_THRESHOLD_DISTANCE = "Threshold: Long distance";
	public static final String CFGKEY_TASK_CONNECTION = "All tasks connected";
	public static final String CFGKEY_LONG_DEPENDENCY = "Long distance dependency";
	
	private final SettingsModelDoubleBounded m_r2b = new SettingsModelDoubleBounded(CFGKEY_THRESHOLD_R2B, 5, 0, 100);
	private final SettingsModelDoubleBounded m_dependency = new SettingsModelDoubleBounded(CFGKEY_THRESHOLD_DEPENDENCY, 90, 0, 100);
	private final SettingsModelDoubleBounded m_length1Loop  = new SettingsModelDoubleBounded(CFGKEY_THRESHOLD_LENGTH_ONE_LOOP, 90, 0, 100);
	private final SettingsModelDoubleBounded m_length2Loop = new SettingsModelDoubleBounded(CFGKEY_THRESHOLD_LENGTH_TWO_LOOP, 90, 0, 100);
	private final SettingsModelDoubleBounded m_longDistance = new SettingsModelDoubleBounded(CFGKEY_THRESHOLD_DISTANCE, 90, 0, 100);
	
	private final SettingsModelBoolean m_allConnected = new SettingsModelBoolean(CFGKEY_TASK_CONNECTION, true);
	private final SettingsModelBoolean m_withLT = new SettingsModelBoolean(CFGKEY_LONG_DEPENDENCY, false);
	
	private HeuristicsNet hnet;
    /**
     * Constructor for the node model.
     */
    protected HeuristicsMinerNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(new PortType[] { XLogPortObject.TYPE }, 
        		new PortType[] { PetriNetPortObject.TYPE });
    }

    
    @Override
	protected PortObject mine(XLog log, final ExecutionContext exec) throws Exception{
    	logger.info("Begin: Heuristic Miner");
    	
    	PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(FlexibleHeuristicsMinerPlugin.class);
    	checkCanceled(pluginContext, exec);
    	HeuristicsMinerSettings heuristicsMinerSettings = getConfiguration();
    	hnet = FlexibleHeuristicsMinerPlugin.run(pluginContext, log, heuristicsMinerSettings);
    	
    	checkCanceled(pluginContext, exec);
    	Object[] result = HeuristicsNetToPetriNetConverter.converter(pluginContext, hnet);
    	
    	AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) result[0], (Marking) result[1]);
    	
    	checkCanceled(exec);
    	PetriNetPortObject pnPO = new PetriNetPortObject(anet);
    	
    	logger.info("End: Heuristics miner");
    	return pnPO;
    }

    // to use hnet, we need to make sure it is serialized there.. Else, it can't open it again
    // when we reload the workflow there
    public HeuristicsNet getHNet() {
    	return hnet; 
    }
    
    
    HeuristicsMinerSettings getConfiguration() {
		// TODO build setting parameter from node model
    	HeuristicsMinerSettings heuristicsMinerSettings = new HeuristicsMinerSettings();
    	
    	heuristicsMinerSettings.setRelativeToBestThreshold(m_r2b.getDoubleValue()/100);
		heuristicsMinerSettings.setDependencyThreshold(m_dependency.getDoubleValue()/100);
		heuristicsMinerSettings.setL1lThreshold(m_length1Loop.getDoubleValue()/100);
		heuristicsMinerSettings.setL2lThreshold(m_length2Loop.getDoubleValue()/100);
		heuristicsMinerSettings.setLongDistanceThreshold(m_longDistance.getDoubleValue()/100);
		heuristicsMinerSettings.setUseAllConnectedHeuristics(m_allConnected.getBooleanValue());
		heuristicsMinerSettings.setUseLongDistanceDependency(m_withLT.getBooleanValue());
		heuristicsMinerSettings.setCheckBestAgainstL2L(false);
		heuristicsMinerSettings.setAndThreshold(Double.NaN);
		
		XEventClassifier classifier = getEventClassifier();
		heuristicsMinerSettings.setClassifier(classifier);
		
		return heuristicsMinerSettings;
	}


    /**
     * {@inheritDoc} here is XLogPortObject in need and it outputs the heutistic port object
     */
    @Override
    protected PortObjectSpec[] configureOutSpec(XLogPortObjectSpec logSpec) {

        return new PortObjectSpec[]{new PetriNetPortObjectSpec()};
    }

    // to create view after reloading, we need to save XLogPortObject as the internal PortObject
    // which means, we need to override the execution method there
	@Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] {logPO};
	}

	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		// TODO here is no use from portObjects, because what we need is the HNet, and we can't serialize it!!
		// we can't save it from the portObject
		logPO = (XLogPortObject) portObjects[0];
	}

	@Override
	protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
		m_r2b.saveSettingsTo(settings);
    	m_dependency.saveSettingsTo(settings); 
    	m_length1Loop.saveSettingsTo(settings); 
        m_length2Loop.saveSettingsTo(settings); 
        m_longDistance.saveSettingsTo(settings);
        
        m_allConnected.saveSettingsTo(settings);
        m_withLT.saveSettingsTo(settings);
	}

	@Override
	protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_r2b.loadSettingsFrom(settings);
    	m_dependency.loadSettingsFrom(settings); 
    	m_length1Loop.loadSettingsFrom(settings); 
        m_length2Loop.loadSettingsFrom(settings); 
        m_longDistance.loadSettingsFrom(settings);
        
        m_allConnected.loadSettingsFrom(settings);
        m_withLT.loadSettingsFrom(settings);
	}
     
   
}

