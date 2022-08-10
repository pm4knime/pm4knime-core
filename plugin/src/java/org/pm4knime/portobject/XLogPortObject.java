package org.pm4knime.portobject;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.node.io.log.reader.XesConvertToXLogAlgorithm;
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logenhancement.view.LogViewContextProM;
import org.processmining.logenhancement.view.LogViewVisualizer;
import org.processmining.logprojection.LogView;
import org.processmining.logprojection.plugins.dottedchart.DottedChart.DottedChartException;
import org.processmining.logprojection.plugins.dottedchart.ui.DottedChartInspector;
import org.processmining.plugins.log.ui.logdialog.SlickerOpenLogSettings;
import org.xesstandard.model.XesLog;
import org.xesstandard.xml.XesXmlParserLenient;

public class XLogPortObject extends AbstractPortObject {

	/**
	 * Define port type of objects of this class when used as PortObjects.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(XLogPortObject.class);

	private XLogPortObjectSpec m_spec;

	private static final String ZIP_ENTRY_NAME = "XLogPortObject";

	private XLog log = null;

	public XLogPortObject() {
	}

	public XLogPortObject(XLog log) {
		// TODO Auto-generated constructor stub
		this.log = log;
	}

	public XLog getLog() {
		return log;
	}

	public void setLog(final XLog log) {
		this.log = log;
	}

	@Override
	public String getSummary() {
		return "This port represents an event log object (XLog)";
	}

	public void setSpec(XLogPortObjectSpec spec) {
		m_spec = spec;
	}

	@Override
	public PortObjectSpec getSpec() {

		if (m_spec == null) {
			System.out.println("New created log spec");
			m_spec = XLogSpecUtil.extractSpec(log);
		}
		return m_spec;
	}

	@Override
	public JComponent[] getViews() {
		if (this.log == null) {
			return new JComponent[] {};
		} else {

			try {
				PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
				SlickerOpenLogSettings defViz = new SlickerOpenLogSettings();
				JComponent logPanel = defViz.showLogVis(context, this.log);
				logPanel.setName("Event Log");

				JComponent tracePanel = new LogViewVisualizer(new LogViewContextProM(context), this.log);
				tracePanel.setName("Variant Explorer");

				JComponent dottedchartPanel = new DottedChartInspector(new LogView(this.log, context.getProgress()),
						context);
				dottedchartPanel.setName("Dotted Chart");
				Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				dottedchartPanel.setPreferredSize(new Dimension(dim.width / 2, dim.height / 2));

				return new JComponent[] { dottedchartPanel, logPanel, tracePanel };

			} catch (DottedChartException e) {
				e.printStackTrace();
				return null;
			}

		}
	}

	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
		final ObjectOutputStream objOut = new ObjectOutputStream(out);
		XSerializer serializer = new XesXmlSerializer();
		serializer.serialize(this.getLog(), objOut);
		out.close();
	}

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		final ZipEntry entry = in.getNextEntry();
		if (!ZIP_ENTRY_NAME.equals(entry.getName())) {
			throw new IOException("Failed to load XLog port object. " + "Invalid zip entry name '" + entry.getName()
					+ "', expected '" + ZIP_ENTRY_NAME + "'.");
		}
		// modification: load all the information in the xml file by using the Leneit
		// read
		// XParser parser = new
		// XesXmlParser(XFactoryRegistry.instance().currentDefault());
		XesXmlParserLenient parser = new XesXmlParserLenient();

		final ObjectInputStream objIn = new ObjectInputStream(in);
		try {
			XesLog xeslog = parser.parse(objIn);
			XesConvertToXLogAlgorithm convertor = new XesConvertToXLogAlgorithm();
			XLog log = convertor.convertToLog(xeslog, exec);
			setLog(log);
			setSpec((XLogPortObjectSpec) spec);
		} catch (Exception e) {
			e.printStackTrace();
		}

		in.close();
	}

	public static class XLogPortObjectSerializer
			extends AbstractPortObject.AbstractPortObjectSerializer<XLogPortObject> {

	}

}
