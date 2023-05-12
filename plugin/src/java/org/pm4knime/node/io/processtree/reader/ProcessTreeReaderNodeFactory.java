package org.pm4knime.node.io.processtree.reader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;

/**
 * <code>NodeFactory</code> for the "ProcessTreeReader" Node.
 * this node is used to read process tree from file ptml * n
 *
 * @author DKF
 */
public class ProcessTreeReaderNodeFactory extends NodeFactory<ProcessTreeReaderNodeModel> implements WizardNodeFactoryExtension<ProcessTreeReaderNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessTreeReaderNodeModel createNodeModel() {
        return new ProcessTreeReaderNodeModel();
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
    public NodeView<ProcessTreeReaderNodeModel> createNodeView(final int viewIndex,
            final ProcessTreeReaderNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new ProcessTreeReaderNodeDialog();
    }

}

