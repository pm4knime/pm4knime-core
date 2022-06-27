package org.pm4knime.node.discovery.cgminer;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.pm4knime.portobject.CausalGraphPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.extendedhybridminer.algorithms.HybridCGMiner;
import org.processmining.extendedhybridminer.algorithms.preprocessing.LogFilterer;
import org.processmining.extendedhybridminer.algorithms.preprocessing.TraceVariantsLog;
import org.processmining.extendedhybridminer.models.causalgraph.ExtendedCausalGraph;
import org.processmining.extendedhybridminer.plugins.HybridCGMinerPlugin;
import org.processmining.extendedhybridminer.plugins.HybridCGMinerSettings;


public class CGMinerNodeModel extends DefaultNodeModel implements PortObjectHolder {
	
	private static final NodeLogger logger = NodeLogger
            .getLogger(CGMinerNodeModel.class);
	
	public static final String FILTER_ACTIVITY = "<html><b>Minimal activity frequency:</b><br>an activity will be included if it occurs in at least x% of cases; <br>set to 0 to include all activities.</html>";
	public static final String FILTER_TRACE = "<html><b>Minimal trace variant frequency:</b><br>a trace variant will be included if it covers at least x% of cases; <br>set to 0 to include all trace variants.</html>";
	public static final String THRESHOLD_CERTAIN_EDGES = "<html><b>Strong causality threshold:</b><br>lower bound for a strong causality between two activities.</html>";
	public static final String THRESHOLD_UNCERTAIN = "<html><b>Weak causality threshold:</b><br>lower bound for a weak causality between two activities; <br>set to 100% to avoid uncertain edges.</html>";
	public static final String THRESHOLD_LONG_DEPENDENCY = "<html><b>Long-term dependency threshold:</b><br>lower bound for a strong long-term causality between two activities.</html>";
	public static final String WEIGHT = "<html><b>Causality weight threshold:</b><br>high values mean more emphasis on the split and join behavior of activities; <br>low values mean more emphasis on the detection of concurrency and loops.</html>";

	public static final SettingsModelDoubleBounded filter_a = new SettingsModelDoubleBounded(FILTER_ACTIVITY, 0, 0, 100);
	public static final SettingsModelDoubleBounded filter_t = new SettingsModelDoubleBounded(FILTER_TRACE, 0, 0, 100);
	public static final SettingsModelDoubleBounded t_certain  = new SettingsModelDoubleBounded(THRESHOLD_CERTAIN_EDGES, 0.4, 0, 1);
	public static final SettingsModelDoubleBounded t_uncertain = new SettingsModelDoubleBounded(THRESHOLD_UNCERTAIN, 0.3, 0, 1);
	public static final SettingsModelDoubleBounded t_longDep = new SettingsModelDoubleBounded(THRESHOLD_LONG_DEPENDENCY, 0.8, 0, 1);
	public static final SettingsModelDoubleBounded weight = new SettingsModelDoubleBounded(WEIGHT, 0.5, 0, 1);
	
	private ExtendedCausalGraph cg;
	protected XLogPortObject logPO = null;

	protected CGMinerNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(new PortType[] { XLogPortObject.TYPE }, 
        		new PortType[] { CausalGraphPortObject.TYPE });
    }

    
	@Override
	protected PortObject[] execute(final PortObject[] inObjects,
	            final ExecutionContext exec) throws Exception {
		// we always put the event log as the first input!! 
		logPO = (XLogPortObject)inObjects[0];
		
    	checkCanceled(null, exec);
		PortObject pnPO = mine(logPO.getLog(), exec);
		checkCanceled(null, exec);
		return new PortObject[] {pnPO};
	
    }
	
	protected PortObject mine(XLog log, final ExecutionContext exec) throws Exception{
    	logger.info("Begin: Causal Graph Miner");
    	
    	PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(HybridCGMinerPlugin.class);
    	checkCanceled(pluginContext, exec);
    	HybridCGMinerSettings settings = getConfiguration();
 
    	XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, settings.getClassifier());
		XLog filteredLog = LogFilterer.filterLogByActivityFrequency(log, logInfo, settings);
		TraceVariantsLog variants = new TraceVariantsLog(filteredLog, settings, settings.getTraceVariantsThreshold());
		HybridCGMiner miner = new HybridCGMiner(filteredLog, filteredLog.getInfo(settings.getClassifier()), variants, settings);
		ExtendedCausalGraph cg = miner.mineFCG();
		cg.setUnfilteredLog(log);
    	
    	checkCanceled(pluginContext, exec);
    	CausalGraphPortObject pnPO = new CausalGraphPortObject(cg);
    	
    	logger.info("End: Causal Graph miner");
    	return pnPO;
    }

    public ExtendedCausalGraph getCG() {
    	return cg; 
    }
    
    
    HybridCGMinerSettings getConfiguration() {
		HybridCGMinerSettings settings = new HybridCGMinerSettings();
    	settings.setFilterAcivityThreshold(filter_a.getDoubleValue()/100);
		settings.setTraceVariantsThreshold(filter_t.getDoubleValue()/100);
		settings.setSureThreshold(t_certain.getDoubleValue());
		settings.setQuestionMarkThreshold(t_uncertain.getDoubleValue());
		settings.setLongDepThreshold(t_longDep.getDoubleValue());
		settings.setCausalityWeight(weight.getDoubleValue());

		return settings;
	}


    protected PortObjectSpec[] configureOutSpec(XLogPortObjectSpec logSpec) {

        return new PortObjectSpec[]{new CausalGraphPortObjectSpec()};
    }

    @Override
	public PortObject[] getInternalPortObjects() {
		return new PortObject[] {logPO};
	}

    @Override
    public void setInternalPortObjects(PortObject[] portObjects) {
		logPO = (XLogPortObject) portObjects[0];
	}

	protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
		filter_a.saveSettingsTo(settings);
    	filter_t.saveSettingsTo(settings); 
    	t_certain.saveSettingsTo(settings); 
        t_uncertain.saveSettingsTo(settings); 
        t_longDep.saveSettingsTo(settings);
        weight.saveSettingsTo(settings);
	}


	protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		filter_a.loadSettingsFrom(settings);
    	filter_t.loadSettingsFrom(settings); 
    	t_certain.loadSettingsFrom(settings); 
        t_uncertain.loadSettingsFrom(settings); 
        t_longDep.loadSettingsFrom(settings);
        weight.loadSettingsFrom(settings);
	}
     
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(XLogPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid event Log!");
		
		XLogPortObjectSpec logSpec = (XLogPortObjectSpec) inSpecs[0];
		
		return configureOutSpec(logSpec);
	}
	
	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		saveSpecificSettingsTo(settings);
	}
	
	
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {		
	}
	
	
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		loadSpecificValidatedSettingsFrom(settings);
	}
	
	
   
}

