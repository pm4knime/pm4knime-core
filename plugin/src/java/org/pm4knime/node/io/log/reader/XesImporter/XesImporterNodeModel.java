package org.pm4knime.node.io.log.reader.XesImporter;

import java.io.File;
import java.util.EnumSet;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.filehandling.core.connections.FSCategory;
import org.knime.filehandling.core.connections.FSFiles;
import org.knime.filehandling.core.connections.FSPath;
import org.knime.filehandling.core.defaultnodesettings.EnumConfig;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.ReadPathAccessor;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;
import org.knime.filehandling.core.defaultnodesettings.filtermode.SettingsModelFilterMode.FilterMode;
import org.knime.filehandling.core.defaultnodesettings.status.NodeModelStatusConsumer;
import org.knime.filehandling.core.defaultnodesettings.status.StatusMessage.MessageType;
import org.pm4knime.node.io.log.reader.XesConvertToXLogAlgorithm;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.log.OpenNaiveLogFilePlugin;
import org.xesstandard.model.XesLog;
import org.xesstandard.xml.XesXmlParserLenient;

/**
 * This is an example implementation of the node model of the "XesImporter"
 * node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author tbd
 */
public class XesImporterNodeModel extends DefaultNodeModel {

	/**
	 * The logger is used to print info/warning/error messages to the KNIME console
	 * and to the KNIME log file. Retrieve it via 'NodeLogger.getLogger' providing
	 * the class of this node model.
	 */
	private static final NodeLogger LOGGER = NodeLogger.getLogger(XesImporterNodeModel.class);
	private static final EnumConfig<FilterMode> mode = EnumConfig.create(FilterMode.FILE);
	private static final EnumSet<FSCategory> DEFAULT_FS = //
			EnumSet.of(FSCategory.LOCAL, FSCategory.MOUNTPOINT, FSCategory.RELATIVE);

	private final static String[] CFG_METHODS = { "OPEN NAIVE", "IEEE Lenient" };
	private SettingsModelString m_method = createMethodModel();
	private SettingsModelString m_fileName = createFileNameModel();
	private static final String SOURCE_FILE = "sourcefile";
	private final SettingsModelReaderFileChooser m_sourceModel;
	// transport errors / warning to node
	private final NodeModelStatusConsumer m_statusConsumer = new NodeModelStatusConsumer(
			EnumSet.of(MessageType.ERROR, MessageType.WARNING));

	/**
	 * Constructor for the node model.
	 */
	XesImporterNodeModel(final PortsConfiguration portsConfig) {
		/**
		 * Here we specify how many data input and output tables the node should have.
		 * In this case its one input and one output table.
		 */

		super(new PortType[] {},
				new PortType[] { PortTypeRegistry.getInstance().getPortType(XLogPortObject.class, false) });
		m_sourceModel = createSourceModel(portsConfig);

		// super(portsConfig.getInputPorts(), portsConfig.getOutputPorts());
		// m_sourceModel = createSourceModel(portsConfig);

	}

	public static SettingsModelString createMethodModel() {
		return new SettingsModelString("Read Method", CFG_METHODS[0]);
	}

	public static SettingsModelString createFileNameModel() {
		return new SettingsModelString("File Name", "");
	}

	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		LOGGER.info("start: import event log");
		// check the reading type and method for it??
		// Are they 1-1 relation?? One method only corresponds to one reading methods??
		// if there is 1 -n , we can choose them!!
		final ReadPathAccessor readAccessor = m_sourceModel.createReadPathAccessor();
		final String paths2 = readAccessor.getPaths(m_statusConsumer).get(0).toAbsolutePath().toString();
		final File file = readAccessor.getFSPaths(m_statusConsumer).get(0).toFile();
		final FSPath inputPath = readAccessor.getFSPaths(m_statusConsumer).get(0);
		String selectionText = m_sourceModel.getPath();
		m_statusConsumer.setWarningsIfRequired(this::setWarningMessage);

		// the difference of format and then later the read plugin should also change
		XLog result = null;
		if (m_method.getStringValue().equals(CFG_METHODS[0])) {
			// Open Naive can read multiple types of event log!!
			OpenNaiveLogFilePlugin plugin = new OpenNaiveLogFilePlugin();
			PluginContext context = PM4KNIMEGlobalContext.instance()
					.getFutureResultAwarePluginContext(OpenNaiveLogFilePlugin.class);
			checkCanceled(context, exec);
			result = (XLog) plugin.importFromStream(context, FSFiles.newInputStream(inputPath), file.getName(),
					file.length());

		} else if (m_method.getStringValue().equals(CFG_METHODS[1])) {
			// this parser imports all extensions in event log.
			XesXmlParserLenient lenientParser = new XesXmlParserLenient();
			if (lenientParser.canParse(file)) {
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

	/**
	 * Create source model
	 */
	static final SettingsModelReaderFileChooser createSourceModel(final PortsConfiguration portsConfig) {
		return new SettingsModelReaderFileChooser(SOURCE_FILE, portsConfig, "", mode, DEFAULT_FS, "xes");
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) {
		XLogPortObjectSpec outSpec = new XLogPortObjectSpec();
		return new PortObjectSpec[] { outSpec };
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_method.saveSettingsTo(settings);
		m_sourceModel.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_method.validateSettings(settings);
		m_sourceModel.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_method.loadSettingsFrom(settings);
		m_sourceModel.loadSettingsFrom(settings);
	}

	public static String[] getCFG_METHODS() {
		return CFG_METHODS;
	}
}
