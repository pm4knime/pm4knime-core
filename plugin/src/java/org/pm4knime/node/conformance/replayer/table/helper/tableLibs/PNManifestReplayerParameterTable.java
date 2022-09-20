package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.manifestreplayer.EvClassPattern;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;

public class PNManifestReplayerParameterTable extends AbstractPNManifestReplayerParameterTable {
	// mapping between transitions to event classes
		protected TransClass2PatternMapTable mapping;
		protected int maxNumOfStates;


		public PNManifestReplayerParameterTable(){
			maxNumOfStates = 200000;
		}
		
		public PNManifestReplayerParameterTable(Map<TransClass, Integer> mapTransClass2Cost, Map<String, Integer> mapEvClass2Cost,
				TransClass2PatternMapTable mapping, int maxNumOfStates, Marking initMarking, Marking[] finalMarkings) {
			this.transClass2Cost = mapTransClass2Cost;
			this.transClassSync2Cost = new HashMap<TransClass,Integer>(0);
			this.mapEvClass2Cost = mapEvClass2Cost;
			this.mapping = mapping;
			this.maxNumOfStates = maxNumOfStates;
			this.initMarking = initMarking;
			this.finalMarkings = finalMarkings;
		}

		public PNManifestReplayerParameterTable(Map<TransClass, Integer> mapTransClass2Cost, Map<String, Integer> mapEvClass2Cost,
				Map<TransClass, Integer> mapTransClassSync2Cost, TransClass2PatternMapTable mapping, int maxNumOfStates, Marking initMarking, Marking[] finalMarkings) {
			this.transClass2Cost = mapTransClass2Cost;
			this.transClassSync2Cost = mapTransClassSync2Cost;
			this.mapEvClass2Cost = mapEvClass2Cost;
			this.mapping = mapping;
			this.maxNumOfStates = maxNumOfStates;
			this.initMarking = initMarking;
			this.finalMarkings = finalMarkings;
		}
		
		/**
		 * Get all patterns mapped for a transition
		 * 
		 * @param t
		 * @return
		 */
		public Set<EvClassPattern> getAllPatternsFor(Transition t) {
			short[] encodedPatterns = mapping.getPatternsOf(t);
			if (encodedPatterns == null) {
				return null;
			} else {
				int counter = 0;
				int i = 0;
				while (i < encodedPatterns.length) {
					counter++;
					i += 2 + encodedPatterns[i + 1];
				}

				Set<EvClassPattern> res = new HashSet<EvClassPattern>(counter);
				i = 0;
				while (i < encodedPatterns.length) {
					EvClassPattern list = new EvClassPattern(encodedPatterns[i + 1]);
					for (int j = i + 2; j < i + 2 + encodedPatterns[i + 1]; j++) {
						//list.add(mapping.decodeEvClass(encodedPatterns[j]));
					}
					res.add(list);
					i += 2 + encodedPatterns[i + 1];
				}
				return res;
			}
		}

		/**
		 * @param mapping the mapping to set
		 */
		public void setMapping(TransClass2PatternMapTable mapping) {
			this.mapping = mapping;
		}

		/**
		 * @param maxNumOfStates the maxNumOfStates to set
		 */
		public void setMaxNumOfStates(int maxNumOfStates) {
			this.maxNumOfStates = maxNumOfStates;
		}

		public int getMaxNumOfStates() {
			return this.maxNumOfStates;
		}

		public TransClass2PatternMapTable getMapping() {
			return this.mapping;
		}
}
