package org.pm4knime.node.io.log.reader;

import java.io.File;
import java.io.IOException;
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
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.plugins.log.OpenNaiveLogFilePlugin;
import org.xesstandard.model.XesLog;
import org.xesstandard.xml.XesXmlParserLenient;

public class XLogReaderNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(XLogReaderNodeModel.class);
	public static String[] CFG_TYPES = {"XES","XES_GZ","MXML","MXML_GZ"};
	 // if we choose the xes, the possible values are limited in the xes , is that true?? Later deal with it 
	public static String[] CFG_METHODS = {"OPEN NAIVE", "IEEE Lenient"};
	
	SettingsModelString m_format = createFormatModel();
	SettingsModelString m_method = createMethodModel();
	SettingsModelString m_fileName = createFileNameModel(); 
	
	public static SettingsModelString createFormatModel() {
		// TODO assign the type value from one string array
		return new SettingsModelString("Read Type", CFG_TYPES[0]);
	}
	
	public static SettingsModelString createMethodModel() {
		// TODO assign the method value from one string array
		// if we have multiple reading method, one class is enough to contain them all. 
		return new SettingsModelString("Read Method", CFG_METHODS[0]);
	}

	public static SettingsModelString createFileNameModel() {
		
		return new SettingsModelString("File Name","");
	}
	
	protected XLogReaderNodeModel() {
		super(new PortType[] {},
				new PortType[] { PortTypeRegistry.getInstance().getPortType(XLogPortObject.class, false) });
	}

	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		logger.info("start: import event log");
		// check the reading type and method for it?? 
		// Are they 1-1 relation?? One method only corresponds to one reading methods?? 
		// if there is 1 -n , we can choose them!!
		
		File file = new File(m_fileName.getStringValue());
		// the difference of format and then later the read plugin should also change
		XLog result = null;
		if(m_method.getStringValue().equals(CFG_METHODS[0])) {
			// Open Naive can read multiple types of event log!!
			OpenNaiveLogFilePlugin plugin = new OpenNaiveLogFilePlugin();
			result = (XLog) plugin.importFile(
					PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(OpenNaiveLogFilePlugin.class), file);
			
		}else if(m_method.getStringValue().equals(CFG_METHODS[1])) {
			// this parser imports all extensions in event log.
			XesXmlParserLenient lenientParser = new XesXmlParserLenient();
			if(lenientParser.canParse(file)) {
				XesLog xlog = lenientParser.parse(file);
				XesConvertToXLogAlgorithm convertor = new XesConvertToXLogAlgorithm();
				result = convertor.convertToLog(xlog);
			}
		}
		
		XLogPortObject logPO = new XLogPortObject();
		
		logPO.setLog(result);
		
		// how to use this classifiers?? 
		logger.info("end: import event log");
		return new PortObject[] { logPO };
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) {
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
		m_format.saveSettingsTo(settings);
		m_method.saveSettingsTo(settings);
		m_fileName.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_format.validateSettings(settings);
		m_method.validateSettings(settings);
		m_fileName.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_format.loadSettingsFrom(settings);
		m_method.loadSettingsFrom(settings);
		m_fileName.loadSettingsFrom(settings);
	}
}
