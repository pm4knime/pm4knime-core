package org.pm4knime.node.discovery.alpha.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.pm4knime.node.discovery.defaultminer.TraceVariantRep;
import org.pm4knime.node.discovery.defaultminer.TraceVariant;
import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.abstractions.AlphaPlusAbstraction;
import org.processmining.alphaminer.abstractions.AlphaPlusPlusAbstraction;
import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.alphaminer.abstractions.AlphaSharpAbstraction;
import org.processmining.alphaminer.abstractions.impl.AlphaClassicAbstractionImpl;
import org.processmining.alphaminer.abstractions.impl.AlphaPlusAbstractionImpl;
import org.processmining.alphaminer.abstractions.impl.AlphaPlusPlusAbstractionImpl;
import org.processmining.alphaminer.abstractions.impl.AlphaRobustAbstractionImpl;
import org.processmining.alphaminer.abstractions.impl.AlphaSharpAbstractionImpl;
import org.processmining.alphaminer.algorithms.AlphaMiner;
import org.processmining.alphaminer.algorithms.AlphaMinerFactory;
import org.processmining.alphaminer.algorithms.AlphaSharpMinerImpl;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaPlusMinerParameters;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.factories.ActivityCountAbstractionFactory;
import org.processmining.logabstractions.factories.CausalAbstractionFactory;
import org.processmining.logabstractions.factories.DirectlyFollowsAbstractionFactory;
import org.processmining.logabstractions.factories.LoopAbstractionFactory;
import org.processmining.logabstractions.factories.StartEndActivityFactory;
import org.processmining.logabstractions.models.CausalPrecedenceAbstraction;
import org.processmining.logabstractions.models.CausalSuccessionAbstraction;
import org.processmining.logabstractions.models.DirectlyFollowsAbstraction;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;
import org.processmining.logabstractions.models.LongTermFollowsAbstraction;
import org.processmining.logabstractions.models.implementations.LongTermFollowsAbstractionImpl;
import org.processmining.logabstractions.util.XEventClassUtils;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

public class AlphaAbstraction {
	public static Object[] apply(PluginContext context, TraceVariantRep variants,
			String eClassifier, AlphaMinerParameters parameters) {
		AlphaMiner<XEventClass, ? extends AlphaClassicAbstraction<XEventClass>, ? extends AlphaMinerParameters> miner = createAlphaMiner(context, variants, eClassifier, parameters);
		Pair<Petrinet, Marking> markedNet = miner.run();
		if (context.getProgress().isCancelled()) {
			context.getFutureResult(0).cancel(true);
			return new Object[] { null, null };
		}
		context.getConnectionManager()
				.addConnection(new InitialMarkingConnection(markedNet.getFirst(), markedNet.getSecond()));
		return new Object[] { markedNet.getFirst(), markedNet.getSecond() };
	}
	
	public static <P extends AlphaMinerParameters> AlphaMiner<XEventClass, ? extends AlphaClassicAbstraction<XEventClass>, ? extends AlphaMinerParameters> createAlphaMiner(
			PluginContext context, TraceVariantRep variants, String eClassifier, P parameters) {
		switch (parameters.getVersion()) {
			case PLUS_PLUS :
				return AlphaMinerFactory.createAlphaPlusPlusMiner(context, new AlphaPlusMinerParameters(parameters.getVersion()),
						createAlphaPlusPlusAbstraction(variants));
			case PLUS :
				return AlphaMinerFactory.createAlphaPlusMiner(context, new AlphaPlusMinerParameters(parameters.getVersion()),
						createAlphaPlusAbstraction(variants));
			case SHARP :
				XEventClassifier classifier = new XEventAttributeClassifier("Event Name", eClassifier);
				Pair<TraceVariantRep, Pair<XEventClass, XEventClass>> startEndLog = addArtificialStartEndToLog(context, variants, classifier);
				return AlphaMinerFactory.createAlphaSharpMiner(context, parameters,
						createAlphaSharpAbstraction(startEndLog.getFirst()),
						startEndLog.getSecond().getFirst(), startEndLog.getSecond().getSecond());
			case ROBUST :
				return AlphaMinerFactory.createAlphaRobustMiner(context, parameters, 
						createAlphaRobustAbstraction(variants,
								(AlphaRobustMinerParameters) parameters));
			case CLASSIC :
			default :
				AlphaClassicAbstraction<XEventClass> abstrClassic = createAlphaClassicAbstraction(variants);
				return AlphaMinerFactory.createAlphaClassicMiner(context, parameters, abstrClassic);
		}
	}
	
	
	private static Pair<TraceVariantRep, Pair<XEventClass, XEventClass>> addArtificialStartEndToLog(PluginContext context,
			TraceVariantRep variants, XEventClassifier classifier) {
		
		
		
		String startClean = AlphaSharpMinerImpl.UNIQUE_ARTIFICIAL_START_IDENTIFIER;
		String startTarget = startClean;
		String startAppendix = "";
		String endClean = AlphaSharpMinerImpl.UNIQUE_ARTIFICIAL_END_IDENTIFIER;
		String endTarget = endClean;
		String endAppendix = "";
		for (int i = 1; i < classifier.getDefiningAttributeKeys().length; i++) {
			startTarget += "+";
			endTarget += "+";
		}
			
		
		XEventClasses classes = XEventClasses.deriveEventClasses(classifier, XFactoryRegistry.instance().currentDefault().createLog());
		HashMap<String, Integer> events = new HashMap<String, Integer>();
		int j = 0;
		for (String a: variants.getEvents()) {
			classes.register(a);
			events.put(a, j);
			j++;
		}
		
		while (classes.getByIdentity(startTarget) != null) {
			Random r = new Random();
			int a = r.nextInt();
			startTarget += a;
			startAppendix += a;
		}
		while (classes.getByIdentity(endTarget) != null) {
			Random r = new Random();
			int a = r.nextInt();
			endTarget += a;
			endAppendix += a;
		}

		while (events.get(startTarget) != null) {
			Random r = new Random();
			int a = r.nextInt();
			startTarget += a;
			startAppendix += a;
		}
		while (events.get(endTarget) != null) {
			Random r = new Random();
			int a = r.nextInt();
			endTarget += a;
			endAppendix += a;
		}
		Pair<String, String> startEndEvent = createStartEndEvents(startClean, endClean, startAppendix,
				endAppendix);
		try {
			TraceVariantRep started = startFilter(variants, startEndEvent.getFirst());
			TraceVariantRep ended = endFilter(started, startEndEvent.getSecond());
			return new Pair<>(ended, getArtificialStartAndEnd(ended, classifier, startTarget, endTarget));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Pair<>(variants, new Pair<XEventClass, XEventClass>(null, null));
	}
	
	private static Pair<XEventClass, XEventClass> getArtificialStartAndEnd(TraceVariantRep variants,
			XEventClassifier classifier, String start, String end) {
		HashSet<String> classes = variants.getEvents();
		String startClass, endClass;
		startClass = null;
		endClass = null;
		for (String clazz : classes) {
			if (clazz.equals(start)) {
				startClass = clazz;
			}
			if (clazz.equals(end)) {
				endClass = clazz;
			}
		}
		XEventClass sClass = new XEventClass(startClass, 0);
		XEventClass eClass = new XEventClass(endClass, classes.size()-1);
		return new Pair<>(sClass, eClass);
	}
	
	public static TraceVariantRep endFilter(TraceVariantRep variants,  String last) {
		ArrayList<TraceVariant> newVariants = new ArrayList<TraceVariant>();
		for (TraceVariant t: variants.getVariants()) {
			ArrayList<String> activities = new ArrayList<String>();
			activities.addAll(t.getActivities());
			activities.add(last);
			TraceVariant t2 = new TraceVariant(activities, t.getFrequency());
			newVariants.add(t2);
		}
		
		HashSet<String> events = new HashSet<String>();
		events.addAll(variants.getEvents());
		events.add(last);
		
		return new TraceVariantRep(newVariants, events, variants.getNumTraces());
	}

	public static TraceVariantRep startFilter(TraceVariantRep variants, String first) {
		
		ArrayList<TraceVariant> newVariants = new ArrayList<TraceVariant>();
		for (TraceVariant t: variants.getVariants()) {
			ArrayList<String> activities = new ArrayList<String>();
			activities.add(first);
			activities.addAll(t.getActivities());
			TraceVariant t2 = new TraceVariant(activities, t.getFrequency());
			newVariants.add(t2);
		}
		
		HashSet<String> events = new HashSet<String>();
		events.add(first);
		events.addAll(variants.getEvents());
		
		return new TraceVariantRep(newVariants, events, variants.getNumTraces());
	}

	private static Pair<String, String> createStartEndEvents(String startClean,
			String endClean, String startAppendix, String endAppendix) {
		String valStart = "";
		String valEnd = "";
		valStart += startClean;
		valEnd += endClean;
		valStart += startAppendix;
		valEnd += endAppendix;
		return new Pair<>(valStart, valEnd);
	}
	
	
	public static AlphaClassicAbstraction<XEventClass> createAlphaClassicAbstraction(TraceVariantRep variants) {
		HashMap<String, Integer> events = new HashMap<String, Integer>();
		int nEvents = variants.getNumEvents();
		int j = 0;
		for (String a: variants.getEvents()) {
			events.put(a, j);
			j++;
		}
		double[][] dfa = new double[nEvents][nEvents]; // directly follows
		double[] starts = new double[nEvents]; // start activity
		double[] ends = new double[nEvents]; // end activity
		double[] lol = new double[nEvents]; // length one loop
		for (TraceVariant trace : variants.getVariants()) {
			
			int freq = trace.getFrequency();
			ArrayList<String> activities = trace.getActivities();
			starts[events.get(activities.get(0))] +=  (freq * StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN);
			ends[events.get(activities.get(activities.size()-1))] += (freq * StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN);
			for (int i = 0; i < activities.size() - 1; i++) {
				String from = activities.get(i);
				String to = activities.get(i + 1);
				dfa[events.get(from)][events.get(to)] = DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
				if (from.equals(to)) {
					lol[events.get(from)] = LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
				}
			}		
		}

		
		XEventClass[] arr = createEventClasses(events);
		return new AlphaClassicAbstractionImpl<>(arr,
				DirectlyFollowsAbstractionFactory.constructDirectlyFollowsAbstraction(arr, dfa,
						DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructStartActivityAbstraction(arr, starts,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructEndActivityAbstraction(arr, ends,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				LoopAbstractionFactory.constructLengthOneLoopAbstraction(arr, lol,
						LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN));
	}

	private static XEventClass[] createEventClasses(HashMap<String, Integer> events) {
		XEventClass[] res = new XEventClass[events.keySet().size()];
		for (String a: events.keySet()) {
			int j = events.get(a);
			res[j] = new XEventClass(a, j);
		}
		return res;
	}

	public static AlphaPlusAbstraction<XEventClass> createAlphaPlusAbstraction(TraceVariantRep variants) {
        HashMap<String, Integer> events = new HashMap<String, Integer>();
		
		int nEvents = variants.getNumEvents();
		int j = 0;
		for (String a: variants.getEvents()) {
			events.put(a, j);
			j++;
		}
		XEventClass[] arr = createEventClasses(events);
		
		
		AlphaClassicAbstraction<XEventClass> aca = createAlphaClassicAbstraction(variants);
		Pair<XEventClass[], int[]> reducedClasses = XEventClassUtils
				.stripLengthOneLoops(arr, aca.getLengthOneLoopAbstraction());
		double[][] ltl = new double[nEvents][nEvents];
		double[][] dfaLf = new double[reducedClasses.getFirst().length][reducedClasses.getFirst().length];
		double[][] ltlLf = new double[reducedClasses.getFirst().length][reducedClasses.getFirst().length];
		double[] startsLf = new double[reducedClasses.getFirst().length];
		double[] endsLf = new double[reducedClasses.getFirst().length];
		for (TraceVariant trace : variants.getVariants()) {
			
			ArrayList<String> activities = trace.getActivities();
			int first, second, third, firstLf, secondLf, thirdLf, current;
			first = second = third = firstLf = secondLf = thirdLf = current = -1;
			for (int i = 0; i < activities.size(); i++) {
				current = events.get(activities.get(i));
				if (i == 0) {
					first = current;
				} else if (i == 1) {
					second = current;
				} else if (i == 2) {
					third = current;
				} else {
					// shift
					first = second;
					second = third;
					third = current;
				}
				if (first != -1 && second != -1 && third != -1 && first == third) {
					ltl[first][second] = LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
				}
				if (aca.getLengthOneLoopAbstraction().holds(current))
					continue;
				if (firstLf == -1) {
					firstLf = current;
					startsLf[reducedClasses.getSecond()[firstLf]] = StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
					continue;
				}
				if (secondLf == -1) {
					secondLf = current;
					dfaLf[reducedClasses.getSecond()[firstLf]][reducedClasses.getSecond()[secondLf]] = DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					continue;
				}
				if (thirdLf != -1) {
					firstLf = secondLf;
					secondLf = thirdLf;
				}
				thirdLf = current;
				dfaLf[reducedClasses.getSecond()[secondLf]][reducedClasses.getSecond()[thirdLf]] = DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
				if (firstLf == thirdLf) {
					ltlLf[reducedClasses.getSecond()[firstLf]][reducedClasses.getSecond()[secondLf]] = LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
				}
			}
			if (thirdLf != -1) {
				endsLf[reducedClasses.getSecond()[thirdLf]] = StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
			} else if (secondLf != -1) {
				endsLf[reducedClasses.getSecond()[secondLf]] = StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
			} else if (firstLf != -1) {
				endsLf[reducedClasses.getSecond()[firstLf]] = StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
			}	
		}
		
		return new AlphaPlusAbstractionImpl<>(aca,
				LoopAbstractionFactory.constructLengthTwoLoopAbstraction(arr, ltl,
						LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				reducedClasses,
				DirectlyFollowsAbstractionFactory.constructDirectlyFollowsAbstraction(reducedClasses.getFirst(), dfaLf,
						DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructStartActivityAbstraction(reducedClasses.getFirst(), startsLf,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructEndActivityAbstraction(reducedClasses.getFirst(), endsLf,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				LoopAbstractionFactory.constructLengthTwoLoopAbstraction(reducedClasses.getFirst(), ltlLf,
						LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN));

	}

	public static AlphaPlusPlusAbstraction<XEventClass> createAlphaPlusPlusAbstraction(TraceVariantRep variants) {
		AlphaPlusAbstraction<XEventClass> apa = createAlphaPlusAbstraction(variants);
		CausalPrecedenceAbstraction<XEventClass> cpa = CausalAbstractionFactory
				.constructAlphaPlusPlusCausalPrecedenceAbstraction(apa.getLengthOneLoopFreeCausalRelationAbstraction(),
						apa.getLengthOneLoopFreeUnrelatedAbstraction());
		CausalSuccessionAbstraction<XEventClass> csa = CausalAbstractionFactory
				.constructAlphaPlusPlusCausalSuccessionAbstraction(apa.getLengthOneLoopFreeCausalRelationAbstraction(),
						apa.getLengthOneLoopFreeUnrelatedAbstraction());

		return new AlphaPlusPlusAbstractionImpl<XEventClass>(apa, cpa, csa,
				constructAlphaPlusPlusLengthOneLoopFreeLongTermFollowsAbstraction(variants,
						apa.getLengthOneLoopFreeDirectlyFollowsAbstraction(), cpa, csa,
						apa.getLengthOneLoopAbstraction()));
	}

	public static AlphaSharpAbstraction<XEventClass> createAlphaSharpAbstraction(TraceVariantRep variants) {
		return new AlphaSharpAbstractionImpl<>(createAlphaPlusAbstraction(variants));
	}
	
	// NEW FOR ROBUST
	public static AlphaRobustAbstraction<XEventClass> createAlphaRobustAbstraction(TraceVariantRep variants, AlphaRobustMinerParameters parameters) {
        HashMap<String, Integer> events = new HashMap<String, Integer>();
		
		int nEvents = variants.getNumEvents();
		int j = 0;
		for (String a: variants.getEvents()) {
			events.put(a, j);
			j++;
		}
		
		double[][] dfa = new double[nEvents][nEvents]; // directly follows (count)
		double[] starts = new double[nEvents]; // start activity
		double[] ends = new double[nEvents]; // end activity
		double[] lol = new double[nEvents]; // length one loop
		double[] ac = new double[nEvents]; // activity count
		
		
		for (TraceVariant trace : variants.getVariants()) {
			int freq = trace.getFrequency();
			ArrayList<String> activities = trace.getActivities();
			starts[events.get(activities.get(0))] += (freq * StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN);
			ends[events.get(activities.get(activities.size()-1))] += (freq * StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN);
			for (int i = 0; i < activities.size() - 1; i++) {
				String from = activities.get(i);
				String to = activities.get(i + 1);
				dfa[events.get(from)][events.get(to)] += (freq * DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN);
				if (from.equals(to)) {
					lol[events.get(from)] = LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
				}
				ac[events.get(from)] += (freq * DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN);
				if (i == activities.size() - 2) { // count final activity as well
					ac[events.get(to)] += (freq * DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN);
				}
			}
			
		}

		XEventClass[] arr = createEventClasses(events);
		return new AlphaRobustAbstractionImpl<>(arr,
				DirectlyFollowsAbstractionFactory.constructDirectlyFollowsAbstraction(arr, dfa,
						DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructStartActivityAbstraction(arr, starts,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructEndActivityAbstraction(arr, ends,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				LoopAbstractionFactory.constructLengthOneLoopAbstraction(arr, lol,
						LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				ActivityCountAbstractionFactory.constructActivityCountAbstraction(arr, ac,
						ActivityCountAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				parameters.getCausalThreshold(),
				parameters.getNoiseThresholdLeastFreq(),
				parameters.getNoiseThresholdMostFreq());
	}
	
	public static final double BOOLEAN_DEFAULT_THRESHOLD = 1.0;


	public static strictfp double[][] constructAlphaPlusPlusLengthOneLoopFreeLongTermFollowsMatrix(TraceVariantRep variants, HashMap<String, Integer> events, DirectlyFollowsAbstraction<XEventClass> dfa,
			CausalPrecedenceAbstraction<XEventClass> cpa, CausalSuccessionAbstraction<XEventClass> csa,
			LengthOneLoopAbstraction<XEventClass> lola) {
		Pair<XEventClass[], int[]> l1lClasses = XEventClassUtils.stripLengthOneLoops(lola.getEventClasses(), lola);
		double[][] matrix = new double[l1lClasses.getFirst().length][l1lClasses.getFirst().length];
		if (!(dfa.getEventClasses().length == cpa.getEventClasses().length
				&& cpa.getEventClasses().length == csa.getEventClasses().length
				&& csa.getEventClasses().length == l1lClasses.getFirst().length)) {
			return matrix;
		} else {

			for (TraceVariant trace : variants.getVariants()) {
				ArrayList<String> activities = trace.getActivities();
				for (int i = 0; i < activities.size() - 1; i++) {
					int a = events.get(activities.get(i));
					if (!lola.holds(a)) {
						for (int j = i + 1; j < activities.size(); j++) {
							int b = events.get(activities.get(j));
							if (!lola.holds(b)) {
								if (!dfa.holds(l1lClasses.getSecond()[a],
										l1lClasses.getSecond()[b])) {
									boolean ltf = true;
									for (int k = i + 1; k <= j - 1; k++) {
										int c = events.get(activities.get(k));
										if (!lola.holds(c)) {
											if (c==a || c==b
													|| cpa.getValue(l1lClasses.getSecond()[c],
															l1lClasses.getSecond()[a]) >= cpa.getThreshold()
													|| csa.getValue(l1lClasses.getSecond()[c],
															l1lClasses.getSecond()[a]) >= csa
																	.getThreshold()) {
												ltf = false;
												break;
											}
										}
									}
									if (ltf) {
										matrix[l1lClasses.getSecond()[a]][l1lClasses.getSecond()[b]] = BOOLEAN_DEFAULT_THRESHOLD;
									}
								}
							}
						}
					}
				}			
			}
			return matrix;
		}
	}
	

	public static strictfp LongTermFollowsAbstraction<XEventClass> constructAlphaPlusPlusLengthOneLoopFreeLongTermFollowsAbstraction(
			TraceVariantRep variants, DirectlyFollowsAbstraction<XEventClass> dfa,
			CausalPrecedenceAbstraction<XEventClass> cpa, CausalSuccessionAbstraction<XEventClass> csa,
			LengthOneLoopAbstraction<XEventClass> lola) {
		HashMap<String, Integer> events = new HashMap<String, Integer>();
		int j = 0;
		for (String a: variants.getEvents()) {
			events.put(a, j);
		    j++;
		}
		return new LongTermFollowsAbstractionImpl<>(dfa.getEventClasses(),
				constructAlphaPlusPlusLengthOneLoopFreeLongTermFollowsMatrix(variants, events, dfa, cpa, csa, lola),
				BOOLEAN_DEFAULT_THRESHOLD);
	}

}
