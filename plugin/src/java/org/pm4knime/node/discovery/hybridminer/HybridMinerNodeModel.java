package org.pm4knime.node.discovery.hybridminer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.CausalGraphPortObject;
import org.pm4knime.portobject.CausalGraphPortObjectSpec;
import org.pm4knime.portobject.HybridPetriNetPortObject;
import org.pm4knime.portobject.HybridPetriNetPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.extendedhybridminer.algorithms.cg2hpn.CGToHybridPN;
import org.processmining.extendedhybridminer.models.causalgraph.ExtendedCausalGraph;
import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;
import org.processmining.extendedhybridminer.models.hybridpetrinet.FitnessType;
import org.processmining.extendedhybridminer.plugins.HybridPNMinerPlugin;
import org.processmining.extendedhybridminer.plugins.HybridPNMinerSettings;


public class HybridMinerNodeModel extends DefaultNodeModel implements PortObjectHolder {
	
	private final NodeLogger logger = NodeLogger
            .getLogger(HybridMinerNodeModel.class);
	
	public final static String THRESHOLD_CANCEL = "<html><b>Threshold for early cancellation of place iterator:</b><br>after x consecutive rejected places, the place iterator is canceled.</html>";
	public final static String THRESHOLD_FITNESS = "<html><b>Fitness threshold for the place evaluation method</b></html>";
	public final static String FITNESS_TYPE = "<html><b>Place evaluation method</b></html>";
	
	public static final Map<String, String> FITNESS_TYPES = Collections.unmodifiableMap(new HashMap<String, String>() {
	    {
	        put("local", "local evaluation");
	    	put("global", "local evaluation with global fitness guarantee");
	}});
	
		
	public final SettingsModelInteger t_cancel = new SettingsModelInteger(THRESHOLD_CANCEL, 1000);
	public final SettingsModelString type_fitness = new SettingsModelString(FITNESS_TYPE, FITNESS_TYPES.get("global")); 
	public final SettingsModelDoubleBounded t_fitness = new SettingsModelDoubleBounded(THRESHOLD_FITNESS, 0.8, 0, 1);
	
	private ExtendedHybridPetrinet pn;
	protected CausalGraphPortObject cgPO = null;

	protected HybridMinerNodeModel() {
        super(new PortType[] { CausalGraphPortObject.TYPE }, 
        		new PortType[] { HybridPetriNetPortObject.TYPE });
    }
	
	
	@Override
	protected PortObject[] execute(final PortObject[] inObjects,
	            final ExecutionContext exec) throws Exception {
		cgPO = (CausalGraphPortObject) inObjects[0];
		checkCanceled(null, exec);
		PortObject pmPO = mine(cgPO.getCG(), exec);
		checkCanceled(null, exec);
		return new PortObject[] { pmPO};
	}
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(CausalGraphPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a causal graph!");		
		CausalGraphPortObjectSpec logSpec = (CausalGraphPortObjectSpec) inSpecs[0];
		
		return configureOutSpec(logSpec);
	}
	
	
	
	public XEventClassifier getEventClassifier() {
		ExtendedCausalGraph cg = cgPO.getCG();
		return cg.getSettings().getClassifier();	
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

	
	public CausalGraphPortObject getCGPO() {
    	return cgPO; 
    }
    
    
    protected PortObject mine(ExtendedCausalGraph cg, final ExecutionContext exec) throws Exception{
    	logger.info("Begin: Hybrid Petri Net Miner");
    	
    	PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(HybridPNMinerPlugin.class);
    	checkCanceled(pluginContext, exec);
    	HybridPNMinerSettings settings = getConfiguration();
    	ExtendedHybridPetrinet pn = CGToHybridPN.fuzzyCGToFuzzyPN(cg, settings);
        pn = addMarkings(pluginContext, pn);
    	checkCanceled(pluginContext, exec);
    	HybridPetriNetPortObject pnPO = new HybridPetriNetPortObject(pn);
    	logger.info("End: Hybrid Petri Net miner");
    	return pnPO;
    }

    private ExtendedHybridPetrinet addMarkings(PluginContext context, ExtendedHybridPetrinet pn) {
    	Place startPlace = pn.getPlace("start");
        Marking im = new Marking();
        im.add(startPlace);
        Place endPlace = pn.getPlace("end");
        Marking fm = new Marking();
        fm.add(endPlace);        
        context.getProvidedObjectManager().createProvidedObject(
                "Initial marking for " + pn.getLabel(),
                im, Marking.class, context);
        context.addConnection(new InitialMarkingConnection(pn, im));
        context.getProvidedObjectManager().createProvidedObject(
                    "Final marking for " + pn.getLabel(),
                    fm, Marking.class, context);
        context.addConnection(new FinalMarkingConnection(pn, fm));
        return pn;
	}

	public ExtendedHybridPetrinet getPN() {
    	return pn; 
    }
    
    
    HybridPNMinerSettings getConfiguration() {
		HybridPNMinerSettings settings = new HybridPNMinerSettings();
    	settings.setThresholdEarlyCancelationIterator(t_cancel.getIntValue());
		settings.setPlaceEvalThreshold(t_fitness.getDoubleValue());
		try {
			settings.setFitnessType(getFitnessType());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return settings;
	}


    public FitnessType getFitnessType() throws Exception {
    	if (type_fitness.getStringValue().equals(FITNESS_TYPES.get("global"))) {
    		return FitnessType.GLOBAL;
    	} else if (type_fitness.getStringValue().equals(FITNESS_TYPES.get("local"))) {
    		return FitnessType.LOCAL;
    	} else {
    		throw new Exception("Invalid place evaluation method: " + type_fitness.getStringValue());
    	}
	}


    protected PortObjectSpec[] configureOutSpec(CausalGraphPortObjectSpec logSpec) {

        return new PortObjectSpec[]{new HybridPetriNetPortObjectSpec()};
    }

    public PortObject[] getInternalPortObjects() {
		return new PortObject[] {cgPO};
	}

	public void setInternalPortObjects(PortObject[] portObjects) {
		cgPO = (CausalGraphPortObject) portObjects[0];
	}

	protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
		t_fitness.saveSettingsTo(settings);
    	t_cancel.saveSettingsTo(settings); 
    	type_fitness.saveSettingsTo(settings); 
	}


	protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		t_fitness.loadSettingsFrom(settings);
		t_cancel.loadSettingsFrom(settings); 
		type_fitness.loadSettingsFrom(settings); 
	}
     
   
}

