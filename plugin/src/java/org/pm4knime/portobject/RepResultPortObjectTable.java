package org.pm4knime.portobject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;


import org.knime.core.data.DataTable;

import org.knime.core.data.container.DataContainer;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PNLogReplayResultVisPanelTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.TableEventLog;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.AbstractResetInhibitorNet;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class RepResultPortObjectTable implements PortObject {
	private static final String ZIP_ENTRY_NAME = "RepResultPortObjectTable";
	private static final String ZIP_ENTRY_LOG = "Log";
	private static final String ZIP_ENTRY_NET = "Accepting Petri net";
	private static final String ZIP_ENTRY_REP_RESULT = "Replay Result";
	// alignment result but only for one trace variance
	// SyncReplayResult alignment;
	
	// serialize the whole result here. but by the way, if we want to store the Subset, just make sure 
	// that the element is serializable. It's ok.
	PNRepResult repResult;
	RepResultPortObjectSpecTable m_rSpec ;
	// it points to the related Petrinet object and xlog port object
	// no need to serialize the pnPO, but needs the reference there??? Difficulty here
	// here one thing is, do we need to use the PortObject, or just Log and Petri net?? 
	// with using of PortObject, one thing is the writing. We can not change the methods, to say if we keep the 
	// input and output strean, close, or open. If there is one, then we need to keep it open
	// so change the saved object without Port Object
	// PetriNetPortObject pnPO;
	// XLogPortObject xlogPO;
	TableEventLog log;
	DataTable tableLog;
	AcceptingPetriNet anet;
	
	public RepResultPortObjectTable(PNRepResult repResult,TableEventLog log, DataTable tableLog, AcceptingPetriNet anet) { // PetriNetPortObject pnPO,
		this.repResult = repResult;
		// this.pnPO = pnPO;
		this.log = log;
		this.tableLog = tableLog;
		this.anet = anet;
	}
	
	public RepResultPortObjectTable() {}
	
	public void setRepResult(PNRepResult repResult) {
		this.repResult = repResult;
	}
	
	public PNRepResult getRepResult() {
		return repResult;
	}
	
	public TableEventLog getLog() {
		return log;
	}
	public DataTable getTable() {
		return tableLog;
	}

	public void setLog(DataTable tableLog, String classifier, String traceClassifier) {
		// TODO Auto-generated method stub
		TableEventLog logTEL = null;
		try {
			logTEL = new TableEventLog(tableLog, classifier, traceClassifier);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.log = logTEL;
		this.tableLog = tableLog;
	}
	public AcceptingPetriNet getNet() {
		return anet;
	}
	
	public void setNet(AcceptingPetriNet anet) {
		this.anet = anet;
	}
	
	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PortObjectSpec getSpec() {
		// TODO if it access the null one??
		if(m_rSpec != null)
			return m_rSpec ;
		return new RepResultPortObjectSpecTable();
	}

	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
		// if we need to the log information, how to serialize it?? 
		// reopening this workflow, we need to load event log such that we can show the portobject;
		// but one step is here, not know how to do this..
		PNLogReplayResultVisPanelTable resultPanel = new PNLogReplayResultVisPanelTable(log, repResult, context.getProgress());
		// resutPanel.setBackground(Color.BLACK);
		resultPanel.setName("Alignment Projection");
		return new JComponent[] {resultPanel};
	}
	
	// here we serialise the PortObject by using the prom plugin
	public static class RepResultPortObjectSerializerTable extends PortObjectSerializer<RepResultPortObjectTable> {
	    
		@Override
		public void savePortObject(RepResultPortObjectTable portObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
				throws IOException, CanceledExecutionException {
			// TODO get item of alignment one item for another item to serialze them
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			System.out.println("Enter the save  "+ ZIP_ENTRY_NAME + " in serializer");
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			// save Petri net without PortObject 
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NET));
			AcceptingPetriNet anet = portObject.getNet();
			PetriNetUtil.exportToStream(anet, out);
			out.closeEntry();
			
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_REP_RESULT));
			PNRepResult repResult = portObject.getRepResult();
			
			// should we store the info at first?? Although they are the same in alignment, but we need to do it
			// objOut.writeObject(repResult.getInfo());
			// TODO: exception happens, because of the duplicated save but with different infoMap.
			// the second one loses dummy and eventClassifier already. But why it is like this??
			// if we save them into string, it should be fine
			Map<String, Object> infoMap = repResult.getInfo();
			serializeInfo(infoMap);
			String classifier = portObject.log.getClassifier();
			String traceClassifier = portObject.log.getTraceClassifier();
			objOut.writeUTF(classifier);
			objOut.writeUTF(traceClassifier);
			// how to make sure the object stored in infoMap is serializable?? No secure way!!
			// so we need to remember only the names for the class, after this, we will recover it.
			objOut.writeObject(infoMap);
			// to mark the end of the elements
			objOut.writeInt(repResult.size());
			Map<Transition, Integer> transOrderMap = new HashMap<Transition, Integer>();
			
			// Here begins to store the SyncReplayResult one by one 
			for(SyncReplayResult alignment : repResult ) {
				
				List<Object> nodeInstances = alignment.getNodeInstance();
				// Serialise it here, if we know the type of object
				// after this, we have the XEvent class, then we need to do?? 
				objOut.writeInt(nodeInstances.size());
				for(Object node: nodeInstances) {
					if(node instanceof String) {
						String ecls = (String) node;
						objOut.writeUTF(ecls.getClass().getName());
						objOut.writeUTF(ecls);
						objOut.writeInt(ecls.length());
					}else if(node instanceof Transition) {
						// transition in Petri net, we need to store a lot of object here
						Transition t = (Transition) node;
						// for the silent transition, only save the names are not enough
						// we need to get the local ID of the transition 
						// to create the right map to the net there
						if(transOrderMap.isEmpty()) {
							int tIdx = 0;
							// if we use another ILPReplayer, it provides us a different result, So here
							// we need to make it other way around here
							AbstractResetInhibitorNet gnet =   (AbstractResetInhibitorNet) t.getGraph();
							for(Transition trans : gnet.getTransitions()) {
								transOrderMap.put(trans, tIdx++);
							}
							
						}
						objOut.writeUTF(t.getClass().getName());
//						objOut.writeUTF(t.getLabel());
						// save the order there
						objOut.writeInt(transOrderMap.get(t));
						
					}
				}
				
				List<StepTypes> stepTypes = alignment.getStepTypes();
				objOut.writeObject(stepTypes);
				objOut.writeObject(alignment.getTraceIndex());
				objOut.writeBoolean(alignment.isReliable());
				objOut.writeObject(alignment.getInfo());
			}
			out.closeEntry();
			
				
				// without the help of port Object
				out.putNextEntry(new ZipEntry(ZIP_ENTRY_LOG));
				// create another OutputStream for log
				 BufferedDataContainer.writeToStream(portObject.getTable(), new NonClosableOutputStream(out), exec);
				 out.closeEntry();

			
			out.close();
			System.out.println("Exit the save "+ ZIP_ENTRY_NAME + " in serializer");
		}

		private void serializeInfo(Map<String, Object> infoMap) {
			// TODO Make the infoMap only contained serializable data.
			// IF the value is not serialzable, we use its class name for it.
			for(String key : infoMap.keySet()) {
				Object value = infoMap.get(key);
				if(!(value instanceof Serializable)) {
					infoMap.put(key, value.getClass().getSimpleName());
				}
				
			}
			
		}

		@Override
		public RepResultPortObjectTable loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			// TODO Auto-generated method stub
			// in the same order of writing part
			
			System.out.println("Enter the load "+ ZIP_ENTRY_NAME + " in serializer");
			ZipEntry nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
			}
			// sth with PortObjectSpec here to guide the verification
			ObjectInputStream objIn = new ObjectInputStream(in);
			
			// put the Petri net reading at first to make the same transition same like in the replayer list there
			nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NET)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NET + "' not present");
			}
			// AcceptingPetriNet anet = PetriNetUtil.importFromStream(in);
			AcceptingPetriNet anet = PetriNetUtil.importFromStream(in);
			// after this, we make a list of transitions ans refer to this transition in the reloading part
			Map<Integer, Transition> transOrderMap = new HashMap();
			int tIdx = 0;
			for(Transition t : anet.getNet().getTransitions())
				transOrderMap.put(tIdx++, t);
			
			// firstly to get the entry name for replay result
			nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_REP_RESULT)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_REP_RESULT + "' not present");
			}
			RepResultPortObjectTable repResultPO = new RepResultPortObjectTable();
			Map<String, Object> infoMap = new HashMap();
			String classifier = objIn.readUTF();
			String traceClassifier = objIn.readUTF();
			try {
				infoMap = (Map<String, Object>) objIn.readObject();
				
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			int aSize = objIn.readInt();
			// begin to read alignment one by one, but how to distinguish the reading for each alignment??
			List<SyncReplayResult> col = new ArrayList<SyncReplayResult>();
			
			for(int sIdx = 0; sIdx < aSize; sIdx++) {
			
				List<Object> nodeInstances = new ArrayList();
				int nSize  = objIn.readInt();
				for(int i=0; i < nSize; i++) {
					
						// make sure of the class to read
						String classType = objIn.readUTF();
						if(classType.equals(String.class.getName())) {
							
							String Id = objIn.readUTF();
							int esize = objIn.readInt();
							
							String ecls = Id;
							nodeInstances.add(ecls);
						}else if(classType.equals(Transition.class.getName())) {
//							String label = objIn.readUTF();
							// get transitions from the anet by comparing its label and the name
							int gIdx = objIn.readInt();
							Transition tInRes = transOrderMap.get(gIdx);
							nodeInstances.add(tInRes);
							
						}
						
					}
					
					// read other attributes here
					try {
						List<StepTypes> stepTypes = (List<StepTypes>) objIn.readObject();
						SortedSet<Integer> traceIndex = (SortedSet<Integer>) objIn.readObject();
						boolean isReliable = objIn.readBoolean();
						Map<String, Double> info = (Map<String, Double>) objIn.readObject();
						
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
			
			
			
			// firstly to get the entry name for replay result
			nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_LOG)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_LOG + "' not present");
			}	
			
			
			// this method closes the InputStream, why it can't read the data
			DataTable log = null;
			try {
				log = DataContainer.readFromStream(in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	

			// use this alignment object, we need to reload it here
			repResultPO.setRepResult(new PNRepResultImpl(col));
			repResultPO.setLog(log,classifier, traceClassifier);
			repResultPO.setNet(anet);
			// when they use the Impl, it creates the info by itselves. So we don't need to store it here.
			// but about the other infoMap, it could be not so lucky!! So, we still read the map and store it here
			repResultPO.getRepResult().setInfo(infoMap);
			// this is very important if we want to have some data from spec!!
			repResultPO.setSpec((RepResultPortObjectSpecTable) spec);
			System.out.println("Exit the load "+ ZIP_ENTRY_NAME + " in serializer");
			return repResultPO;
		}

		
		// end of the serializaer
	}

	public void setSpec(RepResultPortObjectSpecTable m_rSpec2) {
		m_rSpec = m_rSpec2;
	}
}
