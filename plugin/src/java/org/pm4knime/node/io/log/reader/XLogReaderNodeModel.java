package org.pm4knime.node.io.log.reader;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.settingsmodel.XLogReaderNodeSettingsModel;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.plugins.log.OpenNaiveLogFilePlugin;

public class XLogReaderNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(XLogReaderNodeModel.class);
	private final XLogReaderNodeSettingsModel params = new XLogReaderNodeSettingsModel();
	
	XLogPortObject logPO;
	// private XLogPortObjectSpec outSpec ;

	protected XLogReaderNodeModel() {
		super(new PortType[] {},
				new PortType[] { PortTypeRegistry.getInstance().getPortType(XLogPortObject.class, false) });
	}

	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		logger.info("start: import event log");
		File file = new File(params.getFilePathSettingsModel().getStringValue());
		XLog result = null;
		OpenNaiveLogFilePlugin plugin = new OpenNaiveLogFilePlugin();
		result = (XLog) plugin.importFile(
				PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(OpenNaiveLogFilePlugin.class), file);
		logPO = new XLogPortObject();
		
		logPO.setLog(result);
		logger.info("end: import event log");
		
		// System.out.println("print the value of outspec"+ outSpec.toString());
		// po.setSpec(outSpec);
		// we create classifier for this function
		Collection<XEventClassifier> classifiers = result.getClassifiers();
		if(classifiers.isEmpty()) {
			XLogInfo info = XLogInfoFactory.createLogInfo(result);
			classifiers = info.getEventClassifiers();
			
		}
		
		return new PortObject[] { logPO };
	}

	public XLogPortObject getLogPO() {
		return logPO;
	}

	public XLogReaderNodeSettingsModel getParams() {
		return params;
	}

	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) {
		// TODO: delete the SpecCreator class here and don't use 
		
		XLogPortObjectSpec outSpec = new XLogPortObjectSpec();
		
		return new PortObjectSpec[] { outSpec};
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
	protected void reset() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		params.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		params.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		params.loadSettingsFrom(settings);
	}

}
