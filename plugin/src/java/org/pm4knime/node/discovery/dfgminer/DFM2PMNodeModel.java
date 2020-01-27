package org.pm4knime.node.discovery.dfgminer;

import java.io.File;
import java.io.IOException;

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
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMcd;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMd;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMfd;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.IMdProcessTree;
import org.processmining.processtree.ProcessTree;

/**
 * <code>NodeModel</code> for the "DFM2PM" node. PM represents process model, including Process Tree and Petri net.
 *
 * @author Kefang Ding
 */
public class DFM2PMNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger
            .getLogger(DFM2PMNodeModel.class);
	public static final String CFG_VARIANT_KEY= "Variant";
	public static final String[] CFG_VARIANT_VALUES = {"Inductive Miner - directly follows (IMd)", 
			"Inductive Miner - infrequent - directly follows (IMfd)", "Inductive Miner - incompleteness - directly follows (IMcd)"};
	
	public static final String CFGKEY_NOISE_THRESHOLD = "Noise Threshold";
	
	SettingsModelString m_variant = new SettingsModelString(CFG_VARIANT_KEY, "");
	SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(
			CFGKEY_NOISE_THRESHOLD, 0.8, 0, 1.0);
	
	
    /**
     * Constructor for the node model.
     */
    protected DFM2PMNodeModel() {
    
        // TODO: input is a DFM, output are Petri net and a Process tree.
    	super(new PortType[] {DFMPortObject.TYPE}, new PortType[] {ProcessTreePortObject.TYPE} );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: check the input data as DFMPortObject and output a Petri net
    	
    	logger.info("Begin:  DFM2PM Miner");
    	
    	DFMPortObject dfmPO = null ;
    	for(PortObject obj: inData)
        	if(obj instanceof DFMPortObject) {
        		dfmPO = (DFMPortObject)obj;
        		break;
        	}
        
    	Dfg dfm = dfmPO.getDfm();
    	
    	
		// here we have parameter to be set: noiseThrehold, do we still need to reset this threshold? 
		// or not like this?? we set noise threshold for further filtering
		DfgMiningParameters params = null;
		if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[0])) {
			params = new DfgMiningParametersIMd();
		}else if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[1])) {
			params = new DfgMiningParametersIMfd();
		}else if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[2])) {
			params = new DfgMiningParametersIMcd();
		}else {
			throw new Exception("not found variant type");
		}
		
		params.setNoiseThreshold((float) m_noiseThreshold.getDoubleValue());
		
    	ProcessTree pt = IMdProcessTree.mineProcessTree(dfm, params, new Canceller() {
			public boolean isCancelled() {
				// to make it accept the KNIME cancelation and make it cancled.
				return false;
				
			}
		});
    	
    	ProcessTreePortObject ptPO = new ProcessTreePortObject(pt);
    	
		logger.info("End:  DFM2PM Miner");
    	return new PortObject[]{ptPO};
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
    	
    	
    	ProcessTreePortObjectSpec ptPOSpec = new ProcessTreePortObjectSpec();
    	
    	return new PortObjectSpec[]{ ptPOSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_noiseThreshold.saveSettingsTo(settings);
    	m_variant.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_noiseThreshold.loadSettingsFrom(settings);
    	m_variant.loadSettingsFrom(settings);
    	
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

