package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.impl.PRecord;

import gnu.trove.list.TIntList;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.TShortIntMap;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TObjectShortHashMap;
import gnu.trove.map.hash.TShortIntHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.set.TShortSet;
import gnu.trove.set.hash.TShortHashSet;
import nl.tue.astar.AStarThread;
import nl.tue.astar.Record;
import nl.tue.astar.Tail;
import nl.tue.astar.Trace;
import nl.tue.astar.impl.State;
import nl.tue.astar.impl.memefficient.StorageAwareDelegate;
import nl.tue.astar.util.ShortShortMultiset;
import nl.tue.storage.EqualOperation;
import nl.tue.storage.HashOperation;

public abstract class AbstractPDelegateTable<T extends Tail> implements StorageAwareDelegate<PHeadTable, T>  {
	public static final short NEV = -1;
	public static final short INHIBITED = -1;

	protected final PHeadTableCompressor<T> headCompressor;
	protected final TObjectShortMap<Place> place2int = new TObjectShortHashMap<Place>(10, 0.5f, NEV);
	protected final TShortObjectMap<Place> int2place = new TShortObjectHashMap<Place>(10, 0.5f, NEV);
	protected final short places;
	protected final TObjectShortMap<String> act2int = new TObjectShortHashMap<String>(10, 0.5f, NEV);
	protected final TShortObjectMap<String> int2act = new TShortObjectHashMap<String>(10, 0.5f, NEV);
	protected final short activities;
	protected final TObjectShortMap<Transition> trans2int = new TObjectShortHashMap<Transition>(10, 0.5f, NEV);
	protected final TShortObjectMap<Transition> int2trans = new TShortObjectHashMap<Transition>(10, 0.5f, NEV);
	protected final TShortObjectMap<TShortList> actIndex2trans = new TShortObjectHashMap<TShortList>(10, 0.5f, NEV);
	protected final TShortObjectMap<short[]> transIndex2input = new TShortObjectHashMap<short[]>(10, 0.5f, NEV);
	protected final TShortObjectMap<short[]> transIndex2output = new TShortObjectHashMap<short[]>(10, 0.5f, NEV);
	protected final TShortObjectMap<TShortSet> transIndex2act = new TShortObjectHashMap<TShortSet>();
	protected final short transitions;
	protected final TShortIntMap trans2cost = new TShortIntHashMap();
	protected final TShortIntMap sync2cost = new TShortIntHashMap();
	protected final TShortIntMap act2cost = new TShortIntHashMap();
	protected final PetrinetGraph net;
	protected final Set<ShortShortMultiset> finalMarkings;
	protected final TableEventLog log;
	protected final TShortList unmapped = new TShortArrayList();
	private final int delta;
	private int epsilon = 1;
	protected int resetArcs;
	protected int arcs;
	protected int inhibitors;
	private final boolean[] hasResetArc;

	/**
	 * Constructor without cost of move synchronous. In this case cost of move
	 * sync is assumed to be 0
	 * 
	 * @param net
	 * @param log
	 * @param classes
	 * @param map
	 * @param mapTrans2Cost
	 * @param mapEvClass2Cost
	 * @param delta
	 * @param set
	 */
	protected AbstractPDelegateTable(PetrinetGraph net, TableEventLog log,  TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost, int delta,
			Marking... set) {
		this(net, log, map, mapTrans2Cost, mapEvClass2Cost, new HashMap<Transition, Integer>(0), delta, set);
	}

	/**
	 * Constructor with cost of move synchronous.
	 * 
	 * @param net
	 * @param log
	 * @param classes
	 * @param map
	 * @param mapTrans2Cost
	 * @param mapEvClass2Cost
	 * @param mapSync2Cost
	 * @param delta
	 * @param set
	 */
	protected AbstractPDelegateTable(PetrinetGraph net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, Marking... set) {

		this.net = net;
		this.log = log;
		this.delta = delta;
		this.places = (short) net.getPlaces().size();
		this.transitions = (short) net.getTransitions().size();
		this.resetArcs = 0;
		this.arcs = 0;
		this.inhibitors = 0;
		this.hasResetArc = new boolean[transitions];

		this.finalMarkings = new HashSet<ShortShortMultiset>(set.length);

		Collection<String> eventClasses;
		eventClasses = new TreeSet<String>(map.values());
		// eventClasses.addAll(classes.getClasses());
		// eventClasses.addAll(mapEvClass2Cost.keySet());

		this.activities = (short) eventClasses.size();
		this.headCompressor = constructHeadCompressor(places, activities);

		// initialize lookup maps
		initialize(eventClasses, map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, set);
	}

	protected void initialize(Collection<String> eventClasses, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, Marking... set) {

		short i = initPlaces();

		TreeSet<Transition> sortedTransitions = initTransitions(mapTrans2Cost, mapSync2Cost, i);

		initArcs();

		initEventClasses(eventClasses, mapEvClass2Cost);

		initTransitionMapping(map, sortedTransitions);

		initMarkings(set);

	}

	private void initMarkings(Marking... set) {
		for (Marking m : set) {
			ShortShortMultiset mi = new ShortShortMultiset(places);
			for (Place p : m.baseSet()) {
				mi.put(place2int.get(p), m.occurrences(p).shortValue());
			}
			finalMarkings.add(mi);
		}
	}

	private void initTransitionMapping(TransEvClassMappingTable map, TreeSet<Transition> sortedTransitions) {
		short i;
		i = 0;
		for (Transition t : sortedTransitions) {
			Set<String> s = new HashSet<String>();
			s.add(map.get(t));
			if (s != null && !s.isEmpty()) {
				for (String e : s) {
					short a = act2int.get(e);
					if (a == NEV) {
						// somehow, the map contains a event class that is not part of the eventclasses
						// provided (for example a dummy event class).
						// We do not cater for that here.

						continue;
					}
					actIndex2trans.get(a).add(i);
					transIndex2act.get(i).add(a);

				}
			}
			if (transIndex2act.get(i).isEmpty()) {
				unmapped.add(i);
			}
			i++;
		}
	}

	private void initEventClasses(Collection<String> eventClasses, Map<String, Integer> mapEvClass2Cost) {
		short i;
		i = 0;
		for (String a : eventClasses) {
			act2int.put(a, i);
			actIndex2trans.put(i, new TShortArrayList());
			int2act.put(i, a);
			act2cost.put(i, mapEvClass2Cost.get(a));
			i++;
		}
	}

	private void initArcs() {
		// ARCS
		for (PetrinetEdge<?, ?> edge : net.getEdges()) {
			short t, p, w;
			if (edge instanceof Arc) {
				arcs++;
				if (edge.getSource() instanceof Transition) {
					t = trans2int.get(edge.getSource());
					p = place2int.get(edge.getTarget());
					w = (short) ((Arc) edge).getWeight();
					// w tokens are produced on place p by transition t
					if (transIndex2output.get(t)[p] >= 0) {
						// first arc, or update to normal arc
						transIndex2output.get(t)[p] += w;
					} else {
						// place is also reset by this transition. 
						transIndex2output.get(t)[p] -= w;
					}
				} else {
					p = place2int.get(edge.getSource());
					t = trans2int.get(edge.getTarget());
					w = (short) ((Arc) edge).getWeight();
					// w tokens are consumed from place p by transition t
					if (transIndex2input.get(t)[p] != INHIBITED) {
						transIndex2input.get(t)[p] += w;
					}
				}
			} else if (edge instanceof ResetArc) {
				resetArcs++;
				t = trans2int.get(edge.getTarget());
				p = place2int.get(edge.getSource());
				hasResetArc[t] = true;
				// all tokens are consumed from place p by transition t
				w = transIndex2output.get(t)[p];
				if (w >= 0) {
					transIndex2output.get(t)[p] = (short) (-w - 1);
				}
			} else if (edge instanceof InhibitorArc) {
				inhibitors++;
				t = trans2int.get(edge.getTarget());
				p = place2int.get(edge.getSource());
				// no token should be on place p when t fires
				// even if there would be other arcs, these are now made obsolete.
				transIndex2input.get(t)[p] = INHIBITED;
			}

		}
	}

	private TreeSet<Transition> initTransitions(Map<Transition, Integer> mapTrans2Cost,
			Map<Transition, Integer> mapSync2Cost, short i) {
		// TRANSITIONS
		TreeSet<Transition> sortedTransitions = new TreeSet<Transition>(new Comparator<Transition>() {
			public int compare(Transition o1, Transition o2) {
				if (o1.equals(o2)) {
					return 0;
				}
				if (o1.getLabel().equals(o2.getLabel())) {
					return o1.getId().compareTo(o2.getId());
				} else {
					return o1.getLabel().compareTo(o2.getLabel());
				}
			}

		});
		sortedTransitions.addAll(net.getTransitions());

		for (Transition t : sortedTransitions) {
			trans2int.put(t, i);
			int2trans.put(i, t);
			transIndex2input.put(i, new short[places]);
			transIndex2output.put(i, new short[places]);
			transIndex2act.put(i, new TShortHashSet(1));
			trans2cost.put(i, mapTrans2Cost.get(t));

			// add cost of move sync (if available)
			Integer cost = mapSync2Cost.get(t);
			if (cost != null) {
				sync2cost.put(i, cost.intValue());
			} else {
				sync2cost.put(i, 0);
			}

			i++;
		}
		return sortedTransitions;
	}

	private short initPlaces() {
		// PLACES
		short i = 0;
		TreeSet<Place> sortedPlaces = new TreeSet<Place>(new Comparator<Place>() {
			public int compare(Place o1, Place o2) {
				if (o1.equals(o2)) {
					return 0;
				}
				if (o1.getLabel().equals(o2.getLabel())) {
					return o1.getId().compareTo(o2.getId());
				} else {
					return o1.getLabel().compareTo(o2.getLabel());
				}
			}

		});
		sortedPlaces.addAll(net.getPlaces());
		for (Place p : sortedPlaces) {
			place2int.put(p, i);
			int2place.put(i, p);
			i++;
		}
		i = 0;
		return i;
	}

	/**
	 * Overrideable method to change head compressor
	 * 
	 * @param places2
	 * @param activities2
	 * @return
	 */
	protected PHeadTableCompressor<T> constructHeadCompressor(short places2, short activities2) {
		return new PHeadTableCompressor<T>(places, activities);
	}

	public boolean isEnabled(int transition, final ShortShortMultiset marking) {
		short[] needed = transIndex2input.get((short) transition);

		for (short i = places; i-- > 0;) {
			if (needed[i] == INHIBITED) {
				if (marking.get(i) > 0) {
					return false;
				}
			} else {
				if (marking.get(i) < needed[i]) {
					return false;
				}
			}
		}
		return true;
	}

	public short getIndexOf(Place p) {
		return place2int.get(p);
	}

	public short numPlaces() {
		return places;
	}

	public Place getPlace(short i) {
		return int2place.get(i);
	}

	public short getIndexOf(Transition t) {
		return trans2int.get(t);
	}

	public short numTransitions() {
		return transitions;
	}

	public Transition getTransition(short i) {
		return int2trans.get(i);
	}

	public short getIndexOf(String c) {
		return act2int.get(c);
	}

	public short numEventClasses() {
		return activities;
	}

	public String getEventClass(short i) {
		return int2act.get(i);
	}

	public Record createInitialRecord(PHeadTable head, Trace trace) {
		PRecordTable r = new PRecordTable(0, null, head.getMarking().getNumElts(), trace.getSize());
		r.setEstimatedRemainingCost(0, true);
		return r;
	}

	public PHeadTableCompressor<T> getHeadInflater() {
		return headCompressor;
	}

	public PHeadTableCompressor<T> getHeadDeflater() {
		return headCompressor;
	}

	

	
	public int getActivityOf(int trace, int event) {
		if (trace < 0 || event < 0 || trace >= log.getTraces().size() || event >= log.getTraces().get(trace).size()) {
			return AStarThread.NOMOVE;
		}
		String cls = log.getTraces().get(trace).get(event);
		short a = act2int.get(cls);
		if (a < 0) {
			return AStarThread.NOMOVE;
		} else {
			return a;
		}
	}

	public boolean isFinal(ShortShortMultiset marking) {
		return finalMarkings.contains(marking);
	}

	public HashOperation<State<PHeadTable, T>> getHeadBasedHashOperation() {
		return headCompressor;
	}

	public EqualOperation<State<PHeadTable, T>> getHeadBasedEqualOperation() {
		return headCompressor;
	}

	protected PetrinetGraph getPetrinet() {
		return net;
	}

	public List<String> getTrace(int t) {
		return log.getTraces().get(t);
	}

	public int getCostFor(int modelMove, int activity) {
		if (modelMove == AStarThread.NOMOVE) {
			// move on log only
			return getCostForMoveLog((short) activity);
		}
		if (activity == AStarThread.NOMOVE) {
			return getCostForMoveModel((short) modelMove);
		}
		// synchronous move assumed here
		return getCostForMoveSync((short) modelMove);
		//		return 1; AA: this is replaced by cost for move sync
	}

	/**
	 * Cost for move log (but not on model), scaled with delta and added with 1
	 * 
	 * @param activity
	 * @return
	 */
	public int getCostForMoveLog(short activity) {
		if (activity == AStarThread.NOMOVE) {
			return 0;
		}
		return epsilon + delta * act2cost.get(activity);
	}

	/**
	 * Cost for move model (but not on log), scaled with delta and added with 1
	 * 
	 * @param transition
	 * @return
	 */
	public int getCostForMoveModel(short transition) {
		if (transition == AStarThread.NOMOVE) {
			return 0;
		}
		return epsilon + delta * trans2cost.get(transition);
	}

	/**
	 * Cost for move synchronous, scaled with delta and added with epsilon
	 * 
	 * @param transition
	 * @return
	 */
	public int getCostForMoveSync(short transition) {
		return epsilon + delta * sync2cost.get(transition);
	}

	/**
	 * Get epsilon value: value added to ensure we can go out of infinite firing
	 * sequence of transitions with cost 0
	 * 
	 * @return
	 */
	public int getEpsilon() {
		return epsilon;
	}

	/**
	 * Set epsilon value: value added to ensure we can go out of infinite firing
	 * sequence of transitions with cost 0
	 * 
	 * @return
	 */
	public void setEpsilon(int i) {
		assert (i < delta);
		this.epsilon = i;
	}

	public TShortList getTransitions(short activity) {
		return actIndex2trans.get(activity);
	}

	public TShortSet getActivitiesFor(short transition) {
		return transIndex2act.get(transition);
	}

	public boolean hasEnabledTransitions(ShortShortMultiset marking) {
		for (int t = 0; t < transitions; t++) {
			if (isEnabled(t, marking)) {
				return true;
			}
		}
		return false;
	}

	public TIntList getEnabledTransitionsChangingMarking(ShortShortMultiset marking) {
		TIntList list = new TIntArrayList();
		for (short t = 0; t < transitions; t++) {
			if (isEnabled(t, marking)) {
				// check if for at least one place, the marking changes
				short[] input = transIndex2input.get(t);
				short[] output = transIndex2output.get(t);
				boolean changes = false;
				for (short p = places; !changes && p-- > 0;) {
					// inhibitor arcs do not remove tokens
					short i = input[p];
					int effect = i == INHIBITED ? 0 : -i;
					// reset arcs remove all tokens, regular arcs at most as many at on the arc
					// the enablement guarantees that this can be done.
					short o = output[p];
					effect += o >= 0 ? o : -marking.get(p) - o - 1;
					changes = effect != 0;
				}
				if (changes) {
					list.add(t);
				}
			}
		}
		return list;
	}

	public short[] getInputOf(short transition) {
		return transIndex2input.get(transition);
	}

	public short[] getOutputOf(short transition) {
		return transIndex2output.get(transition);
	}

	public Set<ShortShortMultiset> getFinalMarkings() {
		return finalMarkings;
	}

	public double getDelta() {
		return delta;
	}

	public boolean hasResetArc(int transition) {
		return hasResetArc[transition];
	}

}
