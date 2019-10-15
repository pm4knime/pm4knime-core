package org.pm4knime.portobject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
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
import org.processmining.plugins.directlyfollowsmodel.DirectlyFollowsModel;
import org.processmining.plugins.inductiveminer2.helperclasses.graphs.IntGraph;
import org.processmining.plugins.inductiveminer2.plugins.DfgMsdVisualisationPlugin;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsdImpl;

public class DFMPortObject extends AbstractPortObject {

	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(DFMPortObject.class);
	private static final String ZIP_ENTRY_NAME = "DFMPortObject";
	DirectlyFollowsModel dfm;

	public DFMPortObject() {
	}

	public DFMPortObject(DirectlyFollowsModel dfm) {
		this.dfm = dfm;
	}

	public DirectlyFollowsModel getDfm() {
		return dfm;
	}

	public void setDfm(DirectlyFollowsModel dfm) {
		this.dfm = dfm;
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return "DirectlyFollowsModel PortObject";
	}

	@Override
	public PortObjectSpec getSpec() {
		// TODO Auto-generated method stub
		return new DFMPortObjectSpec();
	}

	@Override
	public JComponent[] getViews() {
		// TODO it has view which is
		// we need to change the steps..
		JComponent viewPanel = DfgMsdVisualisationPlugin.fancy(dfm);
		viewPanel.setName("Directly Follows Model");
		return new JComponent[] { viewPanel };
	}

	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO create own way to save the PortObject due to simplicity
		// we use ObjectOutputStream to save this object
		out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
		ObjectOutputStream objOut = new ObjectOutputStream(out);
		
		// the number of activities
		objOut.writeInt(dfm.getNumberOfActivities());
		objOut.writeObject(dfm.getAllActivities());

		/* output number of start activities
		// dfm.getStartActivities() in MultiIntSet, and can't be serialized, so here
		// we convert ways to serialize it
		// objOut.writeObject(dfm.getStartActivities());
		*/
		// write start activities
		objOut.writeInt(dfm.getStartActivities().setSize());
		for(int saIdx : dfm.getStartActivities()) {
			objOut.writeInt(saIdx);
			objOut.writeLong(dfm.getStartActivities().getCardinalityOf(saIdx));
			
		}

		// output end activities
		objOut.writeInt(dfm.getEndActivities().setSize());
		for(int eaIdx : dfm.getEndActivities()) {
			objOut.writeInt(eaIdx);
			objOut.writeLong(dfm.getEndActivities().getCardinalityOf(eaIdx));
			
		}
		
		// objOut.writeObject(dfm.getDirectlyFollowsGraph());
		// output edges, if we can access directly edge[][]
		// objOut.writeObject(dfm.getEdges());
		// but due to the provided iterator, way around
		
		// get the directlyfollows graph there
		IntGraph g = dfm.getDirectlyFollowsGraph();
		int num = 0;
		List<Integer> sourceList = new ArrayList();
		List<Integer> targetList = new ArrayList();
		List<Long> cList = new ArrayList();
		
		for (long edgeIdx : g.getEdges()) {
			sourceList.add(g.getEdgeSource(edgeIdx));
			targetList.add(g.getEdgeTarget(edgeIdx));
			cList.add(g.getEdgeWeight(edgeIdx));
			num++;
		}

		objOut.writeInt(num);
		objOut.writeObject(sourceList);
		objOut.writeObject(targetList);
		objOut.writeObject(cList);
		objOut.close();
		/*
		if(dfm instanceof DfgMsd) {
			IntGraph mg = ((DfgMsd) dfm).getMinimumSelfDistanceGraph();
			
			num = 0;
			sourceList.clear();
			targetList.clear();
			cList.clear();

			for (long edgeIdx : g.getEdges()) {
				sourceList.add(g.getEdgeSource(edgeIdx));
				targetList.add(g.getEdgeTarget(edgeIdx));
				cList.add(g.getEdgeWeight(edgeIdx));
				num++;
			}

			objOut.writeInt(num);
			objOut.writeObject(sourceList);
			objOut.writeObject(targetList);
			
		}
		 */
		
	}

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO load dfm from the saved workdflow
		
		ZipEntry nextEntry = in.getNextEntry();
		if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
			throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
		}
		
		ObjectInputStream objIn = new ObjectInputStream(in);

		// they are totally different, so what to do?? we can set such stuff here,
		// but we can't make sure that they discover such model
		
		int numOfActivities = objIn.readInt();
		String[] activities = new String[numOfActivities];
		try {
			activities = (String[]) objIn.readObject();
			dfm = new DfgMsdImpl(activities);
			for (int i = 0; i < numOfActivities; i++) {
				dfm.addActivity(i);
			}

			// read start activities
			int numOfStartActivities = objIn.readInt();
			int idx;
			long cardinality;
			for(int sIdx =0; sIdx <numOfStartActivities; sIdx++ ) {
				idx = objIn.readInt();
				cardinality = objIn.readLong();
				dfm.getStartActivities().add(idx, cardinality);
			}

			int numOfEndActivities = objIn.readInt();
			for(int eIdx =0; eIdx <numOfEndActivities; eIdx++ ) {
				idx = objIn.readInt();
				cardinality = objIn.readLong();
				dfm.getEndActivities().add(idx, cardinality);
			}

			int numOfEdges = objIn.readInt();
			List<Integer> sourceList = (List<Integer>) objIn.readObject();
			List<Integer> targetList = (List<Integer>) objIn.readObject();
			List<Long> cList = (List<Long>) objIn.readObject();
			
			for (int i = 0; i < numOfEdges; i++) {
				dfm.getDirectlyFollowsGraph().addEdge(sourceList.get(i), targetList.get(i), cList.get(i));
			}
			
			// for DfgMsd it is the same, so don't bother about this.

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		objIn.close();
	}

	public static final class DFMPortObjectSerializer extends AbstractPortObjectSerializer<DFMPortObject> {
	}

}
