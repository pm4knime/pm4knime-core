package org.pm4knime.node.io.log.reader;

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

public class XLogReaderNodeView extends NodeView<XLogReaderNodeModel> {

	protected XLogReaderNodeView(int viewIndex, XLogReaderNodeModel nodeModel) {
		super(nodeModel);
		// TODO we set different view according to the viewIdx
		XLog xlog = nodeModel.getLogPO().getLog();
		// SettingsModelString m_fileName =  nodeModel.getParams().getFilePathSettingsModel();
		
		PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
		// viewIdx = 0, dottedChart 
		switch(viewIndex) {
		case 0: // show the trace variance view
			JComponent traceView = new LogViewVisualizer(new LogViewContextProM(context), xlog);
			// setComponent(traceView);
			JPanel tvPanel = new JPanel();
			tvPanel.add(traceView);
			tvPanel.setLayout(new BoxLayout(tvPanel, BoxLayout.Y_AXIS));
			
			tvPanel.setPreferredSize(new Dimension(1400,600));
			// tvPanel.setName(m_fileName.getStringValue());
			setComponent(tvPanel);
			
			break;
		case 1:
			// now set the dottedChart to show
			
			try {
				JPanel dottedView = new DottedChartInspector(new LogView(xlog, context.getProgress()), context);
				setComponent(dottedView);
			} catch (DottedChartException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		
		}
		// viewIdx = 1, traceVariant, or we can use other to substitute it
	}

	@Override
	protected void onClose() {
		// TODO Auto-generated method stub
		System.out.println("Check the calling hierarchy in model close");
	}

	@Override
	protected void onOpen() {
		// TODO Auto-generated method stub
		// here we might need to check the nodeModel and other stuff, but how to do it??
		// if we can have this value, we don't need to serialize the log and Petri net again
		// we will refer to this value here, try it here 
		// this.getNodeModel();
		System.out.println("Check the calling hierarchy in model open");
	}

	@Override
	protected void modelChanged() {
		// TODO Auto-generated method stub
		System.out.println("Check the calling hierarchy in model changed");
	}

}
