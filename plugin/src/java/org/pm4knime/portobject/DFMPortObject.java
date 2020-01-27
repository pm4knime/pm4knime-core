package org.pm4knime.portobject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClass;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;
import org.processmining.plugins.inductiveVisualMiner.plugins.GraphvizDirectlyFollowsGraph;

public class DFMPortObject extends AbstractPortObject {

	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(DFMPortObject.class);
	private static final String ZIP_ENTRY_NAME = "DFMPortObject";
	private static final String CFG_DFG_START_ACTIVITY_PREFIX = "Start Activity";
	private static final String CFG_DFG_END_ACTIVITY_PREFIX = "End Activity";
	private static final String CFG_DFG_EDGE_PREFIX = "DFG Edge";
	Dfg dfm;

	public DFMPortObject() {
	}

	public DFMPortObject(Dfg dfm) {
		this.dfm = dfm;
	}

	public Dfg getDfm() {
		return dfm;
	}

	public void setDfm(Dfg dfm) {
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
		JComponent viewPanel = GraphvizDirectlyFollowsGraph.visualise(dfm);
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
		
		// write the activities. The number to separate the activities 
		objOut.writeInt(dfm.getNumberOfActivities());
		for(XEventClass ecls : dfm.getActivities()) {
			objOut.writeInt(dfm.getIndexOfActivity(ecls));
			objOut.writeUTF(ecls.getId());
			objOut.writeInt(ecls.getIndex());
			objOut.writeInt(ecls.size());
		}

		// write start activities
		objOut.writeLong(dfm.getStartActivityIndices().length);
		for(int saIdx : dfm.getStartActivityIndices()) {
			objOut.writeInt(saIdx);
			objOut.writeLong(dfm.getStartActivityCardinality(saIdx));
		}

		
		// output end activities
		objOut.writeLong(dfm.getEndActivityIndices().length);
		for(int eaIdx : dfm.getEndActivityIndices()) {
			objOut.writeInt(eaIdx);
			objOut.writeLong(dfm.getEndActivityCardinality(eaIdx));
			
		}

		// get the directlyfollows graph there
		// here it is difficult to get the number of edges due to the format in iterable
		// use the list to accept the values and save them later
		int num = 0;
		List<Long> edgeCardList = new ArrayList();
		List<Integer> sourceIdxList  = new ArrayList();
		List<Integer> targetIdxList  = new ArrayList();
		
		for (long edgeIdx : dfm.getDirectlyFollowsEdges()) {
			
			edgeCardList.add(dfm.getDirectlyFollowsEdgeCardinality(edgeIdx));
			
			sourceIdxList.add(dfm.getDirectlyFollowsEdgeSourceIndex(edgeIdx));
			targetIdxList.add(dfm.getDirectlyFollowsEdgeTargetIndex(edgeIdx));
			num++;
		}
//		objOut.writeObject(dfm.getDirectlyFollowsEdges());
		objOut.writeObject(edgeCardList);
		objOut.writeObject(sourceIdxList);
		objOut.writeObject(targetIdxList);
		
		
		objOut.close();
		
		
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
		
		// read all the activities at first 
		Map<Integer, Integer> aOld2NewIdxMap = new HashMap<Integer, Integer>();
		dfm = new DfgImpl();
		
		try {
			int numberOfActivities = objIn.readInt();
			for(int aIdx =0; aIdx < numberOfActivities; aIdx++ ) {
				int actIdx = objIn.readInt();
				String Id = objIn.readUTF();
				int nIdx = objIn.readInt();
				int esize = objIn.readInt();
				XEventClass ecls = new XEventClass(Id, nIdx);
				ecls.setSize(esize);
				
				// after we add this, we will have a new idx due to the code
				// int index = directlyFollowsGraph.addVertex(activity);
				int newIdx = dfm.addActivity(ecls);
				aOld2NewIdxMap.put(actIdx, newIdx);
				
			}
			
			int idx;
			long cardinality;
			// read start activities
			long numOfStartActivities = objIn.readLong();
			for(int sIdx =0; sIdx <numOfStartActivities; sIdx++ ) {
				idx = objIn.readInt();
				cardinality = objIn.readLong();
				dfm.addStartActivity(aOld2NewIdxMap.get(idx), cardinality);
			}
			
			
			long numOfEndActivities = objIn.readLong();
			for(int eIdx =0; eIdx <numOfEndActivities; eIdx++ ) {
				idx = objIn.readInt();
				cardinality = objIn.readLong();
				dfm.addEndActivity(aOld2NewIdxMap.get(idx), cardinality);
			}
			
			// read edges from graph
//			long[] edgeIndices = (long[]) objIn.readObject();
			List<Long> cList = (List<Long>) objIn.readObject();
			List<Integer> sourceList = (List<Integer>) objIn.readObject();
			List<Integer> targetList = (List<Integer>) objIn.readObject();
			
			
			for (int i=0 ; i< cList.size(); i++) {
				// no need to store the same edge here, I think.. That's the reason we don't use it
				long card = cList.get(i);
				int sOldIdx = sourceList.get(i);
				int tOldIdx = targetList.get(i);
				
				dfm.addDirectlyFollowsEdge(aOld2NewIdxMap.get(sOldIdx), aOld2NewIdxMap.get(tOldIdx), card);
				
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
