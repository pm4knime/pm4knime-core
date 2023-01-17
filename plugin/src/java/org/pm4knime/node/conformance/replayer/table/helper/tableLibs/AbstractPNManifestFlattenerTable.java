package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public abstract class AbstractPNManifestFlattenerTable {
	protected ResetInhibitorNet fnet;
	protected Marking initMarking;
	protected Marking[] finalMarkings;
	protected TransEvClassMappingTable map;
	protected Map<Transition, Integer> mapTrans2Cost = new HashMap<Transition, Integer>();
	protected Map<String, Integer> mapEvClass2Cost;
	protected Map<Transition, Integer> mapSync2Cost = new HashMap<Transition, Integer>(0);

	/**
	 * Mapping back to the original process model. If an original transition is
	 * split to several transitions, each transition is mapped to the original
	 * transition.
	 */
	protected Transition[] origTransArr;
	protected TObjectIntMap<Transition> origTrans2Int;

	// move model transition is mapped to original transition
	protected int[] flatTrans2OrigTransArr;

	// scaling of cost based on length of patterns
	protected int costFactor = 1;

	// required to decode the flattened net back to original one
	protected Transition[] flatTransArr;
	protected TObjectIntMap<Transition> flatTrans2Int;

	protected boolean[] flatTransFragmentEntry; // store whether a transition is an entry of a fragment

	// next transition in the flat
	protected int[] nextTrans;

	// map manifestID to the patterns it represents
	public static int NOPATTERN = -1;
	protected int[] trans2PatternID;

	/**
	 * Create new transition by copying the old transition
	 * 
	 * @param net
	 * @param parameters
	 * @param mapOrigPlace2FlatPlace
	 * @param evClass
	 * @param t
	 * @param transSizeCounter
	 * @param patternID
	 */
	protected void createTransByCopy(PetrinetGraph net, PNManifestReplayerParameterTable parameters,
			Map<Place, Place> mapOrigPlace2FlatPlace, String evClass, Transition t, Integer transSizeCounter,
			int patternID) {
		// no pattern, flatten transition as a single transition
		Transition ft = fnet.addTransition(t.getLabel());

		// map flat transition to original transition
		flatTrans2OrigTransArr[transSizeCounter] = origTrans2Int.get(t);

		// set its cost
		mapTrans2Cost.put(ft, costFactor * parameters.getMoveModelCost(parameters.getMapping().getTransClassOf(t)));
		mapSync2Cost.put(ft, costFactor * parameters.getMoveSyncCost(parameters.getMapping().getTransClassOf(t)));

		// map it to dummy event class
		map.put(ft, evClass);

		// input edges
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(t)) {
			if (edge instanceof InhibitorArc) {
				addInhibitorArc(fnet, mapOrigPlace2FlatPlace.get(edge.getSource()), ft);
			} else if (edge instanceof ResetArc) {
				addResetArc(fnet, mapOrigPlace2FlatPlace.get(edge.getSource()), ft);
			} else {
				fnet.addArc(mapOrigPlace2FlatPlace.get(edge.getSource()), ft, net.getArc(edge.getSource(), t)
						.getWeight());
			}
		}

		// output edges
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(t)) {
			fnet.addArc(ft, mapOrigPlace2FlatPlace.get(edge.getTarget()), net
					.getArc(edge.getSource(), edge.getTarget()).getWeight());
		}

		// put the transition in the right index
		this.flatTransArr[transSizeCounter] = ft;
		this.flatTrans2Int.put(ft, transSizeCounter);
		this.flatTransFragmentEntry[transSizeCounter] = true;

		// map flat transition to pattern
		this.trans2PatternID[transSizeCounter] = patternID;

	}

	/**
	 * Add reset arcs
	 * 
	 * @param fnet
	 * @param source
	 * @param target
	 */
	protected void addResetArc(PetrinetGraph fnet, Place source, Transition target) {
		if (fnet instanceof ResetNet) {
			((ResetNet) fnet).addResetArc(source, target);
		} else if (fnet instanceof ResetInhibitorNet) {
			((ResetInhibitorNet) fnet).addResetArc(source, target);
		}
	}

	/**
	 * Add inhibitor arcs
	 * 
	 * @param fnet
	 * @param source
	 * @param target
	 */
	protected void addInhibitorArc(PetrinetGraph fnet, Place source, Transition target) {
		if (fnet instanceof InhibitorNet) {
			((InhibitorNet) fnet).addInhibitorArc(source, target);
		} else if (fnet instanceof ResetInhibitorNet) {
			((ResetInhibitorNet) fnet).addInhibitorArc(source, target);
		}
	}

	/**
	 * @return the net
	 */
	public ResetInhibitorNet getNet() {
		return fnet;
	}

	/**
	 * @return the transArray
	 */
	public Transition[] getFlatTransArr() {
		return flatTransArr;
	}

	/**
	 * @return the transDecode
	 */
	public TObjectIntMap<Transition> getFlatTrans2Int() {
		return flatTrans2Int;
	}

	/**
	 * @return the nextTrans
	 */
	public int[] getNextTrans() {
		return nextTrans;
	}

	/**
	 * @return the initMarking
	 */
	public Marking getInitMarking() {
		return initMarking;
	}

	/**
	 * @return the finalMarkings
	 */
	public Marking[] getFinalMarkings() {
		return finalMarkings;
	}

	/**
	 * @return the map
	 */
	public TransEvClassMappingTable getMap() {
		return map;
	}

	/**
	 * @return the mapTrans2Cost
	 */
	public Map<Transition, Integer> getMapTrans2Cost() {
		return mapTrans2Cost;
	}

	/**
	 * @return the mapEvClass2Cost
	 */
	public Map<String, Integer> getMapEvClass2Cost() {
		return mapEvClass2Cost;
	}
	
	/**
	 * @return the mapSync2Cost
	 */
	public Map<Transition, Integer> getMapSync2Cost() {
		return mapSync2Cost;
	}

	/**
	 * @return the notMoveModelTransitions
	 */
	public Set<Transition> getFragmentTrans() {
		Set<Transition> setTrans = new HashSet<Transition>();
		for (int i = 0; i < flatTransArr.length; i++) {
			if (nextTrans[i] > 0) {
				setTrans.add(flatTransArr[i]);
				setTrans.add(flatTransArr[nextTrans[i]]);
			}
		}
		return setTrans;
	}

	/**
	 * @return the costFactor
	 */
	public int getCostFactor() {
		return costFactor;
	}

	/**
	 * The following code to calculate gcd and lcm is copied from
	 * http://stackoverflow
	 * .com/questions/4201860/how-to-find-gcf-lcm-on-a-set-of-numbers
	 */
	protected int gcd(int a, int b) {
		while (b > 0) {
			int temp = b;
			b = (a % b); // % is remainder
			a = temp;
		}
		return a;
	}

	protected int lcm(int a, int b) {
		return (a * (b / gcd(a, b)));
	}

	protected int lcm(int... input) {
		int result = input[0];
		for (int i = 1; i < input.length; i++)
			result = lcm(result, input[i]);
		return result;
	}

	/**
	 * Return true if the transition is an entry to a fragment
	 * 
	 * @param trans
	 * @return
	 */
	public boolean isFragmentEntry(int trans) {
		return this.flatTransFragmentEntry[trans];
	}

	public Transition getOrigTransFor(Transition flatTrans) {
		return origTransArr[flatTrans2OrigTransArr[flatTrans2Int.get(flatTrans)]];
	}

	public int getOrigEncTransFor(int flatTrans) {
		return flatTrans2OrigTransArr[flatTrans];
	}

	public Transition[] getOrigTransArr() {
		return this.origTransArr;
	}

	public TObjectIntMap<Transition> getOrigTrans2Int() {
		return this.origTrans2Int;
	}

	public int getPatternIDOf(int encFlatTrans) {
		return this.trans2PatternID[encFlatTrans];
	}

	public int getEncOrigTransFor(Transition flatTrans) {
		return flatTrans2OrigTransArr[flatTrans2Int.get(flatTrans)];
	}

	protected void initialize(PetrinetGraph net, Map<Place, Place> mapFlatPlace2OrigPlace,
			Map<Place, Place> mapOrigPlace2FlatPlace, AbstractPNManifestReplayerParameterTable parameters) {
		// create new net from the given net
		fnet = PetrinetFactory.newResetInhibitorNet("Flat " + net.getLabel());
		
//		if (net instanceof InhibitorNet) {
//			fnet = PetrinetFactory.newInhibitorNet("Flat " + net.getLabel());
//		} else if (net instanceof ResetInhibitorNet) {
//			fnet = PetrinetFactory.newResetInhibitorNet("Flat " + net.getLabel());
//		} else if (net instanceof ResetNet) {
//			fnet = PetrinetFactory.newResetNet("Flat " + net.getLabel());
//		} else {
//			fnet = PetrinetFactory.newPetrinet("Flat " + net.getLabel());
//		}

		// initiate original net encoding
		Collection<Transition> colTrans = net.getTransitions();
		origTransArr = colTrans.toArray(new Transition[colTrans.size()]);
		origTrans2Int = new TObjectIntHashMap<Transition>(origTransArr.length);
		for (int i = 0; i < origTransArr.length; i++) {
			origTrans2Int.put(origTransArr[i], i);
		}

		// add flattened places 
		for (Place p : net.getPlaces()) {
			Place fp = fnet.addPlace(p.getLabel());
			mapFlatPlace2OrigPlace.put(fp, p);
			mapOrigPlace2FlatPlace.put(p, fp);
		}

		// copy markings (initial and final)
		initMarking = new Marking();
		for (Place origPlace : parameters.getInitMarking().baseSet()) {
			initMarking.add(mapOrigPlace2FlatPlace.get(origPlace), parameters.getInitMarking().occurrences(origPlace));
		}

		Marking[] origFinalMarkings = parameters.getFinalMarkings();
		this.finalMarkings = new Marking[origFinalMarkings.length];
		for (int i = 0; i < origFinalMarkings.length; i++) {
			Marking newMarking = new Marking();
			for (Place origPlace : origFinalMarkings[i].baseSet()) {
				newMarking.add(mapOrigPlace2FlatPlace.get(origPlace), origFinalMarkings[i].occurrences(origPlace));
			}
			this.finalMarkings[i] = newMarking;
		}

	}

}
