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
import org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper.DFMPortObject2;
import org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper.DefaultMinerNodeModelBuffTable;
import org.processmining.directlyfollowsmodelminer.mining.DFMMiner;
import org.processmining.directlyfollowsmodelminer.mining.DfgMsd2Dfm;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
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
	    	super( new PortType[]{BufferedDataTable.TYPE }, new PortType[] { DFMPortObject2.TYPE }); 
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
			IMLog IM_log = new BufferedTableIMLog(log,activityClassifier);
			DfgMsd dfg2 = convert(IM_log);
			DirectlyFollowsModel dfg3 = DfgMsd2Dfm.convert(dfg2);
			checkCanceled(exec);
			DFMPortObject2 dfmPO = new DFMPortObject2(dfg3);
			logger.info("End:  DFM Miner");
			return dfmPO;
		}
		
		public static DfgMsd convert(IMLog log) {
			DfgMsdImpl dfg = new DfgMsdImpl(log.getActivities());
			TIntIntHashMap minimumSelfDistances = IMLogInfo.createEmptyMinimumSelfDistancesMap();
			TIntObjectMap<MultiIntSet> minimumSelfDistancesBetween = IMLogInfo.createEmptyMinimumSelfDistancesBetweenMap();

			//walk trough the log
			for (IMTrace trace : log) {
				int toEventClass = -1;
				int fromEventClass = -1;

				int traceSize = 0;
				TIntIntHashMap eventSeenAt = new TIntIntHashMap();
				TIntList readTrace = new TIntArrayList();

				for (IMEventIterator it = trace.iterator(); it.hasNext();) {
					it.nextFast();
					int eventClass = it.getActivityIndex();

					dfg.addActivity(eventClass);

					fromEventClass = toEventClass;
					toEventClass = eventClass;

					readTrace.add(toEventClass);

					if (eventSeenAt.containsKey(toEventClass)) {
						//we have detected an activity for the second time
						//check whether this is shorter than what we had already seen
						int oldDistance = Integer.MAX_VALUE;
						if (minimumSelfDistances.containsKey(toEventClass)) {
							oldDistance = minimumSelfDistances.get(toEventClass);
						}

						if (!minimumSelfDistances.containsKey(toEventClass)
								|| traceSize - eventSeenAt.get(toEventClass) <= oldDistance) {
							//keep the new minimum self distance
							int newDistance = traceSize - eventSeenAt.get(toEventClass);
							if (oldDistance > newDistance) {
								//we found a shorter minimum self distance, record and restart with a new multiset
								minimumSelfDistances.put(toEventClass, newDistance);

								minimumSelfDistancesBetween.put(toEventClass, new MultiIntSet());
							}

							//store the minimum self-distance activities
							MultiIntSet mb = minimumSelfDistancesBetween.get(toEventClass);
							mb.addAll(readTrace.subList(eventSeenAt.get(toEventClass) + 1, traceSize));
						}
					}
					eventSeenAt.put(toEventClass, traceSize);
					{
						if (fromEventClass != -1) {
							//add edge to directly follows graph
							dfg.getDirectlyFollowsGraph().addEdge(fromEventClass, toEventClass, 1);
						} else {
							//add edge to start activities
							dfg.getStartActivities().add(toEventClass, 1);
						}
					}

					traceSize += 1;
				}

				if (toEventClass != -1) {
					dfg.getEndActivities().add(toEventClass, 1);
				}

				if (traceSize == 0) {
					dfg.addEmptyTraces(1);
				}
			}
			for (TIntObjectIterator<MultiIntSet> it = minimumSelfDistancesBetween.iterator(); it.hasNext();) {
				it.advance();
				MultiIntSet tos = it.value();
				for (Iterator<Integer> it2 = tos.iterator(); it2.hasNext();) {
					int to = it2.next();
					long cardinality = tos.getCardinalityOf(to);
					dfg.getMinimumSelfDistanceGraph().addEdge(it.key(), to, cardinality);
				}
			}
			return dfg;
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
			return null;
		}
}

