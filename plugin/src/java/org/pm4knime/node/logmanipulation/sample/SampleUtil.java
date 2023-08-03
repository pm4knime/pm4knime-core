package org.pm4knime.node.logmanipulation.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.pm4knime.util.XLogUtil;

public class SampleUtil {
	
	public static List<Integer> sample(int bound, double prob) {

		int num = (int) (prob * bound);
		return sample(bound, num);
	}

	public static List<Integer> sample(int bound, int num) {
		ArrayList<Integer> idx_list = new ArrayList<>();
		Random random = new Random(44444);
		int index;
		while (num > 0) {
			index = random.nextInt(bound);
			if (!idx_list.contains(index)) { // random.nextDouble()< prob && 
				idx_list.add(index);
				num--;
				// System.out.println(index);
			}
		}
		return idx_list;
	}

	public static XLog[] sampleLog(XLog log, int number) {

		XLog slog = XLogUtil.clonePureLog(log, " sampled");
		XLog dlog = XLogUtil.clonePureLog(log, " not sampled");
		// sample the index for the traces
		List<Integer> sIdx = SampleUtil.sample(log.size(), number);
		int i = 0;
		for (XTrace trace : log) {

			if (sIdx.contains(i)) {
				slog.add((XTrace) trace.clone());
			} else {
				dlog.add((XTrace) trace.clone());
			}
			i++;
		}

		return new XLog[] { slog, dlog };
	}

	/**
	 * sample the log in percentage of the whole size.
	 * 
	 * @param log
	 * @param percentage
	 *            0<=percentage<=1
	 * @returnerc
	 */
	public static XLog[] sampleLog(XLog log, double percentage) {
		// convert percentage to num
		int num = (int) (log.size() * percentage);
		return sampleLog(log, num);
	}
}
