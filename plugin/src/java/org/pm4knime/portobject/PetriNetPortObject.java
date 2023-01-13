package org.pm4knime.portobject;


import java.io.IOException;
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
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.VisualizeAcceptingPetriNetPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.plugins.EfficientTreeVisualisationPlugin;
import org.processmining.plugins.inductiveVisualMiner.plugins.GraphvizPetriNet;

/**
 * this class defines PetriNetPortObject. It includes models as Petrinet + InitialMarking, FinalMarking, FinalMarkings[].
 * Serializer load and save Petrinet with PromPlugin for accepting Petri net. 
 * Views uses the Accepting Petri Net to show it
 * @author kefang-pads
 *
 */
public class PetriNetPortObject extends AbstractDotPanelPortObject {

	/**
	 * Define port type of objects of this class when used as PortObjects.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class);
	public static final PortType TYPE_OPTIONAL =
			PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class, true);
	
	private static final String ZIP_ENTRY_NAME = "PetriNetPortObject";
	
	// use AcceptingPetriNet as the model
	// m_anet: a field that carries anet
	AcceptingPetriNet m_anet ;
	PetriNetPortObjectSpec m_spec;
	private EfficientTree effTree;
	public PetriNetPortObject() {}
	
	public PetriNetPortObject(AcceptingPetriNet anet) {
		m_anet = anet;
		this.effTree = null;
	}
	
	public PetriNetPortObject(AcceptingPetriNet anet,EfficientTree effTree) throws UnknownTreeNodeException, ReductionFailedException {
		this.effTree = effTree;
		this.m_anet =  anet;
	}
	
	
	public AcceptingPetriNet getANet() {
		return m_anet;
	}

	public void setANet(AcceptingPetriNet anet) {
		m_anet = anet;
	}

	@Override
	public String getSummary() {
		return "Transitions: " + m_anet.getNet().getTransitions().size() + ", Places: " + m_anet.getNet().getPlaces().size();
	}

	public boolean equals(Object o) {
		return m_anet.equals(o);
	}
	
	
	@Override
	public PetriNetPortObjectSpec getSpec() {
		// here we need to create a POSpec for Petri net
		if(m_spec!=null)
			return m_spec;
		return new PetriNetPortObjectSpec();
	}

	public void setSpec(PortObjectSpec spec) {
		m_spec = (PetriNetPortObjectSpec) spec;
	}
	/**
	 * If we show the Petri net as the AcceptingPetriNet, better to use AcceptingPetrinet as model.
	 * If there are no finalMarking, we can assign them. That's all the important stuff here.
	 * 
	 */
	@Override
	public JComponent[] getViews() {
		if(effTree != null) {
			JComponent viewPanel = getDotPanel();
			viewPanel.setName("Petri net");
			return new JComponent[] { viewPanel };	
		}
		
		if (m_anet != null) {
			
			PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
			JComponent view = VisualizeAcceptingPetriNetPlugin.visualize(context, m_anet);
			view.setName("Petri net");
			return new JComponent[] {view};
		}
		
		return new JComponent[] {};
	}
	
	@Override
	public DotPanel getDotPanel() {
		
		if(effTree != null) {
			Dot dot = EfficientTreeVisualisationPlugin.fancy(effTree);
			DotPanel navDot = new DotPanel(dot);
			
			navDot.setName("Generated petri net");
			return navDot;
			
		}
		
		
	    if(m_anet != null) {
			
			DotPanel navDot;
			navDot = new DotPanel(GraphvizPetriNet.convert(m_anet));
			navDot.setName("Generated petri net");
			return navDot;
			
		}
		return null;
		
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
			PetriNetUtil.exportToStream(portObject.getANet(), out);
			out.closeEntry();
			// out.close();
			
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
				AcceptingPetriNet anet =PetriNetUtil.importFromStream(in);
				result = new PetriNetPortObject(anet);
				result.setSpec(spec);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// in.close();
			
			return result;
		}
		
	}


	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}
}
