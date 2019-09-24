package org.pm4knime.portobject;


import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.acceptingpetrinet.plugins.VisualizeAcceptingPetriNetPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.base.Pnml.PnmlType;
import org.processmining.plugins.pnml.base.PnmlElementFactory;

/**
 * this class defines PetriNetPortObject. It includes models as Petrinet + InitialMarking, FinalMarking, FinalMarkings[].
 * Serializer load and save Petrinet with PromPlugin for accepting Petri net. 
 * Views uses the Accepting Petri Net to show it
 * @author kefang-pads
 *
 */
public class PetriNetPortObject  implements PortObject{

	/**
	 * Define port type of objects of this class when used as PortObjects.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class);
	public static final PortType TYPE_OPTIONAL =
			PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class, true);
	
	private static final String ZIP_ENTRY_NAME = "PetriNetPortObject";
	
	// use AcceptingPetriNet as the model
	AcceptingPetriNet m_anet ;
		
	
	public PetriNetPortObject() {}
	
	public PetriNetPortObject(AcceptingPetriNet anet) {
		m_anet = anet;
	}
	
	
	public AcceptingPetriNet getANet() {
		return m_anet;
	}

	public void setANet(AcceptingPetriNet anet) {
		m_anet = anet;
	}

	@Override
	public String getSummary() {
		return "This port contains a Petri net object";
	}

	public boolean equals(Object o) {
		return m_anet.equals(o);
	}
	
	public static String convert2String(AcceptingPetriNet anet) {
		PnmlElementFactory factory = new FullPnmlElementFactory();
		Pnml pnml = new Pnml();
		synchronized (factory) {
			pnml.setFactory(factory);
			
			GraphLayoutConnection  layout = new GraphLayoutConnection(anet.getNet());
			
			pnml = new Pnml().convertFromNet(anet.getNet(), anet.getInitialMarking(), anet.getFinalMarkings(), layout);
			pnml.setType(PnmlType.PNML);
		}
		
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + pnml.exportElement(pnml);
		return text;
	}
	
	public static AcceptingPetriNet convert2ANet(InputStream in) throws Exception {
		PluginContext context =  PM4KNIMEGlobalContext.instance().getPM4KNIMEPluginContext();
		AcceptingPetriNet anet = AcceptingPetriNetFactory.createAcceptingPetriNet();
		anet.importFromStream(context, in);
		return anet;
	}
	
	@Override
	public PetriNetPortObjectSpec getSpec() {
		// here we need to create a POSpec for Petri net
		PetriNetPortObjectSpec spec = new PetriNetPortObjectSpec();
		return spec;
	}

	
	/**
	 * If we show the Petri net as the AcceptingPetriNet, better to use AcceptingPetrinet as model.
	 * If there are no finalMarking, we can assign them. That's all the important stuff here.
	 * 
	 */
	@Override
	public JComponent[] getViews() {
		if (m_anet != null) {
			
			PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
			return new JComponent[] {VisualizeAcceptingPetriNetPlugin.visualize(context, m_anet)};
		}
		
		return new JComponent[] {};
	}

	
	// here we serialise the PortObject by using the prom plugin
	public static class PetriNetPortObjectSerializer extends PortObjectSerializer<PetriNetPortObject> {

		@Override
		public void savePortObject(PetriNetPortObject portObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
				throws IOException, CanceledExecutionException {
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			
			// do we need the layout to import or export the AccepingPetrinet??
			// we can't use the ObjectOutputStream, because AcceptingPetrinet not serialized..
			// recode it from AcceptingPetriNetImpl.exportToFile()
			out.write(convert2String(portObject.getANet()).getBytes());
			out.closeEntry();
			out.close();
			
		}

		@Override
		public PetriNetPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			// nothing to do with spec here 
			ZipEntry nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
			}
			
			PetriNetPortObject result = null;
			try {
				// they put layout information into context, if we want to show the them, 
				// we need to keep the context the same in load and save program. But how to do this??
				// that's why there is context in portObject. If we also save the context, what can be done??
				AcceptingPetriNet anet = convert2ANet(in);
				result = new PetriNetPortObject(anet);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			in.close();
			
			return result;
		}
		
	}
}
