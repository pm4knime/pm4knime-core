package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import gnu.trove.iterator.TShortIterator;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.tue.astar.util.ShortShortMultiset;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.manifestanalysis.visualization.performance.TimeFormatter;


/**
 * Class that calculates performance values
 * 
 * @author aadrians Jan 27, 2012
 * 
 */
public class PerfCounterTable {
	public static short THROUGHPUTTIME = 0;
	public static short WAITINGTIME = 1;
	public static short SOJOURNTIME = 2;
	public static short MULTIPLIER = 16;
	/**
	 * Mapping the visualization petri net
	 */

	protected Transition[] idx2Trans;
	private TObjectIntMap<Transition> trans2Idx;

	private Place[] idx2Place;
	private TObjectIntMap<Place> place2Idx;

	private String[] idx2Resource;
	private TObjectIntMap<String> resource2Idx;

	/**
	 * Case related information all time unit in milliseconds
	 */
	private int caseTotalFreq = 0;
	private double caseThroughputMin = Double.NaN;
	private double caseThroughputMax = Double.NaN;
	private double caseThroughputMVal = Double.NaN;
	private double caseThroughputSVal = Double.NaN;
	private long[] caseThroughputAllVals;

	// case fitness
	private int caseNonFittingFreq = 0;
	private int caseProperlyStartedFreq = 0;
	private double caseFitnessMax = -1;
	private double caseFitnessMin = -1;
	private double caseFitnessMVal = -1;
	private double caseFitnessSVal = -1;

	// binded class
	@SuppressWarnings("rawtypes")
	private Class bindedClass;

	// time attribute
	private String timeAtt;

	// the first and last timestamp for move synchronously
	private Long firstSync = null;
	private Long lastSync = null;

	/**
	 * Sequentially for each pattern, store the following info: 1. throughput
	 * time min | max | mval | sval 2. waiting time min | max | mval | sval 3.
	 * sojourn time min | max | mval | sval 4. period (the first and the last
	 * occurrence of the task in all cases)
	 */
	protected double[] patternInfoDouble;

	/**
	 * Store total frequency where patterns occur | total unique case where
	 * patterns occur
	 */
	protected int[] patternInfoInt;

	/**
	 * Store move model only frequency | number of unique case
	 */
	private int[] moveModelOnly;

	/**
	 * Place related information sequentially for each place, store the
	 * following info 1. waiting time min | max | mval | sval 2. sync time min |
	 * max | mval | sval 3. sojourn time min | max | mval | sval
	 */
	private double[] placeInfoDouble;
	protected static final int PLACEWAITING = 0;
	protected static final int PLACESYNCHRONIZATION = 1;
	protected static final int PLACESOJOURN = 2;

	/**
	 * store frequency of each place: waiting time, sync time, sojourn time.
	 * Example: sync time frequency for place id 2 is stored in placeInfoInt[(3
	 * * id_place) + SYNCHRONIZATION] Note that we don't calculate the number of
	 * unique cases when the place occur
	 * 
	 */
	private int[] placeInfoInt;

	/**
	 * for each pair of transition (from-->to), record the following information
	 * [ index where the from is stored | block 1 | block 2 | ...] each block
	 * contains [ to | min | max | mval | sval | freq ]
	 */

	// utility: encode the net
	protected TIntObjectHashMap<short[]> encodedTrans2Pred;
	protected TIntObjectHashMap<short[]> encodedTrans2Succ;
	protected Map<short[], ShortShortMultiset> marking2LogMoveCounter;

	// utility: counter of manifest
	protected TIntIntMap manifestCount;
	protected TIntObjectMap<ManifestTimeInfo> manifestLimit;

	// utility: reference to log
//	protected XLogInfo logInfo;

	public void init(ManifestEvClassPatternTable manifest, String timeAtt, Class<?> c, boolean[] caseFilter) {
		this.timeAtt = timeAtt;
		this.bindedClass = c;

		// depends on the selected time attribute, calculate all performance
		// initialize encoding of newNet and resource classifier
//		/String evClassifier = manifest.getTransClass2PatternMap().getEvClassifier();
//		logInfo = manifest.getLog().getInfo()
		initEncoding(manifest);
        
		// initiate manifest counter
		manifestCount = new TIntIntHashMap(6, 0.5f, -1, -1);
		manifestLimit = new TIntObjectHashMap<ManifestTimeInfo>(6);

		calculatePerformance(manifest, timeAtt, bindedClass, caseFilter);
	}

	public void calculatePerformance(ManifestEvClassPatternTable manifest, boolean[] caseFilter) {
		// use the previously defined time att
		calculatePerformance(manifest, timeAtt, bindedClass, caseFilter);
	}

	public void resetAllStats() {
		caseTotalFreq = 0;
		caseThroughputMin = Double.NaN;
		caseThroughputMax = Double.NaN;
		caseThroughputMVal = Double.NaN;
		caseThroughputSVal = Double.NaN;
		caseThroughputAllVals = null;

		// case fitness
		caseNonFittingFreq = 0;
		caseProperlyStartedFreq = 0;
		caseFitnessMax = -1;
		caseFitnessMin = -1;
		caseFitnessMVal = -1;
		caseFitnessSVal = -1;

		// the first and last timestamp for move synchronously
		firstSync = null;
		lastSync = null;

		patternInfoDouble = null;
		patternInfoInt = null;
		moveModelOnly = null;
	}

	public void calculatePerformance(ManifestEvClassPatternTable manifest, String timeAtt, Class<?> bindedClass,
			boolean[] caseFilter) {
		// reset all stats
		resetAllStats();

		// utilities
		TransClass2PatternMapTable transClass2PatternMap = manifest.getTransClass2PatternMap();

//		XEventClasses eventClasses = logInfo.getEventClasses();
		TIntObjectMap<List<Long>> timedPlaces = new TIntObjectHashMap<List<Long>>(this.idx2Place.length);

		// initiate performance-related calculation
		patternInfoDouble = new double[transClass2PatternMap.getNumPatterns() * MULTIPLIER];
		patternInfoInt = new int[transClass2PatternMap.getNumPatterns() * 2];
		Arrays.fill(patternInfoDouble, Double.NaN);
		Arrays.fill(patternInfoInt, 0);

		placeInfoDouble = new double[idx2Place.length * 12];
		placeInfoInt = new int[idx2Place.length * 3];
		Arrays.fill(placeInfoDouble, Double.NaN);
		Arrays.fill(placeInfoInt, 0);

		moveModelOnly = new int[2 * idx2Trans.length];
		Arrays.fill(moveModelOnly, 0);

		// temporary performance-related variable
		boolean[] patternInvolved = new boolean[transClass2PatternMap.getNumPatterns()];
		boolean[] transitionMoveModel = new boolean[idx2Trans.length];
		Arrays.fill(transitionMoveModel, false);

		// performance calculation
		int[] cases = manifest.getCasePointers();
		caseThroughputAllVals = new long[cases.length];
		Arrays.fill(caseThroughputAllVals, 0L);
		for (int i = 0; i < cases.length; i++) {
			if ((cases[i] >= 0) && (caseFilter[i])) {
				// clear timed place
				timedPlaces.clear();

				// create initial marking
				short[] marking = constructEncInitMarking(manifest.getInitMarking());
				initTimedPlaces(timedPlaces, marking);

				// create trace iterator
				Iterator<String> it = manifest.getLog().getTraces().get(i).iterator();
				Iterator<String> timeStamps_it = manifest.getLog().getTimeTraces().get(i).iterator();
				

				// initialize performance-related calculation
				Arrays.fill(patternInvolved, false);

				// update case fitness calculations
				caseTotalFreq++;
				double caseFitness = manifest.getTraceFitness(i);
				if (this.caseFitnessMax < 0) {
					this.caseFitnessMax = caseFitness;
					this.caseFitnessMin = caseFitness;
					this.caseFitnessSVal = 0;
					this.caseFitnessMVal = caseFitness;
				} else {
					if (Double.compare(caseFitnessMax, caseFitness) < 0) {
						caseFitnessMax = caseFitness;
					}
					if (Double.compare(caseFitnessMin, caseFitness) > 0) {
						caseFitnessMin = caseFitness;
					}
					double oldMVal = this.caseFitnessMVal;
					this.caseFitnessMVal += ((caseFitness - oldMVal) / caseTotalFreq);
					this.caseFitnessSVal += ((caseFitness - oldMVal) * (caseFitness - this.caseFitnessMVal));
				}

				if (Double.compare(caseFitness, 1.00) < 0) {
					this.caseNonFittingFreq++;
				}

				// now, iterate through all manifests for the case
				int[] man = manifest.getManifestForCase(i);

				// temporary variables needed for a case
				Long beginTime = null;
				Long endTime = null;

				int currIdx = 0;
				while (currIdx < man.length) {
					if (man[currIdx] == ManifestTable.MOVELOG) {
						// shared variable
						String currEvent = it.next();
						String currTime = timeStamps_it.next();

						// record log move on current marking
						ShortShortMultiset violationCounter = marking2LogMoveCounter.get(marking);
						if (violationCounter == null) {
							violationCounter = new ShortShortMultiset(
									(short) transClass2PatternMap.getEvClassEnc().length);
							marking2LogMoveCounter.put(marking, violationCounter);
						}
						violationCounter.adjustValue(
								transClass2PatternMap.getEvClassEncFor(currEvent), (short) 1);

						currIdx++;
					} else if (man[currIdx] == ManifestTable.MOVEMODEL) {
						// update marking
						updateMarkingMoveModel(timedPlaces, marking, man[currIdx + 1]);

						// update move model stats
						moveModelOnly[2 * man[currIdx + 1]]++;
						transitionMoveModel[man[currIdx + 1]] = true;

						currIdx += 2;
					} else if (man[currIdx] == ManifestTable.MOVESYNC) {
						// shared variable
						String currEvent = it.next();
						String currTime = timeStamps_it.next();
						

						// if it is the first activity, the case started perfectly
						if (currIdx == 0) {
							this.caseProperlyStartedFreq++;
						}

						// extract time information 
						//String currEventTime = extractTimestamp(currEvent, timeAtt, bindedClass);

						long currEventTime = extractTimestamp(currTime);
						if (beginTime == null) {
							beginTime = currEventTime; // begin time = the moment the case start
						}
						endTime = currEventTime; // end time = the moment the case finishes

						// use the time info to calculate period of pattern
						// check if there is already a manifest with the same id
						int manifestID = man[currIdx + 1];
						int count = manifestCount.get(manifestID);
						if (count < 0) {
							// increase frequency of the manifest
							incPatternFreq(manifest.getPatternIDOfManifest(manifestID));

							// first event of a manifest, take token
							Long lastTokenTakenTime = takeTokens(timedPlaces, marking,
									manifest.getEncTransOfManifest(manifestID), currEventTime);

							// update waiting time for the manifest
							updateManifestWaitingTime(lastTokenTakenTime, currEventTime,
									manifest.getPatternIDOfManifest(manifestID));

							// check how many expected events for this manifest
							int numEvents = transClass2PatternMap.getPatternIDNumElmts(manifest
									.getPatternIDOfManifest(manifestID));
							if (numEvents > 1) {
								// still await for other events (number of events needed to complete is > 1, and now
								// we're seeing the first event)
								manifestCount.put(manifestID, 1);
								manifestLimit.put(manifestID, new ManifestTimeInfo(numEvents, currEventTime,
										lastTokenTakenTime));
							} else {
								// close the manifest and put it on pattern calculation
								updateManifestThroughputTime(currEventTime, currEventTime,
										manifest.getPatternIDOfManifest(manifestID));
								updateManifestSojournTime(lastTokenTakenTime, currEventTime,
										manifest.getPatternIDOfManifest(manifestID));
								produceTokens(timedPlaces, marking, manifest.getEncTransOfManifest(manifestID),
										currEventTime);
							}

							// update involvement in a case
							patternInvolved[manifest.getPatternIDOfManifest(manifestID)] = true;
						} else {
							// there is already existing manifest
							count++;
							ManifestTimeInfo manifestTimeLimit = manifestLimit.get(manifestID);
							if (count < manifestTimeLimit.getLimit()) {
								// increase counter
								manifestCount.put(manifestID, count);
							} else {
								// update performance
								updateManifestThroughputTime(manifestTimeLimit.getFiringTime(), currEventTime,
										manifest.getPatternIDOfManifest(manifestID));

								updateManifestSojournTime(manifestTimeLimit.getLastTokenTakenTime(), currEventTime,
										manifest.getPatternIDOfManifest(manifestID));

								// remove counter and limit
								manifestCount.remove(manifestID);
								manifestLimit.remove(manifestID);

								// update marking
								produceTokens(timedPlaces, marking, manifest.getEncTransOfManifest(manifestID),
										currEventTime);
							}
						}

						currIdx += 2;
					}
				}

				// update case involvement of pattern
				for (int j = 0; j < patternInvolved.length; j++) {
					if (patternInvolved[j]) {
						incPatternCaseInvolvement(j);
					}
				}

				// update case involvement of transition
				for (int j = 0; j < transitionMoveModel.length; j++) {
					if (transitionMoveModel[j]) {
						moveModelOnly[(2 * j) + 1]++;
					}
				}
				Arrays.fill(transitionMoveModel, false);

				// now calculate throughput stats for a case
				if ((beginTime != null) && (endTime != null)) {
					// check first sync
					if (firstSync == null) {
						firstSync = beginTime;
					} else {
						if (firstSync.compareTo(beginTime) > 0) {
							firstSync = beginTime;
						}
					}
					// check last sync
					if (lastSync == null) {
						lastSync = endTime;
					} else {
						if (endTime.compareTo(lastSync) > 0) {
							lastSync = endTime;
						}
					}

					long period = endTime - beginTime;
					if (period < 0) {
						System.out.println("Case " + manifest.getLog().getTraces().get(i)
								+ " has negative period ("
								+ TimeFormatter.formatTime(period, NumberFormat.getInstance()) + ")");
					}
					if (Double.isNaN(caseThroughputMax)) {
						// the first period
						caseThroughputMax = period;
						caseThroughputMin = period;
						caseThroughputMVal = period;
						caseThroughputSVal = 0.0000;
					} else {
						// there has been calculation before
						if (caseThroughputMax < period) {
							caseThroughputMax = period;
						}
						if (caseThroughputMin > period) {
							caseThroughputMin = period;
						}
						double oldM = caseThroughputMVal;
						caseThroughputMVal += (period - oldM) / caseTotalFreq;
						caseThroughputSVal += ((period - oldM) * (period - caseThroughputMVal));
					}
					caseThroughputAllVals[i] = period;
				} else {
					// no events are synchronous
					if (Double.isNaN(caseThroughputMax)) {
						// the first period
						caseThroughputMax = 0;
						caseThroughputMin = 0;
						caseThroughputMVal = 0;
						caseThroughputSVal = 0.0000;
					} else {
						// there has been calculation before
						if (caseThroughputMin > 0) {
							caseThroughputMin = 0;
						}
						double oldM = caseThroughputMVal;
						caseThroughputMVal += (0 - oldM) / caseTotalFreq;
						caseThroughputSVal += ((0 - oldM) * (0 - caseThroughputMVal));
					}
				}
			}
		}
	}

	/**
	 * Calculate throughput time of manifest.
	 * 
	 * @param firingTime
	 * @param currEventTime
	 * @param patternIDOfManifest
	 */
	protected void updateManifestThroughputTime(long firingTime, long currEventTime, int patternIDOfManifest) {
		updatePatternPerformance(THROUGHPUTTIME, patternIDOfManifest, currEventTime - firingTime);
	}

	/**
	 * Calculate sojourn time of manifest, assuming that the lastTokenTakenTime
	 * is NOT null, because if it is null, it has been replaced with firingTime.
	 * 
	 * @param lastTokenTakenTime
	 * @param firingTime
	 * @param patternIDOfManifest
	 */
	protected void updateManifestSojournTime(Long lastTokenTakenTime, long firingTime, int patternIDOfManifest) {
		updatePatternPerformance(SOJOURNTIME, patternIDOfManifest, firingTime - lastTokenTakenTime);
	}

	/**
	 * Calculate waiting time of a pattern manifest, assume the
	 * lastTokenTakenTime is the same as firingTime if it was null. Thus, by
	 * default, if last token time is null, waiting time is 0
	 * 
	 * @param lastTokenTakenTime
	 * @param firingTime
	 * @param patternIDOfManifest
	 */
	protected void updateManifestWaitingTime(Long lastTokenTakenTime, long firingTime, int patternIDOfManifest) {
		if (lastTokenTakenTime == null) {
			// the taken token do not have timestamps/the token is produced by move models 
			lastTokenTakenTime = firingTime;
		}
		updatePatternPerformance(WAITINGTIME, patternIDOfManifest, firingTime - lastTokenTakenTime);
	}

	/**
	 * This method extract timestamp or numerical attribute values, assuming
	 * that the values exists for all events.
	 * 
	 * @param currEvent
	 * @return
	 */
	protected long extractTimestamp(String timestamp) {
//		if (bindedClass.equals(ZonedDateTimeCellFactory.class)) {
	//		return ZonedDateTimeCellFactory (String)
		//} else if (bindedClass.equals(LocalDateTimeCellFactory.class)) {
//			return ((XAttributeDiscrete) currEvent.getAttributes().get(timeAtt)).getValue();
//		} else if (bindedClass.equals(Double.class)) {
//			return (long) ((XAttributeContinuous) currEvent.getAttributes().get(timeAtt)).getValue();
//		}
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(timestamp);
		return zonedDateTime.toInstant().toEpochMilli();
		//return Timestamp.valueOf(timestamp).getTime();//con(currEvent)).getValue().getTime();

		//throw new IllegalArgumentException("Only date, double, and integer datatype are supported");
	}

	protected void initTimedPlaces(final TIntObjectMap<List<Long>> timedPlaces, short[] marking) {
		// decrease the value
		for (int place=0; place < marking.length; place++){
			List<Long> list = new LinkedList<Long>();
			for (int i = 0; i < marking[place]; i++) {
				list.add(null);
			}
			timedPlaces.put(place, list);
		}
	}

	protected void incPatternCaseInvolvement(int patternID) {
		patternInfoInt[(patternID * 2) + 1]++;
	}

	/**
	 * Wrapper class for pattern performance
	 * @param performanceType
	 * @param patternID
	 * @param timePeriod
	 * @see updatePatternPerformance method(int performanceType, int patternID, long timePeriod, int oldFrequency)
	 */
	protected void updatePatternPerformance(int performanceType, int patternID, long timePeriod) {
		updatePatternPerformance(performanceType, patternID, timePeriod, -1);
	}
	
	/**
	 * Update the minimum, maximum, mval, and sval value of a pattern. Remark:
	 * frequency is not updated here.
	 * 
	 * frequencyAfterIncrease is required by waiting and sojourn time, because the frequency of the two 
	 * values may not be the same as the frequency of the manifest
	 * 
	 * @param performanceType
	 * @param patternID
	 * @param timePeriod
	 * @param frequencyAfterIncrease if >= 1 then it is used instead of the frequency from patternInfoInt[patternID * 2]
	 * @see incPatternFreq method
	 */
	protected void updatePatternPerformance(int performanceType, int patternID, long timePeriod, int frequencyAfterIncrease) {
		// update the stats for patternID
		int index = (patternID * MULTIPLIER) + (performanceType * 4);
		if (Double.isNaN(patternInfoDouble[index])) {
			// the pattern is new, because minimum value is 0
			patternInfoDouble[index] = timePeriod; //minimum value
			patternInfoDouble[index + 1] = timePeriod; // maximum value
			patternInfoDouble[index + 2] = timePeriod; // mval
			patternInfoDouble[index + 3] = 0; // sval
		} else {
			// update stats about the pattern
			// minimal value
			if (patternInfoDouble[index] > timePeriod) {
				patternInfoDouble[index] = timePeriod;
			}
			// max value
			if (patternInfoDouble[index + 1] < timePeriod) {
				patternInfoDouble[index + 1] = timePeriod;
			}

			// average and svalue
			double oldMVal = patternInfoDouble[index + 2];
			
			if (frequencyAfterIncrease >= 1){
				patternInfoDouble[index + 2] += (timePeriod - oldMVal) / frequencyAfterIncrease;
			} else {
				patternInfoDouble[index + 2] += (timePeriod - oldMVal) / patternInfoInt[patternID * 2];
			}
			patternInfoDouble[index + 3] += ((timePeriod - oldMVal) * (timePeriod - patternInfoDouble[index + 2]));
		}
	}

	/**
	 * Increse pattern frequency. This value is equal to pattern throughput time
	 * frequency. Not necessarily the same as the frequency of pattern waiting
	 * time/sojourn time.
	 * 
	 * @param patternID
	 */
	protected void incPatternFreq(int patternID) {
		patternInfoInt[patternID * 2]++;
	}

	/**
	 * This firing is only used for move model only move on model means firing
	 * transitions as soon as they are enabled
	 * 
	 * @param timedPlaces
	 * @param marking
	 * @param encTrans
	 * @param time
	 */
	protected void updateMarkingMoveModel(final TIntObjectMap<List<Long>> timedPlaces, final short[] marking,
			int encTrans) {
		final List<Long> oldDate = new ArrayList<Long>(1);
		oldDate.add(null);

		short[] pred = encodedTrans2Pred.get(encTrans);
		if (pred != null) {
			// decrease the value
			for (int place = 0; place < pred.length; place++) {
				if (pred[place] != 0) {
					int needed = 0;
					if (pred[place] > 0) {
						marking[place] -= pred[place];
						needed = pred[place];
					} else if (pred[place] < 0) {
						marking[place] = 0;
						needed = -pred[place] + 1;
					}
					// get predecessor with the latest timestamp (if possible)
					List<Long> listTime = timedPlaces.get(place);
					for (int i = 0; i < needed; i++) {
						Long removedDate = listTime.get(0);
						if (removedDate != null) {
							// there is a chance waiting time can be calculated
							Long comparison = oldDate.iterator().next();
							if (comparison != null) {
								if (removedDate > comparison) {
									oldDate.clear();
									oldDate.add(removedDate);
								}
							} else {
								// oldDate null, but not the removed date
								oldDate.clear();
								oldDate.add(removedDate);
							}
						}
					}
				}
			}

			/** ASSUMING THAT MOVE MODEL IMMEDIATELY MOVING TOKENS **/
			// in the second iteration, update waiting time for places that has token
			final Long maxSyncTime = oldDate.iterator().next();
			for (int place =0; place < pred.length; place++){
				int needed = 0;
				if (pred[place] > 0) {
					needed = pred[place];
				} else if (pred[place] < 0) {
					needed = -pred[place] + 1;
				}
				List<Long> listTime = timedPlaces.get(place);
				
				for (int i = 0; i < needed; i++) {
					Long tokenTime = listTime.remove(0);
					if (tokenTime != null) {
						// update waiting time, synchronization, and sojourn time
						updatePlaceTimeAll(place, tokenTime, maxSyncTime, maxSyncTime);
					} else {
						// 0 waiting time, synchronization, and sojourn time
						updatePlaceTimeAll(place, 0L, 0L, 0L);
					}
				}
			}
		}

		// produce tokens and timestamp
		produceTokens(timedPlaces, marking, encTrans, oldDate.iterator().next());
	}

	/**
	 * Take tokens, return the most recent date of taken tokens (if exists), or
	 * the takenDate if there is no most recent date. Only call this method when
	 * a token is taken by sync move (i.e. takenDate != null).
	 * 
	 * @param timedPlaces
	 * @param marking
	 * @param encTrans
	 * @param tokenTakeTime
	 * @return
	 */
	protected Long takeTokens(final TIntObjectMap<List<Long>> timedPlaces, final short[] marking,
			final int encTrans, final long takenDate) {
		final List<Long> latestDate = new ArrayList<Long>(1);
		latestDate.add(null);

		short[] pred = encodedTrans2Pred.get(encTrans);
		if (pred != null) {
			// decrease the value
			for (int place = 0; place < pred.length; place++) {
				if (pred[place] != 0) {
					int needed = 0;
					if (pred[place] > 0) {
						marking[place] -= pred[place];
						needed = pred[place];
					} else if (pred[place] < 0) {
						marking[place] = 0;
						needed = -pred[place] - 1;
					}
					// get predecessor with the latest timestamp (if possible)
					List<Long> listTime = timedPlaces.get(place);
					for (int i = 0; i < needed; i++) {
						Long getDate = listTime.get(i);

						if (getDate != null) {
							// there is a chance waiting time can be calculated
							Long comparison = latestDate.iterator().next();
							if (comparison != null) {
								if (getDate > comparison) {
									latestDate.clear();
									latestDate.add(getDate);
								}
							} else {
								// oldDate null, but not the removed date
								latestDate.clear();
								latestDate.add(getDate);
							}
						}
					}
				}
			}
			
			// in the second iteration, update waiting time for places that has token
			final Long maxSyncDate;
			if (latestDate.iterator().next() == null) {
				maxSyncDate = takenDate;
			} else {
				maxSyncDate = latestDate.iterator().next();
			}

			for (int place = 0; place < pred.length; place++) {
				if (pred[place] != 0) {
					int needed = 0;
					if (pred[place] > 0) {
						marking[place] -= pred[place];
						needed = pred[place];
					} else if (pred[place] < 0) {
						marking[place] = 0;
						needed = -pred[place] - 1;
					}
					List<Long> listTime = timedPlaces.get(place);
					for (int i = 0; i < needed; i++) {
						Long tokenDate = listTime.remove(0);

						// update synchronization, sojourn, and waiting time of places
						updatePlaceTimeAll(place, tokenDate == null ? maxSyncDate : tokenDate, maxSyncDate, takenDate);
					}
				}
			}
			return maxSyncDate;
		}
		return null;
	}

	/**
	 * Return the frequency of waiting time of pattern patternIDOfManifest
	 * 
	 * @param patternIDOfManifest
	 * @return
	 */
	public int getFreqPatternWaitingTime(int patternIDOfManifest) {
		return patternInfoInt[2 * patternIDOfManifest];
	}

	/**
	 * Return the frequency of sojourn time of pattern patternIDOfManifest
	 * 
	 * @param patternIDOfManifest
	 * @return
	 */
	public int getFreqPatternSojournTime(int patternIDOfManifest) {
		return patternInfoInt[2 * patternIDOfManifest];
	}

	/**
	 * Update stats for place
	 * 
	 * @param place
	 *            place to be updated
	 * @param tokenTime
	 *            the timestamp of token coming to the place
	 * @param maxSyncTime
	 *            maximum timestamp of tokens that are synchronized with the
	 *            place
	 * @param takenTime
	 *            the timestamp where transition takes the token from the place.
	 *            Must not be null
	 */
	protected void updatePlaceTimeAll(int place, Long tokenTime, Long maxSyncTime, Long takenTime) {
		/**
		 * waiting time: the time that passes from the (full) enabling of a
		 * transition until its firing, i.e. time that a token spends in the
		 * place waiting for a transition (to which the place is an input place)
		 * to fire and consume the token.
		 */
		updatePlaceWaitingTime(place, maxSyncTime, takenTime);
		/**
		 * synchronization time: the time that passes from the partial enabling
		 * of a transition (i.e. at least one input place marked) until full
		 * enabling (i.e. all input places are marked). Time that a token spends
		 * in a place, waiting for the transition (to which this place is an
		 * input place) to be fully enabled.
		 */
		updatePlaceSyncTime(place, tokenTime, maxSyncTime);

		/**
		 * sojourn time: the total time a token spends in a place during a visit
		 * (Waiting time + Synchronization time).
		 */
		updatePlaceSojournTime(place, tokenTime, takenTime);
	}

	/**
	 * Update place waiting time without updating frequency
	 * 
	 * @param place
	 * @param maxSyncTime
	 * @param takenTime
	 */
	protected void updatePlaceWaitingTime(int place, Long maxSyncTime, Long takenTime) {
		if ((maxSyncTime != null) && (takenTime != null)) {
			updatePlaceTime(PerfCounterTable.PLACEWAITING, place, takenTime - maxSyncTime);
		}
	}

	/**
	 * Update place sojourn time without updating frequency
	 * 
	 * @param place
	 * @param tokenTime
	 * @param takenTime
	 */
	protected void updatePlaceSojournTime(int place, Long tokenTime, Long takenTime) {
		if ((tokenTime != null) && (takenTime != null)) {
			updatePlaceTime(PerfCounterTable.PLACESOJOURN, place, takenTime - tokenTime);
		}
	}

	/**
	 * Update place sync time without updating frequency
	 * 
	 * @param place
	 * @param tokenTime
	 * @param maxSyncTime
	 */
	protected void updatePlaceSyncTime(int place, Long tokenTime, Long maxSyncTime) {
		if ((tokenTime != null) && (maxSyncTime != null)) {
			updatePlaceTime(PerfCounterTable.PLACESYNCHRONIZATION, place, maxSyncTime - tokenTime);
		}
	}

	/**
	 * Low level code to update statistics of a place (waiting/sync/sojourn) but
	 * NOT the frequency of the place
	 * 
	 * @param updateType
	 * @param place
	 * @param time
	 */
	protected void updatePlaceTime(int updateType, int place, long time) {
		int i = (12 * place) + (4 * updateType);

		if (Double.isNaN(placeInfoDouble[i])) {
			placeInfoInt[(3 * place) + updateType] = 1;

			placeInfoDouble[i] = time; // min
			placeInfoDouble[i + 1] = time; // max
			placeInfoDouble[i + 2] = time; // mvalue
			placeInfoDouble[i + 3] = 0; // svalue

		} else {
			placeInfoInt[(3 * place) + updateType]++;

			if (Double.compare(placeInfoDouble[i], time) > 0) {
				placeInfoDouble[i] = time;
			}

			if (Double.compare(placeInfoDouble[i + 1], time) < 0) {
				placeInfoDouble[i + 1] = time;
			}

			double oldMVal = placeInfoDouble[i + 2];
			placeInfoDouble[i + 2] += (time - oldMVal) / placeInfoInt[(3 * place) + updateType]; // mvalue
			placeInfoDouble[i + 3] += ((time - oldMVal) * (time - placeInfoDouble[i + 2])); // svalue

		}
	}

	/**
	 * Produce new tokens with time the same as the moment manifest fires. Only
	 * call. FiringTime can be null if previously there are only move models
	 * 
	 * @param timedPlaces
	 * @param marking
	 * @param encTrans
	 * @param firingTime
	 */
	protected void produceTokens(final TIntObjectMap<List<Long>> timedPlaces, final short[] marking,
			int encTrans, final Long firingTime) {
		short[] succ = encodedTrans2Succ.get(encTrans);
		if (succ != null) {
			// increase the value
			for (int place=0; place < succ.length; place++){
				marking[place] += succ[place];
				
				// add time to tokens
				List<Long> list = timedPlaces.get(place);
				if (list == null) {
					list = new LinkedList<Long>();
					timedPlaces.put(place, list);
				}
				for (int i = 0; i < succ[place]; i++) {
					list.add(firingTime);
				}
			}
		}
	}

	protected short[] constructEncInitMarking(Marking mNewNet) {
		short[] res = new short[idx2Place.length];
		for (Place p : mNewNet.baseSet()) {
			res[place2Idx.get(p)] = mNewNet.occurrences(p).shortValue();
		}
		return res;
	}

	/**
	 * @param net
	 * @param log
	 */
	protected void initEncoding(ManifestTable manifest) {
		// init transitions
		PetrinetGraph net = manifest.getNet();
		List<Transition> transitions = new ArrayList<Transition>(net.getTransitions());
		int transSize = transitions.size();
		idx2Trans = transitions.toArray(new Transition[transSize]);
		trans2Idx = new TObjectIntHashMap<Transition>(transSize);
		for (int i = 0; i < idx2Trans.length; i++) {
			trans2Idx.put(idx2Trans[i], i);
		}

		// init places
		List<Place> places = new ArrayList<Place>(net.getPlaces());
		int placeSize = places.size();
		idx2Place = places.toArray(new Place[placeSize]);
		place2Idx = new TObjectIntHashMap<Place>(placeSize);
		for (int i = 0; i < idx2Place.length; i++) {
			place2Idx.put(idx2Place[i], i);
		}

		// init input and output place
		encodedTrans2Pred = new TIntObjectHashMap<short[]>(idx2Trans.length);
		encodedTrans2Succ = new TIntObjectHashMap<short[]>(idx2Trans.length);
		for (int i = 0; i < transSize; i++) {

			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inEdges = net
					.getInEdges(idx2Trans[i]);
			if (inEdges != null) {
				short[] newIn = new short[placeSize];
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : inEdges) {
					int sourceEnc = place2Idx.get(edge.getSource());
					if (edge instanceof ResetArc) {
						newIn[sourceEnc] = (short) (-newIn[sourceEnc] - 1);
					} else if (edge instanceof Arc) {
						if (newIn[sourceEnc] < 0) {
							newIn[sourceEnc] = (short) -net.getArc(edge.getSource(), edge.getTarget()).getWeight();
						} else {
							newIn[sourceEnc] = (short) net.getArc(edge.getSource(), edge.getTarget()).getWeight();
						}
					}
				}
				;
				encodedTrans2Pred.put(i, newIn);
			}

			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = net
					.getOutEdges(idx2Trans[i]);
			if (outEdges != null) {
				short[] newOut = new short[placeSize];
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : outEdges) {
					newOut[place2Idx.get(edge.getTarget())] = (short) net.getArc(edge.getSource(), edge.getTarget())
							.getWeight();
				}
				;
				encodedTrans2Succ.put(i, newOut);
			}

		}

		// init counter of move log 
		marking2LogMoveCounter = new HashMap<short[], ShortShortMultiset>();

		// init resources
//		XEventClasses resClasses = manifest.getLog().getActivties()
//				logInfo.getResourceClasses();
//		int resClassSize = resClasses.size();
		idx2Resource = manifest.getLog().getActivties();
		resource2Idx = new TObjectIntHashMap<String>(idx2Resource.length);
		for (int i = 0; i < idx2Resource.length; i++) {
			resource2Idx.put(idx2Resource[i], i);
		}
	}

	/**
	 * increment number of considered cases
	 */
	public void incNumCases() {
		this.caseTotalFreq++;
	}

	/**
	 * increment number of non fitting cases
	 */
	public void incNumNonFittingCases() {
		this.caseNonFittingFreq++;
	}

	/**
	 * @return the caseTotalFreq
	 */
	public int getCaseTotalFreq() {
		return caseTotalFreq;
	}

	/**
	 * @return the caseNonFittingFreq
	 */
	public int getCaseNonFittingFreq() {
		return caseNonFittingFreq;
	}

	/**
	 * @return the caseProperlyStartedFreq
	 */
	public int getCaseProperlyStartedFreq() {
		return caseProperlyStartedFreq;
	}

	/**
	 * @return the caseThroughputMin
	 */
	public double getCaseThroughputMin() {
		return caseThroughputMin;
	}

	/**
	 * @return the caseThroughputMax
	 */
	public double getCaseThroughputMax() {
		return caseThroughputMax;
	}

	/**
	 * @return the caseThroughputAvg
	 */
	public double getCaseThroughputAvg() {
		return caseThroughputMVal;
	}

	/**
	 * @return the caseThroughputStdDev
	 */
	public double getCaseThroughputStdDev() {
		return Math.sqrt(caseThroughputSVal / ((this.caseTotalFreq) - 1));
	}

	/**
	 * @return the caseFitnessMax
	 */
	public double getCaseFitnessMax() {
		return caseFitnessMax;
	}

	/**
	 * @return the caseFitnessMin
	 */
	public double getCaseFitnessMin() {
		return caseFitnessMin;
	}

	public Place[] getIdx2Place() {
		return this.idx2Place;
	}

	public Transition[] getIdx2Trans() {
		return this.idx2Trans;
	}

	public TObjectIntMap<Place> getPlace2Idx() {
		return this.place2Idx;
	}

	public TObjectIntMap<Transition> getTrans2Idx() {
		return this.trans2Idx;
	}

	public String[] getIdx2Resource() {
		return this.idx2Resource;
	}

	public TObjectIntMap<String> getResource2Idx() {
		return this.resource2Idx;
	}

	public double[] getPatternInfoDouble() {
		return this.patternInfoDouble;
	}

	public int[] getPatternInfoInt() {
		return this.patternInfoInt;
	}

	public int[] getMoveModelOnlyCounter() {
		return this.moveModelOnly;
	}

	public double[] getPlaceInfoDouble() {
		return this.placeInfoDouble;
	}

	public int[] getPlaceInfoInt() {
		return this.placeInfoInt;
	}

	public double getCasePeriod() {
		/*
		 * HV: If all alignments are unreliable (for example, if the final marking cannot be reached for some reason),
		 * then there just be no synchronous moves. Deal with that in a best effort way.
		 */
		if (firstSync == null || lastSync == null) {
			return 0.0;
		}
		return lastSync - firstSync;
	}

	public int getNumPlaces() {
		return idx2Place.length;
	}

	public int getNumTrans() {
		return idx2Trans.length;
	}

	public Transition[] getTransArray() {
		return idx2Trans;
	}

	public Place[] getPlaceArray() {
		return idx2Place;
	}

	/**
	 * get stats of places
	 * 
	 * Result:
	 * [0] = min waiting time;
	 * [1] = max waiting time;
	 * [2] = average waiting time;
	 * [3] = std dev waiting time;
	 * [4] = freq waiting time;
	 * [5] = min sync time;
	 * [6] = max sync time;
	 * [7] = average sync time;
	 * [8] = std dev sync time;
	 * [9] = freq sync time;
	 * [10] = min sojourn time;
	 * [11] = max sojourn time;
	 * [12] = average sojourn time;
	 * [13] = std dev sojourn time;
	 * [14] = freq sojourn time;
	 */
	public double[] getPlaceStats(int encodedPlaceID) {
		double[] res = new double[15];
		Arrays.fill(res, 0.0000);

		int i = encodedPlaceID * (4 + 4 + 4);
		int j = 0;
		// waiting time
		res[j++] = this.placeInfoDouble[i++]; // min waiting time [0]
		res[j++] = this.placeInfoDouble[i++]; // max waiting time [1]
		res[j++] = this.placeInfoDouble[i++]; // average waiting time [2]

		if (Double.compare(this.placeInfoDouble[i], 0.0000) == 0) {
			res[j++] = 0; // std dev
			i++;
		} else {
			res[j++] = Math.sqrt(this.placeInfoDouble[i++] / (placeInfoInt[3 * encodedPlaceID] - 1)); // std dev [3]
		}
		res[j++] = placeInfoInt[3 * encodedPlaceID]; // frequency waiting time [4]

		// synchronization time
		res[j++] = this.placeInfoDouble[i++]; // min sync time [5]
		res[j++] = this.placeInfoDouble[i++]; // max sync time [6]
		res[j++] = this.placeInfoDouble[i++]; // average sync time [7]
		if (Double.compare(this.placeInfoDouble[i], 0.0000) == 0) {
			res[j++] = 0; // std dev
			i++;
		} else {
			res[j++] = Math.sqrt(this.placeInfoDouble[i++] / (placeInfoInt[(3 * encodedPlaceID) + 1] - 1)); // std dev [8]
		}
		res[j++] = placeInfoInt[(3 * encodedPlaceID) + 1]; // frequency sync time [9]

		// sojourn time
		res[j++] = this.placeInfoDouble[i++]; // min sojourn time [10]
		res[j++] = this.placeInfoDouble[i++]; // max sojourn time [11]
		res[j++] = this.placeInfoDouble[i++]; // average sojourn time [12]
		if (Double.compare(this.placeInfoDouble[i], 0.0000) == 0) {
			res[j++] = 0; // std dev
			i++;
		} else {
			res[j++] = Math.sqrt(this.placeInfoDouble[i++] / (placeInfoInt[(3 * encodedPlaceID) + 2] - 1)); // std dev [13]
		}
		res[j++] = placeInfoInt[(3 * encodedPlaceID) + 2]; // frequency sojourn time [14]

		return res;
	}

	/**
	 * Get performance information related to a transition. Since a transition
	 * can have more than one pattern, information for each pattern is recorded
	 * Aggregated statistics are also calculated and stored in the first
	 * MULTIPLIER elements. For aggregated ones, no standard
	 * deviation is measured.
	 * 
	 * The structure of the returned values is [0...(IPerCounter-1) = aggregated
	 * stats][.. = info for pattern 1][2*..] and so on
	 * 
	 * NOTE: frequency of throughput time is the same as waiting and sojourn
	 * time.
	 * 
	 * Result:
	 * aggregated:
	 * [0] = min throughput time;
	 * [1] = max throughput time;
	 * [2] = average throughput time;
	 * [3] = std dev throughput time;
	 * [4] = min waiting time;
	 * [5] = max waiting time;
	 * [6] = average waiting time;
	 * [7] = std dev waiting time;
	 * [8] = min sojourn time;
	 * [9] = max sojourn time;
	 * [10] = average sojourn time;
	 * [11] = std dev sojourn time;
	 * [12] = freq throughput time ;
	 * [13] = freq waiting time;
	 * [14] = freq sojourn time;
	 * 
	 * for each pattern
	 * [MULTIPLIER + 0] = pattern ID;
	 * [MULTIPLIER + 1] = min throughput time;
	 * [MULTIPLIER + 2] = max throughput time;
	 * [MULTIPLIER + 3] = average throughput time;
	 * [MULTIPLIER + 4] = std dev throughput time;
	 *
	 * [MULTIPLIER + 5] = min waiting time;
	 * [MULTIPLIER + 6] = max waiting time;
	 * [MULTIPLIER + 7] = average waiting time;
	 * [MULTIPLIER + 8] = std dev waiting time;
	 *
	 * [MULTIPLIER + 9] = min sojourn time;
	 * [MULTIPLIER + 10] = max sojourn time;
	 * [MULTIPLIER + 11] = average sojourn time;
	 * [MULTIPLIER + 12] = std dev sojourn time;
	 *
	 * [MULTIPLIER + 13] = freq throughput time;
	 * [MULTIPLIER + 14] = freq waiting time;
	 * [MULTIPLIER + 15] = freq sojourn time;
	 * [MULTIPLIER + 16] = freq unique case throughput time;
	 * 
	 */
	public double[] getTransStats(ManifestEvClassPatternTable manifest, int encodedTransID) {
		// get all patterns associated with the transition
		TransClass2PatternMapTable map = manifest.getTransClass2PatternMap();
		short[] patterns = map.getPatternsOf(idx2Trans[encodedTransID]);

		if (patterns == null) {
			return null;
		}
		// patterns: [id of patterns][num of event class][event classes]
		TShortList relevantPattern = new TShortArrayList(4);
		int pointer = 0;
		while (pointer < patterns.length) {
			relevantPattern.add(patterns[pointer]);
			pointer += patterns[pointer + 1] + 2;
		}

		double[] res = new double[(relevantPattern.size() * (MULTIPLIER + 1)) + MULTIPLIER];
		Arrays.fill(res, 0.0000);

		TShortIterator it = relevantPattern.iterator();
		int i = MULTIPLIER;
		boolean isFirst = true;
		while (i < res.length) {
			int currPattern = it.next();
			res[i++] = currPattern; // 1. relevant pattern ID (remember a transition can be associated to many patterns)

			double throughputTimeMin = this.patternInfoDouble[(currPattern * MULTIPLIER)
					+ (THROUGHPUTTIME * 4)];
			double throughputTimeMax = this.patternInfoDouble[(currPattern * MULTIPLIER)
					+ (THROUGHPUTTIME * 4) + 1];
			double throughputTimeAvg = this.patternInfoDouble[(currPattern * MULTIPLIER)
					+ (THROUGHPUTTIME * 4) + 2];
			double throughputTimeStdDev = Math.sqrt(this.patternInfoDouble[(currPattern * MULTIPLIER)
					+ (THROUGHPUTTIME * 4) + 3]
					/ (this.patternInfoInt[(currPattern * 2)] - 1));
			res[i++] = throughputTimeMin;
			res[i++] = throughputTimeMax;
			res[i++] = throughputTimeAvg;
			res[i++] = throughputTimeStdDev;

			// waiting time
			double waitingTimeMin = this.patternInfoDouble[(currPattern * MULTIPLIER)
					+ (WAITINGTIME * 4)];
			double waitingTimeMax = this.patternInfoDouble[(currPattern * MULTIPLIER)
					+ (WAITINGTIME * 4) + 1];
			double waitingTimeAvg = this.patternInfoDouble[(currPattern * MULTIPLIER)
					+ (WAITINGTIME * 4) + 2];
//			double waitingTimeStdDev = Math.sqrt(this.patternInfoDouble[(currPattern * MULTIPLIER)
//					+ (WAITINGTIME * 4) + 3]
//					/ (this.patternInfoInt[(currPattern * 2)] - 1));
			double waitingTimeStdDev = Math.sqrt(this.patternInfoDouble[(currPattern * MULTIPLIER)
			                                                            + (WAITINGTIME * 4) + 3]
			                                                            / (getFreqPatternWaitingTime(currPattern) - 1));
			res[i++] = waitingTimeMin;
			res[i++] = waitingTimeMax;
			res[i++] = waitingTimeAvg;
			res[i++] = waitingTimeStdDev;

			// sojourn time
			double sojournTimeMin = this.patternInfoDouble[(currPattern * MULTIPLIER)
					+ (SOJOURNTIME * 4)];
			double sojournTimeMax = this.patternInfoDouble[(currPattern * MULTIPLIER)
					+ (SOJOURNTIME * 4) + 1];
			double sojournTimeAvg = this.patternInfoDouble[(currPattern * MULTIPLIER)
					+ (SOJOURNTIME * 4) + 2];
//			double sojournTimeStdDev = Math.sqrt(this.patternInfoDouble[(currPattern * MULTIPLIER)
//					+ (SOJOURNTIME * 4) + 3]
//					/ (this.patternInfoInt[(currPattern * 2)] - 1));
			double sojournTimeStdDev = Math.sqrt(this.patternInfoDouble[(currPattern * MULTIPLIER)
			                                                            + (SOJOURNTIME * 4) + 3]
			                                                            / (getFreqPatternSojournTime(currPattern) - 1));
			res[i++] = sojournTimeMin;
			res[i++] = sojournTimeMax;
			res[i++] = sojournTimeAvg;
			res[i++] = sojournTimeStdDev;

			// frequency
			int totalFreq = this.patternInfoInt[(currPattern * 2)];
			int totalUniqueTraceFreq = this.patternInfoInt[(currPattern * 2) + 1];
			res[i++] = totalFreq; // total throughput time frequency
			res[i++] = getFreqPatternWaitingTime(currPattern); // total waiting time frequency
			res[i++] = getFreqPatternSojournTime(currPattern); // total sojourn time frequency
			res[i++] = totalUniqueTraceFreq;

			// initiate aggregate
			// NOTE: frequency of throughput time is the same as waiting and sojourn time
			if (isFirst) {
				isFirst = false;
				res[0] = throughputTimeMin;
				res[1] = throughputTimeMax;
				res[2] = throughputTimeAvg * totalFreq;

				res[4] = waitingTimeMin;
				res[5] = waitingTimeMax;
				res[6] = waitingTimeAvg * getFreqPatternWaitingTime(currPattern);

				res[8] = sojournTimeMin;
				res[9] = sojournTimeMax;
				res[10] = sojournTimeAvg * getFreqPatternSojournTime(currPattern);

				res[12] = totalFreq; // freq throughput time
				res[13] = getFreqPatternWaitingTime(currPattern); // freq waiting time
				res[14] = getFreqPatternSojournTime(currPattern); // freq sojourn time
			} else {
				// calculate
				res[0] = Math.min(res[0], throughputTimeMin);
				res[1] = Math.max(res[1], throughputTimeMax);
				res[2] += throughputTimeAvg * totalFreq;

				res[4] = Math.min(res[4], waitingTimeMin);
				res[5] = Math.max(res[5], waitingTimeMax);
				res[6] += waitingTimeAvg * getFreqPatternWaitingTime(currPattern);

				res[8] = Math.min(res[8], sojournTimeMin);
				res[9] = Math.max(res[9], sojournTimeMax);
				res[10] += sojournTimeAvg * getFreqPatternSojournTime(currPattern);

				res[12] += totalFreq; // freq throughput time
				res[13] += getFreqPatternWaitingTime(currPattern); // freq waiting time
				res[14] += getFreqPatternSojournTime(currPattern); // freq sojourn time
			}
		}
		if (relevantPattern.size() > 0) {
			res[2] = res[2] / res[12]; // average throughput time
			res[6] = res[6] / res[13]; // average waiting time
			res[10] = res[10] / res[14]; // average sojourn time
		}
		return res;
	}

	public String getPatternString(ManifestEvClassPatternTable manifest, short patternID) {
		String[] evClassArr = manifest.getTransClass2PatternMap().decodePatternID(patternID);
		String res = "";
		String limiter = "";
		for (String ec : evClassArr) {
			res += limiter + ec;
			limiter = ",";
		}
		return res;
	}

	public double getMoveModelOfTrans(int encodedTransID) {
		return this.moveModelOnly[encodedTransID * 2];
	}

	public int getUniqueCaseMoveModelOfTrans(int encodedTransID) {
		return this.moveModelOnly[(encodedTransID * 2) + 1];
	}

	public int getEncOfTrans(Transition trans) {
		return trans2Idx.get(trans);
	}

	public int getEncOfPlace(Place place) {
		return place2Idx.get(place);
	}

	public long[] getCaseThroughputTime() {
		return this.caseThroughputAllVals;
	}

}

class ManifestTimeInfo {
	private int limit; // the number of events for this manifest
	private long firingTime; // the moment it fires (must be not null)
	private Long lastTokenTakenTime; // the moment last tokens for manifest is available

	@SuppressWarnings("unused")
	private ManifestTimeInfo() {
	}

	/**
	 * Default constructor
	 * 
	 * @param limit
	 *            the number of events this manifest expect
	 * @param firingTime
	 *            the moment the manifest is fired
	 * @param lastTokenTakenTime
	 *            the moment the last token that enables this manifest is
	 *            provided
	 */
	public ManifestTimeInfo(int limit, long firingTime, Long lastTokenTakenTime) {
		this.limit = limit;
		this.firingTime = firingTime;
		this.lastTokenTakenTime = lastTokenTakenTime;
	}

	public int getLimit() {
		return limit;
	}

	public long getFiringTime() {
		return firingTime;
	}

	/**
	 * The moment the last token for firing a manifest is available. Can also be
	 * null, if the consumed tokens are not timestamped as they are produced by
	 * move on models
	 * 
	 * @return
	 */
	public Long getLastTokenTakenTime() {
		return lastTokenTakenTime;
	}
}