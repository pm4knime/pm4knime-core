package org.pm4knime.node.jsvis;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;

/**
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
public class DataExplorerNodeFactory extends NodeFactory<DataExplorerNodeModel> implements
    WizardNodeFactoryExtension<DataExplorerNodeModel, DataExplorerNodeRepresentation, DataExplorerNodeValue> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DataExplorerNodeModel createNodeModel() {
        return new DataExplorerNodeModel(getInteractiveViewName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<DataExplorerNodeModel> createNodeView(final int viewIndex, final DataExplorerNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new DataExplorerNodeDialog();
    }

}
