package org.pm4knime.node.conversion.pt2pn;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;

/**
 * This is an example implementation of the node factory of the
 * "PT2PNConverter" node.
 *
 * @author Kefang Ding
 */
public class PT2PNConverterNodeFactory extends NodeFactory<PT2PNConverterNodeModel> implements WizardNodeFactoryExtension<PT2PNConverterNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

    
	PT2PNConverterNodeModel node;
	
	/**
     * {@inheritDoc}
     */
    @Override
    public PT2PNConverterNodeModel createNodeModel() {
    	node = new PT2PNConverterNodeModel();
		return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
		// The number of views the node should have, in this cases there is none.
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<PT2PNConverterNodeModel> createNodeView(final int viewIndex,
            final PT2PNConverterNodeModel nodeModel) {
		// We return null as this example node does not provide a view. Also see "getNrNodeViews()".
		return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
		return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return null;
    }

}

