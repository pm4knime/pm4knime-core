package org.pm4knime.node.conformance;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.NodeView;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.visualization.projection.PNLogReplayProjectedVisPanel;

/**
 * <code>NodeView</code> for the "ConformanceChecker" node.
 *
 * @author Kefang Ding
 */
public class ConformanceCheckerNodeView extends NodeView<ConformanceCheckerNodeModel> {

	/**
	 * Creates a new view.
	 *
	 * @param nodeModel The model (class: {@link ConformanceCheckerNodeModel})
	 * @throws ConnectionCannotBeObtained
	 */
	protected ConformanceCheckerNodeView(final ConformanceCheckerNodeModel nodeModel) {
		super(nodeModel);
		// TODO: create a view here for the ReplayerResult with Petri net
		final PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
		final AcceptingPetriNet anet = nodeModel.getNetPO().getANet();
		final XLog log = nodeModel.getLogPO().getLog();

		final TransEvClassMapping map = nodeModel.getMapping();
		final PNRepResult result = nodeModel.getRepResultPO().getRepResult();
		JComponent projectView;
		try {
			projectView = new PNLogReplayProjectedVisPanel(context, anet.getNet(), anet.getInitialMarking(), log, map,
					result);
			setComponent(projectView);
		} catch (final ConnectionCannotBeObtained e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onClose() {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {
		// TODO: generated method stub
	}

}
