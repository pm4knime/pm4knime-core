package org.pm4knime.node.discovery.alpha;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.node.discovery.inductiveminer.InductiveMinerNodeModel;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.DefaultMinerNodeModel;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.alphaminer.plugins.AlphaMinerPlugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
/**
 * change it to adapt for more variants, Classic and Alpha++. But not all of them 
 * @author kefang-pads
 *
 */
public class AlphaMinerNodeModel extends DefaultMinerNodeModel {

	// TODO: make the different versions of the alpha available through some
	// settings model
	// TODO: publish the marking as a separate output object

	private static final NodeLogger logger = NodeLogger.getLogger(AlphaMinerNodeModel.class);

	public static final String CFGKEY_VARIANT_TYPE = "AlphaMiner Version";
	public static final String[] variantList = {AlphaVersion.CLASSIC.toString() , AlphaVersion.PLUS.toString()};
	
	private SettingsModelString m_variant =  new SettingsModelString(AlphaMinerNodeModel.CFGKEY_VARIANT_TYPE, variantList[0]);
	
	protected AlphaMinerNodeModel() {
		super(new PortType[] { XLogPortObject.TYPE },
				new PortType[] { PetriNetPortObject.TYPE });
	}

	
	@Override
	protected PortObject mine(XLog log) throws Exception {
		// TODO Auto-generated method stub
		logger.info("Start: Alpha Miner");
		AlphaMinerParameters alphaParams = null;
		
		if(m_variant.getStringValue().equals(AlphaVersion.CLASSIC.toString()))
			alphaParams = new AlphaMinerParameters(AlphaVersion.CLASSIC);
		else if(m_variant.getStringValue().equals(AlphaVersion.PLUS.toString()))
			alphaParams = new AlphaMinerParameters(AlphaVersion.PLUS);
		
		Object[] result = AlphaMinerPlugin.apply(PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(AlphaMinerPlugin.class), log,
				getEventClassifier(), alphaParams);
		
		// when there is no finalMarking available, we set the finalMarking automatically
		Set<Marking> fmSet = PetriNetUtil.guessFinalMarking((Petrinet) result[0]); // new HashMap();
		
		AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) result[0], (Marking) result[1], fmSet);
		
		PetriNetPortObject pnPO = new PetriNetPortObject(anet);
		logger.info("End: Alpha Miner");
		return pnPO;
	}


	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		
		if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
		
		PetriNetPortObjectSpec pnSpec = new PetriNetPortObjectSpec();
		return new PortObjectSpec[] { pnSpec };
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		
		m_variant.saveSettingsTo(settings);
	}

	
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		super.loadValidatedSettingsFrom(settings);
		
		m_variant.loadSettingsFrom(settings);
	}

}
