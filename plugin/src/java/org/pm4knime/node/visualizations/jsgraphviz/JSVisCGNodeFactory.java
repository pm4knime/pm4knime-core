package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


public class JSVisCGNodeFactory 
        extends NodeFactory<JSVisCGNodeModel> implements WizardNodeFactoryExtension<JSVisCGNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

    /**
     * {@inheritDoc}
     */
    @Override
    public JSVisCGNodeModel createNodeModel() {
        return new JSVisCGNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<JSVisCGNodeModel> createNodeView(final int viewIndex,
            final JSVisCGNodeModel nodeModel) {
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

