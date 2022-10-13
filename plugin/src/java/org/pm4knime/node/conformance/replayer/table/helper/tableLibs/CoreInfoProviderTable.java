package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.pnalignanalysis.visualization.projection.CoreInfoProvider;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import gnu.trove.list.array.TIntArrayList;

public class CoreInfoProviderTable {
	
	private Transition[] transArray;
	private Place[] placeArray;
	private String[] ecArray;
	private TIntArrayList encodedStats = null;
	private int transStatBlockSize; // size of elements of Freq. of transition related 
	private int markingStatBlockSize; // size of elements of Freq. of marking related

	private int sumAllMoveLogModel = 0;
	private int sumAllMoveModelOnly = 0;
	private int sumAllMoveLogOnly = 0;
	private int totalTrace = 0;
	private int unreliableAlignments = 0;
	private Map<String, Integer> mapEc2Int;
	private Map<Transition, Integer> mapTrans2Int;
	private Map<Place, Integer> mapPlace2Int;

	private final PetrinetGraph net;
	private final Marking marking;
	private final Set<? extends SyncReplayResult> logReplayResult;
	private final TableEventLog log;
	private final TransEvClassMappingTable mapping;


	public CoreInfoProviderTable(final PetrinetGraph net, Marking marking, TransEvClassMappingTable mapping, TableEventLog log,
			Set<? extends SyncReplayResult> logReplayResult) {
		this.net = net;
		this.marking = marking;
		this.mapping = mapping;
		this.log = log;
		this.logReplayResult = logReplayResult;
		initialize();
	}

	protected void initialize() {
		boolean[] filter = new boolean[logReplayResult.size()];
		Arrays.fill(filter, true);

		initializeEvClassArray(log, mapping);
		initializeTransAndPlaceArray(net);
		transStatBlockSize = 5;
		markingStatBlockSize = placeArray.length + (ecArray.length * 2);

		// initialize necessary things
		mapEc2Int = new HashMap<String, Integer>(ecArray.length);
		for (int i = 0; i < ecArray.length; i++) {
			mapEc2Int.put(ecArray[i], i);
		}

		mapTrans2Int = new HashMap<Transition, Integer>(transArray.length);
		for (int i = 0; i < transArray.length; i++) {
			mapTrans2Int.put(transArray[i], i);
		}

		mapPlace2Int = new HashMap<Place, Integer>(placeArray.length);
		for (int i = 0; i < placeArray.length; i++) {
			mapPlace2Int.put(placeArray[i], i);
		}

		// initialize all encoded result
		// initial capacity allows that each place is a marking
		encodedStats = new TIntArrayList((transStatBlockSize * transArray.length)
				+ (placeArray.length * markingStatBlockSize));
		encodedStats
				.add(new int[(transStatBlockSize * transArray.length) + (placeArray.length * markingStatBlockSize)]);

		extractInfo(filter, null);
	}

	private void initializeTransAndPlaceArray(PetrinetGraph newNet) {
		// transition array
		List<Transition> transList = new ArrayList<Transition>(newNet.getTransitions());
		Collections.sort(transList, new Comparator<Transition>() {

			public int compare(Transition o1, Transition o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});
		transArray = transList.toArray(new Transition[transList.size()]);

		// place array
		List<Place> placeList = new ArrayList<Place>(newNet.getPlaces());
		Collections.sort(placeList, new Comparator<Place>() {

			public int compare(Place o1, Place o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});

		placeArray = placeList.toArray(new Place[placeList.size()]);
	}

	private void initializeEvClassArray(TableEventLog log, TransEvClassMappingTable mapping) {
		//XLogInfo summary = XLogInfoFactory.createLogInfo(log, mapping.getEventClassifier());
		List<String> listClasses = Arrays.asList(log.getActivties());
		Collections.sort(listClasses);

		if (mapping.getDummyEventClass() != null) {
			ecArray = new String[listClasses.size() + 1];
			int ecCounter = 0;
			for (String ec : listClasses) {
				ecArray[ecCounter++] = ec;
			}
			ecArray[ecArray.length - 1] = mapping.getDummyEventClass();
		} else {
			ecArray = listClasses.toArray(new String[listClasses.size()]);
		}
	}

	/**
	 * @return the numRealTrans
	 */
	public int getNumTrans() {
		return transArray.length;
	}

	/**
	 * @param newNet
	 * @param mNewNet
	 * @param mapOrig2ViewNode
	 * @param repResult
	 */
	public void extractInfo(boolean[] filter, Set<Integer> preservedIndexes) {

		// reset all
		sumAllMoveLogModel = 0;
		sumAllMoveModelOnly = 0;
		sumAllMoveLogOnly = 0;
		totalTrace = 0;
		unreliableAlignments = 0;
		encodedStats.fill(0);

		int index = 0;
		for (SyncReplayResult syncRepRes : logReplayResult) {
			// check if this is filtered out
			if (filter != null) {
				if (!filter[index++]) {
					continue;
				}
			}
			// check if this result is reliable
			if (syncRepRes.isReliable()) {
				// filter out traces/keep reliable traces
				Set<Integer> traceIndexes = new HashSet<Integer>(syncRepRes.getTraceIndex());
				if (preservedIndexes != null) {
					traceIndexes.retainAll(preservedIndexes);
				}

				int traceSize = traceIndexes.size();
				if (traceSize == 0) {
					continue;
				}
				extractInfoFromReliableNonEmptyTrace(syncRepRes, traceSize);
			} // end of reliable sequence
			else
			{
				unreliableAlignments++;
			}
		}
	}

	protected void extractInfoFromReliableNonEmptyTrace(SyncReplayResult syncRepRes, int traceSize) {
		// iterate through an alignment
		List<Object> nodeInstances = syncRepRes.getNodeInstance();
		List<StepTypes> stepTypes = syncRepRes.getStepTypes();

		// information accumulator
		boolean isPerfectFitTrace = true;

		// index sequence frequency for this sync rep res: move on model, sync move, ev log move
		int numCurrMarking = (encodedStats.size() - (transStatBlockSize * transArray.length)) / markingStatBlockSize;

		// capacity for storing freq move on model, move on log, and marking index+deviating event class 
		int preCapacity = (2 * transArray.length) + (numCurrMarking * ecArray.length);
		TIntArrayList freqAccTrace = new TIntArrayList(preCapacity);
		freqAccTrace.add(new int[preCapacity]);

		// keep on track on the marking for constructing event class transitions
		int[] m = getEncodedMarking(marking);
		forLoop: for (int index = 0; index < stepTypes.size(); index++) {
			StepTypes type = stepTypes.get(index);
			isPerfectFitTrace &= isFitting(syncRepRes, index);
			switch (type) {
				case L :
					// find index of the current marking
					int mSeqNumber = getMarkingEncodedIndex(m);
					if (mSeqNumber < 0) {
						// add new marking index in the global stat
						mSeqNumber = addNewMarking(m);
					}
					Object objec = nodeInstances.get(index);
					if (objec instanceof String) {
						addMoveOnLogDev(mSeqNumber, mapEc2Int.get(objec), freqAccTrace, traceSize);
					} else {
						String temp = objec.toString();
						for (String key : mapEc2Int.keySet()) {
							if (temp.equals(key)) {
								addMoveOnLogDev(mSeqNumber, mapEc2Int.get(key), freqAccTrace, traceSize);
								break;
							}
						}
					}

					sumAllMoveLogOnly += traceSize;
					break;
				case LMGOOD :
					Transition trans = (Transition) nodeInstances.get(index);

					// find the event class transition index and increment it
					int indexTrans = mapTrans2Int.get(trans);
					freqAccTrace.set(indexTrans, (freqAccTrace.get(indexTrans) + traceSize));

					sumAllMoveLogModel += traceSize;
					m = fireTransition(trans, net, m, mapPlace2Int);
					break;
				case MINVI :
				case MREAL :
					// invi is the same as real
					Transition transI = (Transition) nodeInstances.get(index);

					// find the event class transition index and increment it
					int indexI = mapTrans2Int.get(transI) + transArray.length;
					freqAccTrace.set(indexI, (freqAccTrace.get(indexI) + traceSize));

					sumAllMoveModelOnly += traceSize;

					m = fireTransition(transI, net, m, mapPlace2Int);
					break;
				case LMNOGOOD :
				case LMREPLACED :
				case LMSWAPPED :
					// unable to project this case, stop
					break forLoop;
			}
		}

		// add num of traces
		this.totalTrace += traceSize;

		// NOTE: there can be premature stop if lmnogood is executed

		// update sync and move on model
		for (int i = 0; i < transArray.length; i++) {
			int idxGlobal = transStatBlockSize * i;
			if (freqAccTrace.get(i + transArray.length) > 0) { // move on model > 0
				// total frequency move on model
				encodedStats.set(idxGlobal, (freqAccTrace.get(i + transArray.length) + encodedStats.get(idxGlobal)));

				// total unique trace where it happens
				encodedStats.set(idxGlobal + 1, (traceSize + encodedStats.get(idxGlobal + 1)));
			}

			if (freqAccTrace.get(i) > 0) { // move sync > 0
				// total frequency move sync
				encodedStats.set(idxGlobal + 2, (freqAccTrace.get(i) + encodedStats.get(idxGlobal + 2)));

				// total unique trace where it happens
				encodedStats.set(idxGlobal + 3, (traceSize + encodedStats.get(idxGlobal + 3)));

				if (isPerfectFitTrace) {
					// total unique trace where it happens
					encodedStats.set(idxGlobal + 4, (freqAccTrace.get(i) + encodedStats.get(idxGlobal + 4)));
				}
			}
		}

		// update move on log
		int tracePointerIdx = 2 * transArray.length;
		int globalPointerIdx = transStatBlockSize * transArray.length;
		while (tracePointerIdx < freqAccTrace.size()) {
			for (int i = 0; i < ecArray.length; i++) {
				if (freqAccTrace.get(tracePointerIdx + i) > 0) {
					int updatedIndex = globalPointerIdx + placeArray.length + i;
					encodedStats.set(updatedIndex,
							(encodedStats.get(updatedIndex) + freqAccTrace.get(tracePointerIdx + i)));
					encodedStats.set(updatedIndex + ecArray.length,
							(encodedStats.get(updatedIndex + ecArray.length) + traceSize));
				}
			}
			globalPointerIdx += markingStatBlockSize;
			tracePointerIdx += ecArray.length;
		}

	}

	protected boolean isFitting(SyncReplayResult syncRepRes, int index) {
		switch (syncRepRes.getStepTypes().get(index)) {
			case LMGOOD :
			case MINVI :
				return true;
			default :
				return false;
		}
	}

	private void addMoveOnLogDev(int mSeqNumber, int evClassIdx, TIntArrayList freqAccTrace, int traceSize) {
		int pointerTrace = (transArray.length * 2) + (mSeqNumber * ecArray.length);
		if (pointerTrace >= freqAccTrace.size()) {
			freqAccTrace.fill(pointerTrace, pointerTrace + ecArray.length, 0);
			freqAccTrace.set(pointerTrace + evClassIdx, traceSize);
		} else {
			// add the existing
			freqAccTrace.set(pointerTrace + evClassIdx, (freqAccTrace.get(pointerTrace + evClassIdx) + traceSize));
		}
	}

	/**
	 * add a new marking in the encoded stats return the index of the marking
	 * (the sequence index of all stored marking)
	 */
	private int addNewMarking(int[] m) {
		// get last marking index
		int lastMarkingIndex = (encodedStats.size() - (transStatBlockSize * transArray.length)) / markingStatBlockSize;
		for (int i = 0; i < m.length; i++) {
			encodedStats.add(m[i]);
		}
		encodedStats.fill(encodedStats.size(), encodedStats.size() + (2 * ecArray.length), 0);
		return lastMarkingIndex;
	}

	/**
	 * get marking sequence number in the main array. If not found, return
	 * negative value
	 * 
	 * @param m
	 * @return
	 */
	private int getMarkingEncodedIndex(int[] m) {
		int pointer = (transStatBlockSize * transArray.length);
		int limit = pointer + placeArray.length;
		while (pointer < encodedStats.size()) {
			whileLoop: while (pointer < limit) {
				// check if the marking is the same
				for (int i = 0; i < placeArray.length; i++) {
					if (m[i] != encodedStats.get(i + pointer)) {
						break whileLoop;
					}
				}
				// marking is found
				return ((pointer - (transStatBlockSize * transArray.length)) / markingStatBlockSize);
			}
			// not correct marking
			pointer += markingStatBlockSize;
			limit = pointer + placeArray.length;
		}
		return -1; // marking is not found
	}

	/**
	 * return marking in encoded form
	 * 
	 * @param mNewNet
	 * @return
	 */
	private int[] getEncodedMarking(Marking mNewNet) {
		int[] res = new int[placeArray.length];
		for (int i = 0; i < placeArray.length; i++) {
			res[i] = mNewNet.occurrences(placeArray[i]).shortValue();
		}
		return res;
	}

	/**
	 * Assuming Transition is fireable, no need to check anymore
	 * 
	 * @param trans
	 * @param newNet
	 * @param m
	 * @param mapPlace2Int
	 */
	private int[] fireTransition(Transition trans, PetrinetGraph newNet, int[] m, Map<Place, Integer> mapPlace2Int) {
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = newNet.getInEdges(trans);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
			if (edge instanceof ResetArc) {
				m[mapPlace2Int.get(edge.getSource())] = 0;
			} else if (edge instanceof Arc) {
				// ordinary edges
				int removedTokens = newNet.getArc(edge.getSource(), edge.getTarget()).getWeight();
				if (m[mapPlace2Int.get(edge.getSource())] < removedTokens) {
					m[mapPlace2Int.get(edge.getSource())] = 0;
				} else {
					m[mapPlace2Int.get(edge.getSource())] -= removedTokens;
				}
			}
		}

		edges = newNet.getOutEdges(trans);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
			if (edge instanceof Arc || edge instanceof ResetArc || edge instanceof InhibitorArc) {
				m[mapPlace2Int.get(edge.getTarget())] += newNet.getArc(edge.getSource(), edge.getTarget()).getWeight();
			}
		}
		return m;
	}

	public int[] getInfoNode(int selectedIndex) {
		int[] info = null;
		if (selectedIndex < transArray.length) {
			// refer to the long array for exact statistics on transitions
			int pointer = selectedIndex * transStatBlockSize;
			info = new int[5];
			info[0] = encodedStats.get(pointer + 2); // move sync
			info[1] = encodedStats.get(pointer + 4); // sync in 100% fit cases
			info[2] = encodedStats.get(pointer + 3); // unique case sync
			info[3] = encodedStats.get(pointer); // move on model
			info[4] = encodedStats.get(pointer + 1); // unique case move on model

		} else if (selectedIndex < (transArray.length + placeArray.length)) {
			// return marking index that contains this particular place
			Set<Integer> mIndexWPlaces = new HashSet<Integer>();
			int pointer = (transArray.length * transStatBlockSize) + (selectedIndex - transArray.length);
			int markID = 0;
			while (pointer < encodedStats.size()) {
				if (encodedStats.get(pointer) > 0) {
					mIndexWPlaces.add(markID);
				}
				pointer += markingStatBlockSize;
				markID++;
			}
			info = new int[mIndexWPlaces.size()];
			int i = 0;
			for (Integer index : mIndexWPlaces) {
				info[i] = index;
				i++;
			}
		}
		return info;
	}

	/**
	 * Get marking info: [marking][freq event class][freq unique trace event
	 * class]
	 * 
	 * @param markingIndex
	 * @return
	 */
	public int[] getInfoMarking(int markingIndex) {
		int startMarkingIndex = (transArray.length * transStatBlockSize) + (markingIndex * markingStatBlockSize);
		if (startMarkingIndex > encodedStats.size()) {
			return null;
		} else {
			return encodedStats.toArray(startMarkingIndex, placeArray.length + (2 * ecArray.length));
		}
	}

	public int[] getAllStats() {
		return new int[] { sumAllMoveLogModel, sumAllMoveModelOnly, sumAllMoveLogOnly, totalTrace, unreliableAlignments };
	}

	public Transition[] getTransArray() {
		return this.transArray;
	}

	public Map<Transition, Integer> getTrans2Int() {
		return this.mapTrans2Int;
	}

	/**
	 * Return negative if it does not exist
	 * 
	 * @param t
	 * @return
	 */
	public int getIndexOf(Transition t) {
		int i = 0;
		while ((!t.equals(transArray[i])) && (i < transArray.length)) {
			i++;
		}
		return i < transArray.length ? i : -1;
	}

	/**
	 * Return minimum and maximum values of frequency
	 * 
	 * @param isShowMoveLogModel
	 * @param isShowMoveModelOnly
	 * @return
	 */
	public int[] getMinMaxFreq(boolean isShowMoveLogModel, boolean isShowMoveModelOnly) {
		if (!isShowMoveLogModel && !isShowMoveModelOnly) {
			return new int[] { 0, 0 };
		} else {
			int counter = 0;
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;

			int logIndex = transStatBlockSize * transArray.length;

			while (counter < logIndex) {
				if ((isShowMoveModelOnly) && (!isShowMoveLogModel)) {
					if (counter == 0) {
						min = encodedStats.get(counter);
						max = min;
					} else {
						if (encodedStats.get(counter) < min) {
							min = encodedStats.get(counter);
						}
						if (encodedStats.get(counter) > max) {
							max = encodedStats.get(counter);
						}
					}
				} else if ((isShowMoveLogModel) && (!isShowMoveModelOnly)) {
					if (counter == 0) {
						min = encodedStats.get(counter + 2);
						max = min;
					} else {
						if (encodedStats.get(counter + 2) < min) {
							min = encodedStats.get(counter + 2);
						}

						if (encodedStats.get(counter + 2) > max) {
							max = encodedStats.get(counter + 2);
						}
					}
				} else {
					assert (isShowMoveLogModel && isShowMoveModelOnly);
					if (counter == 0) {
						min = encodedStats.get(counter) + encodedStats.get(counter + 2);
						max = min;
					} else {
						// show move on log and show move on model
						if (encodedStats.get(counter) + encodedStats.get(counter + 2) < min) {
							min = encodedStats.get(counter) + encodedStats.get(counter + 2);
						}

						if (encodedStats.get(counter) + encodedStats.get(counter + 2) > max) {
							max = encodedStats.get(counter) + encodedStats.get(counter + 2);
						}
					}
				}
				counter += transStatBlockSize;
			}

			return new int[] { min, max };
		}
	}

	public int getNumPlaces() {
		return placeArray.length;
	}

	public Place[] getPlaceArray() {
		return placeArray;
	}

	public Map<Place, Integer> getPlace2Int() {
		return this.mapPlace2Int;
	}

	public String[] getEvClassArray() {
		return ecArray;
	}

	public Map<String, Integer> getEC2Int() {
		return this.mapEc2Int;
	}

	/**
	 * get frequency occurrence of places from markings
	 * 
	 * @return
	 */
	public int[] getPlaceFreq() {
		int[] res = new int[placeArray.length];
		Arrays.fill(res, 0);

		// iterate all markings
		int pointerMarking = transStatBlockSize * transArray.length;
		while (pointerMarking < encodedStats.size()) {
			for (int i = 0; i < placeArray.length; i++) {
				// if the marking contains this place
				if (encodedStats.get(pointerMarking + i) > 0) {
					// sum up all move on log that occurred before
					int limit = pointerMarking + placeArray.length + ecArray.length;
					for (int j = pointerMarking + placeArray.length; j < limit; j++) {
						res[i] += encodedStats.get(j);
					}
				}
			}
			pointerMarking += markingStatBlockSize;
		}

		return res;
	}

	public int getPlaceIndexOf(Place p) {
		int i = 0;
		while ((!p.equals(placeArray[i])) && (i < placeArray.length)) {
			i++;
		}
		return i < placeArray.length ? i : -1;
	}


}
