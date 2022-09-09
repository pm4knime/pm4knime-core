package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.manifestreplayer.EvClassPattern;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClasses;

import gnu.trove.list.array.TShortArrayList;

public class TransClass2PatternMapTable {
	/**
	 * index of the event class indicate its encoded form (in short) all event
	 * class should be listed here
	 */
	private String[] evClassEnc;
	private Map<String, Short> evClass2Enc;

	/**
	 * index of transition class indicate its encoded form (in short).
	 */
	private TransClass[] transClassEnc;
	private Map<TransClass, Short> transClass2Enc;

	/**
	 * classifier with the lowest level of granularity
	 */
	private String evClassifier;

	/**
	 * classifier of transitions
	 */
	private TransClasses transClasses;

	/**
	 * [index of the first pattern of a transition (-1 if no
	 * pattern)]{[patternID][num of event class][id of event classes]} encoded
	 * patterns for each transition
	 */
	private short[] patterns;

	/**
	 * fast lookup to the pattern, given a pattern ID e.g. number of event class
	 * of a pattern with ID 3 is patterns[patternID2Idx[3]]
	 */
	private short[] patternID2Idx; // index of pattern 

	public TransClass2PatternMapTable() {
	}
	
	/**
	 * Default constructor, require trans classifier
	 * 
	 * @param log
	 * @param net
	 * @param classifier
	 * @param mapping
	 */
	public TransClass2PatternMapTable(TableEventLog log, PetrinetGraph net,  TransClasses transClasses,
			Map<TransClass, Set<EvClassPattern>> mapping) {
		/* Todo sub evclassPattern*/
		init(log, net, transClasses, mapping);
	}

	public Map<String, Short> getEvClass2Enc() {
		return evClass2Enc;
	}

	public void setEvClass2Enc(Map<String, Short> evClass2Enc) {
		this.evClass2Enc = evClass2Enc;
	}

	public Map<TransClass, Short> getTransClass2Enc() {
		return transClass2Enc;
	}

	public void setTransClass2Enc(Map<TransClass, Short> transClass2Enc) {
		this.transClass2Enc = transClass2Enc;
	}

	public TransClasses getTransClasses() {
		return transClasses;
	}

	public void setTransClasses(TransClasses transClasses) {
		this.transClasses = transClasses;
	}

	public short[] getPatterns() {
		return patterns;
	}

	public void setPatterns(short[] patterns) {
		this.patterns = patterns;
	}

	public short[] getPatternID2Idx() {
		return patternID2Idx;
	}

	public void setPatternID2Idx(short[] patternID2Idx) {
		this.patternID2Idx = patternID2Idx;
	}

	/**
	 * Main method to initialize the mapping
	 * 
	 * @param log
	 * @param net
	 * @param classifier
	 * @param transClassifier
	 * @param mapping
	 */
	public void init(TableEventLog log, PetrinetGraph net,  TransClasses transClasses,
			Map<TransClass, Set<EvClassPattern>> mapping) {
		this.evClassifier = log.getClassifier();
		this.transClasses = transClasses;

		// get all event classes in the log
		List<String> evClassCol = Arrays.asList(log.getActivties());
		Collections.sort(evClassCol, new Comparator<String>(){
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}

		});
		evClassEnc = evClassCol.toArray(new String[evClassCol.size()]);
		evClass2Enc = new HashMap<String, Short>(evClassEnc.length);
		for (short i = 0; i < evClassEnc.length; i++) {
			evClass2Enc.put(evClassEnc[i], i);
		}

		// get all transition classes
		List<TransClass> transClassCol = new ArrayList<TransClass>(transClasses.getTransClasses());
		Collections.sort(transClassCol, new Comparator<TransClass>(){
			public int compare(TransClass o1, TransClass o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		transClassEnc = transClassCol.toArray(new TransClass[transClassCol.size()]);
		transClass2Enc = new HashMap<TransClass, Short>(transClassEnc.length);
		for (short i = 0; i < transClassEnc.length; i++) {
			transClass2Enc.put(transClassEnc[i], i);
		}

		// create the encoded patterns 
		// transition with -1 patterns indicate that there is no pattern for the transition
		TShortArrayList patternsTemp = new TShortArrayList(transClassEnc.length * 2);
		TShortArrayList patternID2IdxTemp = new TShortArrayList();
		short patternIDcounter = 0;

		TShortArrayList tempTail = new TShortArrayList(transClassEnc.length * 4);
		for (int i = 0; i < transClassEnc.length; i++) {
			Set<EvClassPattern> setListEvClass = mapping.get(transClassEnc[i]);
			if (setListEvClass != null) {
				// index of the beginning of a transition should be < Integer.MAX_VALUE
				patternsTemp.add((short) (transClassEnc.length + tempTail.size()));

				for (List<XEventClass> lst : setListEvClass) {
					short patternSize = (short) lst.size();
					patternID2IdxTemp.add((short) (tempTail.size() + transClassEnc.length + 1));
					tempTail.add(patternIDcounter++);
					tempTail.add(patternSize);
					for (XEventClass ec : lst) {
						tempTail.add(evClass2Enc.get(ec).shortValue());
					}
				}
			} else {
				// no pattern for the transition
				patternsTemp.add((short) -1);
			}
		}
		patterns = new short[patternsTemp.size() + tempTail.size()];
		System.arraycopy(patternsTemp.toArray(), 0, patterns, 0, patternsTemp.size());
		System.arraycopy(tempTail.toArray(), 0, patterns, patternsTemp.size(), tempTail.size());

		patternID2Idx = patternID2IdxTemp.toArray();
	}

	public void setEvClassEnc(String[] evClassEnc) {
		this.evClassEnc = evClassEnc;
	}

	public void setTransClassEnc(TransClass[] transClassEnc) {
		this.transClassEnc = transClassEnc;
	}

	public void setEvClassifier(String evClassifier) {
		this.evClassifier = evClassifier;
	}

	/**
	 * Get transition class of a transition
	 * 
	 * @param t
	 * @return
	 */
	public TransClass getTransClassOf(Transition t) {
		return this.transClasses.getClassOf(t);
	}

	/**
	 * get array of ints, indicating patterns for transition t.
	 * Each pattern is an array with the following structure
	 * [id of patterns][num of event class][event classes]
	 * 
	 * @param t
	 * @return
	 */
	public short[] getPatternsOf(Transition t) {
		// find all pattern of transition t
		short transClassEncoded = transClass2Enc.get(getTransClassOf(t));
		if (patterns[transClassEncoded] > 0) {
			int limitIndex;
			if ((transClassEncoded + 1) == transClassEnc.length) {
				// last transition encoded
				limitIndex = patterns.length;
			} else {
				// there is still another transition encoded next
				int i = transClassEncoded + 1;
				while ((patterns[i] < 0) && (i < transClassEnc.length)) {
					i++;
				}
				if (i == transClassEnc.length) { // limit index is all patterns
					limitIndex = patterns.length;
				} else {
					limitIndex = patterns[i]; // limited by next pattern
				}
			}

			// construct an array of index
			short[] res = new short[limitIndex - patterns[transClassEncoded]];
			System.arraycopy(patterns, patterns[transClassEncoded], res, 0, res.length);			
			return res;
		}
		return null;
	}

	/**
	 * Get encoding of an event class
	 * 
	 * @param evClass
	 * @return null if there is no encoding
	 */
	public Short getEvClassEncFor(XEventClass evClass) {
		return evClass2Enc.get(evClass);
	}

	/**
	 * Get encoding of a transition class
	 * 
	 * @param t
	 * @return null if there is no encoding
	 */
	public Short getTransClassEncFor(TransClass t) {
		return transClass2Enc.get(t);
	}

	/**
	 * @return the evClassifier
	 */
	public String getEvClassifier() {
		return evClassifier;
	}

	/**
	 * @return the evClassEnc
	 */
	public String[] getEvClassEnc() {
		return evClassEnc;
	}

	/**
	 * @return the transClassEnc
	 */
	public TransClass[] getTransClassEnc() {
		return transClassEnc;
	}

	/**
	 * decode the encoded event class back to event class
	 * @param s
	 * @return
	 */
	public String decodeEvClass(int s) {
		return this.evClassEnc[s];
	}

	/**
	 * Decode the encoded transition class back to transition class
	 * @param t
	 * @return
	 */
	public TransClass decodeTransClass(short t){
		return this.transClassEnc[t];
	}

	public String[] decodePatternID(short encodedPattern) {
		if (patternID2Idx.length > encodedPattern){
			if ((0 <= patternID2Idx[encodedPattern])&&(patterns.length > patternID2Idx[encodedPattern])){
				int upBound = patterns[patternID2Idx[encodedPattern]] + patternID2Idx[encodedPattern] + 1;
				String[] res = new String[upBound - (patternID2Idx[encodedPattern]+1)];
				int counter = 0;
				for (int i=patternID2Idx[encodedPattern]+1; i < upBound; i++){
					res[counter] = decodeEvClass(patterns[i]);
					counter++;
				}
				return res;
			}
		}
		return null;
	}

	public int getPatternIDNumElmts(int encodedPattern) {
		return patterns[patternID2Idx[encodedPattern]];
	}

	public int getNumPatterns() {
		return patternID2Idx.length;
	}
	
	/**
	 * Return string representation of a pattern
	 * @param patternID
	 * @return
	 */
	public String getPatternStr(int patternID){
		/*Todo pattern*/
		StringBuilder sb = new StringBuilder();
		for (int i=1; i <= patterns[patternID2Idx[patternID]]; i++){
			sb.append("[");
			sb.append(evClassEnc[patterns[patternID2Idx[patternID] + i]]);
			sb.append("]");
		}
		return sb.toString();
	}
}
