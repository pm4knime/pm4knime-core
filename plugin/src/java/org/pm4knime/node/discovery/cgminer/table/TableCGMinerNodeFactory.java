package org.pm4knime.node.discovery.cgminer.table;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;


public class TableCGMinerNodeFactory 
        extends NodeFactory<TableCGMinerNodeModel> implements WizardNodeFactoryExtension<TableCGMinerNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {
	TableCGMinerNodeModel node;

    /**
     * {@inheritDoc}
     */
    @Override
    public TableCGMinerNodeModel createNodeModel() {
		node = new TableCGMinerNodeModel();
		return node;	
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
    public NodeView<TableCGMinerNodeModel> createNodeView(final int viewIndex,
            final TableCGMinerNodeModel nodeModel) {
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
		return new TableCGMinerNodeDialog(node);
    }

}

