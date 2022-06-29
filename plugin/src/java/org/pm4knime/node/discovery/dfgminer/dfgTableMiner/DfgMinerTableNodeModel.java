package org.pm4knime.node.discovery.dfgminer.dfgTableMiner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
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
import org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper.BufferedTableIMLog;
import org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper.DefaultMinerNodeModelBuffTable;
import org.pm4knime.portobject.DFMPortObjectSpec;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.portobject.DfgMsdPortObjectSpec;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.processmining.directlyfollowsmodelminer.mining.DFMMiner;
import org.processmining.directlyfollowsmodelminer.mining.DfgMsd2Dfm;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycleClassifier;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.inductiveminer2.helperclasses.IntDfg;
import org.processmining.plugins.inductiveminer2.helperclasses.MultiIntSet;
import org.processmining.plugins.inductiveminer2.helperclasses.XLifeCycleClassifierIgnore;
import org.processmining.plugins.inductiveminer2.loginfo.IMLog2IMLogInfo;
import org.processmining.plugins.inductiveminer2.loginfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.inductiveminer2.loginfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.inductiveminer2.loginfo.IMLogInfo;
import org.processmining.plugins.inductiveminer2.logs.IMEventIterator;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.logs.IMTrace;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsdImpl;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.Log2DfgMsd;

import cern.colt.Arrays;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;


/**
 * This is an example implementation of the node model of the
 * "DfgMinerTable" node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author 
 */
public class DfgMinerTableNodeModel extends DefaultMinerNodeModelBuffTable {
    
	private static final NodeLogger logger = NodeLogger.getLogger(DfgMinerTableNodeModel.class);
	// one parameter is the set noise, one is the classifier, those two like
		// inductive miner
		public static final String CFG_VARIANT_KEY= "Variant";
		public static final String[] CFG_VARIANT_VALUES = {"use event classes - (IMd, IMfd, IMcd)"};
		
		SettingsModelString m_variant = new SettingsModelString(DfgMinerTableNodeModel.CFG_VARIANT_KEY, "");
		public static List<String> sClfNames; 
		public static List<XEventClassifier> sClassifierList ;
		static {
			sClassifierList = new ArrayList<XEventClassifier>();
			XEventNameClassifier eventNameClf = new XEventNameClassifier();
			XEventAndClassifier lfAndEventClf  = new XEventAndClassifier(new XEventNameClassifier(),
					new XEventLifeTransClassifier());
			sClassifierList.add(eventNameClf);
			sClassifierList.add(lfAndEventClf);
			
			sClfNames = new ArrayList<String>();
			sClfNames.add(eventNameClf.name());
			sClfNames.add(lfAndEventClf.name());
			
		}
		
	    /**
	     * Constructor for the node model.
	     */
	    protected DfgMinerTableNodeModel() {
	    
	        // TODO: Specify the amount of input and output ports needed.
	    	super( new PortType[]{BufferedDataTable.TYPE }, new PortType[] { DfgMsdPortObject.TYPE }); 
	    	}

		@Override
		protected PortObject mine(BufferedDataTable log, final ExecutionContext exec) throws Exception {
			// TODO Auto-generated method stub
			logger.info("Begin:  DFM Miner");
			checkCanceled(exec);
			
			String activityClassifier = getEventClassifier();
			// according to the variant values, there is different implementation
			IMLog2IMLogInfo logInfo ;
			if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[0])) {
				logInfo = new IMLog2IMLogInfoDefault();
			}else if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[1])) {
				logInfo = new IMLog2IMLogInfoLifeCycle();
			}else {
				throw new Exception("not found variant type");
			}
			checkCanceled(exec);
			IMLog imLog = new BufferedTableIMLog(log,activityClassifier);
			DfgMsd dfgmsd = Log2DfgMsd.convert(imLog);
			checkCanceled(exec);
			logger.info("End:  DFM Miner");
			DfgMsdPortObject dfgMsdObj = new DfgMsdPortObject(dfgmsd);
			return dfgMsdObj;
		}
		

		
	
		
		@Override
		public String getEventClassifier() {
			String nClf = super.getEventClassifier();
			if(nClf != null) {
				return nClf;
			}
			return null;
		}



		@Override
		protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
			// TODO Auto-generated method stub
			m_variant.saveSettingsTo(settings);
		}

		@Override
		protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
			// TODO Auto-generated method stub
			m_variant.validateSettings(settings);
		}

		@Override
		protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
			// TODO Auto-generated method stub
			m_variant.loadSettingsFrom(settings);
		}


		@Override
		protected PortObjectSpec[] configureOutSpec(DataTableSpec logSpec) {
			// TODO Auto-generated method stub
				return new PortObjectSpec[] { new DfgMsdPortObjectSpec() };
			
		}
}

