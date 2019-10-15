package org.pm4knime.node.discovery.heuritsicsminer;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.knime.core.node.NodeView;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.heuristics.HeuristicsNetGraph;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.gui.HeuristicsNetVisualizer;
import org.processmining.plugins.heuristicsnet.visualizer.annotatedvisualization.AnnotatedVisualizationGenerator;
import org.processmining.plugins.heuristicsnet.visualizer.annotatedvisualization.AnnotatedVisualizationSettings;

/**
 * <code>NodeView</code> for the "HeuristicsMiner" node.
 *
 * @author Kefang Ding
 */
public class HeuristicsMinerNodeView extends NodeView<HeuristicsMinerNodeModel> {

	private JComponent m_hnetVisualization;
	private HeuristicsNet hnet;
    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link HeuristicsMinerNodeModel})
     */
    protected HeuristicsMinerNodeView(final HeuristicsMinerNodeModel nodeModel) {
        super(nodeModel);
        // TODO: generated method stub
        hnet =  nodeModel.getHNet();
        AnnotatedVisualizationGenerator generator = new AnnotatedVisualizationGenerator();
		
		AnnotatedVisualizationSettings settings = new AnnotatedVisualizationSettings();
		HeuristicsNetGraph graph = generator.generate(hnet, settings);
		
		m_hnetVisualization = HeuristicsNetVisualizer.visualizeGraph(graph, hnet, settings, null);
		
		JPanel viewPanel = new JPanel();
		viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.Y_AXIS));
		viewPanel.setPreferredSize(new Dimension(1000,600));
		viewPanel.add(m_hnetVisualization);
		setComponent(viewPanel);
		
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: generated method stub
    	// hnet = ((HeuristicsMinerNodeModel) getNodeModel()).getHNet();
    	// repaint the object by repeat the codes?? Or we set it null??
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

