package org.pm4knime.portobject;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClass;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortObject.PortObjectSerializer;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.petrinet.replayresult.visualization.PNLogReplayResultVisPanel;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
/**
 * this class serializes alignment in the form of SyncReplayResult. It contains DataTable to serve
 * as the port object?? Or we just use the java data?
 * 1. save as DataTable, buut needs access to create DataTable in execution part. 
 *     Haha, in serializer, we have access to ExecutionMonitor. But do we need to store them in such way?? Not really!!
 * 2. object in java form 
 * 3. to consider the whole list of ReplayResult, we need to traverse and serialize them all... How to do this?? 
 *    and one important stuff is that we don't really now the types of the
 * @author kefang-pads
 *
 */
public class RepResultPortObject implements PortObject {

	private static final String ZIP_ENTRY_NAME = "RepResultPortObject";
	// alignment result but only for one trace variance
	// SyncReplayResult alignment;
	
	// serialize the whole result here. but by the way, if we want to store the Subset, just make sure 
	// that the element is serializable. It's ok.
	PNRepResult repResult;
	
	// it points to the related Petrinet object and xlog port object
	// no need to serialize the pnPO, but needs the reference there??? Difficulty here
	// PetriNetPortObject pnPO;
	XLogPortObject xlogPO;
	
	public RepResultPortObject(PNRepResult repResult, XLogPortObject xlogPO) { // PetriNetPortObject pnPO,
		this.repResult = repResult;
		// this.pnPO = pnPO;
		this.xlogPO = xlogPO;
	}
	
	public RepResultPortObject() {}
	
	public void setRepResult(PNRepResult repResult) {
		this.repResult = repResult;
	}
	
	public PNRepResult getRepResult() {
		return repResult;
	}
	
	public XLogPortObject getLogPO() {
		return xlogPO;
	}
	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PortObjectSpec getSpec() {
		// TODO Auto-generated method stub
		return new RepResultPortObjectSpec();
	}

	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
		// if we need to the log information, how to serialize it?? 
		// reopening this workflow, we need to load event log such that we can show the portobject;
		// but one step is here, not know how to do this..
		PNLogReplayResultVisPanel resultPanel = new PNLogReplayResultVisPanel(xlogPO.getLog(), repResult, context.getProgress());
		// resutPanel.setBackground(Color.BLACK);
		resultPanel.setName("Alignment Projection");
		return new JComponent[] {resultPanel};
		
		
	}
	
	// here we serialise the PortObject by using the prom plugin
	public static class RepResultPortObjectSerializer extends PortObjectSerializer<RepResultPortObject> {

		@Override
		public void savePortObject(RepResultPortObject portObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
				throws IOException, CanceledExecutionException {
			// TODO get item of alignment one item for another item to serialze them
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			
			System.out.println("Enter the save PO in serializer");
			
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			PNRepResult repResult = portObject.getRepResult();
			// to mark the end of the elements
			objOut.writeInt(repResult.size());
			// should we store the info at first?? Although they are the same in alignment, but we need to do it
			// objOut.writeObject(repResult.getInfo());
			
			// Here begins to store the SyncReplayResult one by one 
			for(SyncReplayResult alignment : repResult ) {
				
				List<Object> nodeInstances = alignment.getNodeInstance();
				// Serialise it here, if we know the type of object
				// after this, we have the XEvent class, then we need to do?? 
				objOut.writeInt(nodeInstances.size());
				for(Object node: nodeInstances) {
					if(node instanceof XEventClass) {
						XEventClass ecls = (XEventClass) node;
						
						objOut.writeUTF(ecls.getClass().getName());
						objOut.writeUTF(ecls.getId());
						objOut.writeInt(ecls.getIndex());
						objOut.writeInt(ecls.size());
					}else if(node instanceof Transition) {
						// transition in Petri net, we need to store a lot of object here
						Transition t = (Transition) node;
						// we get the attribute of t ?? But how to store them?? we need to relate so much
						// we only store the label, id and related net here
						objOut.writeUTF(t.getClass().getName());
						objOut.writeUTF(t.getLabel());
						objOut.writeObject(t.getId());
						// objOut.writeObject(t.getLocalID());
						// here how to serialize a graph??
						// we can't serializa for each transition like this way, 
						// one object but multiple references, t.getGraph only has the Petri net,
						// do we need to store all of those information here, if not what we can do??
						// we just have the basic information of transitions, but not the net;;
						// if we recover the process, what to do?? We will check it when we use it!!
						// because for event log, we need to save it there, for the transition it should work in this way, too
//						AcceptingPetriNet anet = (AcceptingPetriNet) t.getGraph();
//						// should we follow the same procedure like Petrinet, but it has the convert let me see
//						objOut.writeObject(PetriNetPortObject.convert2String(anet).getBytes());
						
					}
				}
				
				List<StepTypes> stepTypes = alignment.getStepTypes();
				objOut.writeObject(stepTypes);
				objOut.writeObject(alignment.getTraceIndex());
				objOut.writeBoolean(alignment.isReliable());
				objOut.writeObject(alignment.getInfo());
			}
			// can not save the portObject there
			portObject.getLogPO().save(out, exec);
			
			objOut.close();
			
			// out.close();
			System.out.println("Exit the save PO in serializer");
		}

		@Override
		public RepResultPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			// TODO Auto-generated method stub
			// in the same order of writing part
			
			System.out.println("Enter the load PO in serializer");
			ZipEntry nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
			}
			// sth with PortObjectSpec here to guide the verification
			
			RepResultPortObject repResultPO = new RepResultPortObject();
			ObjectInputStream inObj = new ObjectInputStream(in);
			int aSize = inObj.readInt();
			// begin to read alignment one by one, but how to distinguish the reading for each alignment??
			List<SyncReplayResult> col = new ArrayList<SyncReplayResult>();
			
			for(int sIdx = 0; sIdx < aSize; sIdx++) {
			
				List<Object> nodeInstances = new ArrayList();
				int nSize  = inObj.readInt();
				for(int i=0; i < nSize; i++) {
					
						// make sure of the class to read
						String classType = inObj.readUTF();
						if(classType.equals(XEventClass.class.getName())) {
							
							String Id = inObj.readUTF();
							int nIdx = inObj.readInt();
							int esize = inObj.readInt();
							
							XEventClass ecls = new XEventClass(Id, nIdx);
							ecls.setSize(esize);
							nodeInstances.add(ecls);
						}else if(classType.equals(Transition.class.getName())) {
							String label = inObj.readUTF();
							Petrinet net = PetrinetFactory.newPetrinet("Temprorary Petri net for RepResult Loading");
							try {
								NodeID nId  = (NodeID) inObj.readObject();
								// here how to serialize the object is a problem
								// it only reads bytes list, we need to convert the object into a net again
								// !! Be careful about the converting and casting part
								// net = (AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>) PetriNetPortObject.convert2ANet(inObj);
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// but here we need to have the transition with net, so we can store them all, but do we need ??
							Transition t = new Transition(label, (AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>) net);
							nodeInstances.add(t);
						}
						
					}
					
					// read other attributes here
					try {
						List<StepTypes> stepTypes = (List<StepTypes>) inObj.readObject();
						SortedSet<Integer> traceIndex = (SortedSet<Integer>) inObj.readObject();
						boolean isReliable = inObj.readBoolean();
						Map<String, Double> info = (Map<String, Double>) inObj.readObject();
						
						SyncReplayResult result = new SyncReplayResult(nodeInstances, stepTypes, traceIndex.first());
						result.setTraceIndex(traceIndex);
						result.setReliable(isReliable);
						result.setInfo(info);
						
						col.add(result);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			XLogPortObject logPO = new XLogPortObject();
			logPO.load(in, null, exec);
			
			inObj.close();
			
			
			repResultPO.setRepResult(new PNRepResultImpl(col));
			// repResultPO.setLogPO(logPO);
			in.close();
			System.out.println("Exit the load PO in serializer");
			return repResultPO;
		}

		// end of the serializaer
	}

	public void setLogPO(XLogPortObject logPO) {
		// TODO Auto-generated method stub
		xlogPO = logPO;
	}

}
