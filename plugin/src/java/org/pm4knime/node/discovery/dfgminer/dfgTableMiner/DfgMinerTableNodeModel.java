package org.pm4knime.node.discovery.dfgminer.dfgTableMiner;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerModel;
import org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper.BufferedTableIMLog;
import org.pm4knime.portobject.DfgMsdPortObject;
import org.pm4knime.portobject.DfgMsdPortObjectSpec;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.Log2DfgMsd;


public class DfgMinerTableNodeModel extends DefaultTableMinerModel {
    
	private static final NodeLogger logger = NodeLogger.getLogger(DfgMinerTableNodeModel.class);
	// one parameter is the set noise, one is the classifier, those two like
		// inductive miner
//		public static final String CFG_VARIANT_KEY= "Variant";
//		public static final String[] CFG_VARIANT_VALUES = {"use event classes - (IMd, IMfd, IMcd)"};
		
//		SettingsModelString m_variant = new SettingsModelString(DfgMinerTableNodeModel.CFG_VARIANT_KEY, "");
//		public static List<String> sClfNames; 
//		public static List<XEventClassifier> sClassifierList ;
//		static {
//			sClassifierList = new ArrayList<XEventClassifier>();
//			XEventNameClassifier eventNameClf = new XEventNameClassifier();
//			XEventAndClassifier lfAndEventClf  = new XEventAndClassifier(new XEventNameClassifier(),
//					new XEventLifeTransClassifier());
//			sClassifierList.add(eventNameClf);
//			sClassifierList.add(lfAndEventClf);
//			
//			sClfNames = new ArrayList<String>();
//			sClfNames.add(eventNameClf.name());
//			sClfNames.add(lfAndEventClf.name());
//			
//		}
		
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
			
			//String activityClassifier = getEventClassifier();
			// according to the variant values, there is different implementation
			//IMLog2IMLogInfo logInfo ;
//			if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[0])) {
//				logInfo = new IMLog2IMLogInfoDefault();
//			}else if(m_variant.getStringValue().equals(CFG_VARIANT_VALUES[1])) {
//				logInfo = new IMLog2IMLogInfoLifeCycle();
//			}else {
//				throw new Exception("not found variant type");
//			}
			//checkCanceled(exec);
			BufferedTableIMLog imLog = new BufferedTableIMLog(logPO, getEventClassifier(), getTraceClassifier());	
			DfgMsd dfgmsd = Log2DfgMsd.convert(imLog);
			checkCanceled(exec);
			logger.info("End:  DFM Miner");
			DfgMsdPortObject dfgMsdObj = new DfgMsdPortObject(dfgmsd);
			return dfgMsdObj;
		}
		

		@Override
		protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
			// TODO Auto-generated method stub
//			m_variant.saveSettingsTo(settings);
		}

		@Override
		protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
			// TODO Auto-generated method stub
//			m_variant.validateSettings(settings);
		}

		@Override
		protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
			// TODO Auto-generated method stub
//			m_variant.loadSettingsFrom(settings);
		}


		@Override
		protected PortObjectSpec[] configureOutSpec(DataTableSpec logSpec) {
			// TODO Auto-generated method stub
				return new PortObjectSpec[] { new DfgMsdPortObjectSpec() };			
		}

}

