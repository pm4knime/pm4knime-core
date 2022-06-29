package org.pm4knime.portobject;


import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.util.HybridPetriNetUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.extendedhybridminer.models.hybridpetrinet.ExtendedHybridPetrinet;
import org.processmining.extendedhybridminer.models.causalgraph.gui.HybridPetrinetVisualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.xesstandard.xml.XesXmlParserLenient;


public class HybridPetriNetPortObject extends AbstractPortObject{

	/**
	 * Define port type of objects of this class when used as PortObjects.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(HybridPetriNetPortObject.class);
	public static final PortType TYPE_OPTIONAL =
			PortTypeRegistry.getInstance().getPortType(HybridPetriNetPortObject.class, true);
	
	private static final String ZIP_ENTRY_NAME = "HybridPetriNetPortObject";
	
	static ExtendedHybridPetrinet pn ;
	HybridPetriNetPortObjectSpec m_spec;
	
	public HybridPetriNetPortObject() {}
	
	public HybridPetriNetPortObject(ExtendedHybridPetrinet pn) {
		HybridPetriNetPortObject.pn = pn;
	}
	
	
	public ExtendedHybridPetrinet getPN() {
		return pn;
	}

	public void setPN(ExtendedHybridPetrinet net) {
		pn = net;
	}

	@Override
	public String getSummary() {
		return "This port contains a HybridPetriNet object";
	}

	public boolean equals(Object o) {
		return pn.equals(o);
	}
	
	
	@Override
	public HybridPetriNetPortObjectSpec getSpec() {
		if(m_spec!=null)
			return m_spec;
		return new HybridPetriNetPortObjectSpec();
	}

	public void setSpec(PortObjectSpec spec) {
		m_spec = (HybridPetriNetPortObjectSpec) spec;
	}
	
	@Override
	public JComponent[] getViews() {
		if (pn != null) {
			
			PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
			JComponent view = null;
			try {
				view = HybridPetrinetVisualizer.visualize(context, pn);
				int n = view.getComponents().length - 1;
				view.remove(n);
				view.remove(n-1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			view.setName("Hybrid Petri Net");
			return new JComponent[] {view};
		}
		
		return new JComponent[] {};
	}
	
	public DotPanel getDotPanel() {
		
//	if(cg != null) {
//			
//			DotPanel navDot;
//			navDot = new DotPanel(GraphvizPetriNet.convert(m_anet));
//			navDot.setName("Generated Causal Graph");
//			return navDot;
//			
//		}
		return null;
		
	}

	
	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		
		out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
		final ObjectOutputStream objOut = new ObjectOutputStream(out);
		
		objOut.writeInt(pn.getSurePlaceColor().getRGB());
		objOut.writeInt(pn.getSureColor().getRGB());
		objOut.writeInt(pn.getUnsureColor().getRGB());
		objOut.writeInt(pn.getLDColor().getRGB());
		
		HybridPetriNetUtil.exportHybridPetrinetToFile(objOut, pn);
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
		final ObjectInputStream objIn = new ObjectInputStream(in);
		try {
			setSpec((HybridPetriNetPortObjectSpec) spec);

			Color color1 = new Color(objIn.readInt());
			Color color2 = new Color(objIn.readInt());
			Color color3 = new Color(objIn.readInt());	
			Color color4 = new Color(objIn.readInt());	
						
			ExtendedHybridPetrinet net = new ExtendedHybridPetrinet("Hybrid Petri Net");
			HybridPetriNetUtil.importHybridPetrinetFromStream(objIn, net);
			
			net.updateSurePlaceColor(color1);
			net.updateSureColor(color2);
			net.updateUnsureColor(color3);
			net.updateLDColor(color4);
			setPN(net);
		} catch (Exception e) {
			e.printStackTrace();
		}

		in.close();
	}

	public static class HybridPetriNetPortObjectSerializer
			extends AbstractPortObject.AbstractPortObjectSerializer<HybridPetriNetPortObject> {

	}
}
