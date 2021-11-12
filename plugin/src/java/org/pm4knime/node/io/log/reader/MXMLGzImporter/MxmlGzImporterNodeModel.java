package org.pm4knime.node.io.log.reader.MXMLGzImporter;

import java.io.File;
import java.io.IOException;

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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.node.io.log.reader.XesConvertToXLogAlgorithm;
import org.pm4knime.node.io.log.reader.XesImporter.XesImporterNodeModel;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.log.OpenNaiveLogFilePlugin;
import org.xesstandard.model.XesLog;
import org.xesstandard.xml.XesXmlParserLenient;

/**
 * <code>NodeModel</code> for the "MxmlGzImporter" node.
 *
 * @author tbd
 */
public class MxmlGzImporterNodeModel extends DefaultNodeModel {
	 /**
	 * The logger is used to print info/warning/error messages to the KNIME console
	 * and to the KNIME log file. Retrieve it via 'NodeLogger.getLogger' providing
	 * the class of this node model.
	 */
	private static final NodeLogger LOGGER = NodeLogger.getLogger(MxmlGzImporterNodeModel.class);



	private final static String[] CFG_METHODS = {"OPEN NAIVE", "IEEE Lenient"};
	private SettingsModelString m_method = createMethodModel();
	private SettingsModelString m_fileName = createFileNameModel(); 
	/**
	 * Constructor for the node model.
	 */
	protected MxmlGzImporterNodeModel() {
		/**
		 * Here we specify how many data input and output tables the node should have.
		 * In this case its one input and one output table.
		 */
		super(new PortType[] {},
				new PortType[] { PortTypeRegistry.getInstance().getPortType(XLogPortObject.class, false) });
		}


	
	public static SettingsModelString createMethodModel() {
		return new SettingsModelString("Read Method", CFG_METHODS[0]);
	}

	public static SettingsModelString createFileNameModel() {
		return new SettingsModelString("File Name","");
	}

	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		LOGGER.info("start: import event log");
		// check the reading type and method for it?? 
		// Are they 1-1 relation?? One method only corresponds to one reading methods?? 
		// if there is 1 -n , we can choose them!!
		
		File file = new File(m_fileName.getStringValue());
		// the difference of format and then later the read plugin should also change
		XLog result = null;
		if(m_method.getStringValue().equals(CFG_METHODS[0])) {
			// Open Naive can read multiple types of event log!!
			OpenNaiveLogFilePlugin plugin = new OpenNaiveLogFilePlugin();
			PluginContext context = PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(OpenNaiveLogFilePlugin.class);
			checkCanceled(context, exec);
			result = (XLog) plugin.importFile(context
					, file);
			
		}else if(m_method.getStringValue().equals(CFG_METHODS[1])) {
			// this parser imports all extensions in event log.
			XesXmlParserLenient lenientParser = new XesXmlParserLenient();
			if(lenientParser.canParse(file)) {
				XesLog xlog = lenientParser.parse(file);
				XesConvertToXLogAlgorithm convertor = new XesConvertToXLogAlgorithm();
				result = convertor.convertToLog(xlog, exec);
			}
		}
		
		XLogPortObject logPO = new XLogPortObject();
		
		logPO.setLog(result);
		
		// how to use this classifiers?? 
		LOGGER.info("end: import event log");
		return new PortObject[] { logPO };
	}

	

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) {
		XLogPortObjectSpec outSpec = new XLogPortObjectSpec();
		return new PortObjectSpec[] { outSpec};
	}

	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_method.saveSettingsTo(settings);
		m_fileName.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_method.validateSettings(settings);
		m_fileName.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_method.loadSettingsFrom(settings);
		m_fileName.loadSettingsFrom(settings);
	}

	public static String[] getCFG_METHODS() {
		return CFG_METHODS;
	}

}

