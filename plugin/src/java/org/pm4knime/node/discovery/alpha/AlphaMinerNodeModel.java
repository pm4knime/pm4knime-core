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
public class AlphaMinerNodeModel extends NodeModel {

	// TODO: make the different versions of the alpha available through some
	// settings model
	// TODO: publish the marking as a separate output object

	private static final NodeLogger logger = NodeLogger.getLogger(AlphaMinerNodeModel.class);

	public static final String CFGKEY_VARIANT_TYPE = "AlphaMiner Version";
	public static final String[] variantList = {AlphaVersion.CLASSIC.toString() , AlphaVersion.PLUS.toString()};
	
	private SettingsModelString m_variant =  new SettingsModelString(AlphaMinerNodeModel.CFGKEY_VARIANT_TYPE, variantList[0]);
	
	protected AlphaMinerNodeModel() {
		super(new PortType[] { PortTypeRegistry.getInstance().getPortType(XLogPortObject.class, false) },
				new PortType[] { PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class, false) });
	}

	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		logger.info("start: alpha miner");
		XLog log = ((XLogPortObject) inObjects[0]).getLog();
		AlphaMinerParameters alphaParams = null;
		
		if(m_variant.getStringValue().equals(AlphaVersion.CLASSIC.toString()))
			alphaParams = new AlphaMinerParameters(AlphaVersion.CLASSIC);
		else if(m_variant.getStringValue().equals(AlphaVersion.PLUS.toString()))
			alphaParams = new AlphaMinerParameters(AlphaVersion.PLUS);
		
		Object[] result = AlphaMinerPlugin.apply(PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(AlphaMinerPlugin.class), log,
				new XEventNameClassifier(), alphaParams);
		
		// when there is no finalMarking available, we set the finalMarking automatically
		Set<Marking> fmSet = PetriNetUtil.guessFinalMarking((Petrinet) result[0]); // new HashMap();
		
		AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) result[0], (Marking) result[1], fmSet);
		
		PetriNetPortObject po = new PetriNetPortObject(anet);
		logger.info("end: alpha miner");
		return new PortObject[] { po };
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		
		if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
		
		PetriNetPortObjectSpec spec = new PetriNetPortObjectSpec();
		return new PortObjectSpec[] { spec };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}
