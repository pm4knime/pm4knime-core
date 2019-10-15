package org.pm4knime.node.discovery.dfgminer;

import java.io.File;
import java.io.IOException;

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
import org.pm4knime.portobject.DFMPortObject;
import org.pm4knime.portobject.DFMPortObjectSpec;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduceParametersForPetriNet;
import org.processmining.plugins.directlyfollowsmodel.DirectlyFollowsModel;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerPlugin;
import org.processmining.plugins.inductiveminer2.withoutlog.InductiveMinerWithoutLog;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;
import org.processmining.plugins.inductiveminer2.withoutlog.variants.MiningParametersIMInfrequentWithoutLog;
import org.processmining.plugins.inductiveminer2.withoutlog.variants.MiningParametersIMWithoutLog;
import org.processmining.processtree.ProcessTree;

/**
 * <code>NodeModel</code> for the "DFM2PM" node. PM represents process model, including Process Tree and Petri net.
 *
 * @author Kefang Ding
 */
public class DFM2PMNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger
            .getLogger(DFM2PMNodeModel.class);
	
//	private static final int CFG_PN_OPIDX = 0;
//	private static final int CFG_PT_OPIDX = 1;
	
	// this is for further noise filtering after dfg methods. 
	public static final String CFGKEY_NOISE_THRESHOLD = "NoiseThreshold";
	public static final String CFGKEY_USE_MULTITHREAD = "Use multiple thread";
	private SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(DFM2PMNodeModel.CFGKEY_NOISE_THRESHOLD, 0.0, 0, 1.0);
	private SettingsModelBoolean m_useMT = new SettingsModelBoolean(DFM2PMNodeModel.CFGKEY_USE_MULTITHREAD, true);
	
    /**
     * Constructor for the node model.
     */
    protected DFM2PMNodeModel() {
    
        // TODO: input is a DFM, output are Petri net and a Process tree.
    	super(new PortType[] {DFMPortObject.TYPE}, new PortType[] {PetriNetPortObject.TYPE, ProcessTreePortObject.TYPE} );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: check the input data as DFMPortObject and output a Petri net
    	
    	logger.info("Begin:  DFM2PMMiner");
    	
    	DFMPortObject dfmPO = null ;
    	for(PortObject obj: inData)
        	if(obj instanceof DFMPortObject) {
        		dfmPO = (DFMPortObject)obj;
        		break;
        	}
        
    	DirectlyFollowsModel dfm = dfmPO.getDfm();
    	
    	// needs the plugin context all the time
    	PluginContext context =  PM4KNIMEGlobalContext.instance().getPluginContext(); //.getFutureResultAwarePluginContext(IM.class);
    	Canceller cancler = new Canceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		};
		// here we have parameter to be set: noiseThrehold, do we still need to reset this threshold? 
		// or not like this?? we set noise threshold for further filtering
		// MiningParametersIMWithoutLog params = new MiningParametersIMWithoutLog();
		MiningParametersIMInfrequentWithoutLog params = new MiningParametersIMInfrequentWithoutLog();
		params.setNoiseThreshold((float) m_noiseThreshold.getDoubleValue());
		params.setUseMultithreading(m_useMT.getBooleanValue());
		
		EfficientTree tree = InductiveMinerWithoutLog.mineEfficientTree((DfgMsd) dfm, params, cancler);
    	
    	ProcessTree pt = EfficientTree2processTree.convert(tree);
    	ProcessTreePortObject ptPO = new ProcessTreePortObject(pt);
    	
		AcceptingPetriNet anet = InductiveMinerPlugin.postProcessTree2PetriNet(tree, cancler);
		PetriNetPortObject pnPO = new PetriNetPortObject(anet);
    	
    	return new PortObject[]{pnPO, ptPO};
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

    	if(!inSpecs[0].getClass().equals(DFMPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid directly-follows model!");
    	
    	PetriNetPortObjectSpec pnPOSpec = new PetriNetPortObjectSpec();
    	ProcessTreePortObjectSpec ptPOSpec = new ProcessTreePortObjectSpec();
    	
    	return new PortObjectSpec[]{pnPOSpec, ptPOSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_noiseThreshold.saveSettingsTo(settings);
    	m_useMT.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_noiseThreshold.loadSettingsFrom(settings);
    	m_useMT.loadSettingsFrom(settings);
    	
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

