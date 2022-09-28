package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.manifestreplayresult.ManifestEvClassPattern;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class ManifestFactoryTable {
	public static ManifestEvClassPatternTable construct(PetrinetGraph net, Marking initMarking, Marking[] finalMarkings, TableEventLog log,
			PNManifestFlattenerTable flattener, PNRepResult pnRepResult, TransClass2PatternMap transClass2PatternMap)
			throws Exception {
		// necessary results for Manifest object
		TIntList info = new TIntArrayList();
		int[] casePtr;

		// temporary variable to calculate manifest
		int[] nextTrans = flattener.getNextTrans();
		TObjectIntMap<Transition> flatTrans2Int = flattener.getFlatTrans2Int();
		TDoubleArrayList fitnessStats;
		boolean[] reliabilityStats;
		TIntIntMap index2FitnessStats = new TIntIntHashMap();
		TIntList manifest2PatternID = new TIntArrayList(pnRepResult.size());

		int manifestID = 0;
		int infoPointer = 0; // required to update casePtr
		TIntObjectMap<TIntList> openManifests = new TIntObjectHashMap<TIntList>(nextTrans.length); // keys are id of transition

		// init
//		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
		casePtr = new int[log.getTraces().size()];
		Arrays.fill(casePtr, -1); // pointer from case id to position in the info

		fitnessStats = new TDoubleArrayList(casePtr.length);

		reliabilityStats = new boolean[casePtr.length];
		Arrays.fill(reliabilityStats, false); // assume all case are unreliable

		// iterate through all cases
		for (SyncReplayResult syncRepResult : pnRepResult) {
			for (int idx : syncRepResult.getTraceIndex()) {
				casePtr[idx] = infoPointer;
				reliabilityStats[idx] = syncRepResult.isReliable();
			}

			// store raw fitness information, num of states generated
			Map<String, Double> infoFromSyncRepRes = syncRepResult.getInfo();
			index2FitnessStats.put(infoPointer, fitnessStats.size());

			// add all keys to 
			Double temp = infoFromSyncRepRes.get(PNRepResult.RAWFITNESSCOST);
			fitnessStats.add(temp == null ? Manifest.NOSTATS : temp / flattener.getCostFactor());
			temp = infoFromSyncRepRes.get(PNRepResult.MOVELOGFITNESS);
			fitnessStats.add(temp == null ? Manifest.NOSTATS : temp);
			temp = infoFromSyncRepRes.get(PNRepResult.MOVEMODELFITNESS);
			fitnessStats.add(temp == null ? Manifest.NOSTATS : temp);
			temp = infoFromSyncRepRes.get(PNRepResult.TRACEFITNESS);
			fitnessStats.add(temp == null ? Manifest.NOSTATS : temp);
			temp = infoFromSyncRepRes.get(PNRepResult.NUMSTATEGENERATED);
			fitnessStats.add(temp == null ? Manifest.NOSTATS : temp);
			temp = infoFromSyncRepRes.get(PNRepResult.TIME);
			fitnessStats.add(temp == null ? Manifest.NOSTATS : temp);

			Iterator<StepTypes> itStepTypes = syncRepResult.getStepTypes().iterator();
			Iterator<Object> itNodeInst = syncRepResult.getNodeInstance().iterator();
			int lengthPointer = infoPointer; // later needs to be updated
			info.add(0);
			infoPointer++;

			while (itStepTypes.hasNext()) {
				switch (itStepTypes.next()) {
					case L :
						// just add it to info
						info.add(Manifest.MOVELOG);

						// increase infoPointer
						infoPointer++;

						// point to next node instance
						itNodeInst.next();
						break;
					case LMGOOD :
						int currManifestID;

						// check open node instance
						Transition ot = (Transition) itNodeInst.next();
						int flatTrans = flatTrans2Int.get(ot);

						// check if this is the entry point of a fragment
						if (flattener.isFragmentEntry(flatTrans)) {
							// yes, then give a new manifest ID
							currManifestID = manifestID;

							// map manifest ID to pattern
							manifest2PatternID.add(flattener.getEncOrigTransFor(ot)); // encoded orig transition
							manifest2PatternID.add(flattener.getPatternIDOf(flatTrans)); // encoded pattern id

							// increase new ID
							manifestID++;
						} else {
							currManifestID = openManifests.get(flatTrans).removeAt(0);
						}

						// add the movement
						info.add(Manifest.MOVESYNC);

						// annotate the current event with the manifest ID
						info.add(currManifestID);

						// increase pointer to info
						infoPointer += 2;

						// if there is a continuation, update it
						if (nextTrans[flatTrans] >= 0) {
							TIntList list = openManifests.get(nextTrans[flatTrans]);
							if (list == null) {
								list = new TIntLinkedList();
								openManifests.put(nextTrans[flatTrans], list);
							}
							list.add(currManifestID);
						}
						break;
					case MINVI :
					case MREAL :
						info.add(Manifest.MOVEMODEL);
						info.add(flattener.getOrigEncTransFor(flatTrans2Int.get(itNodeInst.next())));
						infoPointer += 2;
						break;
					default :
						throw new Exception("Invalid step type is identified");
				}
			}
			info.set(lengthPointer, (infoPointer - lengthPointer));
		}

		ManifestEvClassPatternTable newManifest = new ManifestEvClassPatternTable(net, initMarking, finalMarkings, log,
				flattener.getOrigTransArr(), flattener.getOrigTrans2Int(), casePtr, reliabilityStats, info.toArray(),
				transClass2PatternMap, index2FitnessStats, manifest2PatternID.toArray(), fitnessStats.toArray());
		newManifest.setMoveModelCost(flattener.getMapTrans2Cost());
		newManifest.setMoveLogCost(flattener.getMapEvClass2Cost());
		return newManifest;
	}

	public static Manifest construct(Petrinet net, Marking initialMarking, Marking[] array, TableEventLog log,
			PNManifestFlattenerTable flattener, PNRepResult repResult, TransClass2PatternMapTable mapping) {
		// TODO Auto-generated method stub
		return null;
	}

}
