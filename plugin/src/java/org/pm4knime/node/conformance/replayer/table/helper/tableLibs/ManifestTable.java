package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//import org.deckfour.xes.classification.XEventClass;
//import org.deckfour.xes.classification.XEventClassifier;
//import org.deckfour.xes.model.XEvent;
//import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;


public abstract class ManifestTable {
	public static short MOVEMODEL = 0;
	public static short MOVELOG = 1;
	public static short MOVESYNC = 2;

	// static value to indicate no stats available
	public static double NOSTATS = -1;

	// pointers to other objects
	protected PetrinetGraph net;
	protected TableEventLog log;
	protected Marking initMarking;
	protected Marking[] finalMarkings;

	// mapping to transitions
	protected TObjectIntMap<Transition> trans2idx; // encoding of the original transition
	protected Transition[] transArr; // decode the encoded original transitions

	// cost function
	protected int[] moveModelCost;
	protected Map<String, Integer> moveLogCost;

	/**
	 * a long array that contains all information about alignment. Information
	 * is stored as: [numOfElements][detailsInf] detailsInf contains the 1. ID
	 * of movement (move model/log/sync) and 2. if move model, transition ID if
	 * move sync, manifest ID if move log, none
	 */
	protected int[] info;

	// given an id of a case, return the index of manifest in info
	protected int[] casePtr;
	protected boolean[] caseReliability;

	/**
	 * map manifest ID to [transition][pattern ID]. Later this pattern can be
	 * translated back to sequence of activity instance through mapping from
	 * transition class to pattern (e.g. class transClass2PatternMap for event
	 * class pattern). Example: pattern ID of manifest ID 3 is
	 * manifest2PatternID[2*3 + 1] and its transition is manifest2PatternID[2*3]
	 */
	protected int[] manifest2PatternID;

	/**
	 * Statistics from replay result. For each index, store
	 * [RAWFITNESSCOST][MOVELOGFITNESS][MOVEMODELFITNESS][TRACEFITNESS]
	 * [NUMSTATEGENERATED][TIME] if there's no stats, the value of each is -1
	 */
	protected double[] fitnessStats;
	/**
	 * map index of info to index of the stats in fitnessStats
	 */
	protected TIntIntMap index2FitnessStats;

	/**
	 * Pointer to value on doubleStats
	 */
	public static final int RAWFITNESSCOST = 0;
	public static final int MOVELOGFITNESS = 1;
	public static final int MOVEMODELFITNESS = 2;
	public static final int TRACEFITNESS = 3;
	public static final int NUMSTATEGENERATED = 4;
	public static final int TIME = 5;

	@SuppressWarnings("unused")
	private ManifestTable() {
	}

	public ManifestTable(PetrinetGraph net, Marking initMarking, Marking[] finalMarkings, TableEventLog log, Transition[] transArr,
			TObjectIntMap<Transition> trans2idx, int[] casePtr, boolean[] caseReliability, int[] info,
			TIntIntMap index2FitnessStats, int[] manifest2PatternID, double[] fitnessStats) {
		this.net = net;
		this.log = log;
		this.transArr = transArr;
		this.trans2idx = trans2idx;
		this.casePtr = casePtr;
		this.caseReliability = caseReliability;
		this.info = info;
		this.index2FitnessStats = index2FitnessStats;
		this.initMarking = initMarking;
		this.finalMarkings = finalMarkings;
		this.fitnessStats = fitnessStats;
		this.manifest2PatternID = manifest2PatternID;
	}

	public void setMoveLogCost(Map<String, Integer> map) {
		this.moveLogCost = map;
	}

	public void setMoveModelCost(Map<Transition, Integer> mm) {
		moveModelCost = new int[transArr.length];
		for (Entry<Transition, Integer> entry : mm.entrySet()) {
			moveModelCost[trans2idx.get(entry.getKey())] = entry.getValue().intValue();
		}
	}
	
	public void setMoveModelCost(int[] moveModelCost) {
		this.moveModelCost = moveModelCost;
	}

	public void printManifestForCase(int caseID) throws IllegalArgumentException {
		StringBuilder sb = new StringBuilder();
		if (caseID < casePtr.length) {
			Iterator<String> it = log.getTraces().get(caseID).iterator();

			int ptr = casePtr[caseID] + 1;
			int upLimit = casePtr[caseID] + info[casePtr[caseID]];
			while (ptr < upLimit) {
				if (info[ptr] == ManifestTable.MOVEMODEL) {
					// move model
					sb.append("M:");
					sb.append(transArr[info[ptr + 1]].getLabel());
					sb.append('\n');
					ptr += 2;
				} else if (info[ptr] == ManifestTable.MOVELOG) {
					// move log
					sb.append("L:");
					sb.append(it.next().toString());
					sb.append('\n');
					ptr++;
				} else {
					// info is positive
					sb.append("L/M:");
					sb.append(transArr[getEncTransOfManifest(info[ptr + 1])].getLabel());
					sb.append(it.next().toString());
					sb.append('\n');

					ptr += 2;
				}
			}
			System.out.println(sb.toString());
		} else {
			throw new IllegalArgumentException("Case ID is out of bound");
		}
	}

	public TObjectIntMap<Transition> getTrans2idx() {
		return trans2idx;
	}

	public Transition[] getTransArr() {
		return transArr;
	}

	public int[] getMoveModelCost() {
		return moveModelCost;
	}

	public Map<String, Integer> getMoveLogCost() {
		return moveLogCost;
	}

	public int[] getInfo() {
		return info;
	}

	public int[] getCasePtr() {
		return casePtr;
	}

	public int[] getManifest2PatternID() {
		return manifest2PatternID;
	}

	public double[] getFitnessStats() {
		return fitnessStats;
	}

	public TIntIntMap getIndex2FitnessStats() {
		return index2FitnessStats;
	}

	public static int getRawfitnesscost() {
		return RAWFITNESSCOST;
	}

	public static int getMovelogfitness() {
		return MOVELOGFITNESS;
	}

	public static int getMovemodelfitness() {
		return MOVEMODELFITNESS;
	}

	public static int getTracefitness() {
		return TRACEFITNESS;
	}

	public static int getNumstategenerated() {
		return NUMSTATEGENERATED;
	}

	public static int getTime() {
		return TIME;
	}

	public PetrinetGraph getNet() {
		return this.net;
	}

	public TableEventLog getLog() {
		return this.log;
	}

	public int[] getCasePointers() {
		return casePtr;
	}

	public boolean[] getCaseReliability() {
		return caseReliability;
	}

	public Boolean isCaseReliable(int caseID) {
		if (caseID < caseReliability.length) {
			return caseReliability[caseID];
		}
		return null;
	}

	/**
	 * 
	 * @param caseID
	 *            case ID
	 * @return NOSTATS if raw cost fitness is not calculated
	 */
	public double getRawCostFitness(int caseID) {
		if (casePtr[caseID] >= 0) {
			return (fitnessStats[index2FitnessStats.get(casePtr[caseID] + RAWFITNESSCOST)]);
		}
		return NOSTATS;
	}

	/**
	 * 
	 * @param caseID
	 *            case ID
	 * @return NOSTATS if move log fitness is not calculated
	 */
	public double getMoveLogFitness(int caseID) {
		if (casePtr[caseID] >= 0) {
			return (fitnessStats[index2FitnessStats.get(casePtr[caseID]) + MOVELOGFITNESS]);
		}
		return NOSTATS;
	}

	/**
	 * 
	 * @param caseID
	 * @return NOSTATS if move model fitness is not calculated
	 */
	public double getMoveModelFitness(int caseID) {
		if (casePtr[caseID] >= 0) {
			return (fitnessStats[index2FitnessStats.get(casePtr[caseID]) + MOVEMODELFITNESS]);
		}
		return NOSTATS;
	}

	/**
	 * 
	 * @param caseID
	 * @return NOSTATS if trace fitness is not calculated
	 */
	public double getTraceFitness(int caseID) {
		if (casePtr[caseID] >= 0) {
			return (fitnessStats[index2FitnessStats.get(casePtr[caseID]) + TRACEFITNESS]);
		}
		return NOSTATS;
	}

	/**
	 * 
	 * @param caseID
	 * @return NOSTATS if number of generated states is not calculated
	 */
	public double getNumStates(int caseID) {
		if (casePtr[caseID] >= 0) {
			return (fitnessStats[index2FitnessStats.get(casePtr[caseID]) + NUMSTATEGENERATED]);
		}
		return NOSTATS;
	}

	/**
	 * 
	 * @param caseID
	 * @return NOSTATS if computation time is not calculated
	 */
	public double getComputationTime(int caseID) {
		if (casePtr[caseID] >= 0) {
			return (fitnessStats[index2FitnessStats.get(casePtr[caseID]) + TIME]);
		}
		return NOSTATS;
	}

	public Marking getInitMarking() {
		return initMarking;
	}

	public Marking[] getFinalMarkings() {
		return finalMarkings;
	}

	public int getPatternIDOfManifest(int manifestID) {
		return this.manifest2PatternID[(2 * manifestID) + 1];
	}

	public int getEncTransOfManifest(int manifestID) {
		return this.manifest2PatternID[2 * manifestID];
	}

	/**
	 * return int array [movement][values]...[movement][values] values depends
	 * on movement
	 * 
	 * @param caseID
	 * @return
	 */
	public int[] getManifestForCase(int caseID) {
		if (caseID < casePtr.length) {
			// initiate
			int[] res = new int[info[casePtr[caseID]] - 1];
			System.arraycopy(info, casePtr[caseID] + 1, res, 0, res.length);
			return res;
		} else {
			throw new IllegalArgumentException("Case ID is out of bound");
		}
	}

	public Transition getTransitionOf(int encTrans) {
		return transArr[encTrans];
	}

	public abstract String getEvClassifier();

	public abstract TransClass getTransClassOf(Transition t);
}
