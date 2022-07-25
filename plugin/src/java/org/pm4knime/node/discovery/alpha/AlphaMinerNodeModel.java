package org.pm4knime.node.discovery.alpha;

import java.util.Set;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerModel;
import org.pm4knime.node.discovery.defaultminer.TraceVariantRep;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaPlusMinerParameters;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.alphaminer.plugins.AlphaMinerPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
/**
 * change it to adapt for more variants, Classic and Alpha++. But not all of them 
 * @author kefang-pads
 *
 */
public class AlphaMinerNodeModel extends DefaultTableMinerModel {

	// TODO: make the different versions of the alpha available through some
	// settings model
	// TODO: publish the marking as a separate output object

	private static final NodeLogger logger = NodeLogger.getLogger(AlphaMinerNodeModel.class);

	public static final String CFGKEY_VARIANT_TYPE = "Alpha Miner Variant";
	public static final String CFGKEY_THRESHOLD_NOISE_LF = "Noise threshhold for least frequency";
	public static final String CFGKEY_THRESHOLD_NOISE_MF = "Noise threshhold for most frequency";
	public static final String CFGKEY_THRESHOLD_CASUAL = "Casual threshhold";
	public static final String CFG_IGNORE_LL = "Ignore the lenght of the loops";
	public static final String[] variantList = {AlphaVersion.CLASSIC.toString() , AlphaVersion.PLUS.toString(),
			AlphaVersion.PLUS_PLUS.toString(), AlphaVersion.SHARP.toString(), AlphaVersion.ROBUST.toString()};
	
	SettingsModelString m_variant =  new SettingsModelString(AlphaMinerNodeModel.CFGKEY_VARIANT_TYPE, variantList[0]);
	SettingsModelDoubleBounded m_noiseTLF = new SettingsModelDoubleBounded(AlphaMinerNodeModel.CFGKEY_THRESHOLD_NOISE_LF, 0, 0, 100);
	SettingsModelDoubleBounded m_noiseTMF = new SettingsModelDoubleBounded(AlphaMinerNodeModel.CFGKEY_THRESHOLD_NOISE_MF, 0, 0, 100);
	SettingsModelDoubleBounded m_casualTH = new SettingsModelDoubleBounded(AlphaMinerNodeModel.CFGKEY_THRESHOLD_CASUAL, 0, 0, 100);
	SettingsModelBoolean m_ignore_ll = new SettingsModelBoolean(AlphaMinerNodeModel.CFG_IGNORE_LL, false);
	
	protected AlphaMinerNodeModel() {
		super( new PortType[]{BufferedDataTable.TYPE } , new PortType[] { PetriNetPortObject.TYPE });
		
		m_noiseTLF.setEnabled(false);
    	m_noiseTMF.setEnabled(false);
    	m_casualTH.setEnabled(false);
    	m_ignore_ll.setEnabled(false);
	}

	
	@Override
	protected PortObject mine(BufferedDataTable table, final ExecutionContext exec) throws Exception {
		// TODO Auto-generated method stub
		logger.info("Start: Alpha Miner");
		AlphaMinerParameters alphaParams = null;
		
		if(m_variant.getStringValue().equals(AlphaVersion.CLASSIC.toString()))
			alphaParams = new AlphaMinerParameters(AlphaVersion.CLASSIC);
		else if(m_variant.getStringValue().equals(AlphaVersion.PLUS.toString())) {
			alphaParams = new AlphaPlusMinerParameters(AlphaVersion.PLUS, m_ignore_ll.getBooleanValue());
			alphaParams.setVersion(AlphaVersion.PLUS);
		}
		else if(m_variant.getStringValue().equals(AlphaVersion.PLUS_PLUS.toString()))
			alphaParams = new AlphaMinerParameters(AlphaVersion.PLUS_PLUS);
		else if(m_variant.getStringValue().equals(AlphaVersion.SHARP.toString()))
			alphaParams = new AlphaMinerParameters(AlphaVersion.SHARP);
		else if(m_variant.getStringValue().equals(AlphaVersion.ROBUST.toString())) {
			alphaParams = new AlphaRobustMinerParameters(m_casualTH.getDoubleValue(), m_noiseTLF.getDoubleValue(), m_noiseTMF.getDoubleValue());
			alphaParams.setVersion(AlphaVersion.ROBUST);
		}
		PluginContext context = PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(AlphaMinerPlugin.class);
		
		checkCanceled(context, exec);
		TraceVariantRep variants = new TraceVariantRep(table, getTraceClassifier(), getEventClassifier());
		Object[] result = AlphaAbstraction.apply(context, variants, getEventClassifier(), alphaParams);
		
		// when there is no finalMarking available, we set the finalMarking automatically
		Set<Marking> fmSet = PetriNetUtil.guessFinalMarking((Petrinet) result[0]); // new HashMap();
		
		AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) result[0], (Marking) result[1], fmSet);
		checkCanceled(exec);
		PetriNetPortObject pnPO = new PetriNetPortObject(anet);
		logger.info("End: Alpha Miner");
		return pnPO;
	}

	
	@Override
	protected PortObjectSpec[] configureOutSpec(DataTableSpec logSpec) {
		// TODO Auto-generated method stub
		PetriNetPortObjectSpec ptSpec = new PetriNetPortObjectSpec();
		return new PortObjectSpec[] { ptSpec };
	}


	@Override
	protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		m_variant.saveSettingsTo(settings);
		m_noiseTLF.saveSettingsTo(settings);
		m_noiseTMF.saveSettingsTo(settings);
		m_casualTH.saveSettingsTo(settings);
		m_ignore_ll.saveSettingsTo(settings);
	}


	@Override
	protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_variant.loadSettingsFrom(settings);
		m_noiseTLF.loadSettingsFrom(settings);
		m_noiseTMF.loadSettingsFrom(settings);
		m_casualTH.loadSettingsFrom(settings);
		m_ignore_ll.loadSettingsFrom(settings);
	}


	
}
