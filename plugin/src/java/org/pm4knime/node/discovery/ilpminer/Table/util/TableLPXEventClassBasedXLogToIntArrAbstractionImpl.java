package org.pm4knime.node.discovery.ilpminer.Table.util;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.pm4knime.util.defaultnode.TraceVariant;
import org.pm4knime.util.defaultnode.TraceVariantRepresentation;
import org.processmining.hybridilpminer.models.abstraction.abstracts.AbstractLPLogAbstraction;
import org.processmining.hybridilpminer.utils.trove.hashing.SetOfIntArrHashingStrategy;

public class TableLPXEventClassBasedXLogToIntArrAbstractionImpl extends AbstractLPLogAbstraction<XEventClass> {

	public TableLPXEventClassBasedXLogToIntArrAbstractionImpl(final TraceVariantRepresentation log, final TableHybridILPMinerParametersImpl configuration) {
		constructAlphabet(log, configuration);
		for (TraceVariant var : log.getVariants()) {
			ArrayList<String> trace = var.getActivities();
			int freq = var.getFrequency();
			for (int i = 0; i<freq; i++) {
				this.addTrace(trace, configuration);
			}
			
		}
	}

	protected void addTrace(ArrayList<String> t, TableHybridILPMinerParametersImpl configuration) {
		Set<int[]> prefixes = new THashSet<>();
		List<XEventClass> prefix = new ArrayList<>();
		int[] prefixAbstraction = this.encode(prefix);
		prefixClosure.adjustOrPutValue(prefixAbstraction, 1, 1);
		prefixes.add(prefixAbstraction);
		for (int i = 0; i < t.size(); i++) {
			XEventClass eventClass = new XEventClass(configuration.getMap().get(t.get(i)).getId(), -1);
			prefix.add(eventClass);
			prefixAbstraction = this.encode(prefix);
			prefixClosure.adjustOrPutValue(prefixAbstraction, 1, 1);
			prefixes.add(prefixAbstraction);
		}
		this.log.adjustOrPutValue(prefixAbstraction, 1, 1);
		if (this.traceMap.get(prefixAbstraction) == null) {
			this.traceMap.put(prefixAbstraction, new TCustomHashSet<Set<int[]>>(new SetOfIntArrHashingStrategy()));

		}
		this.traceMap.get(prefixAbstraction).add(prefixes);
	}

	protected TObjectIntMap<XEventClass> constructAlphabet(TraceVariantRepresentation log, TableHybridILPMinerParametersImpl configuration) {
		for (XEventClass ec : configuration.getMap().values()) {
			super.alphabet.putIfAbsent(ec, ec.getIndex());
		}
		return super.alphabet;
	}

}
