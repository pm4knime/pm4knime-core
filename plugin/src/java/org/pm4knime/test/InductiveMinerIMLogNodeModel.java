package org.pm4knime.test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
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
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper.BufferedTableIMLog;
import org.pm4knime.node.discovery.inductiveminer.InductiveMinerNodeModel;
import org.pm4knime.node.discovery.inductiveminer.InductiveMinerNodeModel2;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.ProcessTreePortObject;
import org.pm4knime.portobject.ProcessTreePortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.defaultnode.DefaultMinerNodeModel;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;

import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.logs.IMLogImpl;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerPlugin;
import org.processmining.plugins.inductiveminer2.variants.MiningParametersIM;
import org.processmining.processtree.ProcessTree;

/**
 * <code>NodeModel</code> for the "InductiveMinerIMLog" node.
 *
 * @author 
 */
public class InductiveMinerIMLogNodeModel extends DefaultMinerNodeModel {
    
	// the logger instance
		private static final NodeLogger logger = NodeLogger.getLogger(InductiveMinerIMLogNodeModel.class);

		public static final String[] defaultType = { "Inductive Miner", //
				"Inductive Miner - Infrequent", //
				"Inductive Miner - Incompleteness", //
				"Inductive Miner - exhaustive K-successor", //
				"Inductive Miner - Life cycle" };

		public static final String CFG_KEY_METHOD_TYPE = "Method";
		public static final String CFG_KEY_NOISE_THRESHOLD = "Noise Threshold";

		private SettingsModelString m_type = new SettingsModelString(InductiveMinerNodeModel.CFGKEY_METHOD_TYPE,
				defaultType[1]);
		private SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(
				InductiveMinerNodeModel.CFGKEY_NOISE_THRESHOLD, 0.0, 0, 1.0);

		protected InductiveMinerIMLogNodeModel() {
			super(new PortType[] { XLogPortObject.TYPE }, new PortType[] { PetriNetPortObject.TYPE  });
		}

		@Override
		protected PortObjectSpec[] configureOutSpec(XLogPortObjectSpec logSpec) {
			// TODO Auto-generated method stub
			PetriNetPortObjectSpec ptSpec = new PetriNetPortObjectSpec();
			return new PortObjectSpec[] { ptSpec };
		}
		

		@Override
		protected PortObject mine(XLog log, final ExecutionContext exec) throws Exception {
			// TODO the most important part, however, should we set the classifier
			// as one parameter, and no need to explicitly infer the classifier from it ??
			// Now, it is just fine
			// the same effort to use it
			logger.info("Begin: Inductive Miner");
			checkCanceled(exec);

			MiningParametersIM param =  createParameters();
			Instant start = Instant.now();
			IMLog imlog = new IMLogImpl(log, param.getClassifier(), param.getLifeCycleClassifier());
			EfficientTree ptE = InductiveMinerPlugin.mineTree(imlog, param,  new Canceller() {
				public boolean isCancelled() {
					try {
						checkCanceled(exec);
					}catch (final CanceledExecutionException ce) {
						return true;
					}
					return false;
				}
			});
			Instant end = Instant.now();
			System.out.println(Duration.between(start, end).toMinutes());
			System.out.println("End of Inductive Miner");
			AcceptingPetriNet net = InductiveMinerPlugin.postProcessTree2PetriNet(ptE, new Canceller() {
				public boolean isCancelled() {
					try {
						checkCanceled(exec);
					} catch (final CanceledExecutionException ce) {
						return true;
					}
					return false;
				}
			});

			checkCanceled(exec);
			PetriNetPortObject petriObj = new PetriNetPortObject(net, ptE);
			logger.info("End: Inductive Miner");
			return petriObj;
		}

		private MiningParametersIM createParameters() throws InvalidSettingsException {
			MiningParametersIM param;

			if (m_type.getStringValue().equals(defaultType[0]))
				param = new MiningParametersIM();
			else
				throw new InvalidSettingsException("unknown inductive miner type " + m_type.getStringValue());

			param.setNoiseThreshold((float) m_noiseThreshold.getDoubleValue());

			XEventClassifier classifier = getEventClassifier();
			param.setClassifier(classifier);

			param.getLifeCycleClassifier();
			return param;
		}

		@Override
		protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
			// TODO Auto-generated method stub
			m_type.saveSettingsTo(settings);
//			 noiseThreshold, we need to save it. Only difference is that if it can be used in later process. 
//			 so no need to check if it is enabled or not. We just let it be 
//			 if(m_noiseThreshold.isEnabled())
			m_noiseThreshold.saveSettingsTo(settings);
		}

		@Override
		protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
			// TODO Auto-generated method stub
			m_type.validateSettings(settings);
			String tmp = settings.getString(m_type.getKey());

			if (tmp.equals(defaultType[0])) {
				m_noiseThreshold.setEnabled(false);
			} else {
				m_noiseThreshold.setEnabled(true);
				m_noiseThreshold.validateSettings(settings);
			}
		}

		@Override
		protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
			// TODO Auto-generated method stub
			m_type.loadSettingsFrom(settings);
			m_noiseThreshold.loadSettingsFrom(settings);
		}

}

