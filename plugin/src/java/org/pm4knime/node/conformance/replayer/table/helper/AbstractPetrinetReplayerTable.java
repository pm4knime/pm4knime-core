package org.pm4knime.node.conformance.replayer.table.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.graphbased.directed.opennet.OpenNet;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PartialOrderBuilder;
import org.processmining.plugins.astar.petrinet.AbstractPetrinetReplayer.Representative;
import org.processmining.plugins.astar.petrinet.AbstractPetrinetReplayer.Result;
import org.processmining.plugins.astar.petrinet.impl.AbstractPDelegate;
import org.processmining.plugins.astar.petrinet.impl.AbstractPILPDelegate;

import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNPartialOrderAwareReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParamProvider;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import nl.tue.astar.AStarException;
import nl.tue.astar.AStarThread;
import nl.tue.astar.ObservableAStarThread;
import nl.tue.astar.Tail;
import nl.tue.astar.Trace;
import nl.tue.astar.AStarThread.Canceller;
import nl.tue.astar.impl.memefficient.MemoryEfficientAStarAlgorithm;
import nl.tue.astar.util.LinearTrace;

public abstract class AbstractPetrinetReplayerTable <T extends Tail, D extends AbstractPDelegateTable<T>> implements
IPNPartialOrderAwareReplayAlgorithmTable {
	
	private PartialOrderBuilder poBuilder = PartialOrderBuilder.DEFAULT;

	public static class Representative {
		public Representative(int trace, TIntList unUsedIndices, TIntIntMap trace2orgTrace) {
			this.trace = trace;
			this.unUsedIndices = unUsedIndices;
			this.trace2orgTrace = trace2orgTrace;
		}

		public int trace;
		public TIntIntMap trace2orgTrace;
		public TIntList unUsedIndices;
	}

	public static class Result {
		public PRecordTable record;
		public int states;
		public long milliseconds;
		public int trace;
		public Trace filteredTrace;
		public boolean reliable;
		public int queuedStates;
		public TIntList unUsedIndices;
		public TIntIntMap trace2orgTrace;
		public int traversedArcs;
	}

	protected int visitedStates = 0;
	protected int queuedStates = 0;
	protected int traversedArcs = 0;

	/**
	 * Imported parameters
	 */
	// required parameters for replay
	protected Map<Transition, Integer> mapTrans2Cost;
	protected Map<String, Integer> mapEvClass2Cost;
	protected Map<Transition, Integer> mapSync2Cost;
	protected String classifier;
	protected int maxNumOfStates;
	protected Marking initMarking;
	protected Marking[] finalMarkings;
	protected boolean usePartialOrderEvents = false;

	/**
	 * Return true if all replay inputs are correct
	 */
	public boolean isAllReqSatisfied(PluginContext context, PetrinetGraph net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayParameter parameter) {
		if ((net instanceof ResetInhibitorNet) || (net instanceof InhibitorNet) || (net instanceof ResetNet)
				|| (net instanceof Petrinet) || (net instanceof OpenNet)) {
			// check number of transitions, places, and event classes, should be less than Short.MAX_VALUE
			if ((net.getTransitions().size() < Short.MAX_VALUE) && (net.getPlaces().size() < Short.MAX_VALUE)) {
				// check the number of event classes, should be less than Short.MAX_VALUE
				int eventClassSize = log.getActivties().length;
				if (eventClassSize < Short.MAX_VALUE) {
					return isParameterReqCorrect(net, log, mapping, parameter);
				}

			}
		}
		return false;
	}

	/**
	 * Return true if input of replay without parameters are correct
	 */
	public boolean isReqWOParameterSatisfied(PluginContext context, PetrinetGraph net, TableEventLog log,
			TransEvClassMappingTable mapping) {
		if ((net instanceof ResetInhibitorNet) || (net instanceof InhibitorNet) || (net instanceof ResetNet)
				|| (net instanceof Petrinet) || (net instanceof OpenNet)) {
			// check number of transitions, places, and event classes, should be less than Short.MAX_VALUE
			if ((net.getTransitions().size() < Short.MAX_VALUE) && (net.getPlaces().size() < Short.MAX_VALUE)) {
				// check the number of event classes, should be less than Short.MAX_VALUE
				return (log.getActivties().length < Short.MAX_VALUE);
			}
		}
		return false;
	}

	/**
	 * Return true if all replay inputs are correct: parameter type is correct
	 * and non empty (no null); all transitions are mapped to cost; all event
	 * classes (including dummy event class, i.e. an event class that does not
	 * exist in log, any transitions that are NOT silent and not mapped to any
	 * event class in the log is mapped to it) are mapped to cost; all costs
	 * should be non negative; numStates is non negative
	 */
	public boolean isParameterReqCorrect(PetrinetGraph net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayParameter parameter) {
		if (parameter instanceof CostBasedCompleteParamTable) {
			CostBasedCompleteParamTable param = (CostBasedCompleteParamTable) parameter;
			if ((param.getMapTrans2Cost() != null) && (param.getMaxNumOfStates() != null)
					&& (param.getMapEvClass2Cost() != null) && (param.getInitialMarking() != null)
					&& (param.getFinalMarkings() != null)) {
				// check all transitions are indeed mapped to cost
				if ((param.getMaxNumOfStates() >= 0)
						&& (param.getMapTrans2Cost().keySet().containsAll(net.getTransitions()))) {
					Set<String> evClassWithCost = param.getMapEvClass2Cost().keySet();
					String[] eventClassesName = log.getActivties();
					List<String> eventClassList = new ArrayList<String>(Arrays.asList(eventClassesName));
					if (evClassWithCost.containsAll(eventClassList)) {
						// dummy event class has to be mapped to cost
						//if (mapping.getDummyEventClass() != null) {
						//	if (!evClassWithCost.contains(mapping.getDummyEventClass())) {
						//		return false;
						//	}
						//	;
						//}

						// all cost should be non negative
						for (Integer costVal : param.getMapEvClass2Cost().values()) {
							if (costVal < 0) {
								return false;
							}
						}
						for (Integer costVal : param.getMapTrans2Cost().values()) {
							if (costVal < 0) {
								return false;
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Assign values of private attributes as given in parameters
	 * 
	 * @param parameters
	 */
	protected void importParameters(CostBasedCompleteParamTable parameters) {
		// replay parameters
		mapTrans2Cost = parameters.getMapTrans2Cost();
		maxNumOfStates = parameters.getMaxNumOfStates();
		mapEvClass2Cost = parameters.getMapEvClass2Cost();
		mapSync2Cost = parameters.getMapSync2Cost();
		initMarking = parameters.getInitialMarking();
		finalMarkings = parameters.getFinalMarkings();
		usePartialOrderEvents = parameters.isPartiallyOrderedEvents();
	}

	/**
	 * construct GUI in which the parameter for this algorithm can be obtained
	 */
	public IPNReplayParamProvider constructParamProvider(PluginContext context, PetrinetGraph net, TableEventLog log,
			TransEvClassMappingTable mapping) {
		/*
		 * TODO: UI CHANGING
		 */
		return  null;
	}

	protected SyncReplayResult recordToResult(AbstractPDelegateTable<?> d, List<String> trace, Trace filteredTrace, PRecordTable r,
			int traceIndex, int stateCount, boolean isReliable, long milliseconds, int queuedStates, int traversedArcs,
			int minCostMoveModel, TIntList unUsedIndices, TIntIntMap trace2orgTrace) {
		List<PRecordTable> history = PRecordTable.getHistory(r);
		double mmCost = 0; // total cost of move on model
		double mlCost = 0; // total cost of move on log
		double mSyncCost = 0; // total cost of synchronous move

		double mmUpper = 0; // total cost if all movements are move on model (including the synchronous one)
		double mlUpper = 0; // total cost if all events are move on log

		int eventInTrace = -1;
		List<StepTypes> stepTypes = new ArrayList<StepTypes>(history.size());
		List<Object> nodeInstance = new ArrayList<Object>();

		TIntIterator it = unUsedIndices.iterator();
		int firstUnUsed = it.hasNext() ? it.next() : Integer.MAX_VALUE;
		for (PRecordTable rec : history) {
			if (rec.getMovedEvent() == AStarThread.NOMOVE) {
				// move model only
				Transition t = d.getTransition((short) rec.getModelMove());
				if (t.isInvisible()) {
					stepTypes.add(StepTypes.MINVI);
				} else {
					stepTypes.add(StepTypes.MREAL);
				}
				nodeInstance.add(t);
				mmCost += (d.getCostForMoveModel((short) rec.getModelMove()) - 1.0) / d.getDelta();
				mmUpper += (d.getCostForMoveModel((short) rec.getModelMove()) - 1.0) / d.getDelta();
			} else {
				// a move occurred in the log. Check if class aligns with class in trace

				// check rec.getMovedEvent. If this is larger than unUsedIndices, then include all unUsedIndices
				// upto rec.getMovedEvent as LogMoves right now.
				//Todo: might be drastic change******check
				while (trace2orgTrace.get(rec.getMovedEvent()) > firstUnUsed) {
					String clsInTrace = trace.get(firstUnUsed); // this an unused event

					stepTypes.add(StepTypes.L);
					nodeInstance.add(clsInTrace);
					mlCost += mapEvClass2Cost.get(clsInTrace);
					eventInTrace++;

					firstUnUsed = it.hasNext() ? it.next() : Integer.MAX_VALUE;
				}

				short a = (short) filteredTrace.get(rec.getMovedEvent()); // a is the event obtained from the replay
				eventInTrace++;
				//				XEventClass clsInTrace = d.getClassOf(trace.get(eventInTrace)); // this is the current event
				//				while (d.getIndexOf(clsInTrace) != a) {
				//					// The next event in the trace is not of the same class as the next event in the A-star result.
				//					// This is caused by the class in the trace not being mapped to any transition.
				//					// move log only
				//
				//					// TODO: This is bullshit for partially ordered traces!
				//					stepTypes.add(StepTypes.L);
				//					nodeInstance.add(clsInTrace);
				//					mlCost += mapEvClass2Cost.get(clsInTrace);
				//					eventInTrace++;
				//					clsInTrace = d.getClassOf(trace.get(eventInTrace));
				//				}
				if (rec.getModelMove() == AStarThread.NOMOVE) {
					// move log only
					stepTypes.add(StepTypes.L);
					nodeInstance.add(d.getEventClass(a));
					mlCost += (d.getCostForMoveLog(a) - 1.0) / d.getDelta();
					//					mlUpper += (d.getCostForMoveLog(a) - 1.0) / d.getDelta();
				} else {
					// sync move
					stepTypes.add(StepTypes.LMGOOD);
					nodeInstance.add(d.getTransition((short) rec.getModelMove()));
					mSyncCost += (d.getCostForMoveSync((short) rec.getModelMove()) - 1.0) / d.getDelta();
					//					mlUpper += (d.getCostForMoveLog(a) - 1.0) / d.getDelta();
					mmUpper += (d.getCostForMoveModel((short) rec.getModelMove()) - 1.0) / d.getDelta();
				}
			}

		}

		// add the rest of the trace
		eventInTrace++;
		//		while (eventInTrace < trace.size()) {
		while (firstUnUsed < trace.size()) {
			// move log only
			String a = trace.get(firstUnUsed);
			eventInTrace++;
			stepTypes.add(StepTypes.L);
			nodeInstance.add(a);
			mlCost += mapEvClass2Cost.get(a);
			//			mlUpper += mapEvClass2Cost.get(a);
			firstUnUsed = it.hasNext() ? it.next() : Integer.MAX_VALUE;

		}

		// calculate mlUpper (because in cases where we have synchronous move in manifest, more than one events are aggregated
		// in one movement
		for (String evt : trace) {
			mlUpper += mapEvClass2Cost.get(evt);
		}

		SyncReplayResult res = new SyncReplayResult(nodeInstance, stepTypes, traceIndex);

		res.setReliable(isReliable);
		Map<String, Double> info = new HashMap<String, Double>();
		info.put(PNRepResult.RAWFITNESSCOST, (mmCost + mlCost + mSyncCost));
		info.put(PNRepResult.MAXFITNESSCOST, (mlUpper + minCostMoveModel));
		info.put(PNRepResult.MAXMOVELOGCOST, (mlUpper));

		if (mlCost > 0) {
			info.put(PNRepResult.MOVELOGFITNESS, 1 - (mlCost / mlUpper));
		} else {
			info.put(PNRepResult.MOVELOGFITNESS, 1.0);
		}

		if (mmCost > 0) {
			info.put(PNRepResult.MOVEMODELFITNESS, 1 - (mmCost / mmUpper));
		} else {
			info.put(PNRepResult.MOVEMODELFITNESS, 1.0);
		}
		info.put(PNRepResult.NUMSTATEGENERATED, (double) stateCount);
		info.put(PNRepResult.QUEUEDSTATE, (double) queuedStates);
		info.put(PNRepResult.TRAVERSEDARCS, (double) traversedArcs);

		// set info fitness
		if (mmCost > 0 || mlCost > 0 || mSyncCost > 0) {
			info.put(PNRepResult.TRACEFITNESS, 1 - ((mmCost + mlCost + mSyncCost) / (mlUpper + minCostMoveModel)));
		} else {
			info.put(PNRepResult.TRACEFITNESS, 1.0);
		}
		info.put(PNRepResult.TIME, (double) milliseconds);
		info.put(PNRepResult.ORIGTRACELENGTH, (double) eventInTrace);
		res.setInfo(info);
		return res;
	}

	/**
	 * get list of event class. Record the indexes of non-mapped event classes.
	 * 
	 * @param trace
	 * @param unUsedIndices
	 * @param trace2orgTrace
	 * @param classes
	 * @param mapEvClass2Trans
	 * @param listMoveOnLog
	 * @return
	 */
	protected LinearTrace getLinearTrace(TableEventLog log, int trace, AbstractPDelegateTable<?> delegate, TIntList unUsedIndices,
			TIntIntMap trace2orgTrace) {
		List<String> traces = log.getTraces().get(trace);
		int s = traces.size();
		String name = log.getTraceName(trace);
		if (name == null || name.isEmpty()) {
			name = "Trace " + trace;
		}
		TIntList activities = new TIntArrayList(s);
		for (int i = 0; i < s; i++) {
			int act = delegate.getActivityOf(trace, i);
			if (act != AStarThread.NOMOVE) {
				trace2orgTrace.put(activities.size(), i);
				activities.add(act);
			} else {
				unUsedIndices.add(i);
			}
		}

		LinearTrace result = new LinearTrace(name, activities);

		return result;
	}

	public String getHTMLInfo() {
		return "<html>This is an algorithm to calculate cost-based fitness between a log and a Petri net. <br/><br/>"
				+ "Given a trace and a Petri net, this algorithm "
				+ "return a matching between the trace and an allowed firing sequence of the net with the"
				+ "least deviation cost using the A* algorithm-based technique. The firing sequence has to reach proper "
				+ "termination (possible final markings/dead markings) of the net. <br/><br/>"
				+ "To minimize the number of explored state spaces, the algorithm prunes visited states/equally visited states. <br/><br/>"
				+ "Cost for skipping (move on model) and inserting (move on log) "
				+ "activities can be assigned uniquely for each move on model/log. </html>";
	}

	public PNRepResult replayLog(final PluginContext context, PetrinetGraph net, final TableEventLog log,
			TransEvClassMappingTable mapping, final IPNReplayParameter parameters) throws AStarException {
		importParameters((CostBasedCompleteParamTable) parameters);
		classifier = mapping.getEventClassifier();

		if (parameters.isGUIMode()) {
			if (maxNumOfStates != Integer.MAX_VALUE) {
				context.log("Starting replay with max state " + maxNumOfStates + "...");
			} else {
				context.log("Starting replay with no limit for max explored state...");
			}
		}

	
		final String[] classes = log.getActivties();

		final int delta = 1000;
		final int threads = parameters.getNumThreads();
		final D localDelegate = getDelegate(net, log, classes, mapping, delta, threads);

		final MemoryEfficientAStarAlgorithm<PHeadTable, T> aStar = new MemoryEfficientAStarAlgorithm<PHeadTable, T>(localDelegate);

		ExecutorService pool = Executors.newFixedThreadPool(threads);

		final List<Future<Result>> result = new ArrayList<Future<Result>>();

		final TIntObjectMap<Representative> doneMap = new TIntObjectHashMap<Representative>();

		long start = System.currentTimeMillis();

		if (context != null) {
			context.getProgress().setMaximum(log.getTraces().size() + 1);
		}
		TObjectIntMap<Trace> traces = new TObjectIntHashMap<Trace>(log.getTraces().size() / 2, 0.5f, -1);

		final List<SyncReplayResult> col = new ArrayList<SyncReplayResult>();

		try {
			// calculate first cost of empty trace

			// CPU EFFICIENT:
			//TObjectIntMap<PHead> head2int = new TObjectIntHashMap<PHead>(256 * 1024);
			//List<State<PHead, T>> stateList = new ArrayList<State<PHead, T>>(256 * 1024);

			int minCostMoveModel = getMinBoundMoveModel(parameters, delta, aStar, localDelegate);
			//int minCostMoveModel = 0; // AA: temporarily

			final Canceller canceller = parameters.getCanceller() == null ? new Canceller() {
				public boolean isCancelled() {
					if (context != null) {
						return context.getProgress().isCancelled();
					}
					return false;
				}
			} : parameters.getCanceller();

			for (int i = 0; i < log.getTraces().size(); i++) {
				if (parameters.getCanceller() != null) {
					if (parameters.getCanceller().isCancelled()) {
						break;
					}
				}

				PHeadTable initial = constructHead(localDelegate, initMarking, log.getTraces().get(i));

				final TIntList unUsedIndices = new TIntArrayList();
				final TIntIntMap trace2orgTrace = new TIntIntHashMap(log.getTraces().get(i).size(), 0.5f, -1, -1);
				/**
				 * Todo partial order
				 */
				final Trace trace = getLinearTrace(log, i, localDelegate, unUsedIndices,
						trace2orgTrace);
				int first = traces.get(trace);
				if (first >= 0) {
					doneMap.put(i, new Representative(first, unUsedIndices, trace2orgTrace));
					//System.out.println(i + "/" + log.size() + "-is the same as " + first);
					continue;
				} else {
					traces.put(trace, i);
				}

				final ObservableAStarThread<PHeadTable, T> thread;

				// MEMORY EFFICIENT
				thread = new AStarThread.MemoryEfficient<PHeadTable, T>(aStar, initial, trace, maxNumOfStates);

				thread.setType(parameters.getType());
				thread.setASynchronousMoveSorting(parameters.getAsynchronousMoveSort());
				thread.setQueueingModel(parameters.getQueueingModel());
				thread.setEpsilon(parameters.getEpsilon());
				// set the expected alignment length to the overrun times the length of the trace
				// rounded up.
				thread.setExpectedLength((int) (trace.getSize() * parameters.getExpectedAlignmentOverrun() + 0.5));

				// CPU EFFICIENT:
				//thread = new FastAStarThread<PHead, T>(delegate, head2int, stateList, initial, trace, maxNumOfStates);

				// To output dot files for each graph, use:
				//------------------
				//				String traceID = XConceptExtension.instance().extractName(log.get(i));
				//				if (traceID == null || traceID.isEmpty()) {
				//					traceID = "" + i;
				//				}
				//				final String dotFileName = "D:/temp/alignment/prAm6/trace_" + traceID + "_graph";
				//				final java.io.File dotFile = new java.io.File(dotFileName);
				//				final DotGraphAStarObserver graphObserver = new DotGraphAStarObserver(dotFile);//, aStar.getStore());
				//				thread.addObserver(graphObserver);
				//
				//				final String spFileName = "D:/temp/alignment/prAm6/trace_" + traceID + "_sptree";
				//				final java.io.File spFile = new java.io.File(spFileName);
				//				final AStarObserver treeObserver = new DotSpanningTreeObserver(spFile);
				//				thread.addObserver(treeObserver);

				//------------------
				// To use a fast implementation rather than a memory-efficient,use:
				//				TObjectIntMap<PHead> head2int = new TObjectIntHashMap<PHead>(10000);
				//				List<State<PHead, T>> stateList = new ArrayList<State<PHead, T>>(10000);
				//				thread = new FastAStarThread<PHead, T>(delegate,
				//						head2int, stateList, initial, trace, "trace" + i, maxNumOfStates);

				final int j = i;
				result.add(pool.submit(new Callable<Result>() {

					public Result call() throws Exception {
						Result result = new Result();
						result.trace = j;
						result.filteredTrace = trace;
						result.unUsedIndices = unUsedIndices;
						result.trace2orgTrace = trace2orgTrace;

						// long start = System.nanoTime();
						long start = System.currentTimeMillis();
						result.record = (PRecordTable) thread.getOptimalRecord(canceller);
						//long end = System.nanoTime();
						long end = System.currentTimeMillis();
						result.reliable = thread.wasReliable();

						//uncomment to have all classes of optimal alignments
						//while (thread.wasReliable()) {
						//thread.getOptimalRecord(c, result.record.getTotalCost());
						//}

						if (context != null) {
							synchronized (context) {
								if (parameters.isGUIMode() && (j % 100 == 0)) {
									//									context.log(j + "/" + log.size() + " queueing " + thread.getQueuedStateCount()
									//											+ " states, visiting " + thread.getVisitedStateCount() + " states took "
									//											+ (end - start) / 1000000000.0 + " seconds.");
									context.log(j + "/" + log.getTraces().size() + " queueing " + thread.getQueuedStateCount()
											+ " states, visiting " + thread.getVisitedStateCount() + " states took "
											+ (end - start) + " seconds.");
								}

							}
						}
						visitedStates += thread.getVisitedStateCount();
						queuedStates += thread.getQueuedStateCount();
						traversedArcs += thread.getTraversedArcCount();

						result.queuedStates = thread.getQueuedStateCount();
						result.states = thread.getVisitedStateCount();
						result.milliseconds = end - start;
						result.traversedArcs = thread.getTraversedArcCount();

						// uncomment the following two lines if state space graph is printed
						//						graphObserver.close();
						//						java.io.File f = new java.io.File(dotFileName + " "
						//								+ (result.record.getCostSoFar() - result.record.getBacktraceSize() - 1) + ".dot");
						//						if (f.exists()) {
						//							dotFile.delete();
						//						} else {
						//							if (AlignmentTest.iteration == 0) {
						//								dotFile.renameTo(f);
						//							} else {
						//								dotFile.renameTo(new java.io.File(dotFileName + " " + AlignmentTest.iteration + " "
						//										+ (result.record.getCostSoFar() - result.record.getBacktraceSize() - 1)
						//										+ " COPY.dot"));
						//							}
						//
						//						}
						//
						//						treeObserver.close();
						//						f = new java.io.File(spFileName + " "
						//								+ (result.record.getCostSoFar() - result.record.getBacktraceSize() - 1) + ".dot");
						//						if (f.exists()) {
						//							spFile.delete();
						//						} else {
						//							if (AlignmentTest.iteration == 0) {
						//								spFile.renameTo(f);
						//							} else {
						//								spFile.renameTo(new java.io.File(spFileName + " " + AlignmentTest.iteration + " "
						//										+ (result.record.getCostSoFar() - result.record.getBacktraceSize() - 1)
						//										+ " COPY.dot"));
						//							}
						//
						//						}
						return result;

					}
				}));

			}
			if (context != null) {
				context.getProgress().inc();
			}
			pool.shutdown();
			while (!pool.isTerminated()) {
				try {
					pool.awaitTermination(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
				}
			}

			long maxStateCount = 0;
			long time = 0;
			//			long ui = System.currentTimeMillis();
			for (Future<Result> f : result) {
				Result r = null;
				try {
					while (r == null) {
						try {
							r = f.get();
						} catch (InterruptedException e) {
						}
					}
					List<String> trace = log.getTraces().get(r.trace);
					int states = addReplayResults((context == null ? null : context.getProgress()), localDelegate,
							trace, r, r.unUsedIndices, r.trace2orgTrace, doneMap, log, col, r.trace, minCostMoveModel);//, null);
					maxStateCount = Math.max(maxStateCount, states);
					time += r.milliseconds;
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			long end = System.currentTimeMillis();
			// each PRecord uses 56 bytes in memory

			maxStateCount *= 56;
			if (parameters.isGUIMode()) {
				context.log("Total time : " + (end - start) / 1000.0 + " seconds");
				context.log("Time for A*: " + time / 1000.0 + " seconds");
				context.log("In total " + visitedStates + " unique states were visited.");
				context.log("In total " + traversedArcs + " arcs were traversed.");
				context.log("In total " + queuedStates + " states were queued.");
				//				context.log("In total " + aStar.getStatespace().size()
				//						+ " marking-parikhvector pairs were stored in the statespace.");
				//				context.log("In total " + aStar.getStatespace().getMemory() / (1024.0 * 1024.0)
				//						+ " MB were needed for the statespace.");
				context.log("At most " + maxStateCount / (1024.0 * 1024.0)
						+ " MB was needed for a trace (overestimate).");
				context.log("States / second:  " + visitedStates / (time / 1000.0));
				context.log("Traversed arcs / second:  " + traversedArcs / (time / 1000.0));
				context.log("Queued states / second:  " + queuedStates / (time / 1000.0));
				//				context.log("Storage / second: " + aStar.getStatespace().size() / ((ui - start) / 1000.0));
				//				context.log("EQUAL calls:" + PHeadCompressor.EQUALCALLS);
				//				context.log("EQUAL hash: " + PHeadCompressor.EQUALHASH);
				//				context.log("UNEQUAL:    " + PHeadCompressor.NONEQUAL);
			}
			synchronized (col) {
				//				if (outputStream != null) {
				//					outputStream.close();
				//				}
				return new PNRepResultImpl(col);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			/*
			 * TODO: fix later when abstractilp is there
			 */
			/*if (localDelegate instanceof AbstractPILPDelegate) {
				((AbstractPILPDelegate<?>) localDelegate).deleteLPs();
			}*/
		}
		return null;// debug code
	}

	protected PHeadTable constructHead(D delegate, Marking m, List<String> trace) {
		return new PHeadTable(delegate, m, trace);
	}

	/**
	 * get cost if an empty trace is replayed on a model
	 * 
	 * @param context
	 * @param net
	 * @param mapping
	 * @param classes
	 * @param delta
	 * @param threads
	 * @param aStar
	 * @param delegateD
	 * @return
	 */
	protected int getMinBoundMoveModel(IPNReplayParameter parameters, final int delta,
			final MemoryEfficientAStarAlgorithm<PHeadTable, T> aStar, D delegateD) throws AStarException {
		// create a log 
		List<String> emptyTrace = new ArrayList<String>();

		//final D delegateD = getDelegate(net, log, classes, mapping, delta, threads);
		PHeadTable initialD = constructHead(delegateD, initMarking, emptyTrace);

		final AStarThread<PHeadTable, T> threadD = new AStarThread.MemoryEfficient<PHeadTable, T>(aStar, initialD,
				new LinearTrace("Empty", 0), maxNumOfStates);

		threadD.setType(parameters.getType());
		threadD.setASynchronousMoveSorting(parameters.getAsynchronousMoveSort());
		threadD.setQueueingModel(parameters.getQueueingModel());
		threadD.setEpsilon(parameters.getEpsilon());
		// set the expected alignment length to the overrun times the length of the trace
		// rounded up.
		threadD.setExpectedLength(10);

		Canceller canceller = parameters.getCanceller();
		try {
			if (canceller == null) {
				canceller = new Canceller() {

					@Override
					public boolean isCancelled() {
						return false;
					}
				};
			}
			PRecordTable recordD = (PRecordTable) threadD.getOptimalRecord(canceller);
			if (recordD == null) {
				return 0;
			}
			// resolution due to numerical inconsistency problem of double data type
			assert (recordD.getCostSoFar() - recordD.getBacktraceSize() - 1) % delta == 0;
			int tempRes = (recordD.getCostSoFar() - recordD.getBacktraceSize() - 1) / delta;

			//			AbstractPILPDelegate.calls = 0;
			return tempRes;

		} catch (AStarException e1) {
			e1.printStackTrace();
			return 0;
		}
	}

	protected abstract D getDelegate(PetrinetGraph net, TableEventLog log, String[] classes, TransEvClassMappingTable mapping,
			int delta, int threads);

	protected int addReplayResults(Progress progress, D delegate, List<String> trace, Result r, TIntList unUsedIndices,
			TIntIntMap trace2orgTrace, TIntObjectMap<Representative> doneMap, TableEventLog log, List<SyncReplayResult> col,
			int traceIndex, int minCostMoveModel) {//, Map<Integer, SyncReplayResult> mapRes) {

		SyncReplayResult srr = recordToResult(delegate, trace, r.filteredTrace, r.record, traceIndex, r.states,
				r.reliable, r.milliseconds, r.queuedStates, r.traversedArcs, minCostMoveModel, unUsedIndices,
				trace2orgTrace);
		col.add(srr);
		if (progress != null) {
			progress.inc();
		}

		//BVD		if (mapRes == null) {
		HashMap<Integer, SyncReplayResult> mapRes = new HashMap<Integer, SyncReplayResult>(4);
		//BVD		}
		mapRes.put(traceIndex, srr);

		//BVD		boolean done = false;
		forLoop: for (int key : doneMap.keys()) {
			Representative value = doneMap.get(key);
			if (value != null && value.trace == r.trace) {
				// Consider all Traces which are mapped to the same internal trace of r.

				// Get the actual Trace from the log
				List<String> keyTrace = log.getTraces().get(key);

				// Consider all log traces for which a replay result is available and
				// try to find an XTrace that has the same event list as keyTrace.
				for (Integer keyMapRes : mapRes.keySet()) {
					if (compareEventClassList(delegate, log.getTraces().get(keyMapRes), keyTrace)) {
						// Now add key to the synchronous replay result for the keyMapRes, to 
						// indicate that the synchronous replay result for keyTrace is identical to
						// the synchronous replay result of keyMapRes.
						mapRes.get(keyMapRes).addNewCase(key);
						if (progress != null) {
							progress.inc();
						}
						// remove key from the doneMap, by mapping it to null (cannot change the map because of forLoop.
						//BVD						doneMap.put(key, null);
						continue forLoop;
					}
				}
				//BVD				if (!done) {
				// We were unable to find a log trace similar to keyTrace for which a replay result
				// is available.
				srr = recordToResult(delegate, keyTrace, r.filteredTrace, r.record, key, r.states, r.reliable,
						r.milliseconds, r.queuedStates, r.traversedArcs, minCostMoveModel, value.unUsedIndices,
						value.trace2orgTrace);
				col.add(srr);
				mapRes.put(key, srr);
				if (progress != null) {
					progress.inc();
				}
				//BVD					addReplayResults(delegate, keyTrace, r, value.unUsedIndices, value.trace2orgTrace, doneMap, log,
				//BVD							col, key, minCostMoveModel, mapRes);
				//BVD					done = true;
				//BVD				}
			}
		}
		//BVD		col.add(srr);

		return r.states;
	}

	protected boolean compareEventClassList(D d, List<String> t1, List<String> t2) {
		if (t1.size() != t2.size()) {
			return false;
		}
		Iterator<String> it = t2.iterator();
		for (String e : t1) {
			if (!e.equals(it.next())){
				return false;
			}
		}
		return true;
	}

	public void setPartialOrderBuilder(PartialOrderBuilder poBuilder) {
		this.poBuilder = poBuilder;
	}

	public PartialOrderBuilder getPartialOrderBuilder() {
		return poBuilder;
	}

}
