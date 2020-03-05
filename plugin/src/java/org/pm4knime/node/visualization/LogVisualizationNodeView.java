package org.pm4knime.node.visualization;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

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
	int currentViewIdx ;

	/**
	 * Creates a new view.
	 * 
	 * @param nodeModel  The model (class: {@link LogVisualizationNodeModel})
	 * @param viewPanels
	 */
	protected LogVisualizationNodeView(int viewIndex, final LogVisualizationNodeModel nodeModel, JPanel[] viewPanels) {
		super(nodeModel);

		m_viewPanels = viewPanels;
		currentViewIdx = viewIndex;
		switch (currentViewIdx) {
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
			
			switch (currentViewIdx) {
			case 0: // traceView 
				JPanel traceView = new LogViewVisualizer(new LogViewContextProM(context), xlog);
				// m_viewPanels[0].setLayout(new BoxLayout(m_viewPanels[0], BoxLayout.Y_AXIS));
				
//				m_viewPanels[0].setLayout(new BoxLayout(m_viewPanels[0], BoxLayout.Y_AXIS));
				m_viewPanels[0].setLayout(new BorderLayout());
				Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				m_viewPanels[0].setPreferredSize(new Dimension(dim.width/2, dim.height/2));
				m_viewPanels[0].add(traceView);
				
				break;
			case 1: 
				try {
					JPanel dottedView = new DottedChartInspector(new LogView(xlog, context.getProgress()), context);
					m_viewPanels[1].setLayout(new BorderLayout());
					m_viewPanels[1].add(dottedView);
				} catch (DottedChartException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			
		}
	}

}
