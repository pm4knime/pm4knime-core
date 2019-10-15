package org.pm4knime.node.discovery.heuritsicsminer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.discovery.inductiveminer.InductiveMinerNodeModel;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
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
public class HeuristicsMinerNodeModel extends NodeModel {
	
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	logger.info("Begin Heuristic Miner");
        // TODO: due heutistic parameter setting, we generate a heuristic model here, 
    	// one is to convert it directly into Petri net , another is to create separate portObject here
    	// create one model here to show HeuristicNet. So we need to create one net object to read and write it again?? 
    	XLogPortObject logPortObject = null ;
    	XLog log = null;
    	for(PortObject obj: inData)
        	if(obj instanceof XLogPortObject) {
        		logPortObject = (XLogPortObject)obj;
        		break;
        	}
        
        log = logPortObject.getLog();
    	
    	PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(FlexibleHeuristicsMinerPlugin.class);
    	
    	HeuristicsMinerSettings heuristicsMinerSettings = getConfiguration();
    	hnet = FlexibleHeuristicsMinerPlugin.run(pluginContext, log, heuristicsMinerSettings);
    	
    	Object[] result = HeuristicsNetToPetriNetConverter.converter(pluginContext, hnet);
    	AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) result[0], (Marking) result[1]);
    	
    	PetriNetPortObject pnPO = new PetriNetPortObject(anet);
    	
    	logger.info("End: heuristics miner");
        return new PortObject[]{pnPO};
    }

    public HeuristicsNet getHNet() {
    	return hnet; 
    }
    
    private HeuristicsMinerSettings getConfiguration() {
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
		heuristicsMinerSettings.setClassifier(getXEventClassifier());
		
		return heuristicsMinerSettings;
	}

	private XEventClassifier getXEventClassifier() {
		// TODO Auto-generated method stub
		return new XEventNameClassifier();
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc} here is XLogPortObject in need and it outputs the heutistic port object
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        // TODO: generated method stub
    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	
        return new PortObjectSpec[]{new PetriNetPortObjectSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: 
    	m_r2b.saveSettingsTo(settings);
    	m_dependency.saveSettingsTo(settings); 
    	m_length1Loop.saveSettingsTo(settings); 
        m_length2Loop.saveSettingsTo(settings); 
        m_longDistance.saveSettingsTo(settings);
        
        m_allConnected.saveSettingsTo(settings);
        m_withLT.saveSettingsTo(settings);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	
    	m_r2b.loadSettingsFrom(settings);
    	m_dependency.loadSettingsFrom(settings); 
    	m_length1Loop.loadSettingsFrom(settings); 
        m_length2Loop.loadSettingsFrom(settings); 
        m_longDistance.loadSettingsFrom(settings);
        
        m_allConnected.loadSettingsFrom(settings);
        m_withLT.loadSettingsFrom(settings);
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

