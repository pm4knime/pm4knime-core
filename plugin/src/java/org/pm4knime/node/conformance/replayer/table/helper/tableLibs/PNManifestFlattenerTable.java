package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.astar.petrinet.manifestreplay.AbstractPNManifestFlattener;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class PNManifestFlattenerTable extends AbstractPNManifestFlattenerTable {
	@SuppressWarnings("unused")
	private PNManifestFlattenerTable() {
	};

	public PNManifestFlattenerTable(PetrinetGraph net, PNManifestReplayerParameterTable parameters) {
		/**
		 * temporary variables
		 */
		Map<Place, Place> mapFlatPlace2OrigPlace = new HashMap<Place, Place>();
		Map<Place, Place> mapOrigPlace2FlatPlace = new HashMap<Place, Place>();

		initialize(net, mapFlatPlace2OrigPlace, mapOrigPlace2FlatPlace, parameters);

		// create dummy event class
		String dummyEvClass = "DUMMY";

		// get mapping to pattern
		TransClass2PatternMapTable mapping = parameters.getMapping();
		this.map = new TransEvClassMappingTable(mapping.getEvClassifier(), dummyEvClass);

		/**
		 * Calculate the length of the longest pattern to determine costFactor.
		 */
		TIntSet patternLengths = new TIntHashSet(1);
		TransClass2PatternMapTable orMapping = parameters.getMapping();
		Collection<Transition> transCol = net.getTransitions();
		int transCounter = 0;
		for (Transition t : transCol) {
			short[] pattern = orMapping.getPatternsOf(t);
			if (pattern != null) {
				// there is a pattern
				int currIdx = 0;
				while (currIdx < pattern.length) {
					patternLengths.add(pattern[currIdx + 1]);
					transCounter += pattern[currIdx + 1];

					// one pattern at a time
					currIdx += 2 + pattern[currIdx + 1];
				}

				if (pattern.length > 3) {
					transCounter++; // there is another transition for move model
				}
			} else {
				transCounter++;
			}
		}
		if (patternLengths.isEmpty()) {
			this.costFactor = 1;
		} else {
			this.costFactor = lcm(patternLengths.toArray());
		}

		// initiate map of result back to original
		flatTrans2OrigTransArr = new int[transCounter];
		trans2PatternID = new int[transCounter];
		Arrays.fill(trans2PatternID, NOPATTERN);

		// flatten transitions and insert them into array for fast lookup
		flatTransArr = new Transition[transCounter];
		flatTrans2Int = new TObjectIntHashMap<Transition>(transCounter);
		nextTrans = new int[transCounter];
		Arrays.fill(nextTrans, -1);

		flatTransFragmentEntry = new boolean[transCounter];
		Arrays.fill(flatTransFragmentEntry, false);

		transCounter = 0;

		for (Transition t : transCol) {
			// check if the transition has patterns, otherwise just give a cost 0
			// remember, pattern = [id of patterns][num of event class][event classes]
			short[] pattern = orMapping.getPatternsOf(t);
			if (pattern == null) {
				createTransByCopy(net, parameters, mapOrigPlace2FlatPlace, dummyEvClass, t, transCounter, NOPATTERN);
				transCounter++;
			} else {
				// there is a pattern, add transitions according to pattern
				// if there is only on transition, just make one transition without specialized move model trans
				if (pattern.length == 3) { // [id of pattern][num of evClass][id of event class]
					createTransByCopy(net, parameters, mapOrigPlace2FlatPlace, orMapping.decodeEvClass(pattern[2]), t,
							transCounter, pattern[0]);
					transCounter++;
				} else {
					// add move on model transitions
					Transition moveModelTrans = fnet.addTransition("M-" + t.getLabel());

					// update encode and decode
					flatTransArr[transCounter] = moveModelTrans;
					this.flatTrans2Int.put(moveModelTrans, transCounter);

					mapTrans2Cost.put(moveModelTrans,
							costFactor * parameters.getMoveModelCost(parameters.getMapping().getTransClassOf(t)));
					flatTrans2OrigTransArr[transCounter] = origTrans2Int.get(t);
					transCounter++;

					// shared input places
					Set<Place> inputInhibitor = new HashSet<Place>(1);
					Set<Place> inputReset = new HashSet<Place>(1);
					Set<Pair<Place, Integer>> inputNormal = new HashSet<Pair<Place, Integer>>(1);

					for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(t)) {
						if (edge instanceof InhibitorArc) {
							inputInhibitor.add(mapOrigPlace2FlatPlace.get(edge.getSource()));
							addInhibitorArc(fnet, mapOrigPlace2FlatPlace.get(edge.getSource()), moveModelTrans);
						} else if (edge instanceof ResetArc) {
							inputReset.add(mapOrigPlace2FlatPlace.get(edge.getSource()));
							addResetArc(fnet, mapOrigPlace2FlatPlace.get(edge.getSource()), moveModelTrans);
						} else {
							Place source = mapOrigPlace2FlatPlace.get(edge.getSource());
							int weight = (net.getArc(edge.getSource(), edge.getTarget()).getWeight());
							inputNormal.add(new Pair<Place, Integer>(source, weight));
							fnet.addArc(source, moveModelTrans, weight);
						}
					}

					// shared output places
					Set<Pair<Place, Integer>> outputNormal = new HashSet<Pair<Place, Integer>>(1);
					for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(t)) {
						Place target = mapOrigPlace2FlatPlace.get(edge.getTarget());
						int weight = net.getArc(edge.getSource(), edge.getTarget()).getWeight();
						outputNormal.add(new Pair<Place, Integer>(target, weight));
						fnet.addArc(moveModelTrans, target, weight);
					}

					// all patterns share the same input and output place
					int currIdx = 0;
					int totalSyncCost = (costFactor * parameters.getMoveSyncCost(parameters.getMapping()
							.getTransClassOf(t)));
					while (currIdx < pattern.length) {
						// connect first transition to shared input places
						Transition currTrans = fnet.addTransition(t.getLabel() + "-" + pattern[currIdx + 2]);

						for (Place p : inputInhibitor) {
							addInhibitorArc(fnet, p, currTrans);
						}
						for (Place p : inputReset) {
							addResetArc(fnet, p, currTrans);
						}
						for (Pair<Place, Integer> pair : inputNormal) {
							fnet.addArc(pair.getFirst(), currTrans, pair.getSecond());
						}

						// map flat transition to original transition
						flatTrans2OrigTransArr[transCounter] = origTrans2Int.get(t);

						// set its cost
						int stdCost = (costFactor * parameters.getMoveModelCost(parameters.getMapping()
								.getTransClassOf(t))) / pattern[currIdx + 1];
						mapTrans2Cost.put(currTrans, stdCost);

						int stdSyncCost = (costFactor * parameters.getMoveSyncCost(parameters.getMapping()
								.getTransClassOf(t))) / (pattern[currIdx + 1]);
						mapSync2Cost.put(currTrans, stdSyncCost);
						totalSyncCost -= stdSyncCost;

						// map it to lowest granularity event class
						map.put(currTrans, orMapping.decodeEvClass(pattern[currIdx + 2]));

						// increment
						flatTransArr[transCounter] = currTrans;
						this.flatTrans2Int.put(currTrans, transCounter);
						this.flatTransFragmentEntry[transCounter] = true;

						// update the mapping from transition to pattern
						this.trans2PatternID[transCounter] = pattern[currIdx]; // only the first transition is mapped to pattern
						transCounter++;

						// create transitions in between
						int upLimit = currIdx + 2 + pattern[currIdx + 1];
						for (int j = currIdx + 3; j < upLimit; j++) {
							// create new transition and connect it to the previous
							Transition nextT = fnet.addTransition(t.getLabel() + "-" + pattern[j]);
							Place inBetween = fnet.addPlace("p-" + t.getLabel() + "-" + pattern[j]);
							fnet.addArc(currTrans, inBetween);
							fnet.addArc(inBetween, nextT);

							// update mapping to activity
							map.put(nextT, orMapping.decodeEvClass(pattern[j]));

							// associate cost properly
							mapTrans2Cost.put(nextT, stdCost);
							if (j == upLimit - 1) {
								mapSync2Cost.put(nextT, totalSyncCost);
							} else {
								mapSync2Cost.put(nextT, stdSyncCost);
								totalSyncCost -= stdSyncCost;
							}

							// update mapping to original transition
							flatTrans2OrigTransArr[transCounter] = origTrans2Int.get(t);

							// connect current to next
							nextTrans[transCounter - 1] = transCounter;
							flatTransArr[transCounter] = nextT;
							this.flatTrans2Int.put(nextT, transCounter);
							transCounter++;

							// continue
							currTrans = nextT;
						}

						// connect last transition from the pattern to shared output places
						for (Pair<Place, Integer> pair : outputNormal) {
							fnet.addArc(currTrans, pair.getFirst(), pair.getSecond());
						}

						// go to next pattern
						currIdx += 2 + pattern[currIdx + 1];
					}
				}
			}
		} // end for looping all transitions

		// copy cost for move on log, multiply by costFactor
		this.mapEvClass2Cost = new HashMap<String, Integer>();
		for (Entry<String, Integer> entry : parameters.getMapEvClass2Cost().entrySet()) {
			this.mapEvClass2Cost.put(entry.getKey(), entry.getValue() * costFactor);
		}
		mapEvClass2Cost.put(dummyEvClass, 0);
	}
}
