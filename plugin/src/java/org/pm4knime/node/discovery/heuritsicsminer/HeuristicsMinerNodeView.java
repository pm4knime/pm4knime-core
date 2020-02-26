package org.pm4knime.node.discovery.heuritsicsminer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.NodeView;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.heuristics.HeuristicsNetGraph;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.FlexibleHeuristicsMinerPlugin;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.gui.HeuristicsNetVisualizer;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;
import org.processmining.plugins.heuristicsnet.visualizer.annotatedvisualization.AnnotatedVisualizationGenerator;
import org.processmining.plugins.heuristicsnet.visualizer.annotatedvisualization.AnnotatedVisualizationSettings;

/**
 * <code>NodeView</code> for the "HeuristicsMiner" node.
 * Modification : 2020/02/26  Reason : to pass the test workflow; 
 *  Way to deal with it, referred to the codes:  
 *  https://github.com/knime/knime-base/blob/master/org.knime.base/src/org/knime/base/node/viz/plotter/box/BoxPlotNodeView.java\
 *  
 *  One Panel is passed to the view as the new value. 
 *  Then detect if the nodeModel has changed
 *  If it changes, show the view, else just use the Panel before.
 * @author Kefang Ding
 */
public class HeuristicsMinerNodeView extends NodeView<HeuristicsMinerNodeModel>  {

	private JPanel m_viewPanel;
    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link HeuristicsMinerNodeModel})
     */
    protected HeuristicsMinerNodeView(final HeuristicsMinerNodeModel nodeModel, JPanel viewPanel) {
        super(nodeModel);
        // TODO: generated method stub
        m_viewPanel = viewPanel; 
        setComponent(m_viewPanel);
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: check if the nodeModel is not null, generate the hnet view.
    	if(getNodeModel() !=null) {
    		HeuristicsMinerNodeModel nodeModel = getNodeModel();
    		
    		
    		HeuristicsNet hnet =  nodeModel.getHNet();
    		if(hnet == null) {
    			PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
    					.getFutureResultAwarePluginContext(FlexibleHeuristicsMinerPlugin.class);
    			
    			HeuristicsMinerSettings heuristicsMinerSettings = nodeModel.getConfiguration();
    			XLog log = nodeModel.getXLogPO().getLog();
    	    	hnet = FlexibleHeuristicsMinerPlugin.run(pluginContext, log, heuristicsMinerSettings);
    			
    		}
            AnnotatedVisualizationGenerator generator = new AnnotatedVisualizationGenerator();
    		
    		AnnotatedVisualizationSettings settings = new AnnotatedVisualizationSettings();
    		HeuristicsNetGraph graph = generator.generate(hnet, settings);
    		
    		JComponent m_hnetVisualization = HeuristicsNetVisualizer.visualizeGraph(graph, hnet, settings, null);
    		m_viewPanel.add(m_hnetVisualization);
    	}
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

