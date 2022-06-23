package org.pm4knime.portobject;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.node.io.log.reader.XesConvertToXLogAlgorithm;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.extendedhybridminer.algorithms.HybridCGMiner;
import org.processmining.extendedhybridminer.algorithms.preprocessing.LogFilterer;
import org.processmining.extendedhybridminer.algorithms.preprocessing.TraceVariantsLog;
import org.processmining.extendedhybridminer.models.causalgraph.ExtendedCausalGraph;
import org.processmining.extendedhybridminer.models.causalgraph.gui.HybridCausalGraphVisualization;
import org.processmining.extendedhybridminer.models.causalgraph.gui.HybridCausalGraphVisualizer;
import org.processmining.extendedhybridminer.plugins.HybridCGMinerSettings;
import org.processmining.framework.plugin.PluginContext;
import org.xesstandard.model.XesLog;
import org.xesstandard.xml.XesXmlParserLenient;
import org.knime.core.node.port.AbstractPortObject;


public class CausalGraphPortObject extends AbstractPortObject{

	
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(CausalGraphPortObject.class);
	public static final PortType TYPE_OPTIONAL =
			PortTypeRegistry.getInstance().getPortType(CausalGraphPortObject.class, true);
	
	private static final String ZIP_ENTRY_NAME = "CausalGraphPortObject";
	
	ExtendedCausalGraph cg ;
	CausalGraphPortObjectSpec m_spec;
	
	public CausalGraphPortObject() {}
	
	public CausalGraphPortObject(ExtendedCausalGraph cg) {
		this.cg = cg;
	}
	
	
	public ExtendedCausalGraph getCG() {
		return cg;
	}

	public void setCG(ExtendedCausalGraph cg) {
		this.cg = cg;
	}

	@Override
	public String getSummary() {
		return "This port contains a CausalGraph object";
	}

	public boolean equals(Object o) {
		return cg.equals(o);
	}
	
	
	@Override
	public CausalGraphPortObjectSpec getSpec() {
		if(m_spec!=null)
			return m_spec;
		return new CausalGraphPortObjectSpec();
	}

	public void setSpec(PortObjectSpec spec) {
		m_spec = (CausalGraphPortObjectSpec) spec;
	}
	
	@Override
	public JComponent[] getViews() {
		if (cg != null) {
			PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
			JComponent view = HybridCausalGraphVisualizer.visualize(context, cg);
			view.remove(14);
			view.remove(13);
			view.remove(10);
			view.remove(9);
			view.remove(8);
			view.remove(7);
			view.validate();
			view.repaint();
			view.setName("Causal Graph");
			return new JComponent[] {view};
		}
		
		return new JComponent[] {};
	}

	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
		final ObjectOutputStream objOut = new ObjectOutputStream(out);
		
		HybridCGMinerSettings settings = cg.getSettings();
		objOut.writeDouble(settings.getFilterAcivityThreshold());
		objOut.writeDouble(settings.getTraceVariantsThreshold());
		objOut.writeDouble(settings.getSureThreshold());
		objOut.writeDouble(settings.getQuestionMarkThreshold());
		objOut.writeDouble(settings.getLongDepThreshold());
		objOut.writeDouble(settings.getCausalityWeight());
		objOut.writeInt(cg.getSureColor().getRGB());
		objOut.writeInt(cg.getUnsureColor().getRGB());
		objOut.writeInt(cg.getLongDepColor().getRGB());
		
		XSerializer serializer = new XesXmlSerializer();
		serializer.serialize(this.getCG().getUnfilteredLog(), objOut);
		objOut.close();
		out.close();
	}

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		final ZipEntry entry = in.getNextEntry();
		if (!ZIP_ENTRY_NAME.equals(entry.getName())) {
			throw new IOException("Failed to load Causal Graph port object. " + "Invalid zip entry name '" + entry.getName()
					+ "', expected '" + ZIP_ENTRY_NAME + "'.");
		}
		XesXmlParserLenient parser = new XesXmlParserLenient();

		final ObjectInputStream objIn = new ObjectInputStream(in);
		try {
			setSpec((CausalGraphPortObjectSpec) spec);
			HybridCGMinerSettings settings = new HybridCGMinerSettings();
			settings.setFilterAcivityThreshold(objIn.readDouble());
			settings.setTraceVariantsThreshold(objIn.readDouble());
			settings.setSureThreshold(objIn.readDouble());
			settings.setQuestionMarkThreshold(objIn.readDouble());
			settings.setLongDepThreshold(objIn.readDouble());
			settings.setCausalityWeight(objIn.readDouble());
			
			Color color1 = new Color(objIn.readInt());
			Color color2 = new Color(objIn.readInt());
			Color color3 = new Color(objIn.readInt());		
			
			XesLog xeslog = parser.parse(objIn);
			XesConvertToXLogAlgorithm convertor = new XesConvertToXLogAlgorithm();
			XLog log = convertor.convertToLog(xeslog, exec);
			
			
			
	    	XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, settings.getClassifier());
			XLog filteredLog = LogFilterer.filterLogByActivityFrequency(log, logInfo, settings);
			TraceVariantsLog variants = new TraceVariantsLog(filteredLog, settings, settings.getTraceVariantsThreshold());
			HybridCGMiner miner = new HybridCGMiner(filteredLog, filteredLog.getInfo(settings.getClassifier()), variants, settings);
			ExtendedCausalGraph cg = miner.mineFCG();
			cg.setUnfilteredLog(log);
			cg.updateSureColor(color1);
			cg.updateUnsureColor(color2);
			cg.updateLongDepColor(color3);
			setCG(cg);
		} catch (Exception e) {
			e.printStackTrace();
		}

		in.close();
	}

	public static class CausalGraphPortObjectSerializer
			extends AbstractPortObject.AbstractPortObjectSerializer<CausalGraphPortObject> {

	}


}
