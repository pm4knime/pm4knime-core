package org.pm4knime.node.discovery.ilpminer.Table;

import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.hybridilpminer.parameters.DiscoveryStrategyType;
import org.processmining.hybridilpminer.parameters.XLogHybridILPMinerParametersImpl;


public class ILPMinerTableUtil {

	public static Object[] discoverWithArtificialStartEnd(PluginContext context, BufferedDataTable table,
			BufferedDataTable artifTable, XLogHybridILPMinerParametersImpl param) {
		if (param.getDiscoveryStrategy().getDiscoveryStrategyType().equals(DiscoveryStrategyType.CAUSAL_FLEX_HEUR)
				&& param.getDiscoveryStrategy().getSimpleCag() == null) {
			return applyExpress(context, table, param.getEventClassifier(), param);
		}
//		String artificStartLabel = parameters.getEventClassifier().getClassIdentity(artificialLog.get(0).get(0));
//		String artificEndLabel = parameters.getEventClassifier()
//				.getClassIdentity(artificialLog.get(0).get(artificialLog.get(0).size() - 1));
//		return discoverWithArtificialStartEnd(context, artificialLog, parameters, artificStartLabel, artificEndLabel);
	}
	
	
}
