package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import gnu.trove.map.TIntObjectMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ReliablePerfCounterTable extends PerfCounterTable {

	/**
	 * Store total waiting and sojourn time frequency E.g. sojourn time
	 * frequency of pattern number 3 is patternInfoWaitingTimeInt[(2 * 3) + 1]
	 */
	private int[] patternInfoWaitingTimeInt;

	/**
	 * In this overriden method, we also need to initiate
	 * patternInfoWaitingTimeInt. Waiting time and sojourn time is only
	 * calculated when all taken tokens have time info
	 */

	public void init(ManifestEvClassPatternTable manifest, String timeAtt, Class<?> c, boolean[] caseFilter) {
		// initialize patternInfoWaitingTimeInt first
		patternInfoWaitingTimeInt = new int[manifest.getTransClass2PatternMap().getNumPatterns() * 2];
		Arrays.fill(patternInfoWaitingTimeInt, 0);

		super.init(manifest, timeAtt, c, caseFilter);
	}

	/**
	 * This firing is only used for move model only. Timestamps of all tokens
	 * are null
	 * 
	 * @param timedPlaces
	 * @param marking
	 * @param encTrans
	 * @param time
	 */
	@Override
	protected void updateMarkingMoveModel(final TIntObjectMap<List<Long>> timedPlaces, final short[] marking,
			int encTrans) {
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
						listTime.remove(0);
					}
				}
			}
		}

		// produce tokens and timestamp
		produceTokens(timedPlaces, marking, encTrans, null);
	}

	/**
	 * Calculate waiting time of a pattern manifest Since this is reliable, only
	 * update if last token time is NOT null
	 * 
	 * @param lastTokenTakenTime
	 * @param firingTime
	 * @param patternIDOfManifest
	 */
	@Override
	protected void updateManifestWaitingTime(Long lastTokenTakenTime, long firingTime, int patternIDOfManifest) {
		if (lastTokenTakenTime != null) {
			patternInfoWaitingTimeInt[patternIDOfManifest * 2]++;
			updatePatternPerformance(PerfCounterTable.WAITINGTIME, patternIDOfManifest, firingTime - lastTokenTakenTime, patternInfoWaitingTimeInt[patternIDOfManifest * 2]);
		}
	}

	/**
	 * Calculate sojourn time of manifest, the lastTokenTakenTime MAYBE null. If
	 * it is null, do nothing.
	 * 
	 * @param lastTokenTakenTime
	 * @param firingTime
	 * @param patternIDOfManifest
	 */
	@Override
	protected void updateManifestSojournTime(Long lastTokenTakenTime, long firingTime, int patternIDOfManifest) {
		if (lastTokenTakenTime != null) {
			patternInfoWaitingTimeInt[(patternIDOfManifest * 2) + 1]++;
			updatePatternPerformance(PerfCounterTable.SOJOURNTIME, patternIDOfManifest, firingTime - lastTokenTakenTime, patternInfoWaitingTimeInt[(patternIDOfManifest * 2) + 1]);
		}
	}

	/**
	 * Return the frequency of waiting time of pattern patternIDOfManifest
	 * 
	 * @param patternIDOfManifest
	 * @return
	 */
	@Override
	public int getFreqPatternWaitingTime(int patternIDOfManifest) {
		return patternInfoWaitingTimeInt[2 * patternIDOfManifest];
	}

	/**
	 * Return the frequency of sojourn time of pattern patternIDOfManifest
	 * 
	 * @param patternIDOfManifest
	 * @return
	 */
	@Override
	public int getFreqPatternSojournTime(int patternIDOfManifest) {
		return patternInfoWaitingTimeInt[(2 * patternIDOfManifest) + 1];
	}

	/**
	 * Take tokens, return null if there is any of the taken tokens have null
	 * timestamp
	 * 
	 * @param timedPlaces
	 * @param marking
	 * @param encTrans
	 * @param tokenTakeTime
	 * @return
	 */
	@Override
	protected Long takeTokens(final TIntObjectMap<List<Long>> timedPlaces, final short[] marking, final int encTrans,
			final long takenTime) {
		final List<Long> latestDate = new ArrayList<Long>(1); // store the latestTakenTokenTime
		latestDate.add(null); // if this variable has more than one date, means 

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
						if (latestDate.size() == 1) { // no taken tokens with null timestamp
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
							} else {
								// make the latestDate 2 to indicate that there is tokens with null identifier 
								latestDate.add(0, null);
							}
						} else {
							break;
						}
					}
				}
			}

			// in the second iteration, update waiting time for places that has timestamped token
			final Long maxSyncTime = latestDate.iterator().next();

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
						Long tokenTime = listTime.remove(0);
						if (tokenTime != null) {
							// update synchronization, sojourn, and waiting time
							updatePlaceTimeAll(place, tokenTime, maxSyncTime, takenTime);
						}
					}
				}
			}
			return maxSyncTime;
		}
		return null;
	}

}