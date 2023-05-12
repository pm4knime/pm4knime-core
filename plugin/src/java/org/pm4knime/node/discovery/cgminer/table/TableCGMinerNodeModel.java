package org.pm4knime.node.discovery.cgminer.table;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.pm4knime.node.discovery.cgminer.CGMinerNodeModel;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerModel;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.pm4knime.portobject.CausalGraphPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.extendedhybridminer.algorithms.HybridCGMiner;
import org.processmining.extendedhybridminer.algorithms.preprocessing.TraceVariantsLog;
import org.processmining.extendedhybridminer.models.causalgraph.ExtendedCausalGraph;
import org.processmining.extendedhybridminer.plugins.HybridCGMinerPlugin;
import org.processmining.extendedhybridminer.plugins.HybridCGMinerSettings;
import org.processmining.framework.plugin.PluginContext;


public class TableCGMinerNodeModel extends DefaultTableMinerModel {
	
	private final NodeLogger logger = NodeLogger
            .getLogger(TableCGMinerNodeModel.class);
	
	public final SettingsModelDoubleBounded filter_a = new SettingsModelDoubleBounded(CGMinerNodeModel.FILTER_ACTIVITY, 0, 0, 100);
	public final SettingsModelDoubleBounded filter_t = new SettingsModelDoubleBounded(CGMinerNodeModel.FILTER_TRACE, 0, 0, 100);
	public final SettingsModelDoubleBounded t_certain  = new SettingsModelDoubleBounded(CGMinerNodeModel.THRESHOLD_CERTAIN_EDGES, 0.4, 0, 1);
	public final SettingsModelDoubleBounded t_uncertain = new SettingsModelDoubleBounded(CGMinerNodeModel.THRESHOLD_UNCERTAIN, 0.3, 0, 1);
	public final SettingsModelDoubleBounded t_longDep = new SettingsModelDoubleBounded(CGMinerNodeModel.THRESHOLD_LONG_DEPENDENCY, 0.8, 0, 1);
	public final SettingsModelDoubleBounded weight = new SettingsModelDoubleBounded(CGMinerNodeModel.WEIGHT, 0.5, 0, 1);
	
	
	private ExtendedCausalGraph cg;
	
	protected TableCGMinerNodeModel() {
        super(new PortType[] { BufferedDataTable.TYPE }, 
        		new PortType[] { CausalGraphPortObject.TYPE },
        		"Causal Graph JS View");
    }

	
//	@Override
//	protected PortObject[] execute(final PortObject[] inObjects,
//	            final ExecutionContext exec) throws Exception {
//		logPO = (BufferedDataTable)inObjects[0];
//    	checkCanceled(null, exec);
//		PortObject pmPO = mine(logPO, exec);
//		checkCanceled(null, exec);
//		return new PortObject[] {pmPO};
//	
//    }
	
	protected PortObject mine(BufferedDataTable table, final ExecutionContext exec) throws Exception{
    	logger.info("Begin: Causal Graph Miner (Table)");
    	String tClassifier = getTraceClassifier();
    	String eClassifier = getEventClassifier();
    	PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(HybridCGMinerPlugin.class);
//    	checkCanceled(pluginContext, exec);
    	HybridCGMinerSettings settings = getConfiguration();
		TraceVariantsLog variants = new TraceVariantsTable(table, settings, tClassifier, eClassifier);
		HybridCGMiner miner = new HybridCGMiner(null, null, variants, settings);
		ExtendedCausalGraph cg = miner.mineFCG();
    	
//    	checkCanceled(pluginContext, exec);
    	CausalGraphPortObject pnPO = new CausalGraphPortObject(cg);
    	
    	logger.info("End: Causal Graph miner");
    	return pnPO;
    }

    public ExtendedCausalGraph getCG() {
    	return cg; 
    }
    
    
    
    
    HybridCGMinerSettings getConfiguration() {
		HybridCGMinerSettings settings = new HybridCGMinerSettings();
    	settings.setFilterAcivityThreshold(filter_a.getDoubleValue()/100.0);
		settings.setTraceVariantsThreshold(filter_t.getDoubleValue()/100.0);
		settings.setSureThreshold(t_certain.getDoubleValue());
		settings.setQuestionMarkThreshold(t_uncertain.getDoubleValue());
		settings.setLongDepThreshold(t_longDep.getDoubleValue());
		settings.setCausalityWeight(weight.getDoubleValue());

		return settings;
	}


    protected PortObjectSpec[] configureOutSpec(DataTableSpec logSpec) {
        return new PortObjectSpec[]{new CausalGraphPortObjectSpec()};
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
	protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
	}
   
}

