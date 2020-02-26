package org.pm4knime.node.visualization;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.NodeView;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logenhancement.view.LogViewContextProM;
import org.processmining.logenhancement.view.LogViewVisualizer;
import org.processmining.logprojection.LogView;
import org.processmining.logprojection.plugins.dottedchart.DottedChart.DottedChartException;
import org.processmining.logprojection.plugins.dottedchart.ui.DottedChartInspector;

/**
 * <code>NodeView</code> for the "LogVisualization" node.
 *
 * @author Kefang Ding
 */
public class LogVisualizationNodeView extends NodeView<LogVisualizationNodeModel> {

	JPanel[] m_viewPanels;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel  The model (class: {@link LogVisualizationNodeModel})
	 * @param viewPanels
	 */
	protected LogVisualizationNodeView(int viewIndex, final LogVisualizationNodeModel nodeModel, JPanel[] viewPanels) {
		super(nodeModel);

		m_viewPanels = viewPanels;
		// viewIdx = 0, dottedChart
		switch (viewIndex) {
			case 0: // show the trace variance view
				setComponent(m_viewPanels[0]);
				break;
			case 1:
				// now set the dottedChart to show
				setComponent(m_viewPanels[1]);
				break;
			}
	}

	@Override
	protected void onClose() {
		// TODO Auto-generated method stub
//		System.out.println("Check the calling hierarchy in model close");
	}

	@Override
	protected void onOpen() {

//		System.out.println("Check the calling hierarchy in model open");
	}

	@Override
	protected void modelChanged() {
		// TODO following the same strategy to avoid view null exception
		if (getNodeModel() != null) {
			LogVisualizationNodeModel nodeModel = getNodeModel();
			XLog xlog = nodeModel.getLogPO().getLog();
			PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
			JComponent traceView = new LogViewVisualizer(new LogViewContextProM(context), xlog);
			// setComponent(traceView);
			JPanel tvPanel = new JPanel();
			tvPanel.add(traceView);
			tvPanel.setLayout(new BoxLayout(tvPanel, BoxLayout.Y_AXIS));

			tvPanel.setPreferredSize(new Dimension(1400, 600));
			m_viewPanels[0].add(tvPanel);
			
			// add view to the second index
			JPanel dottedView;
			try {
				dottedView = new DottedChartInspector(new LogView(xlog, context.getProgress()), context);
				m_viewPanels[1].add(dottedView);
			} catch (DottedChartException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
	}

}
