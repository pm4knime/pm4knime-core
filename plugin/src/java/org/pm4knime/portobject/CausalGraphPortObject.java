package org.pm4knime.portobject;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4knime.node.discovery.cgminer.table.TraceVariantsTable;
import org.pm4knime.node.visualizations.jsgraphviz.util.GraphvizCausalGraph;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.extendedhybridminer.algorithms.preprocessing.TraceVariant;
import org.processmining.extendedhybridminer.algorithms.preprocessing.TraceVariantsLog;
import org.processmining.extendedhybridminer.models.causalgraph.ExtendedCausalGraph;
import org.processmining.extendedhybridminer.models.causalgraph.HybridDirectedGraphEdge;
import org.processmining.extendedhybridminer.models.causalgraph.HybridDirectedGraphNode;
import org.processmining.extendedhybridminer.models.causalgraph.HybridDirectedLongDepGraphEdge;
import org.processmining.extendedhybridminer.models.causalgraph.HybridDirectedSureGraphEdge;
import org.processmining.extendedhybridminer.models.causalgraph.HybridDirectedUncertainGraphEdge;
import org.processmining.extendedhybridminer.models.causalgraph.gui.HybridCausalGraphVisualizer;
import org.processmining.extendedhybridminer.plugins.HybridCGMinerSettings;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

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
		return "Nodes: " + cg.getNodes().size() + ", Edges: " + cg.getEdges().size();
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

		Map<String, Integer> freqMap = cg.getActivityFrequencyMap();
		objOut.writeInt(freqMap.size());
		for(String a: freqMap.keySet()) {
			objOut.writeUTF(a);	
			objOut.writeInt(freqMap.get(a));
		}
		
		Map<String, Integer> idMap = cg.getActivitiesMapping();
		int numAct = idMap.size();
		objOut.writeInt(numAct);
		for(String a: idMap.keySet()) {
			objOut.writeUTF(a);	
			int i = idMap.get(a);
			objOut.writeInt(i);
			for (int j=0; j<numAct; j++) {
				objOut.writeDouble(cg.getInputDirectSuccessionDependency(i, j));
				objOut.writeDouble(cg.getOutputDirectSuccessionDependency(i, j));
				objOut.writeDouble(cg.getILD().get(i, j));
				objOut.writeDouble(cg.getOLD().get(i, j));
				objOut.writeDouble(cg.getDirectSuccessionCount(i, j));
				objOut.writeDouble(cg.getRel1(i, j));
				objOut.writeDouble(cg.getRel2(i, j));
				objOut.writeDouble(cg.getEF().get(i, j));
				objOut.writeDouble(cg.getrel1LD().get(i, j));
				objOut.writeDouble(cg.getrel2LD().get(i, j));
			}
		}		
		
		// write the nodes
		objOut.writeInt(cg.getNodes().size());
		for(HybridDirectedGraphNode n: cg.getNodes()) {
			objOut.writeUTF(n.getLabel());
		}
			
        // write certain edges
		objOut.writeInt(cg.getSureGraphEdges().size());
		for(HybridDirectedGraphEdge e: cg.getSureGraphEdges()) {
			objOut.writeUTF(e.getSource().getLabel());
			objOut.writeUTF(e.getTarget().getLabel());
			objOut.writeDouble(e.getDirectSuccession());
			objOut.writeDouble(e.getDirectSuccessionDependency());
			objOut.writeDouble(e.getCausalityMetric());
			objOut.writeDouble(e.getAbDependencyMetric());
			objOut.writeDouble(e.getODSD());
			objOut.writeDouble(e.getIDSD());
		}
		
        // write uncertain edges
		objOut.writeInt(cg.getUncertainGraphEdges().size());
		for(HybridDirectedUncertainGraphEdge e: cg.getUncertainGraphEdges()) {
			objOut.writeUTF(e.getSource().getLabel());
			objOut.writeUTF(e.getTarget().getLabel());
			objOut.writeDouble(e.getDirectSuccession());
			objOut.writeDouble(e.getDirectSuccessionDependency());
			objOut.writeDouble(e.getCausalityMetric());
			objOut.writeDouble(e.getAbDependencyMetric());
			objOut.writeDouble(e.getODSD());
			objOut.writeDouble(e.getIDSD());
		}
				
		// write longDep edges
		objOut.writeInt(cg.getLongDepGraphEdges().size());
		for(HybridDirectedGraphEdge e: cg.getLongDepGraphEdges()) {
			objOut.writeUTF(e.getSource().getLabel());
			objOut.writeUTF(e.getTarget().getLabel());
			objOut.writeDouble(e.getDirectSuccession());
			objOut.writeDouble(e.getDirectSuccessionDependency());
			objOut.writeDouble(e.getCausalityMetric());
			objOut.writeDouble(e.getAbDependencyMetric());
			objOut.writeDouble(e.getODSD());
			objOut.writeDouble(e.getIDSD());
		}	
		
		writeTraceVariantsLog(cg.getTraceVariants(), objOut);
		
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
			
			//HybridCGMiner miner = new HybridCGMiner(null, null, new TraceVariantsTable(settings), settings);
			ExtendedCausalGraph cg = new ExtendedCausalGraph();
			cg.updateSureColor(color1);
			cg.updateUnsureColor(color2);
			cg.updateLongDepColor(color3);
			
			cg.setActivityFrequencyMap(settings.getActivityFrequencyMap());
			cg.setSettings(settings);		

			Map<String, Integer> freqMap = new HashMap<String, Integer>();
			int numIterations = objIn.readInt();
			for(int i =0; i < numIterations; i++ ) {
				freqMap.put(objIn.readUTF(), objIn.readInt());
			}
			cg.setActivityFrequencyMap(freqMap);
			
			HashMap<String, Integer> actToIndexMap = new HashMap<String, Integer>();
			HashMap<Integer, String> indexToActMap = new HashMap<Integer, String>();
			numIterations = objIn.readInt();
			DoubleMatrix2D oLD = DoubleFactory2D.sparse.make(numIterations, numIterations, 0);
			DoubleMatrix2D iLD = DoubleFactory2D.sparse.make(numIterations, numIterations, 0);
			DoubleMatrix2D oDD = DoubleFactory2D.sparse.make(numIterations, numIterations, 0);
			DoubleMatrix2D iDD = DoubleFactory2D.sparse.make(numIterations, numIterations, 0);
			DoubleMatrix2D directSuccessionCount = DoubleFactory2D.sparse.make(numIterations, numIterations, 0);
			DoubleMatrix2D rel1 = DoubleFactory2D.sparse.make(numIterations, numIterations, 0);
			DoubleMatrix2D rel2 = DoubleFactory2D.sparse.make(numIterations, numIterations, 0);
			DoubleMatrix2D ef = DoubleFactory2D.sparse.make(numIterations, numIterations, 0);
			DoubleMatrix2D rel1LD = DoubleFactory2D.sparse.make(numIterations, numIterations, 0);
			DoubleMatrix2D rel2LD = DoubleFactory2D.sparse.make(numIterations, numIterations, 0);			

			for(int i =0; i < numIterations; i++ ) {
				String act = objIn.readUTF();
				int index = objIn.readInt();
				actToIndexMap.put(act, index);
				indexToActMap.put(index, act);
				for (int j=0; j<numIterations; j++) {
					iDD.set(i, j, objIn.readDouble());
					oDD.set(i, j, objIn.readDouble());
					iLD.set(i, j, objIn.readDouble());
					oLD.set(i, j, objIn.readDouble());
					directSuccessionCount.set(i, j, objIn.readDouble());
					rel1.set(i, j, objIn.readDouble());
					rel2.set(i, j, objIn.readDouble());
					ef.set(i, j, objIn.readDouble());
					rel1LD.set(i, j, objIn.readDouble());
					rel2LD.set(i, j, objIn.readDouble());
				}
			}
			cg.setActivitiesMapping(indexToActMap, actToIndexMap);
			cg.setMetrics(numIterations, directSuccessionCount, rel1, rel2, oDD, iDD, ef, oLD, iLD, rel1LD, rel2LD);
			
			// add nodes
			numIterations = objIn.readInt();
			for(int i =0; i < numIterations; i++ ) {
				cg.addNode(objIn.readUTF());
			}
			
			// add certain edges
			numIterations = objIn.readInt();
			for(int i =0; i < numIterations; i++ ) {
				HybridDirectedGraphNode source = cg.getNode(objIn.readUTF());
				HybridDirectedGraphNode target = cg.getNode(objIn.readUTF());
				HybridDirectedSureGraphEdge e = cg.addSureEdge(source, target);
				e.setDirectSuccession(objIn.readDouble());
				e.setDirectSuccessionDependency(objIn.readDouble());
				e.setCausalityMetric(objIn.readDouble());
				e.setAbDependencyMetric(objIn.readDouble());
				e.setIDSD_ODSD(objIn.readDouble(), objIn.readDouble());
			}
			
			// add uncertain edges
			numIterations = objIn.readInt();
			for(int i =0; i < numIterations; i++ ) {
				HybridDirectedGraphNode source = cg.getNode(objIn.readUTF());
				HybridDirectedGraphNode target = cg.getNode(objIn.readUTF());
				HybridDirectedUncertainGraphEdge e = cg.addUncertainEdge(source, target);
				e.setDirectSuccession(objIn.readDouble());
				e.setDirectSuccessionDependency(objIn.readDouble());
				e.setCausalityMetric(objIn.readDouble());
				e.setAbDependencyMetric(objIn.readDouble());
				e.setIDSD_ODSD(objIn.readDouble(), objIn.readDouble());	
			}
			
			// add longDep edges
			numIterations = objIn.readInt();
			for(int i =0; i < numIterations; i++ ) {
				HybridDirectedGraphNode source = cg.getNode(objIn.readUTF());
				HybridDirectedGraphNode target = cg.getNode(objIn.readUTF());
				HybridDirectedLongDepGraphEdge e = cg.addLongDepEdge(source, target);
				e.setDirectSuccession(objIn.readDouble());
				e.setDirectSuccessionDependency(objIn.readDouble());
				e.setCausalityMetric(objIn.readDouble());
				e.setAbDependencyMetric(objIn.readDouble());
				e.setIDSD_ODSD(objIn.readDouble(), objIn.readDouble());		
			}	
			
			cg.setTraceVariants(loadTraceVariants(settings, objIn));			
			setCG(cg);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		in.close();
	}

	private void writeTraceVariantsLog(TraceVariantsLog variants, ObjectOutputStream objOut) throws IOException {
		objOut.writeInt(variants.getOriginalLogSize());
		objOut.writeInt(variants.getNumberOfCoveredTraces());
		objOut.writeInt(variants.getSize());
		for (TraceVariant v: variants.getVariants()) {
			ArrayList<String> activites = v.getActivities();
			objOut.writeInt(activites.size());
			for (String a: activites) {
				objOut.writeUTF(a);
			}
			objOut.writeInt(v.getFrequency());
		}
	}
	
	private TraceVariantsTable loadTraceVariants(HybridCGMinerSettings settings, ObjectInputStream objIn) throws IOException {
		TraceVariantsTable res = new TraceVariantsTable(settings);
		res.loadFromStream(settings, objIn);
		return res;
	}

	public static class CausalGraphPortObjectSerializer
			extends AbstractPortObject.AbstractPortObjectSerializer<CausalGraphPortObject> {

	}

public DotPanel getDotPanel() {
		
		if(cg != null) {
			
			DotPanel navDot;
			navDot = new DotPanel(GraphvizCausalGraph.convert(cg));
			navDot.setName("Generated Causal Graph");
			return navDot;
			
		}
		return null;
		
	}


}
